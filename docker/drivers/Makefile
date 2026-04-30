# default: all

REPO := bzengnexus1.idmz.eng.ses/

TOPTARGETS := all push

SUBDIRS := $(wildcard */.)

$(TOPTARGETS): $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS) REPO=$(REPO)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
