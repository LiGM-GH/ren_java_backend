package com.ren_backend.ren_backend;

class NsfwPredictor {
    public static native String predict(String filename);

    static {
        System.loadLibrary("nsfw_lib");
    }

    public static void main(String[] args) {
        System.out.println("Calling Rust!");
        String result = NsfwPredictor.predict(args[0]);
        System.out.println("Thanks, Rust!");
        System.out.println(result);
    }
}
