
from spell.lib.registry import REGISTRY
    
try:
    print REGISTRY.interfaces()
    print dir(REGISTRY['EXEC'])
except BaseException,ex:
    print "TEST FAILED",str(ex)
