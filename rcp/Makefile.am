include ${top_srcdir}/common.mk

if WITH_GUI
install-exec-hook:
	mkdir -p ${exec_prefix}/rcp
if HAVE_RSYNC 
	$(RSYNC) ${top_srcdir}/rcp/* ${exec_prefix}/rcp/
else 
	cp -r ${top_srcdir}/rcp/* ${exec_prefix}/rcp/.
	find ${exec_prefix}/rcp -name ".svn" | xargs rm -rf
	rm -f ${exec_prefix}/rcp/Makefile*
endif 
endif

