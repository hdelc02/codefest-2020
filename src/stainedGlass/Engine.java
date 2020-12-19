package stainedGlass;

import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    }

    public void draw() {

    }

    public PImage transform(PImage image) {

    }

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

    public ArrayList<Point> contourPoints(PImage image) {
        ArrayList<Point> basePoints = new ArrayList<Point>();
        Collections.addAll(basePoints, getPoints(image));
        ArrayList<Point> points = reducePoints(image, basePoints, 3);
        return points;
    }

    private ArrayList<Point> reducePoints(PImage image, ArrayList<Point> points, int iteration) {

    }

    public Point[] nearPoints(Point origin, ArrayList<Point> points, int numPoints) {
        Point[] output = new Point[numPoints];
        float[] dists = new float[numPoints];
        ArrayList<Point> pointsCopy = new ArrayList<Point>(points);
        pointsCopy.remove(origin);
        int maxDist = 0;
        for(int i=0; i<pointsCopy.size(); i++) {
            if(i<numPoints) {
                output[i] = pointsCopy.get(i);
                dists[i] = dist(pointsCopy.get(i).x, pointsCopy.get(i).y, origin.x, origin.y);
                if(dists[maxDist]<=dists[i])
                    dists[maxDist] = dists[i];
            }
            else {
                float thisDist = dist(pointsCopy.get(i).x, pointsCopy.get(i).y, origin.x, origin.y)
                if(thisDist < dists[maxDist]) {
                    dists[maxDist] = thisDist;
                    output[maxDist] = pointsCopy.get(i);
                    for(int j=0; j<numPoints; j++) {
                        if(dists[j]>maxDist)
                            maxDist = j;
                    }
                }
            }
        }
        return output;
    }
}
