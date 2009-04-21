from django.contrib.auth import REDIRECT_FIELD_NAME
from django.contrib.auth.decorators import login_required
from django.contrib.auth.forms import PasswordChangeForm
from django.contrib.auth.models import User
from django.contrib.auth.views import login, logout_then_login
from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpResponseRedirect, Http404, HttpResponseBadRequest, HttpResponseForbidden
from django.shortcuts import render_to_response, get_object_or_404
from django.template import RequestContext
from django.views.generic.create_update import create_object

from deposit.depositapp import forms, models
from deposit.depositapp.queries import TransferQuery


@login_required
def index(request):
    """
    Redirect to a user's overview page.  Leaving here instead of just
    rewriting urlconf to send / to overview in case we need to do 
    something else here at index() sometime.
    """
    return HttpResponseRedirect(reverse('overview_url',
            args=[request.user.username]))

def logout(request):
    """
    Log a user out, and return them to the login screen.

    Note that django's logout_then_login will fetch LOGIN_URL
    from settings.py.
    """
    return logout_then_login(request)

@login_required
def overview(request, username=None):
    """
    Show the home page for a given user.  Defaults to the logged-in
    user unless a different username is specified.
    
    Presuming that non-staff/non-supervisors should not be able
    to see other users.
    """
    if username == request.user.username:
        deposit_user = request.user
        is_user = True
    elif request.user.is_staff \
        or request.user.is_superuser:
        deposit_user = get_object_or_404(User, username=username)
        is_user = False
    else:
        request.user.message_set.create(message='Invalid request.')
        return HttpResponseRedirect(reverse('overview_url',
            args=[request.user.username]))
    q = TransferQuery()
    q.include_received = False
    projects = models.Project.objects.all()
    return render_to_response('overview.html', {
        'deposit_user': deposit_user, 'is_user': is_user, 
        'projects': projects, 'query':q},
        context_instance=RequestContext(request))

@login_required
def user(request, username=None, command=None):    
    """
    Allow a user to update her information, or allow staff/superuser
    to update another user's information.

    FIXME: awkward to handle the user info and the user profile info
    with separate forms.
    """
    if request.user.username == username:
        deposit_user = request.user
    elif request.user.is_staff \
        or request.user.is_superuser:
        try:
            deposit_user = models.User.objects.get(username=username)
        except User.DoesNotExist:
            raise Http404
    else:
        # BZZzzzt!  go away.
        request.user.message_set.create(message='Invalid request.')
        return HttpResponseRedirect(reverse('overview_url', 
            args=[request.user.username]))
        
    # It's possible a User gets created without a UserProfile,
    # so fetch it just in case, even though we don't need it here.
    # Should only ever create anything once.
    user_profile, created = models.UserProfile.objects.get_or_create(
        user=deposit_user)
    
    updated = False
    message = ''
    if request.method == 'POST':
        if command == 'user':
            user_form = forms.AuthUserForm(request.POST, 
                instance=deposit_user)
            if user_form.is_valid():
                user_form.save()
                updated = True
                message = 'Updated user information.'
            else:
                message = 'Please check your changes and try again.'
        elif command == 'profile':
            profile_form = forms.UserProfileForm(request.POST, 
                instance=deposit_user.get_profile())
            if profile_form.is_valid():
                profile_form.save()
                updated = True
                message = 'Updated user information.'
            else:
                message = 'Please check your changes and try again.'
        elif command == 'password':
            password_form = PasswordChangeForm(deposit_user,
                request.POST)
            if password_form.is_valid():
                password_form.save()
                updated = True
                message = 'Updated password.'
            else:
                message = 'Please re-enter and confirm your new password again.'

    if message:
        request.user.message_set.create(message=message)
    if updated:
        return HttpResponseRedirect(reverse('overview_url',
            args=[request.user.username]))

    user_form = forms.AuthUserForm(request.POST,
        instance=deposit_user)
    profile_form = forms.UserProfileForm(request.POST, 
        instance=deposit_user.get_profile())
    password_form = PasswordChangeForm(deposit_user)

    return render_to_response('user.html', {
        'deposit_user': deposit_user,
        'user_form': user_form, 
        'profile_form': profile_form,
        'password_form': password_form, 
        }, context_instance=RequestContext(request))


