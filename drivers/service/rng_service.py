
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_RNGServicer,\
    add_SPELLDriverInterface_RNGServicer_to_server
from spell.stubs.SPELLDriver_pb2 import Names
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue, StringValue

class SPELLDriverRNGInterfaceServicer(SPELLDriverInterface_RNGServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_RNGServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['RNG'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['RNG'].cleanup()
        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def EnableRanging(self, request, context):
        config = UnpackValue(request.config)

        result = REGISTRY['RNG']._enableRanging(config)

        return BoolValue(value = result)

    def DisableRanging(self, request, context):
        config = UnpackValue(request.config)

        result = REGISTRY['RNG']._disableRanging(config)

        return BoolValue(value = result)

    def AbortRanging(self, request, context):
        config = UnpackValue(request.config)

        result = REGISTRY['RNG']._abortRanging(config)

        return BoolValue(value = result)


    def StartRanging(self, request, context):
        config = UnpackValue(request.config)
        bbe = request.bbe
        antenna = request.antenna

        result = REGISTRY['RNG']._startRanging(bbe, antenna, config)

        return BoolValue(value = result)


    def StartCalibration(self, request, context):
        config = UnpackValue(request.config)
        bbe = request.bbe
        antenna = request.antenna

        result = REGISTRY['RNG']._startCalibration(bbe, antenna, config)

        return BoolValue(value = result)

    def GetBasebandConfig(self, request, context):
        config = UnpackValue(request.config)
        bbe = request.bbe
        param = request.param

        result = REGISTRY['RNG']._getBasebandConfig(bbe, param, config)

        request.value = str(result)

        return request

    def SetBasebandConfig(self, request, context):
        config = UnpackValue(request.config)
        bbe = request.bbe
        param = request.param
        value = request.value

        result = REGISTRY['RNG']._setBasebandConfig(bbe, param, value, config)

        return BoolValue(value = result)

    def GetAntennaNames(self, request, context):
        config = UnpackValue(request)

        result = REGISTRY['RNG']._getAntennaNames(config)

        return Names(names = result)

    def GetRangingPaths(self, request, context):
        config = UnpackValue(request)

        result = REGISTRY['RNG']._getRangingPaths(config)

        return Names(names = result)

    def GetBasebandNames(self, request, context):
        config = UnpackValue(request)

        result = REGISTRY['RNG']._getBasebandNames(config)

        return Names(names = result)

    def GetRangingStatus(self, request, context):
        config = UnpackValue(request)

        result = REGISTRY['RNG']._getRangingStatus(config)

        return StringValue(value = result)

    def UpdateCortexConf(self, request, context):
        config = UnpackValue(request)

        result = REGISTRY['RNG']._updateCortexConf(config)

        return BoolValue(value = result)
