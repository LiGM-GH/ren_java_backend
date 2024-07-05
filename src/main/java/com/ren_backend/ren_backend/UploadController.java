package com.ren_backend.ren_backend;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

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

    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @CrossOrigin(value = "*", maxAge = 1800)
    @PostMapping("/register-design")
    public ResponseEntity<String> uploadImage(@RequestPart("image") String imageFile,
            @RequestParam("json") String requestJsonString)
            throws IOException {
        System.out.println("CropData: " + requestJsonString);
        System.out.println("imageFile: " + imageFile);
        ObjectMapper mapper = new ObjectMapper();
        RequestJson requestJson = mapper.readValue(requestJsonString, RequestJson.class);
        System.out.println("RequestJson: " + requestJson.toString());

        imageFile = imageFile.replace("data:image/png;base64", "");
        System.out.println("Base64 conversion start");
        byte[] image = Base64.getMimeDecoder().decode(imageFile);
        System.out.println("Base64 conversion end");

        System.out.println("Image read");
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));
        System.out.println("Image write");
        String contentType = "image/png";

        if (contentType == null || !contentType.contains("image/")) {
            return ResponseEntity.badRequest().build();
        }
        contentType = contentType.replace("image/", "");

        System.out.println("Uploaded file has the following content type: " + contentType);

        try {
            Path filename = Files.createTempFile(Path.of(UPLOAD_DIRECTORY), "ren_backend",
                    ".temp_image." + contentType);

            // System.out.println("Uploading image file " + imageFile.getOriginalFilename()
            // + " which is " + contentType
            // + " as " + filename);

            // BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());

            // crop
            bufferedImage = bufferedImage.getSubimage(requestJson.getCropData().getX(),
                    requestJson.getCropData().getY(), requestJson.getCropData().getWidth(),
                    requestJson.getCropData().getHeight());

            bufferedImage = ImageHelper.scaleImage(bufferedImage);

            System.out.println("ImageIO write start");
            ImageIO.write(bufferedImage, contentType, filename.toFile());
            System.out.println("ImageIO write end");

            Thread thr = new Thread(new MyRunnable(bufferedImage, requestJson, contentType, filename));
            System.out.println("Starting a thread");
            thr.start();
            System.out.println("Stopped starting a thread");

            System.out.println("Trying to find out if the image isSafeForWork");
            String result = NsfwPredictor.predict(filename.toString());
            System.out.println("Trying to find out if the image isSafeForWork: " + result);

            Boolean isSafeForWork = Boolean.valueOf(result.stripTrailing());
            if (isSafeForWork) {
                System.out.println("Result was true: " + result);
                db.save(imageFile.getBytes());
                System.out.println("imageFile written to DB");

                return ResponseEntity.status(200).build();
            } else {
                System.out.println("Result was not true: " + result);
                return ResponseEntity.status(400).build();
            }
        } catch (Exception e) {
            System.out.println("Exception happened! ");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

class MyRunnable implements Runnable {
    BufferedImage img;
    RequestJson requestJson;
    String contentType;
    Path filename;

    public MyRunnable(BufferedImage bufferedImage, RequestJson requestJson, String contentType, Path filename) {
        this.img = bufferedImage;
        this.requestJson = requestJson;
        this.contentType = contentType;
        this.filename = filename;
    }

    public void run() {
        try {
            LogoBuilder builder = new LogoBuilder();
            BufferedImage logo;
            if (requestJson.getLogoMinimization()) {
                logo = builder.withImage(img)
                        .withLogoIcon(Color.decode(requestJson.getLogoColors().getLetter()))
                        .withBackgroundLetter(Color.decode(requestJson.getLogoColors().getBg())).build();
            } else {
                String text = requestJson.getLogoColors().getText();
                String bg = requestJson.getLogoColors().getBg();
                bg = bg.contains("n") ? null : bg;
                System.out.println("BG = " + bg);

                logo = builder.withImage(img)
                        .withLogoIcon(Color.decode(requestJson.getLogoColors().getLetter()))
                        .withLogoText(text == "none" ? new Color(0) : Color.decode(text))
                        .withBackgroundLetter(bg == null ? null : Color.decode(bg))
                        .build();
            }

            BufferedImage mirLogo;
            String bg = requestJson.getMirColors().getBg();
            String mirColor = requestJson.getMirColors().getMain();
            if (mirColor.contains("u")) {
                mirLogo = ImageIO.read(new File("assets/mir-logo.png"));

                if (!bg.contains("n")) {
                    BufferedImage bgColoredImage = new BufferedImage(mirLogo.getWidth(), mirLogo.getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = bgColoredImage.getGraphics();
                    graphics.setColor(Color.decode(bg));
                    graphics.fillRect(0, 0, mirLogo.getWidth(), mirLogo.getHeight());
                    mirLogo = ImageHelper.overlayImages(bgColoredImage, mirLogo, 0, 0);
                }
            } else {
                mirLogo = ImageIO.read(new File("assets/mir-logo-mono.png"));
                mirLogo = ImageHelper.changeImageColor(mirLogo, Color.decode(mirColor).getRGB(),
                        Color.decode(bg).getRGB());
            }

            if (requestJson.getLogoSide().contains("l")) {
                img = ImageHelper.overlayImages(img, logo, 0, 0);
            } else {
                img = ImageHelper.overlayImages(img, logo,
                        img.getWidth() - logo.getWidth(), 0);
            }

            img = ImageHelper.overlayImages(img, mirLogo,
                    img.getWidth() - mirLogo.getWidth(),
                    img.getHeight() - mirLogo.getHeight());

            ImageIO.write(img, contentType, filename.toFile());
        } catch (Exception e) {
        }
    }
}
