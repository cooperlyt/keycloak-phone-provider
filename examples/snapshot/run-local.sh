#!/bin/bash

mvn -f ../../pom.xml clean package \
  && docker build -t keycloak:snapshot ../../target/ \
  && docker-compose -f keycloak/docker-compose.yml down \
  && docker-compose -f keycloak/docker-compose.yml up
