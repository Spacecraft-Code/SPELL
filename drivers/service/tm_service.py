
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2 import LookupAnswer, LimitSet
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_TMServicer,\
    add_SPELLDriverInterface_TMServicer_to_server
from spell.utils.grpc_utils import UnpackValue, PackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverTMInterfaceServicer(SPELLDriverInterface_TMServicer):

    def __init__(self, server):
        self._tmItemRegistry = {}
        add_SPELLDriverInterface_TMServicer_to_server(self, server)

    def CreateTmItem(self, request, context):
        tmId = request.id
        if not tmId or tmId not in list(self._tmItemRegistry.keys()):
            tmItem = REGISTRY['TM']._createTmItem(str(request.mnemonic),
                                                  str(request.description))
            tmId = request.mnemonic
            self._tmItemRegistry[str(tmId)] = tmItem
            request.id = tmId

        return request

    def _findItem(self, request):
        # RSA: do we need all these str conversions?
        if str(request.item.id) in self._tmItemRegistry:
            tmItem = self._tmItemRegistry[str(request.item.id)]
        else:
            tmItem = REGISTRY['TM']._createTmItem(str(request.item.mnemonic),
                                                  str(request.item.description))
            request.item.id = str(request.item.mnemonic)

        return tmItem

    def RefreshItem(self, request, context):
        tmItem = self._findItem(request)
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}
        _, validity = REGISTRY['TM']._refreshItem(tmItem, config)

        timestamp = str(tmItem.time())
        request.engValue.CopyFrom(PackValue(tmItem.eng()))
        request.rawValue.CopyFrom(PackValue(tmItem.raw()))

        if timestamp and tmItem.time() is not None:
            request.timestamp = timestamp

        request.validity = bool(validity)

        return request

    def DatabaseLookup(self, request, context):
        answer = REGISTRY['TM']._databaseLookup(str(request.name),
                                                str(request.type),
                                                str(request.source))

        return LookupAnswer(lookup = PackValue(answer))

    def GetLimits(self, request, context):
        tmItem = self._findItem(request)
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        answer = REGISTRY['TM']._getLimits(tmItem,config)

        return LimitSet(limitset = PackValue(answer))

    def SetLimits(self, request, context):
        tmItem = self._findItem(request)
        if request.HasField('set'):
            limitset = UnpackValue(request.set.limitset)
        else:
            limitset = {}

        answer = REGISTRY['TM']._setLimits(tmItem,limitset)
        if answer is None:
            answer = True

        return BoolValue(value = answer)

    def IsAlarmed(self, request, context):
        tmItem = self._findItem(request)
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        answer = REGISTRY['TM']._isAlarmed(tmItem, config)
        if answer is None:
            answer = False

        return BoolValue(value = answer)

    def InjectItem(self, request, context):
        tmItem = self._findItem(request)
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}
        value = UnpackValue(request.engValue)

        answer = REGISTRY['TM']._injectItem(tmItem, value, config)

        if answer is None:
            answer = False

        return BoolValue(value = answer)

    def RestoreNormalLimits(self, request, context):
        answer = REGISTRY['TM']._restoreNormalLimits(UnpackValue(request))

        if answer is None:
            answer = False

        return BoolValue(value = answer)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['TM'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['TM'].cleanup()

        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        # TODO
        return Empty()
