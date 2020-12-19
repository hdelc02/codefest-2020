package stainedGlass;

import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;

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
        PImage transformed = transform(input);
        background(input);
    }

    public void draw() {

    }

    public PImage transform(PImage image) {

    }

    public Point[] getPoints(PImage image) {
        final int GRID_SIZE = 10;
        Point[] output = new Point[GRID_SIZE*GRID_SIZE];
        double ratio = image.height/image.width;
        for(int i=0; i<output.length; i++) {
            double angle = Math.random()*2*Math.PI;
            double range = (Math.pow(Math.cos(angle), 2) + ratio*Math.pow(Math.sin(angle), 2))*(image.width/(GRID_SIZE*2));
            double magnitude = Math.random()*range;
            output[i] = new Point((int)(magnitude*Math.cos(angle)), (int)(magnitude*Math.sin(angle)));
        }
        return output;
    }
}
