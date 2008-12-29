from django.http import HttpResponse

from django.template.loader import render_to_string

class UnsupportedMediaType(HttpResponse):
    status_code = 415


class PreConditionFailed(HttpResponse):
    status_code = 412


class Created(HttpResponse):
    status_code = 201
    mimetype = 'application/atom+xml'

    def __init__(self, transfer):
        HttpResponse.__init__(self)
        self['location'] = '/api/collection/%s/%s' % (transfer.project.id,
                                                      transfer.id)
        self.content = render_to_string('entry.xml', {'transfer': transfer})

