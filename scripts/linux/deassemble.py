###################################################################################
## FILE       : deassemble.py
## DATE       : Mar 17, 2011
## PROJECT    : SPELL
## DESCRIPTION: Bytecode deassembler
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## SPELL is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## SPELL is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

import dis, marshal, struct, sys, time, types,os

def show_file(fname):
    if os.path.exists(fname):
        f = open(fname, "rb")
        magic = f.read(4)
        moddate = f.read(4)
        modtime = time.asctime(time.localtime(struct.unpack('L', moddate)[0]))
        print "magic %s" % (magic.encode('hex'))
        print "moddate %s (%s)" % (moddate.encode('hex'), modtime)
        code = marshal.load(f)
        code_tree(code)
        show_code(code)
    
def show_code(code, indent=''):
    print "%scode" % indent
    indent += '   '
    print "%sargcount %d" % (indent, code.co_argcount)
    print "%snlocals %d" % (indent, code.co_nlocals)
    print "%sstacksize %d" % (indent, code.co_stacksize)
    print "%sflags %04x" % (indent, code.co_flags)
    show_hex("code", code.co_code, indent=indent)
    dis.disassemble(code)
    print "%sconsts" % indent
    for const in code.co_consts:
        if type(const) == types.CodeType:
            show_code(const, indent+'   ')
        else:
            print "   %s%r" % (indent, const)
    print "%snames %r" % (indent, code.co_names)
    print "%svarnames %r" % (indent, code.co_varnames)
    print "%sfreevars %r" % (indent, code.co_freevars)
    print "%scellvars %r" % (indent, code.co_cellvars)
    print "%sfilename %r" % (indent, code.co_filename)
    print "%sname %r" % (indent, code.co_name)
    print "%sfirstlineno %d" % (indent, code.co_firstlineno)
    show_hex("lnotab", code.co_lnotab, indent=indent)
    show_table(code.co_lnotab,indent=indent)
    
def code_tree(code, indent=''):
    print "%scode object: %s" % (indent,code)
    for const in code.co_consts:
        if type(const) == types.CodeType:
            code_tree(const, indent+'   ')
    
def show_table( lnotab, indent ):
    lnotab = lnotab.encode('hex')
    if len(lnotab) < 32:
        print indent,
        for idx in range(0,len(lnotab),4):
            offset = int(lnotab[idx:idx+2], 16)
            sline  = int(lnotab[idx+2:idx+4], 16)
            print "(%s,%s) " % (offset,sline),
        print
    else:
        for i in range(0, len(lnotab), 32):
            line = lnotab[i:i+32]
            print indent,
            for idx in range(0,len(line),4):
                offset = int(lnotab[idx:idx+2], 16)
                sline  = int(lnotab[idx+2:idx+4], 16)
                print "(%s,%s) " % (offset,sline),
            print

def show_hex(label, h, indent):
    h = h.encode('hex')
    if len(h) < 60:
        print "%s%s %s" % (indent, label, h)
    else:
        print "%s%s" % (indent, label)
        for i in range(0, len(h), 60):
            print "%s   %s" % (indent, h[i:i+60])

show_file(sys.argv[1])
