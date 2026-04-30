
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_PCSServicer,\
    add_SPELLDriverInterface_PCSServicer_to_server
from spell.stubs.SPELLDriver_pb2 import Names, PtcResult
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput, PackValue

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue, StringValue
from spell.lang.functions import PCS_GetStatus

class SPELLDriverPCSInterfaceServicer(SPELLDriverInterface_PCSServicer):

    def __init__(self, server):
        self._ptcItemRegistry = {}
        add_SPELLDriverInterface_PCSServicer_to_server(self, server)

    #==========================================================================
    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['PCS'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    #==========================================================================
    def Cleanup(self, request, context):
        REGISTRY['PCS'].cleanup()
        return Empty()

    #==========================================================================
    def Ping(self, request, context):
        return Empty()

    #==========================================================================
    def Close(self, request, context):
        return Empty()

    #==========================================================================

    def CreatePtcItem(self, request, context):
        ptcId = request.id
        if not ptcId or ptcId not in list(self._ptcItemRegistry.keys()):
            ptcItem = REGISTRY['PCS']._createPtcItem(str(request.mnemonic),
                                                     str(request.description))
            ptcId = request.mnemonic
            self._ptcItemRegistry[str(ptcId)] = ptcItem
            request.id = ptcId

        return request

    #==========================================================================
    def _findItem(self, request):
        # RSA: do we need all these str conversions?
        # strings coming from grpc are all in unicode.
        # str and unicode have different hashes even if they got the same content.
        # That's why we have to convert all values to str when using dicts.
        if str(request.item.id) in self._ptcItemRegistry:
            ptcItem = self._ptcItemRegistry[str(request.item.id)]
        else:
            ptcItem = REGISTRY['PCS']._createPtcItem(str(request.item.mnemonic),
                                                   str(request.item.description))
            request.item.id = str(request.item.mnemonic)

        return ptcItem

    #==========================================================================
    def PCS_GetStatus(self, request, context):
        answer = REGISTRY['PCS']._PCS_GetStatus(UnpackValue(request))

        return PackValue(answer)

    #==========================================================================
    def PCS_IsArqEnabled(self, request, context):
        answer = REGISTRY['PCS']._PCS_IsArqEnabled(UnpackValue(request))

        return answer.value

    #==========================================================================
    def PCS_IsVerifyModeEnabled(self, request, context):
        answer = REGISTRY['PCS']._PCS_IsVerifyModeEnabled(UnpackValue(request))

        return answer.value

    #==========================================================================
    def PCS_ReadyToCommand(self, request, context):
        answer = REGISTRY['PCS']._PCS_ReadyToCommand(UnpackValue(request))

        return answer.value

    #==========================================================================
    def PCS_AbortPtc(self, request, context):
        answer = REGISTRY['PCS']._PCS_AbortPtc(UnpackValue(request))

        return answer.value

    #==========================================================================
    def _extractPtc(self, request):
        ptcItem = self._ptcItemRegistry[str(request.command.id)]
        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        args = UnpackValue(request.args)
        self._addArgsToPTC(ptcItem, args, config)

        return ptcItem

    #==========================================================================
    def _addArgsToPTC(self, ptcItem, args, config):
        ptcItem.clear()
        # RSA: Is this correct?
        for arg in args:
            ptcItem[arg[0]] = arg[1:]
        ptcItem.configure(config)

    #==========================================================================
    def _createPtcResultAnswer(self,result):
        answer = PtcResult(code = result['code'],
                           success = result['success'],
                           stage = result['stage'],
                           data = result['data'],
                           error = result['error'])

        return answer

    #==========================================================================
    def PCS_Control(self, request, context):
        ptcItem = self._extractPtc(request)

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['PCS']._PCS_Control(ptcItem, config)

        return self._createPtcResultAnswer(result)
    #==========================================================================
    def SendPtcCommand(self, request, context):
        ptcItem = self._extractPtc(request)

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['PCS']._sendPtcCommand(ptcItem, config)

        return self._createPtcResultAnswer(result)

    #==========================================================================
    def SendPtcList(self, request, context):
        cmdList = []
        for cmd in request.commands:
            cmdList += [self._extractPtc(cmd)]

        if request.HasField('config'):
            config = UnpackValue(request.config)
        else:
            config = {}

        result = REGISTRY['PCS']._sendPtcList(cmdList, config)

        return self._createPtcResultAnswer(result)
