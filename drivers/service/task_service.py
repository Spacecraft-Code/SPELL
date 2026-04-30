
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_TASKServicer,\
    add_SPELLDriverInterface_TASKServicer_to_server
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverTASKInterfaceServicer(SPELLDriverInterface_TASKServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_TASKServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['TASK'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['TASK'].cleanup()

        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def StartTask(self, request, context):
        name = request.name
        args = UnpackValue(request.args)
        config = UnpackValue(request.config)
        answer = REGISTRY['TASK']._startTask(name, args, config)

        return BoolValue(value = answer)

    def StopTask(self, request, context):
        name = request.name
        config = UnpackValue(request.config)
        answer = REGISTRY['TASK']._stopTask(name, config)

        return BoolValue(value = answer)

    def CheckTask(self, request, context):
        name = request.name
        config = UnpackValue(request.config)
        answer = REGISTRY['TASK']._checkTask(name, config)

        return BoolValue(value = answer)

    def OpenDisplay(self, request, context):
        name = request.name
        config = UnpackValue(request.config)
        REGISTRY['TASK']._openDisplay(name, config)

        return Empty()

    def CloseDisplay(self, request, context):
        name = request.name
        config = UnpackValue(request.config)
        REGISTRY['TASK']._closeDisplay(name, config)

        return Empty()

    def PrintDisplay(self, request, context):
        name = request.name
        config = UnpackValue(request.config)
        REGISTRY['TASK']._printDisplay(name, config)

        return Empty()
