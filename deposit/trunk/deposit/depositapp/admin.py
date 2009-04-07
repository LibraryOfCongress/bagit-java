from django.contrib import admin

from deposit.depositapp.models import UserProfile, UserProject, \
    Project, NetworkTransfer, ShipmentTransfer

admin.site.register((UserProfile, UserProject,
    Project, NetworkTransfer, ShipmentTransfer))
