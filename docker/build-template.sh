#!/bin/sh

get_arch=`arch`
docker build -t coopersoft/keycloak:$get_arch-@version.keycloak@_phone-@project.version@ .