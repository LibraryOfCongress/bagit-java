import md5 

from django.template import RequestContext
from django.views.decorators.http import require_GET
from django.http import HttpResponse, HttpResponseForbidden, \
    HttpResponseNotAllowed
from django.shortcuts import render_to_response, get_object_or_404

from deposit.sword.basicauth import logged_in_or_basicauth 
from deposit.depositapp import models
from deposit.settings import REALM


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
        post_data = PostData(request)
        filename, tmpfile = _data(request)

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

def _data(request):
    """streams data from the body of a PUT or POST to a file and returns
    """
    expected_md5 = request.META.get('Content-MD5')
    found_md5 = md5.new()
    content_length = int(request.META.get('HTTP_CONTENT_LENGTH', 
        request.META.get('CONTENT_LENGTH',1)))

    input = request.environ['wsgi.input']
    while True:
        buffer_size = min(content_length, 1024)
        bytes = input.read(buffer_size)
        found_md5.update(bytes)
        #content-length -= buffer_size

    if m.hexdigest() != expected_md5:
        raise "uhoh"

    return "ok"


class ErrorChecksumMismatch(HttpResponse):
    status_code = 412
