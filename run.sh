#!/usr/bin/sh

export $(cat ./.env | grep -v ^# | xargs) >/dev/null

cd ./nsfw_lib/
just release-build
just release-daemon & \
cd .. && \
./mvnw spring-boot:run && \
fg
