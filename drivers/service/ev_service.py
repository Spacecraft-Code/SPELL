
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_EVServicer,\
    add_SPELLDriverInterface_EVServicer_to_server
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty

class SPELLDriverEVInterfaceServicer(SPELLDriverInterface_EVServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_EVServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['EV'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['EV'].cleanup()
        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def RaiseEvent(self, request, context):
        msg = str(request.message)
        config = UnpackValue(request.config)

        REGISTRY['EV']._raiseEvent(msg, config)

        return Empty()
