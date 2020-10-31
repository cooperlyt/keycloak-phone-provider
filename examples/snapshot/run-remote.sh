#!/bin/bash

REMOTE_ADDR="192.168.1.21"
REMOTE_PATH="/home/docker"

echo *********** build and run *********************

mvn -f ../../pom.xml clean package \
  && docker build -t keycloak-phone:snapshot ../../target/ \
  && docker save -o ../../target/keycloak-phone.tar keycloak-phone:snapshot \
  && scp -r keycloak root@$REMOTE_ADDR:$REMOTE_PATH/ \
  && scp ../../target/keycloak-phone.tar root@$REMOTE_ADDR:$REMOTE_PATH/keycloak/ \
  && rm -f ../../target/keycloak-phone.tar \
  && echo "REMOTE_PATH=${REMOTE_PATH}" | cat - remote.run.template > temp && mv temp ../../target/remote_run.sh \
  && scp ../../target/remote_run.sh root@$REMOTE_ADDR:$REMOTE_PATH/keycloak/run.sh && rm -f ../../target/remote_run.sh \
  && ssh -t root@$REMOTE_ADDR "sh ${REMOTE_PATH}/keycloak/run.sh"

#  run as local
#  && docker-compose -f ~/keycloak/docker-compose.yml down \
#  && docker-compose -f ~/keycloak/docker-compose.yml up
