import md5 
import os
import re
import wsgiref.util

from django.template import RequestContext
from django.views.decorators.http import require_GET
from django.http import HttpResponse, HttpResponseForbidden, \
    HttpResponseNotAllowed, HttpResponseServerError
from django.shortcuts import render_to_response, get_object_or_404

from deposit.sword.basicauth import logged_in_or_basicauth 
from deposit.sword import exceptions as ex
from deposit.sword import responses as r
from deposit.depositapp.models import User, Project
from deposit.sword.models import SwordTransfer, TransferFile
from deposit.settings import REALM, STORAGE, MAX_UPLOAD_SIZE_BYTES

BAGIT = 'http://purl.org/net/sword-types/bagit'


@require_GET
@logged_in_or_basicauth(REALM)
def service(request):
    user, projects = _user_projects(request)
    max_upload_size_kb = MAX_UPLOAD_SIZE_BYTES / 1024
    return render_to_response('service.xml', dictionary=locals(),
                              mimetype="application/atomsvc+xml",
                              context_instance=RequestContext(request))


@logged_in_or_basicauth(REALM)
def collection(request, project_id):
    user, projects = _user_projects(request)
    host = request.get_host()

    # make sure the user has access to this project
    project = get_object_or_404(Project, id=project_id)
    if project not in projects:
        return HttpResponseForbidden("You don't have permission to view/modify this collection")

    # if getting the collection just give 'em an atom feed for the project
    if request.method == 'GET':
        transfers = list(project.transfers.all())
        return render_to_response('collection.xml', dictionary=locals(),
                                  mimetype="application/atom+xml",
                                  context_instance=RequestContext(request))

    # otherwise we need to create a new transfer
    elif request.method == 'POST':
        transfer = None
        response = HttpResponseForbidden("ERROR: must perform GET or POST to collection URI")
        try:
            mimetype = _get_content_type(request)
            if mimetype != 'application/zip':
                return UnsupportedMediaType("ERROR: must POST application/zip to collection URI")
            packaging = _get_packaging(request)
            transfer = SwordTransfer(user=user, project=project,
                                     packaging=packaging)
            transfer.save()
            transfer_file = _save_data(request, transfer)
            transfer_file.save()
            response = r.Created(transfer, host)
        except ex.PackagingInvalid, e:
            response = r.UnsupportedMediaType("ERROR: %s" % e)
        except ex.MD5Missing, e:
            response = r.PreConditionFailed("ERROR: %s" % e)
        except ex.MD5Mismatch, e:
            response = r.PreConditionFailed("ERROR: %s" % e)
        except ex.ContentDispositionInvalid, e:
            response = r.PreConditionFailed("ERROR: %s" % e)
        except ex.ContentLengthMissing, e:
            response = r.LengthRequired("ERROR: %s" % e)
        except ex.ContentTooLarge, e:
            response = r.RequestEntityTooLarge("ERROR: %s" % e)
        except Exception, e:
            import traceback
            traceback.print_exc()
            response = HttpResponseServerError("Server Error: see log")

    return response 


@logged_in_or_basicauth(REALM)
def entry(request, project_id, transfer_id):
    user, projects = _user_projects(request)
    host = request.get_host()
    transfer = get_object_or_404(SwordTransfer, project__id=project_id, id=transfer_id)
    if transfer.project not in projects:
        return HttpResponseForbidden("You don't have permission to view/modify this collection")
    return render_to_response('entry.xml', mimetype='application/atom+xml',
                              dictionary=locals(), 
                              context_instance=RequestContext(request))


