#!/bin/sh

get_arch=`arch`
docker build -t @docker.image.name@:$get_arch-@version.keycloak@_phone-@project.version@ .