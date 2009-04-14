from django.contrib.auth.models import User
from django.forms import ModelForm, URLField

import deposit.depositapp.models as models

class NetworkTransferForm(ModelForm):
    class Meta:
        model = models.NetworkTransfer
        exclude = ('project','user','received', 'received_by')
        
class ShipmentTransferForm(ModelForm):
    class Meta:
        model = models.ShipmentTransfer
        exclude = ('project','user','received', 'received_by')

class NdnpNetworkTransferForm(ModelForm):
    class Meta:
        model = models.NdnpNetworkTransfer
        exclude = ('project','user','received', 'received_by')
        
class NdnpShipmentTransferForm(ModelForm):
    class Meta:
        model = models.NdnpShipmentTransfer
        exclude = ('project','user','received', 'received_by')

class AuthUserForm(ModelForm):
    class Meta:
        model = User
        fields = ('first_name', 'last_name', 'email')
        
class UserProfileForm(ModelForm):
    class Meta:
        model = models.UserProfile
        fields = ('organization', 'address','phone')
    
