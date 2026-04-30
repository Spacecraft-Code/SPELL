
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLExecutor_pb2_grpc import SPELLExecutorStub
from spell.utils.timing import RepeatingThread

from google.protobuf.empty_pb2 import Empty
from grpc import RpcError
import os
from spell.config.reader import Config

debugOn = False
def debug(msg):
    if debugOn: print(msg)

# EXEC interface
class EXEC_Client(SPELLExecutorStub):

    def __init__(self):
        self._ready = False
        self._ping = None
        self._retryCount = 0
        self._spellAccess = False

    def _setup(self, channel=None, options=None):
        self._ready = True
        if not channel:
            return
        SPELLExecutorStub.__init__(self, channel)
        self.createPingThread()

    def _close(self):
        if self._ping is None:
            return
        self._ping.cancel()
        self._ping = None

    def createPingThread(self):
        self._ping = RepeatingThread(REGISTRY['CONFIG'].getDriverConfig().getGRPCPingTime(),
                                     self.checkConnectionAlive)
        self._ping.start()

    def checkConnectionAlive(self):
        try:
            self.Ping(Empty(),
                      timeout = REGISTRY['CONFIG'].getDriverConfig().getGRPCTimeout())
            self._retryCount = 0
            debug('[DRIVER] SPELL Executor is alive!')
        except RpcError as e:
            import traceback
            excMsg = traceback.format_exception_only(RpcError, e)
            self._retryCount += 1
            if self._retryCount <= REGISTRY['CONFIG'].getDriverConfig().getGRPCRetry():
                debug('[DRIVER] SPELL Executor does not react anymore! Retry %s/%s' \
                    % (self._retryCount, REGISTRY['CONFIG'].getDriverConfig().getGRPCRetry()))
                debug(str(excMsg))
            else:
                debug('[DRIVER] SPELL Executor stopped reacting! Exiting SPELL Driver')
                os._exit(0)
            del traceback

    # Obtain executor status
    def getStatus(self, *arg):
        debug('EXEC: getStatus: %s' % repr(arg))

    # Obtain execution environment
    def getEnvironment(self, *arg):
        debug('EXEC: getEnvironment: %s' % repr(arg))

    # Set the language lock
    def processLock(self, *arg):
        debug('EXEC: processLock: %s' % repr(arg))

    # Unset the language lock
    def processUnlock(self, *arg):
        debug('EXEC: processUnlock: %s' % repr(arg))

    # Start waiting process
    def startWait(self, *arg):
        debug('EXEC: startWait: %s' % repr(arg))

    # End waiting process
    def finishWait(self, *arg):
        debug('EXEC: finishWait: %s' % repr(arg))

    # Start prompt
    def startPrompt(self, *arg):
        debug('EXEC: startPrompt: %s' % repr(arg))

    # End prompt
    def endPrompt(self, *arg):
        debug('EXEC: endPrompt: %s' % repr(arg))

    # Wait for event
    def wait(self, *arg):
        debug('EXEC: wait: %s' % repr(arg))

    # Open child procedure
    def openSubProcedure(self, *arg):
        debug('EXEC: openSubProcedure: %s' % repr(arg))

    # Close child procedure
    def closeSubProcedure(self, *arg):
        debug('EXEC: closeSubProcedure: %s' % repr(arg))

    # Kill child procedure
    def killSubProcedure(self, *arg):
        debug('EXEC: killSubProcedure: %s' % repr(arg))

    # Get child procedure status
    def getChildStatus(self, *arg):
        debug('EXEC: getChildStatus: %s' % repr(arg))

    # Get child procedure error info
    def getChildError(self, *arg):
        debug('EXEC: getChildError: %s' % repr(arg))

    # Is subprocedure in error
    def isChildError(self, *arg):
        debug('EXEC: isChildError: %s' % repr(arg))

    # Is subprocedure finished
    def isChildFinished(self, *arg):
        debug('EXEC: isChildFinished: %s' % repr(arg))

    # Abort execution
    def abort(self, *arg):
        debug('EXEC: abort: %s' % repr(arg))

# init
REGISTRY['EXEC'] = EXEC_Client()
