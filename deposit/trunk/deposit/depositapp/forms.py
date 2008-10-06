from django.forms import ModelForm, URLField
import deposit.depositapp.models as models
from django.contrib.auth.models import User

class NetworkTransferForm(ModelForm):
    class Meta:
        model = models.NetworkTransfer
        exclude = ('project','user')
        
class ShipmentTransferForm(ModelForm):
    class Meta:
        model = models.ShipmentTransfer
        exclude = ('project','user')

class NdnpNetworkTransferForm(ModelForm):
    class Meta:
        model = models.NdnpNetworkTransfer
        exclude = ('project','user')
        
class NdnpShipmentTransferForm(ModelForm):
    class Meta:
        model = models.NdnpShipmentTransfer
        exclude = ('project','user')

class UserForm(ModelForm):
    class Meta:
        model = User
        fields = ('first_name', 'last_name', 'email')
        
class DepositUserForm(ModelForm):
    class Meta:
        model = models.User
        fields = ('first_name', 'last_name', 'email','organization','address','phone')
    