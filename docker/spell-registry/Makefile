default: build

REPO := bzengnexus1.idmz.eng.ses/
NAME := spell/spell-registry
COMMIT := $(shell git rev-parse --short HEAD)
TAG := $(shell git describe --abbrev=0 --tags ${COMMIT} 2>/dev/null || true)
TAG_COMMIT := $(shell git rev-list -n 1 --abbrev-commit ${TAG})

VERSION := $(TAG:v%=%)
ifneq ($(COMMIT), $(TAG_COMMIT))
    VERSION := $(VERSION)-dev
endif
TAG := $(VERSION)


build:
#	docker run -it --rm  -e http_proxy=http://bzengpx1.idmz.eng.ses:8080 -e https_proxy=http://bzengpx1.idmz.eng.ses:8080 -e no_proxy=10.113.66.135,10.35.66.136,localhost,eng.ses,xeneng.ses,az.ses -v  $(PWD):/app -w /app sbtscala/scala-sbt:eclipse-temurin-19_36_1.8.0_3.2.1 sbt docker:publishLocal
	VERSION=$(TAG) sbt docker:publishLocal

push:
	docker tag spell-registry:$(TAG) $(REPO)$(NAME):v$(TAG)
	docker push $(REPO)$(NAME):v$(TAG)

