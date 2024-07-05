package com.ren_backend.ren_backend;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class LogoBuilder {
    BufferedImage image = null;
    Color logoIcon = null;
    Color logoText = null;
    Color textBackground = null;

    public LogoBuilder() {}

    public LogoBuilder withImage(BufferedImage image) {
        this.image = image;
        return this;
    }

    public LogoBuilder withLogoIcon(Color logoIcon) {
        this.logoIcon = logoIcon;
        return this;
    }

    public LogoBuilder withBackgroundLetter(Color backgroundLetter) {
        this.textBackground = backgroundLetter;
        return this;
    }

    public LogoBuilder withLogoText(Color logoText) {
        this.logoText = logoText;
        return this;
    }

    public BufferedImage build() throws IOException {
        if (image == null || logoIcon == null) {
            throw new IllegalArgumentException("Image and logoIcon must be present");
        }

        int textBackgroundInt;
        if (textBackground == null) {
            textBackgroundInt = 0;
        } else {
            textBackgroundInt = textBackground.getRGB();
        }

        BufferedImage logoIconImage;
        if (logoText == null) {
            logoIconImage = ImageIO.read(new File("assets/bank-minilogo.png"));
            logoIconImage = ImageHelper.changeImageColor(logoIconImage, logoIcon.getRGB(), textBackgroundInt);
        } else {
            logoIconImage = ImageIO.read(new File("assets/bank-letter.png"));
            logoIconImage = ImageHelper.changeImageColor(logoIconImage, logoIcon.getRGB(), textBackgroundInt);
            BufferedImage logoTextImage = ImageIO.read(new File("assets/bank-name.png"));
            logoTextImage = ImageHelper.changeImageColor(logoTextImage, logoText.getRGB(), textBackgroundInt);
            logoIconImage = ImageHelper.horisontalMergeImages(logoIconImage, logoTextImage);
        }

        return logoIconImage;
    }
}
