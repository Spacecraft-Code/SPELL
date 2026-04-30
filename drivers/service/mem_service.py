
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLDriver_pb2_grpc import SPELLDriverInterface_MEMServicer,\
    add_SPELLDriverInterface_MEMServicer_to_server
from spell.utils.grpc_utils import UnpackValue, LogFunctionInput, PackValue

from google.protobuf.empty_pb2 import Empty
from google.protobuf.wrappers_pb2 import BoolValue

class SPELLDriverMEMInterfaceServicer(SPELLDriverInterface_MEMServicer):

    def __init__(self, server):
        add_SPELLDriverInterface_MEMServicer_to_server(self, server)

    def Setup(self, request, context):
        from spell.lib.drivermgr import DriverManager
        mgr = DriverManager.instance()
        REGISTRY['MEM'].setup(mgr.contextConfig,mgr.driverConfig)
        del DriverManager
        return request

    def Cleanup(self, request, context):
        REGISTRY['MEM'].cleanup()
        return Empty()

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        return Empty()

    def GenerateReport(self, request, context):
        image = request.image
        report_type = request.report_type
        begin = request.begin
        end = request.end
        source = request.source
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._generateReport(image, report_type, begin,
                                                 end, source, config)

        return BoolValue(value = result)

    def CompareImages(self, request, context):
        image1 = request.image1
        image2 = request.image2
        report_type = request.report_type
        begin = request.begin
        end = request.end
        source = request.source
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._compareImages(image1, image2, report_type,
                                                begin, end, source, config)

        return BoolValue(value = result)


    def CreateGoldenImage(self, request, context):
        scp = request.scp
        filename = request.filename
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._createGoldenImage(self, scp, filename, config)

        return BoolValue(value = result)


    def MemoryLookup(self, request, context):
        name = request.name
        address = request.address
        image = request.image
        report_type = request.report_type
        source = request.source
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._memoryLookup(name, address, image,
                                               report_type, source, config)

        return PackValue(result)


    def DataObjectLookup(self, request, context):
        name = request.name
        ltype = request.type
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._dataObjectLookup(name, ltype, config)

        return PackValue(result)


    def DataItemLookup(self, request, context):
        name = request.name
        ltype = request.type
        source = request.source
        valformat = request.valformat
        config = UnpackValue(request.config)

        result = REGISTRY['MEM']._dataItemLookup(name, ltype, source, valformat, config)

        return PackValue(result)
