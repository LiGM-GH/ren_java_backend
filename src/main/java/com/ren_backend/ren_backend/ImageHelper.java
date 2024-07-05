package com.ren_backend.ren_backend;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

class ImageHelper {
    public static BufferedImage changeImageColor(BufferedImage originalImage, int newColor, int backgroundColor) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = originalImage.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xff;

                int red = (backgroundColor >> 16) & 0xff;
                int green = (backgroundColor >> 8) & 0xff;
                int blue = backgroundColor & 0xff;

                int blendedRed = (alpha * ((newColor >> 16) & 0xff) + (0xff - alpha) * red) / 0xff;
                int blendedGreen = (alpha * ((newColor >> 8) & 0xff) + (0xff - alpha) * green) / 0xff;
                int blendedBlue = (alpha * ((newColor) & 0xff) + (0xff - alpha) * blue) / 0xff;
                int blendedAlpha = (backgroundColor >> 24) == 0 ? alpha : 0xff;

                int newPixel = (blendedAlpha << 24) | (blendedRed << 16) | (blendedGreen << 8) | blendedBlue;
                newImage.setRGB(x, y, newPixel);
            }
        }
        return newImage;
    }

    public static BufferedImage scaleImage(BufferedImage image) throws IOException {
        // 757 - logo height
        // 3028 - card height
        // 4800 - card len
        BufferedImage result = new BufferedImage(4800, 3028, BufferedImage.TYPE_INT_ARGB);
        AffineTransform transform = new AffineTransform();
        transform.scale(4800.0 / image.getWidth(), 3028.0 / image.getHeight());
        AffineTransformOp oper = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        oper.filter(image, result);

        return result;
    }

    public static BufferedImage overlayImages(BufferedImage backImage, BufferedImage frontImage, int topLeftX, int  topLeftY) throws IOException {
        BufferedImage combined = new BufferedImage(backImage.getWidth(), backImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined.getGraphics();
        g.drawImage(backImage, 0, 0, null);
        g.drawImage(frontImage, topLeftX, topLeftY, null);

        return combined;
    }

    public static BufferedImage horisontalMergeImages(BufferedImage leftImage, BufferedImage rightImage) {
        BufferedImage combined = new BufferedImage(leftImage.getWidth() + rightImage.getWidth(), leftImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined.getGraphics();
        g.drawImage(leftImage, 0, 0, null);
        g.drawImage(rightImage, leftImage.getWidth(), 0, null);

        return combined;
    }
}
