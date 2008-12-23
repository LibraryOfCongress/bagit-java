from StringIO import StringIO
from xml.etree import ElementTree as ET

from django.core.servers.basehttp import AdminMediaHandler
from django.core.handlers.wsgi import WSGIHandler
from django.core.urlresolvers import reverse
from django.test import TestCase

import twill
from twill import commands as tc

from deposit.settings import REALM

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


class TwillTest(TestCase):

    def setUp(self):
        tc.reset_browser()
        app = AdminMediaHandler(WSGIHandler())
        twill.add_wsgi_intercept(HOST, PORT, lambda: app)
        twill.set_output(StringIO())

    def tearDown(self):
        twill.remove_wsgi_intercept(HOST, PORT)

    def assertXpathEqual(self, path, text):
        xml = tc.show()
        doc = ET.fromstring(xml)
        e = doc.find(path)
        if not e:
            self.fail("element not found for %s" % path)
        if e.text != text:
            self.fail("%s not equal to %s" % (e.text, text))

 
class SwordTests(TwillTest):

    def test_service_no_login(self):
        # unauthenticated user looking at service document
        tc.go(url('/api/service'))
        tc.code('401')

    def test_service_with_staff_login(self):
        # superuser looking at service document should see all project
        # collections
        tc.add_auth(REALM, HOME, 'justin', 'justin')
        tc.go(url('/api/service'))
        tc.code('200')
        doc = ET.fromstring(tc.show())
        c = doc.findall('.//{%(app)s}collection' % NS)
        self.assertEqual(len(c), 2)
        self.assertEqual(c[0].findtext('{%(atom)s}title' % NS), 'NDNP')
        self.assertEqual(c[0].attrib['href'], '/api/collection/1')
        self.assertEqual(c[1].findtext('{%(atom)s}title' % NS), 'NDIIPP')
        self.assertEqual(c[1].attrib['href'], '/api/collection/2')

    def test_service_with_project_login(self):
        # an authenticated user should only see the projects they are
        # associated with in the service document
        tc.add_auth(REALM, HOME, 'jane', 'jane')
        tc.go(url('/api/service'))
        tc.code('200')
        doc = ET.fromstring(tc.show())
        c = doc.findall('.//{%(app)s}collection' % NS)
        self.assertEqual(len(c), 1)
        self.assertEqual(c[0].findtext('{%(atom)s}title' % NS), 'NDIIPP')
        self.assertEqual(c[0].attrib['href'], '/api/collection/2')

    def test_collection_no_login(self):
        # must be authenticated to see a collection
        tc.go(url('/api/collection/1'))
        tc.code('401')

    def test_collection_superuser_login(self):
        # superuser sould be able to see collections
        tc.add_auth(REALM, HOME, 'justin', 'justin')
        tc.go(url('/api/collection/1'))
        tc.code('200')

    def test_wrong_collection_project_login(self):
        # must be associated with a project to see its collection
        tc.add_auth(REALM, HOME, 'jane', 'jane')
        tc.go(url('/api/collection/1'))
        tc.code('403')

    def test_right_collection_project_login(self):
        # make sure a project user can see the feed for their project
        tc.add_auth(REALM, HOME, 'jane', 'jane')
        tc.go(url('/api/collection/2'))
        tc.code('200')

    def a_test_post_collection(self):
        tc.add_auth(REALM, HOME, 'jane', 'jane')
        b = twill.get_browser()
        mb = b._browser
        mb.open(url('/api/collection/2'), 'data')

