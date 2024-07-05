package com.ren_backend.ren_backend;

class RequestJson {
    MirColors mirColors;
    CropData cropData;
    LogoColors logoColors;
    boolean logoMinimization;
    String logoSide;

    public MirColors getMirColors() {
        return mirColors;
    }

    public void setMirColors(MirColors mirColors) {
        this.mirColors = mirColors;
    }

    public CropData getCropData() {
        return cropData;
    }

    public void setCropData(CropData cropData) {
        this.cropData = cropData;
    }

    public LogoColors getLogoColors() {
        return logoColors;
    }

    public void setLogoColors(LogoColors logoColors) {
        this.logoColors = logoColors;
    }

    public boolean getLogoMinimization() {
        return logoMinimization;
    }

    public void setLogoMinimization(boolean logoMinimization) {
        this.logoMinimization = logoMinimization;
    }

    public String getLogoSide() {
        return logoSide;
    }

    public void setLogoSide(String logoSide) {
        this.logoSide = logoSide;
    }
}

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

class LogoColors {
    String bg;
    String letter;
    String text;

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getText() {
        return text == "none" ? null : text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

class MirColors {
    String bg;
    String main;

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }
}
