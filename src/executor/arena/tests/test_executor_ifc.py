
try:
    print globals()
    assert( REGISTRY.exists("EXEC") == True )
    print REGISTRY.get('EXEC').getStatus()
    print "TEST OK"
except BaseException,ex:
    print "TEST FAILED",str(ex)
