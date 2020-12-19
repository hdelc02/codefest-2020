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
        //PImage transformed = transform(input);
        background(input);
        Point[] points = getPoints(input);
        for(int i=0; i<points.length; i++)
        {
            System.out.println(points[i]);
        }
    }

    public void draw() {

    }

//    public PImage transform(PImage image) {
//
//    }

    public Point[] getPoints(PImage image) {
        final int GRID_SIZE = 10;
        Point[] output = new Point[GRID_SIZE*GRID_SIZE];
        double ratio = (double)image.height/(double)image.width;
        int x, y;
        for(int i=0; i<output.length; i++) {
            double angle = Math.random()*2*Math.PI;
            double range = Math.sqrt(((Math.pow(Math.cos(angle), 2) + ratio*Math.pow(Math.sin(angle), 2))))*((double)image.width/(double)((GRID_SIZE-1)*2));
            double magnitude = Math.random()*range;
            x = (int)((magnitude*Math.cos(angle)) + (i%GRID_SIZE)*((double)image.width/(double)(GRID_SIZE-1)));
            y = (int)((magnitude*Math.sin(angle)) + (i/GRID_SIZE)*((double)image.height/(double)(GRID_SIZE-1)));
            if(i<GRID_SIZE) {
                y = 0;
            }
            else if(i/GRID_SIZE == GRID_SIZE-1) {
                y = image.height-1;
            }
            if(i%GRID_SIZE == 0) {
                x = 0;
            }
            else if(i%GRID_SIZE == GRID_SIZE-1) {
                x = image.width-1;
            }
            output[i] = new Point(x, y);
        }
        return output;
    }
}
