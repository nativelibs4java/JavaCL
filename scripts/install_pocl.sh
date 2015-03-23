#!/bin/bash

set -e

# On mac: sudo port install libtool
sudo apt-get install -qq libltdl-dev
sudo apt-get install -qq ocl-icd-libopencl1

git clone https://github.com/pocl/pocl.git
cd pocl
./autogen.sh

# --disable-icd
./configure
make

sudo make install
