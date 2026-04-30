
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_TCServicer,\
    add_SPELLDriverInterface_TCServicer_to_server
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverTCInterfaceServicer(SPELLDriverInterface_TCServicer):

    def __init__(self, server):
        self._tcItemRegistry = {}
        add_SPELLDriverInterface_TCServicer_to_server(self, server)

    def CreateTcItem(self, request, context):
        tcId = request.id
        if not tcId or tcId not in list(self._tcItemRegistry.keys()):
            tcItem = REGISTRY['TC']._createTcItem(str(request.mnemonic),
                                                  str(request.description))
            tcId = id(tcItem)
            self._tcItemRegistry[str(tcId)] = tcItem
            request.id = tcId

        return request

    def _extractCommand(self, request):
        tcItem = self._tcItemRegistry[str(request.command.id)]
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        args = UnpackValue(request.args)
        self._addArgsToTC(tcItem, args, config)

        return tcItem

    def _addArgsToTC(self, tcItem, args, config):
        tcItem.clear()
        # RSA: Is this correct?
        for arg in args:
            tcItem[arg[0]] = arg[1:]
        tcItem.configure(config)

    def SendCommand(self, request, context):
        tcItem = self._extractCommand(request)

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['TC']._sendCommand(tcItem, config)

        return BoolValue(value = result)

    def SendList(self, request, context):
        cmdList = []
        for cmd in request.commands:
            cmdList += [self._extractCommand(cmd)]

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['TC']._sendList(cmdList, config)

        return BoolValue(value = result)

    def SendBlock(self, request, context):
        cmdList = []
        for cmd in request.commands:
            cmdList += [self._extractCommand(cmd)]

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['TC']._sendBlock(cmdList, config)

        return BoolValue(value = result)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['TC'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['TC'].cleanup()
        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        # TODO
        return Empty()
