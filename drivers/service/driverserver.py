
from spell.lib.registry import REGISTRY
from spell.stubs.SPELLExecutor_pb2_grpc import SPELLExecutorStub
from spell.stubs.SPELLDriver_pb2 import Interfaces
import spell.stubs.SPELLDriver_pb2 as SPELLDriver_pb2
import spell.stubs.SPELLDriver_pb2_grpc as SPELLDriver_pb2_grpc

from info_service import DriverService
from tm_service  import SPELLDriverTMInterfaceServicer
from tc_service import SPELLDriverTCInterfaceServicer
from ev_service  import SPELLDriverEVInterfaceServicer
from rsc_service import SPELLDriverRSCInterfaceServicer
from time_service import SPELLDriverTIMEInterfaceServicer
from task_service import SPELLDriverTASKInterfaceServicer
from user_service import SPELLDriverUSERInterfaceServicer
from rng_service import SPELLDriverRNGInterfaceServicer
from mem_service import SPELLDriverMEMInterfaceServicer
try:
    from pcs_service import SPELLDriverPCSInterfaceServicer
except:
    pass

from spell.utils.grpc_utils import GRPCThreadPool
from spell.utils.log import LOG

from concurrent import futures
from contextlib import closing
import grpc
from grpc import RpcError
import socket
import time
import signal

KNOWN_INTERFACES = {
     'TM'    : lambda server: (Interfaces.TM     , SPELLDriverTMInterfaceServicer(server)),  # @UndefinedVariable
     'TC'    : lambda server: (Interfaces.TC     , SPELLDriverTCInterfaceServicer(server)),  # @UndefinedVariable
     'EV'    : lambda server: (Interfaces.EV     , SPELLDriverEVInterfaceServicer(server)),  # @UndefinedVariable
     'RSC'   : lambda server: (Interfaces.RSC    , SPELLDriverRSCInterfaceServicer(server)),  # @UndefinedVariable
     'TIME'  : lambda server: (Interfaces.TIME   , SPELLDriverTIMEInterfaceServicer(server)),  # @UndefinedVariable
     'TASK'  : lambda server: (Interfaces.TASK   , SPELLDriverTASKInterfaceServicer(server)),  # @UndefinedVariable
     'USER'  : lambda server: (Interfaces.USER   , SPELLDriverUSERInterfaceServicer(server)),  # @UndefinedVariable
#      'CONFIG': lambda server: (Interfaces.CONFIG , SPELLDriverCONFIGInterfaceServicer(server)),  # @UndefinedVariable
     'RNG'   : lambda server: (Interfaces.RNG    , SPELLDriverRNGInterfaceServicer(server)),  # @UndefinedVariable
     'MEM'   : lambda server: (Interfaces.MEM    , SPELLDriverMEMInterfaceServicer(server)),  # @UndefinedVariable
     'PCS'   : lambda server: (Interfaces.PCS    , SPELLDriverPCSInterfaceServicer(server))   # @UndefinedVariable
    }

# Global for signal handling
server = None

def signal_handler(signal, frame):
    global server
    log('signal received: %s' % str(signal))
    if server != None:
        server.stop(0)
    server = None
    REGISTRY['EXEC']._close()

def log(msg):
    LOG('[driverserver]: ' + msg)

def find_free_port():
    with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
        s.bind(('', 0))
        return s.getsockname()[1]

def serve(executorHost = None, executorPort = None):
    global server
    server = grpc.server(GRPCThreadPool('DRIVER', max_workers=50))
    port = find_free_port()
    service = DriverService(port)
    SPELLDriver_pb2_grpc.add_SPELLDriverInterfaceServicer_to_server(service, server)
    endpoint = '[::]:' + str(port)
    port = server.add_insecure_port(endpoint)
    service._setPort(port)

    # install signal handler
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    log('starting gRPC server at ' + endpoint)
    server.start()

    if executorHost and executorPort:
        log('connecting to SPELL Executor')

        driverName = REGISTRY['CTX'].getDriver()
        driverLogin = SPELLDriver_pb2.DriverInfo(driverName = driverName,
                                                 host =socket.getfqdn(),
                                                 port = port)

        driverInterfaces = REGISTRY['CONFIG'].getDriverConfig().getInterfaces()
        log('registering the following interfaces for driver %s:' % driverName)
        log(driverInterfaces)
        for interface in driverInterfaces.split(','):
            interface = interface.strip()
            if interface in list(KNOWN_INTERFACES.keys()):
                try:
                    log('creating driver interface %s' % interface)
                    ifcId, ifcService = KNOWN_INTERFACES[interface](server)
                    if ifcService:
                        driverLogin.interfaces.append(ifcId)
                    log('created driver interface %s for id %s ' \
                        % (interface, ifcId))
                except:
                    log('could not create interface service for %s!' % interface)
                    import traceback
                    traceback.print_exc()
                    del traceback

        channel = grpc.insecure_channel('%s:%d' % (executorHost, executorPort))
        stub = SPELLExecutorStub(channel)
        try:
            response = stub.DriverLogin(driverLogin)
            REGISTRY['CIF']._setup(channel, response)
            REGISTRY['EXEC']._setup(channel, response)
            log('login to SPELL Executor succeeded')
        except RpcError as e:
            log('login to SPELL Executor failed with error code %s' % e.code())
            log(e)
            server.stop(0)
            REGISTRY['EXEC']._close()
            return
    else:
        REGISTRY['CIF']._setup()
        REGISTRY['EXEC']._setup()

    log('started to serve requests, hit ctrl-c to stop')
    while server != None:
        time.sleep(1)
