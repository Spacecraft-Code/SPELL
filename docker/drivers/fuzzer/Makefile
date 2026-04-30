default: build

REPO := bzengnexus1.idmz.eng.ses/
NAME := spell/spell-driver-ubuntu-fuzzer
COMMIT := $(shell git rev-parse --short HEAD)
TAG := $(shell git describe --abbrev=0 --tags ${COMMIT} 2>/dev/null || true)
TAG_COMMIT := $(shell git rev-list -n 1 --abbrev-commit ${TAG})

VERSION := $(TAG:v%=%)
ifneq ($(COMMIT), $(TAG_COMMIT))
    VERSION := $(VERSION)-dev
endif
TAG := $(VERSION)

build:
	docker build --build-arg http_proxy=http://bzengpx1.idmz.eng.ses:8080 --build-arg https_proxy=http://bzengpx1.idmz.eng.ses:8080 --build-arg no_proxy=10.113.66.135,10.35.66.136,localhost,eng.ses,xeneng.ses,az.ses -t $(REPO)$(NAME):v$(TAG) ../../../ -f ./Dockerfile

push:
	docker push $(REPO)$(NAME):v$(TAG)

