FROM rootproject/root:latest

RUN apt update \
 && apt install -y \
        binutils \
        build-essential \
        clang \
        clojure \
        dpkg-dev \
        git \
        libx11-dev \
        libssl-dev \
        libtbb-dev \
        libxext-dev \
        libxft-dev \
        libxpm-dev \
        openjdk-21-jre \
        python3-dev \
 && true

RUN curl -s https://yamlscript.org/install | BIN=1 bash

RUN git config --global --add safe.directory /repo

RUN apt install -y \
        bash-completion \
        tig \
        vim \
 && echo 'source /etc/bash_completion' >> /root/.bashrc \
 && true
