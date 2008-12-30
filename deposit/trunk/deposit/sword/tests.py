import md5
import sys
import os.path
from StringIO import StringIO
from xml.etree import ElementTree as ET

from django.core.servers.basehttp import AdminMediaHandler
from django.core.handlers.wsgi import WSGIHandler
from django.core.urlresolvers import reverse
from django.test import TestCase

import httplib2
import wsgi_intercept
import wsgi_intercept.httplib2_intercept

from deposit.settings import REALM, STORAGE
from deposit.depositapp.models import Project, User
from deposit.sword.models import SwordTransfer, TransferFile

HOST = '127.0.0.1'
PORT = 9876
HOME = "http://%s:%i" % (HOST, PORT)

NS = {
        'app':   'http://www.w3.org/2007/app',
        'atom':  'http://www.w3.org/2005/Atom',
        'sword': 'http://purl.org/net/sword/'
     }

def url(path):
    return HOME + path


class SwordTests(TestCase):

    def setUp(self):
        app = AdminMediaHandler(WSGIHandler())
        wsgi_intercept.httplib2_intercept.install()
        wsgi_intercept.add_wsgi_intercept(HOST, PORT, lambda: app)
        self.client = httplib2.Http()

    def tearDown(self):
        wsgi_intercept.remove_wsgi_intercept(HOST, PORT)

    def test_storage_exists(self):
        self.assertTrue(os.path.isdir(STORAGE))

    def test_service_no_login(self):
        # unauthenticated user looking at service document
        response, content = self.client.request(url('/api/service'))
        self.assertEqual(response['status'], '401')

    def test_service_with_staff_login(self):
        # superuser looking at service document should see all project
        # collections
        self.client.add_credentials('justin', 'justin')
        response, content = self.client.request(url('/api/service'))
        self.assertEqual(response['status'], '200')
        doc = ET.fromstring(_munge(content))
        c = doc.findall('.//{%(app)s}collection' % NS)
        self.assertEqual(len(c), 2)
        self.assertEqual(c[0].findtext('{%(atom)s}title' % NS), 'NDNP')
        self.assertEqual(c[0].attrib['href'], '/api/collection/1')
        self.assertEqual(c[1].findtext('{%(atom)s}title' % NS), 'NDIIPP')
        self.assertEqual(c[1].attrib['href'], '/api/collection/2')

    def test_service_with_project_login(self):
        # an authenticated user should only see the projects they are
        # associated with in the service document
        self.client.add_credentials('jane', 'jane')
        response, content = self.client.request(url('/api/service'))
        self.assertEqual(response['status'], '200')
        doc = ET.fromstring(_munge(content))
        c = doc.findall('.//{%(app)s}collection' % NS)
        self.assertEqual(len(c), 1)
        self.assertEqual(c[0].findtext('{%(atom)s}title' % NS), 'NDIIPP')
        self.assertEqual(c[0].attrib['href'], '/api/collection/2')

    def test_collection_no_login(self):
        # must be authenticated to see a collection
        response, content = self.client.request(url('/api/collection/1'))
        self.assertEqual(response['status'], '401')

    def test_collection_superuser_login(self):
        # superuser sould be able to see collections
        self.client.add_credentials('justin', 'justin')
        response, content = self.client.request(url('/api/collection/1'))
        self.assertEqual(response['status'], '200')

    def test_wrong_collection_project_login(self):
        # must be associated with a project to see its collection
        self.client.add_credentials('jane', 'jane')
        response, content = self.client.request(url('/api/collection/1'))
        self.assertEqual(response['status'], '403')

    def test_right_collection_project_login(self):
        # make sure a project user can see the feed for their project
        self.client.add_credentials('jane', 'jane')
        response, content = self.client.request(url('/api/collection/2'))
        self.assertEqual(response['status'], '200')

    def test_post_collection(self):
        # should be able to post application/zip to collection URI
        self.client.add_credentials('jane', 'jane')
        content = 'foobar'
        m = md5.new()
        m.update(content)
        headers = {
                    'Content-type': 'application/zip',
                    'Content-md5': m.hexdigest(),
                    'Content-disposition': 'attachment ; filename=foobar.txt',
                    'X-packaging': 'http://purl.org/net/sword-types/bagit'
                  }
        response, content = self.client.request(url('/api/collection/2'), 
                                                method='POST', body=content,
                                                headers=headers)
        self.assertEqual(response['status'], '201')
        entry = ET.fromstring(_munge(content))
        self.assertEqual(entry.tag, '{%(atom)s}entry' % NS)
        self.assertTrue(entry.findtext('{%(atom)s}title' % NS).startswith('NDIIPP'))
        links = entry.findall('.//{%(atom)s}link' % NS)
        self.assertEqual(links[0].attrib['rel'], 'edit')
        self.assertEqual(links[0].attrib['href'], '/api/collection/2/1')


class SwordModelTests(TestCase):

    def test_transfer(self):
        project = Project.objects.get(name='NDIIPP')
        user = User.objects.get(username='jane')

        transfer = SwordTransfer()
        transfer.ip_address='127.0.0.1'
        transfer.project=project
        transfer.user = user
        transfer.save()

        self.assertTrue(len(transfer.uuid) > 0)
        self.assertEqual(transfer.storage_dir, '%s/2/%s' % \
                         (STORAGE, transfer.uuid))

        transfer_file = TransferFile()
        transfer_file.transfer = transfer
        transfer_file.filename = 'README.txt'
        transfer_file.mimetype = 'text/plain'
        transfer_file.md5 = '9572f7ca62720f31cd80a3a9333a702c'
        transfer_file.save()

        self.assertEqual(transfer_file.storage_filename,
                '%s/2/%s/README.txt' % (STORAGE, transfer.uuid))

# for some reason the wsgi_intercept w/ httplib2 results in duplicated content
# so this is a hack to cut it in half ... this must be a bug somewhere in 
# wsgi_intercept's interaction w/ django's wsgi
def _munge(content):
    return content[0:len(content)/2]
