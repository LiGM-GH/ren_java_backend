set dotenv-load

default:
    just --list

build:
    (cd ./nsfw_lib/; cargo build --release)

    rm ./lib/*
    rm ./bin/*
    cp nsfw_lib/target/release/libnsfw_lib.*  ./lib/
    cp nsfw_lib/target/release/nsfw_daemon    ./bin/

run: build
    ./bin/nsfw_daemon 1>&2 |         \
    NSFW_DAEMON_TCP_PORT=1248        \
    NSFW_DAEMON_TCP_HOST="localhost" \
    LD_LIBRARY_PATH="./lib"          \
    ./mvnw spring-boot:run
