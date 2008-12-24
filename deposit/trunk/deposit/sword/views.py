import md5 
from tempfile import NamedTemporaryFile

from django.template import RequestContext
from django.views.decorators.http import require_GET
from django.http import HttpResponse, HttpResponseForbidden, \
    HttpResponseNotAllowed
from django.shortcuts import render_to_response, get_object_or_404

from deposit.sword.basicauth import logged_in_or_basicauth 
from deposit.sword.dblogger import log
from deposit.depositapp import models
from deposit.settings import REALM, STORAGE


@require_GET
@logged_in_or_basicauth(REALM)
def service(request):
    user, projects = _user_projects(request)
    return render_to_response('service.xml', dictionary=locals(),
                              mimetype="application/atomsvc+xml",
                              context_instance=RequestContext(request))


@logged_in_or_basicauth(REALM)
def collection(request, project_id):
    user, projects = _user_projects(request)

    # make sure the user has access to this project
    project = get_object_or_404(models.Project, id=project_id)
    if project not in projects:
        return HttpResponseForbidden()

    # if getting the collection just give 'em an atom feed for the project
    if request.method == 'GET':
        transfers = list(project.transfers.all())
        return render_to_response('collection.xml', dictionary=locals(),
                                  mimetype="application/atom+xml",
                                  context_instance=RequestContext(request))

    # otherwise we need to create a new transfer
    elif request.method == 'POST':
        mimetype = _get_content_type(request)
        log.info("user=%s posting %s to project=%s" % (user, mimetype, project))
        if mimetype != 'application/zip':
            return UnsupportedMediaType()

        filename, md5 = _save_data(request)

        t = models.Transfer(user=user, project=project)
        t.save()

        f = models.TransferFile(transfer=transfer, filename=filename,
                                md5=md5, mimetype=mimetype)
        f.save()

        return Created("%s - %s" % (filename, md5))

    return HttpResponseForbidden()


def entry(request, entry_id):
    pass


def _user_projects(request):
    """helper for looking up user and the users projects based on the
    AuthUser that is passed in in a request
    """
    # if the AuthUser is staff let them see all the projects
    user = request.user
    if user.is_staff:
        return (user, list(models.Project.objects.all()))

    # otherwise we need to cast the django.contrib.auth.models.User
    # as a deposit.depositapp.models.User so we an see what projects
    # they have access to
    user = get_object_or_404(models.User,id=request.user.id)
    return (user, list(user.projects.all()))


def _save_data(request):
    expected_md5 = _get_md5(request)
    content_length = _get_content_length(request)
    output = _get_file(request)

    input = request.environ['wsgi.input']
    found_md5 = md5.new()
    while True:
        print "content_length=%s" % content_length
        if content_length <= 0:
            break
        buffer_size = min(content_length, 1024)
        print "reading %s bytes" % buffer_size
        bytes = input.read(buffer_size)
        output.write(bytes)
        found_md5.update(bytes)
        content_length -= buffer_size
    output.close()

    #if expected_md5 != found_md5.hexdigest():
    #    os.remove(output.name)
    #    raise MD5Mismatch("Content-MD5 header said md5 was %s but server received content with md5 of %s" % (expected_md5, found_md5.hexdigest()))
    print "returning"

    return output.name(), expected_md5 


def _get_file(request):
    return file('storage/blah', 'w')


def _get_md5(request):
    expected_md5 = request.META.get('HTTP_CONTENT_MD5', None)
    if not expected_md5:
        raise MD5Missing
    return expected_md5.lower()


def _get_content_length(request):
    l = request.META.get('CONTENT_LENGTH', None)
    if not l: 
        raise ContentLengthMissing()
    try:
        l = int(l)
    except ValueError:
        raise ContentLengthInvalid(l)
    return l


def _get_content_type(request):
    mimetype = request.META.get('CONTENT_TYPE', None)
    return mimetype


class MD5Missing(Exception):
    pass


class MD5Mismatch(Exception):
    pass


class InvalidFileDisposition(Exception):
    pass


class ContentLengthMissing:
    pass


class ContentLengthInvalid:
    pass

class UnsupportedMediaType(HttpResponse):
    status_code = 415

class Created(HttpResponse):
    status_code = 201
