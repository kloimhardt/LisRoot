SHELL := bash

ROOT := $(shell pwd)

FILES := \
  root_plot \
  python_comparison \
  cpp_comparison \
  cpp_native \
  translation \
  ytranslation \

TEST_TARGETS := \
  $(FILES:%=test-%)

ifneq (,$(shell command -v root-config))
CLANG_OPTS := $(shell root-config --glibs --cflags --libs)
endif

FERRET := ferret.jar

DOCKER_NAME := kloimhardt-lisroot
DOCKER_HISTORY := /tmp/docker-bash-history


#------------------------------------------------------------------------------
default:

runall:
	-./runall.sh
	chown -R --reference=Makefile .

generate:
	-$(MAKE) $(TEST_TARGETS)
	chown -R --reference=Makefile .

open:
ifeq (,$(wildcard *.pdf))
	@echo "Nothing to open. Try 'make docker-generate' first." && exit 1
endif
ifndef OPENER
	@echo 'OPENER variable not set' && exit 1
endif
	$(OPENER) *.pdf

clean:
	$(RM) $(FILES) *.pdf *.cpp
	$(RM) ytranslation.clj
	$(RM) $(FERRET)

status:
	git status --ignored


#------------------------------------------------------------------------------
docker-build:
	docker build --tag=$(DOCKER_NAME) .

docker-shell: docker-build
	touch $(DOCKER_HISTORY)
	docker run --rm -it \
	  -v $(ROOT):/repo \
	  -v $(DOCKER_HISTORY):/root/.bash_history \
	  -w /repo \
	  $(DOCKER_NAME) bash

docker-generate docker-test docker-clean: docker-build
	docker run --rm -it \
	  -v $(ROOT):/repo \
	  -w /repo \
	  $(DOCKER_NAME) make $(@:docker-%=%)


#------------------------------------------------------------------------------
%.clj: %.yaml
	ys --version
	ys --compile $< > $@
	chown -R --reference=Makefile .

$(FERRET):
	wget --quiet https://ferret-lang.org/builds/$@
	chown -R --reference=Makefile .

test-%: %.clj $(FERRET)
	$(eval override NAME := $(@:test-%=%))
	@echo
	@echo ========== Testing $(NAME)
	java -jar $(FERRET) -i $<
	clang++ $(NAME).cpp $(CLANG_OPTS) -o $(NAME)
	./$(NAME)
	chown -R --reference=Makefile .