@require_GET
@logged_in_or_basicauth(REALM)
def package(request, project_id, transfer_id):
    user, projects = _user_projects(request)
    transfer = get_object_or_404(SwordTransfer, project__id=project_id, 
                                 id=transfer_id)
    if transfer.project not in projects:
        return HttpResponseForbidden("You don't have permission to view this collection")
    transfer_files = list(transfer.transfer_files.all())
    if len(transfer_files) == 0:
        return HttpResponseNotFound()
    elif len(transfer_files) > 1:
        return NotImplemented("Service does not return serialized compound package objects...yet")
    else:
        tf = transfer_files[0]
        f = file(tf.storage_filename)
        return HttpResponse(wsgiref.util.FileWrapper(f), mimetype=tf.mimetype)


def _user_projects(request):
    """helper for looking up user and the users projects based on the
    AuthUser that is passed in in a request
    """
    # if the AuthUser is staff let them see all the projects
    user = request.user
    if user.is_staff:
        return (user, list(Project.objects.all()))

    # otherwise we need to cast the django.contrib.auth.models.User
    # as a deposit.depositapp.models.User so we an see what projects
    # they have access to
    user = get_object_or_404(User,id=request.user.id)
    return (user, list(user.projects.all()))


def _save_data(request, transfer):
    expected_md5 = _get_md5(request)
    content_length = _get_content_length(request)
    output_filename = _get_filename(request)
    mimetype = _get_content_type(request)
    tf = TransferFile(transfer=transfer, filename=output_filename, 
                    mimetype=mimetype)

    # make the directory if it needs one
    dir = os.path.dirname(tf.storage_filename)
    if not os.path.isdir(dir):
        os.makedirs(dir)

    input = request.environ['wsgi.input']
    output = file(tf.storage_filename, 'w')
    found_md5 = md5.new()
    while True:
        if content_length <= 0:
            break
        buffer_size = min(content_length, 1024)
        bytes = input.read(buffer_size)
        output.write(bytes)
        found_md5.update(bytes)
        content_length -= buffer_size
    output.close()

    if expected_md5 != found_md5.hexdigest():
        os.remove(output.name)
        raise ex.MD5Mismatch("Content-MD5 header said md5 was %s but server received content with md5 of %s" % (expected_md5, found_md5.hexdigest()))

    tf.mimetype = mimetype
    tf.md5 = expected_md5
    tf.save()
    return tf


def _get_filename(request):
    header = request.META.get('HTTP_CONTENT_DISPOSITION', None)
    if header == None:
        raise ex.ContentDispositionInvalid("Content-disposition header missing")

    match = re.search(r'filename=(.+)', header)
    if not match:
        raise ex.ContentDispositionInvalid('Content-disposition header "%s" missing filename part' % header)

    filename = match.group(1)
    if filename.startswith('/'):
        raise ex.ContentDispositionInvalid('Content-disposition header "%s" has absolute filename' % header)

    if '..' in filename:
        raise ex.ContentDispositionInvalid('Content-disposition header "%s" cannot walk directories' % header)

    return filename


def _get_md5(request):
    expected_md5 = request.META.get('HTTP_CONTENT_MD5', None)
    if not expected_md5:
        raise ex.MD5Missing('please supply Content-md5 header')
    return expected_md5.lower()


def _get_content_length(request):
    l = request.META.get('CONTENT_LENGTH', None)
    if not l: 
        raise ex.ContentLengthMissing()
    try:
        l = int(l)
    except ValueError:
        raise ex.ContentLengthInvalid(l)
    if l > MAX_UPLOAD_SIZE_BYTES:
        raise ex.ContentTooLarge("Service cannot accept POSTS larger than %s btyes" % MAX_UPLOAD_SIZE_BYTES)

    return l


def _get_content_type(request):
    mimetype = request.META.get('CONTENT_TYPE', None)
    return mimetype


def _get_packaging(request):
    packaging = request.META.get('HTTP_X_PACKAGING', None)
    if packaging == None:
        raise ex.PackagingInvalid("missing X-packaging header")
    if packaging != BAGIT:
        raise ex.PackagingInvalid("this service only accepts BAGIT X-packaging: %s" % BAGIT)
    return packaging
