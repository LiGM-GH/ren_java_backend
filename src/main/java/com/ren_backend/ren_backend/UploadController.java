package com.ren_backend.ren_backend;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.SQLException;

import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class UploadController {
    public static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";
    DBImageController db;

    public UploadController() throws IllegalArgumentException, SQLException {
        db = new DBImageController();
    }

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

    @PostMapping("/register-design")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile,
            @RequestParam("json") String requestJsonString)
            throws IOException {
        System.out.println("CropData: " + requestJsonString);
        ObjectMapper mapper = new ObjectMapper();
        RequestJson requestJson = mapper.readValue(requestJsonString, RequestJson.class);
        System.out.println("RequestJson: " + requestJson.toString());

        String contentType = imageFile.getContentType();

        if (contentType == null || !contentType.contains("image/")) {
            return ResponseEntity.badRequest().build();
        }
        contentType = contentType.replace("image/", "");

        System.out.println("Uploaded file has the following content type: " + contentType);

        try {
            Path filename = Files.createTempFile(Path.of(UPLOAD_DIRECTORY), "ren_backend",
                    ".temp_image." + contentType);

            System.out.println("Uploading image file " + imageFile.getOriginalFilename() + " which is " + contentType
                    + " as " + filename);

            BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());

            bufferedImage = bufferedImage.getSubimage(requestJson.getCropData().getX(),
                    requestJson.getCropData().getY(), requestJson.getCropData().getWidth(),
                    requestJson.getCropData().getHeight());

            ImageIO.write(bufferedImage, contentType, filename.toFile());

            System.out.println("Trying to find out if the image isSafeForWork");
            String result = NsfwPredictor.predict(filename.toString());
            System.out.println("Trying to find out if the image isSafeForWork: " + result);

            Boolean isSafeForWork = Boolean.valueOf(result.stripTrailing());
            if (isSafeForWork) {
                System.out.println("Result was true: " + result);
                db.save(imageFile.getBytes());
            } else {
                System.out.println("Result was not true: " + result);
                return ResponseEntity.status(400).build();
            }
        } catch (Exception e) {
            System.out.println("Exception happened! ");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("OK");
    }
}
