import os, sys

sys.path.append('/opt/local/deposit')
sys.path.append('/opt/local/deposit/deposit')
os.environ['DJANGO_SETTINGS_MODULE'] = 'deposit.settings'

import django.core.handlers.wsgi

application = django.core.handlers.wsgi.WSGIHandler()

