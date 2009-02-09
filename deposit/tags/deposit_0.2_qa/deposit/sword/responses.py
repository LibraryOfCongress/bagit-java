from django.http import HttpResponse

from django.template.loader import render_to_string

class NotImplemented(HttpResponse):
    status_code = 501


class UnsupportedMediaType(HttpResponse):
    status_code = 415


class PreConditionFailed(HttpResponse):
    status_code = 412


class RequestEntityTooLarge(HttpResponse):
    status_code = 413


class LengthRequired(HttpResponse):
    status_code = 411


class Created(HttpResponse):
    status_code = 201
    mimetype = 'application/atom+xml'

    def __init__(self, transfer, host):
        HttpResponse.__init__(self)
        self['location'] = '/api/collection/%s/%s' % (transfer.project.id,
                                                      transfer.id)
        self.content = render_to_string('entry.xml', 
                                        {'transfer': transfer, 'host': host })

