
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_RSCServicer,\
    add_SPELLDriverInterface_RSCServicer_to_server
from spell.stubs.SPELLDriver_pb2 import ResourceRequest
from spell.utils.grpc_utils import UnpackValue, PackValue, unicode2string,\
    LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverRSCInterfaceServicer(SPELLDriverInterface_RSCServicer):

    def __init__(self, server):
        self._tmItemRegistry = {}
        add_SPELLDriverInterface_RSCServicer_to_server(self, server)

    def SetLink(self, request, context):
        REGISTRY['RSC'].setLink(str(request.name), unicode2string(request.status),
                                UnpackValue(request.config))
        return BoolValue(value = True)

    def CheckLink(self, request, context):
        result = REGISTRY['RSC'].checkLink(str(request.name))

        return BoolValue(value = result)

    def SetResource(self, request, context):
        result = REGISTRY['RSC']._setResource(str(request.name),
                                              UnpackValue(request.value),
                                              UnpackValue(request.config))

        return BoolValue(value = result)

    def GetResource(self, request, context):
        result = REGISTRY['RSC']._getResource(str(request.name),
                                              UnpackValue(request.config))

        return ResourceRequest(value = PackValue(result))

    def GetResources(self, request, context):
        # FIXME: What about isLocal?
        result = REGISTRY['RSC']._getResources(UnpackValue(request.config))

        return ResourceRequest(value = PackValue(result))

    def GetResourceStatus(self, request, context):
        result = REGISTRY['RSC']._getResourceStatus(str(request.name),
                                                    UnpackValue(request.config))

        return ResourceRequest(value = PackValue(result))

    def IsResourceOK(self, request, context):
        result = REGISTRY['RSC']._isResourceOK(str(request.name),
                                               UnpackValue(request.config))

        return BoolValue(value = result)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['RSC'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['RSC'].cleanup()

        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        # TODO
        return Empty()
