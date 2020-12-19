package stainedGlass;

import processing.core.PApplet;
import processing.core.PImage;

public class Engine extends PApplet {

    PImage input;

    public static void main(String[] args) {
        PApplet.main("stainedGlass.Engine");
    }

    public void settings() {
        input = loadImage("swag cat.jpg");
        size(input.width, input.height);
    }

    public void setup() {
        background(input);
    }

    public void draw() {

    }
}
