# What is this?
This is backend for a card designer.


# API
- presets list available at `/list_presets`
- presets themselves available at `/presets`
- Upload images at `/upload` with body of form-data with image: image and CropData: { "x": int, "y": int, "width": int, "height": int, "unit": String }


# How to run?
Fill the needed env variables in `.env`:
```bash
NSFW_DAEMON_TCP_PORT="daemon_port_probably_on_localhost"
NSFW_DAEMON_TCP_HOST="probably_localhost"
IMAGE_DB_URL="postgresql://some_host:some_port/dbname"
IMAGE_DB_USERNAME="some_username"
IMAGE_DB_PASSWORD="some_password"
IMAGE_DB_TABLENAME="probably_images"
```
and then `just run`

# Where to curl?
You can find the actual port in `./src/main/resources/application.properties`, property `server.port`
