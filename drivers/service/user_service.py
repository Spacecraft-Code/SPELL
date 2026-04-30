
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_USERServicer,\
    add_SPELLDriverInterface_USERServicer_to_server
from spell.lib.registry import REGISTRY
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverUSERInterfaceServicer(SPELLDriverInterface_USERServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_USERServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['USER'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['USER'].cleanup()

        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def Login(self, request, context):
        name = request.username
        pwd = request.password
        config = UnpackValue(request.config)

        result = REGISTRY['USER']._login(name, pwd, config)

        return BoolValue(value = result)

    def Logout(self, request, context):
        name = request.username
        config = UnpackValue(request.config)

        result = REGISTRY['USER']._logout(name, config)

        return BoolValue(value = result)


    def IsLoggedIn(self, request, context):
        name = request.username
        config = UnpackValue(request.config)

        result = REGISTRY['USER']._isLoggedIn(name, config)

        return BoolValue(value = result)
