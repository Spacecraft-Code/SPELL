
from spell.lib.registry import REGISTRY
import spell.stubs.SPELLDriver_pb2 as SPELLDriver_pb2
import spell.stubs.SPELLDriver_pb2_grpc as SPELLDriver_pb2_grpc

from google.protobuf.empty_pb2 import Empty
import socket
from threading import Timer

class DriverService(SPELLDriver_pb2_grpc.SPELLDriverInterfaceServicer):

    def __init__(self, port):
        self.driverName = REGISTRY['CTX'].getDriver()
        self.host = socket.getfqdn()
        self.port = port

    def _setPort(self, port):
        self.port = port

    def GetDriverInfo(self, request, context):
        return SPELLDriver_pb2.DriverInfo(driverName = self.driverName,
                                          host = self.host, port = self.port)

    def Cleanup(self, request, context):
        return request

    def Ping(self, request, context):
        return Empty()

    def Close(self, request, context):
        # TODO: test
        def doExit():
            exit(0)
        exitTimer = Timer(2,doExit,[]).start()
        return Empty()
