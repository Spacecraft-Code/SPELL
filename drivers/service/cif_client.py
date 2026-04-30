
from spell.lib.registry import REGISTRY
from spell.utils.log import LOG

debugOn = False
def debug(msg):
    if debugOn: print(msg)

# CIF interface
class CIF_Client():

    def __init__(self):
        self._ready = False

    def _setup(self, channel=None, options=None):
        self._ready = True
        LOG.setRedirectMethod(self.remoteLog)

    # Change CIF verbosity
    def setVerbosity(self, *arg):
        debug('CIF setVerbosity: %s' % repr(arg))

    # Reset CIF verbosity
    def resetVerbosity(self, *arg):
        debug('CIF resetVerbosity: %s' % repr(arg))

    # Write a message
    def write(self, *arg):
        debug('CIF write: %s' % repr(arg))

    # Send a notification
    def notify(self, notifyType = None, name = None, value = None,
               status = None, reason = None, time = None, stack = None):
        debug('CIF Notify: %s %s=%s (%s) %s' \
            % (notifyType, name, value, status, reason))

    # Send a prompt
    def prompt(self, *arg):
        debug('CIF prompt: %s' % repr(arg))

    def remoteLog(self, message, severity, level):
        # return False: To prevent the LoggerClass from handling the logging itself (prevent double output in log file and console log)
        # return True: Append log entry to configured log file and/or console output as configured
        return True

# init
REGISTRY['CIF'] = CIF_Client()
