import time,sys,os

def function( a, b ):
    print globals()
    print "--"
    print repr(a) + "+" + repr(b)
    print "--?"
    raw_input()
    return 66
X=99
print "MAIN 5"
print globals()
print "LOOP"
for i in range(0,100):
    D = 55
print "MAIN 6"
function(3,4)
print "MAIN 7"
print "MAIN 8"
print "MAIN 9"
print time.time()
print "MAIN 10"
print "MAIN 11"
print X
print "MAIN 12"
print globals()
print "MAIN 13"
#function(1,2)
print "MAIN 14"
print "MAIN 15"

