import logging

from deposit.sword.models import LogMessage

class SwordLogHandler(logging.Handler):

    def emit(self, record):
        try:
            msg = LogMessage()
            msg.name = record.name
            msg.levelname = record.levelname
            msg.filename = record.filename
            msg.lineno = record.lineno
            msg.message = record.message
            msg.save()
        except:
            import traceback
            ei = sys.exc_info()
            traceback.print_exception(ei[0], ei[1], ei[2], None, sys.stderr)
            del ei

handler = SwordLogHandler('SWORD')
log = logging.getLogger()
log.setLevel(logging.DEBUG)
log.addHandler(handler)
