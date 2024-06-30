package com.ren_backend.ren_backend;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    @GetMapping("/list_presets")
    public ResponseEntity<List<String>> getStandardPatterns() throws IOException {
        String directory_name = System.getProperty("user.dir") + "/src/main/resources/static/presets";
        System.out.println(directory_name);
        File dir = new File(directory_name);
        File[] files = dir.listFiles();
        List<String> filenames = Stream.of(files).map((file) -> file.getName()).toList();

        for (var file : filenames) {
            System.out.println(file);
        }

        return ResponseEntity.ok(filenames);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file, @RequestParam("x") int x,
            @RequestParam("y") int y, @RequestParam("width") int width, @RequestParam("height") int height)
            throws IOException {
        String contentType = file.getContentType();

        if (contentType == null || !contentType.contains("image/")) {
            return ResponseEntity.badRequest().build();
        }
        contentType = contentType.replace("image/", "");

        System.out.println("Uploaded file has the following content type: " + contentType);

        try {
            Path filename = Files.createTempFile(Path.of(UPLOAD_DIRECTORY), "ren_backend",
                    ".temp_image." + contentType);

            System.out.println("Uploading image file " + file.getOriginalFilename() + " which is " + contentType
                    + " as " + filename);

            Files.write(filename, file.getBytes());

            System.out.println("Trying to identify NSFW");
            String result = NsfwPredictor.predict(filename.toString());
            System.out.println("Trying to identify NSFW: " + result);
            DBImageController controller = new DBImageController();

            if (result.startsWith("true")) {
                System.out.println("Result was true: " + result);
                controller.save(file.getBytes());
            } else {
                System.out.println("Result was not true: " + result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("OK");
    }
}
