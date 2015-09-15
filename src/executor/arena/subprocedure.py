
import time,sys,os

















def function1():
    print "FUNCTION 1START"
    function2()
    print "FUNCTION 1 END"
    return 0

def function2():
    print "FUNCTION 2 START"
    A=55; B=66
    try:
        print "TRY ",A
        #print x
    except NameError,err:
        print "EXCEPT"
    finally:
        print "FINALLY"
    print "FUNCTION 2 END"
    return 1

def function3():
    print "FUNCTION 3 START"
    for i in range(0,5):
        print "LOCALS",locals()
        print "LOOP",i
        raw_input()
    print "FUNCTION 3 END"
    return 1

def function4():
    print "FUNCTION 4 START"
    if 0<1:
        print "IF"
    else:
        print "ELSE"
    print "FUNCTION 4 END"
    return 1
