package com.ren_backend.ren_backend;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

class CropData {
    int x;
    int y;
    int width;
    int height;
    String unit;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "CropData {\n\tx: " + this.x + ",\n\ty: " + this.y + ",\n\twidth: " + this.width + ",\n\theight: "
                + this.height + ",\n\tunit: " + this.unit + "\n}";
    }
}

@RestController
public class UploadController {

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";
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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile imageFile,
            @RequestParam("CropData") String cropDataString)
            throws IOException {
        System.out.println("CropData: " + cropDataString);
        ObjectMapper mapper = new ObjectMapper();
        CropData cropData = mapper.readValue(cropDataString, CropData.class);
        System.out.println("CropData: " + cropData.toString());

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

            bufferedImage = bufferedImage.getSubimage(cropData.getX(), cropData.getY(), cropData.getWidth(),
                    cropData.getHeight());

            ImageIO.write(bufferedImage, contentType, filename.toFile());

            System.out.println("Trying to identify NSFW");
            String result = NsfwPredictor.predict(filename.toString());
            System.out.println("Trying to identify NSFW: " + result);

            if (result.startsWith("true")) {
                System.out.println("Result was true: " + result);
                db.save(imageFile.getBytes());
            } else {
                System.out.println("Result was not true: " + result);
            }
        } catch (Exception e) {
            System.out.println("Exception happened! ");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("OK");
    }
}
