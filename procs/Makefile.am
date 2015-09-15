include ${top_srcdir}/common.mk

if WITH_PROCS
install-exec-hook:
	mkdir -p ${exec_prefix}/data/Procedures/STD
	mkdir -p ${exec_prefix}/data/UserLib/STD
if HAVE_RSYNC 
	$(RSYNC) ${top_srcdir}/procs/procedures/* ${exec_prefix}/data/Procedures/STD/
	$(RSYNC) ${top_srcdir}/procs/userlib/* ${exec_prefix}/data/UserLib/STD/
else 
	cp -r ${top_srcdir}/procs/procedures/* ${exec_prefix}/data/Procedures/STD/.
	cp -r ${top_srcdir}/procs/userlib/* ${exec_prefix}/data/UserLib/STD/.
	find ${exec_prefix}/data/Procedures/STD -name ".svn" | xargs rm -rf
	find ${exec_prefix}/data/UserLib/STD -name ".svn" | xargs rm -rf
endif 
endif