#login_required
def transfer(request, transfer_id):
    if request.method == 'POST':
        return HttpResponseNotAllowed()
    trans = get_object_or_404(models.Transfer, id=transfer_id)
    transfer_class = getattr(models, trans.transfer_type)
    transfer_sub = transfer_class.objects.get(id=transfer_id)
    template_name = "%s.html" % trans.transfer_type.lower()
    return render_to_response(template_name, {'transfer':transfer_sub},
            context_instance=RequestContext(request))    

@login_required
def project(request, project_id):
    if request.method == 'POST':
        return HttpResponseNotAllowed()
    project = get_object_or_404(models.Project, id=project_id)
    return render_to_response("project.html", {
        'project': project,
        },
        context_instance=RequestContext(request))

@login_required
def transfer_received(request, transfer_id):
    if request.method == 'GET':
        return HttpResponseNotAllowed()
    if not request.user.is_staff:
        return HttpResponseForbidden()
    transfer = get_object_or_404(models.Transfer, id=transfer_id)
    transfer.update_received(request.user)
    transfer.save()
    request.user.message_set.create(message="The transfer was marked as received.  A notification has been sent to %s." % 
            (transfer.user.email))
    return HttpResponseRedirect(reverse('transfer_url',
            args=[transfer_id]))

@login_required
def create_transfer(request, transfer_type):
    form_class = getattr(forms, transfer_type + "Form")
    if request.method == 'GET':
        project_id = request.GET['project_id']         
    if request.method == 'POST':
        project_id = request.POST['project_id']
        form = form_class(request.POST, request.FILES)
        if form.is_valid():
            new_object = form.save(commit=False)                     
            new_object.project = models.Project.objects.get(id=project_id)
            new_object.user = models.User.objects.get(
                username=request.user.username)
            new_object.save()
            request.user.message_set.create(message="The transfer was registered.  A confirmation has been sent to %s and %s." % 
                    (new_object.user.email, new_object.project.contact_email))
            return HttpResponseRedirect(new_object.get_absolute_url())
    else:
        form = form_class()

    return render_to_response('transfer_form.html', {
        'form': form,
        'project_id': project_id, 
        'transfer_type': transfer_type,
        }, context_instance=RequestContext(request))

@login_required
def transfer_list(request):
    """
    List all the relevant transfer info for a user or project or both.
    If any of the following conditions are not met, set a useful message 
    and send the user back to their page (where that message should 
    display).

    1. Any request must at least specify a username or a project id,
    or it is invalid.

    2. To see a particular project's info, a user must be at least 
    one of:

        a) explicitly associated with a project
        b) a superuser
        c) a staff member

    3. Only staff or superusers may see another user's projects.
    """
    q = TransferQuery(request)

    # Verify condition 1.  This must be true, kick them up if not.
    if not q.username and not q.project_id:
        request.user.message_set.create(message='Bad listing request.')
        return HttpResponseRedirect(reverse('overview_url', 
            args=[request.user.username]))

    # Verify condition 2.  Kick them up if not, also.
    if q.project_id:
        allow = False
        project = get_object_or_404(models.Project, id=q.project_id)
        # 2b and 2c.
        if request.user.is_superuser or request.user.is_staff:
            allow = True
        else:
            # 2a.
            project_users = models.User.objects.filter(projects__project__id=q.project_id)
            if q.username in [user.username for user in project_users]:
                allow = True
        if not allow:
            request.user.message_set.create(message='Invalid transfer project')
            return HttpResponseRedirect(reverse('overview_url', 
                args=[request.user.username]))

    # Verify condition 3.
    if q.username and request.user.username != q.username:
        if not (request.user.is_staff or request.user.is_superuser):
            request.user.message_set.create(message='Invalid transfer user')
            return HttpResponseRedirect(reverse('overview_url', 
                args=[request.user.username]))

    # If they've made it this far, it's a valid request.
    transfers = q.query()
    return render_to_response('transfer_list.html', {
        'query': q, 
        'transfers': transfers,
        }, context_instance=RequestContext(request))

