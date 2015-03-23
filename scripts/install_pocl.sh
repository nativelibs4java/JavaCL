#!/bin/bash

set -e

git clone https://github.com/pocl/pocl.git
cd pocl
./autogen.sh

# --disable-icd
./configure
make

sudo make install
