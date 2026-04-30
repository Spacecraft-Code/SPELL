
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_TIMEServicer,\
    add_SPELLDriverInterface_TIMEServicer_to_server
from spell.stubs.SPELLDriver_pb2 import Time
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty

class SPELLDriverTIMEInterfaceServicer(SPELLDriverInterface_TIMEServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_TIMEServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['TIME'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['TIME'].cleanup()

        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def GetUTC(self, request, context):
        config = UnpackValue(request)
        answer = REGISTRY['TIME']._getUTC(config)

        return Time(timeMsg = str(answer))
