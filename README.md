# What is this?
This is backend for a card designer.


# API
- presets list available at `/list_presets`
- presets themselves available at `/presets`
- Upload images at `/upload` with body of form-data with image: image and CropData: { "x": int, "y": int, "width": int, "height": int, "unit": String }


# How to run?
Fill the needed env variables in `.env` and then `just run`


# Where to curl?
You can find the actual port in `./src/main/resources/application.properties`, property `server.port`
