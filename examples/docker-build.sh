#!/bin/sh

cd .. \
  && mvn clean package -Dmaven.test.skip="true" \
  && cd target \
  && sh build-template.sh "$1"