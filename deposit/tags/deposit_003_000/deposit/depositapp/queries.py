from django.db.models import Q

import deposit.depositapp.models as models

class TransferQuery():

    include_received = False
    username = None
    project_id = None
    
    def __init__(self, request=None):
        if not request:
            return

        if request.REQUEST.has_key("username") and len(request.REQUEST["username"]) > 0:
            self.username = request.REQUEST["username"]
        if request.REQUEST.has_key("project_id") and len(request.REQUEST["project_id"]) > 0:
            self.project_id=request.REQUEST["project_id"]
        if request.REQUEST.has_key("include_received") and request.REQUEST["include_received"] == "on":
            self.include_received=True

    def get_querystring(self, include_received=None, username=None, project_id=None):
        if include_received or (include_received==None and self.include_received):
            qs = "include_received=on"
        else:
            qs = "include_received=off"
        if username:
            qs = "%s&username=%s" % (qs, username)
        elif self.username:
            qs = "%s&username=%s" % (qs, self.username)
        if project_id:
            qs = "%s&project_id=%s" % (qs, project_id)
        elif self.project_id:
            qs = "%s&project_id=%s" % (qs, self.project_id)
        return qs
    
    def query(self):
        q = Q()
        if not self.include_received:
            q = q & Q(received__isnull=True)
        if self.username:
            q = q & Q(user__username=self.username)
        if self.project_id:
            q = q & Q(project__id=self.project_id)
        return models.Transfer.objects.filter(q)
