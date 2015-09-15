
from spell.config.reader import *

try:
    print "CONFIG ", Config.instance()
except BaseException,ex:
    print "TEST FAILED",str(ex)
