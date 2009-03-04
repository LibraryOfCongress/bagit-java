# FIXME: commented out for deposit_0.2_qa
# But maybe it's not necessary here at all?  Doesn't seem to get used.
# import uuid
import os.path

from django.db import models

from deposit.depositapp.models import Transfer
from deposit.settings import STORAGE
from deposit.sword.rfc3339 import rfc3339


class SwordTransfer(Transfer):
    summary = models.CharField(max_length=500)
    packaging = models.CharField(max_length=200)
    ip_address = models.CharField(max_length=25)
    completed = models.DateTimeField(null=True)
    purged = models.DateTimeField(null=True)

    @property 
    def created_rfc3339(self):
        return rfc3339(self.created)

    @property
    def updated_rfc3339(self):
        return rfc3339(self.updated)

    @property
    def completed_rfc3339(self):
        return rfc3339(self.completed)

    @property 
    def purged_rfc3339(self):
        return rfc3339(self.purged)

    @property
    def storage_dir(self):
        return os.path.join(STORAGE, str(self.project.id), self.uuid)

    def __repr__(self):
        return "SwordTransfer: uuid=%s" % self.uuid


class TransferFile(models.Model):
    transfer = models.ForeignKey(SwordTransfer, related_name="transfer_files")
    filename = models.CharField(max_length=500)
    mimetype = models.CharField(max_length=50)
    created = models.DateTimeField(auto_now_add=True)
    md5 = models.CharField(max_length=50)

    @property
    def created_rfc3339(self):
        return rfc3339(self.created)

    @property
    def storage_filename(self):
        return os.path.join(self.transfer.storage_dir, self.filename)

    def has_files(self):
        return self.transfer_files.all().count() > 0



class LogMessage(models.Model):
    name = models.CharField(max_length=50)
    levelname = models.CharField(max_length=10)
    filename = models.CharField(max_length=255)
    lineno = models.IntegerField()
    message = models.CharField(max_length=500)
    created = models.DateTimeField(auto_now_add=True) 
