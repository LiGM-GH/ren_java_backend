#!/usr/bin/bash
cd ./nsfw_lib/
just release-build
just release-daemon & \
cd .. && \
NSFW_DAEMON_TCP_PORT=1248 \
NSFW_DAEMON_TCP_HOST="localhost" \
LD_LIBRARY_PATH="./lib" \
./mvnw spring-boot:run && \
fg
