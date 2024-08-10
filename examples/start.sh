#!/bin/sh

docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin  coopersoft/keycloak:25.0.2_phone-2.4.1-snapshot start-dev --spi-phone-default-service=dummy