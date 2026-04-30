# default: all

REPO := bzengnexus1.idmz.eng.ses/
# TAG := 1.0.0

TOPTARGETS := all push

SUBDIRS := $(wildcard */.)

$(TOPTARGETS): $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS) REPO=$(REPO)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
