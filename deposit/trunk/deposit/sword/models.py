from django.db import models

from deposit.depositapp.models import Transfer

class TransferFile(models.Model):
    transfer = models.ForeignKey(Transfer, related_name="transfer_files")
    filename = models.CharField(max_length=500)
    mimetype = models.CharField(max_length=50)
    created = models.DateTimeField(auto_now_add=True)
    md5 = models.CharField(max_length=50)

class LogMessage(models.Model):
    name = models.CharField(max_length=50)
    levelname = models.CharField(max_length=10)
    filename = models.CharField(max_length=255)
    lineno = models.IntegerField()
    message = models.CharField(max_length=500)
    created = models.DateTimeField(auto_now_add=True) 

