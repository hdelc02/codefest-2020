package stainedGlass;

import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;
import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Engine extends PApplet {

    PImage input;
    final int GRID_SIZE = 20;

    public static void main(String[] args) {
        PApplet.main("stainedGlass.Engine");
    }

    Boolean fileLoaded = false;
    public void settings() {
        try {
            fileSelector();
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("Progress");
        size(input.width, input.height);
    }

    public void fileSelector() throws InterruptedException {
        synchronized(fileLoaded) {
            selectInput("Select an image:", "fileSelection");
            fileLoaded.wait();
        }
    }

    public void fileSelection(File selected) {
        synchronized(fileLoaded) {
            input = loadImage(selected.getAbsolutePath());
            fileLoaded.notify();
            System.out.println("image loaded");
            fileLoaded = true;
        }
    }

    ArrayList<Point> points;
    ArrayList<Vector2D> vectors;
    DelaunayTriangulator triangulator;
    ArrayList<Triangle2D> triangles;

    public void setup() {
        background(IMAGE);
        loadPixels();
        noLoop();

        points = contourPoints(IMAGE);
        vectors = convertPointListToVector2DList(points);
        triangulator = new DelaunayTriangulator(vectors);
        try {
            triangulator.triangulate();
        } catch (NotEnoughPointsException e) {
            e.printStackTrace();
        }

        System.out.println(triangulator.getTriangles().size());
        triangles = (ArrayList<Triangle2D>) triangulator.getTriangles();

        delay(1000);
    }

    public void draw() {
        strokeWeight(0);

        for(int i=0; i<triangles.size(); i++) {
            drawTriangle(triangles.get(i));
        }

        System.out.println(triangles.size());
    }

    public ArrayList<Vector2D> convertPointListToVector2DList(ArrayList<Point> points){
        ArrayList<Vector2D> vectors = new ArrayList<Vector2D>();
        for(int i=0; i<points.size(); i++) {
            vectors.add(new Vector2D(points.get(i).x, points.get(i).y));
        }
        return vectors;
    }


    public void drawTriangle(Triangle2D t) {
        setTriangleFill(t);
        triangle((float)t.a.x, (float)t.a.y, (float)t.b.x, (float)t.b.y, (float)t.c.x, (float)t.c.y);
    }


    public void setTriangleFill(Triangle2D t) {
        int a =	IMAGE.get((int)t.a.x, (int)t.a.y);
        int b = IMAGE.get((int)t.b.x, (int)t.b.y);
        int c = IMAGE.get((int)t.c.x, (int)t.c.y);

        float avgRed = (red(a) + red(b) + red(c))/3;
        float avgGreen = (green(a) + green(b) + green(c))/3;
        float avgBlue = (blue(a) + blue(b) + blue(c))/3;

        fill(avgRed, avgGreen, avgBlue);
    }

    public Point[] getPoints(PImage image) {
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
        ArrayList<Point> edgePoints = new ArrayList<Point>();
        for(int i=0; i<basePoints.size(); i++) {
            if(i<=GRID_SIZE || i>=basePoints.size()-GRID_SIZE || i%GRID_SIZE == 0 || i%GRID_SIZE == GRID_SIZE-1)
                edgePoints.add(basePoints.get(i));
        }
        ArrayList<Point> points = new ArrayList<Point>(basePoints);
        points = reducePoints(image, points);
        points.addAll(edgePoints);
        return points;
    }

    private ArrayList<Point> reducePoints(PImage image, ArrayList<Point> points) {
        ArrayList<Point> output = new ArrayList<Point>();
        for(int i=0; i<points.size(); i++) {
            Point[] near = nearPoints(points.get(i), points, 2);
            for (Point point : near) {
                if(isDifferent(image, point, points.get(i))) {
                    Point poi = deltaPoint(image, point, points.get(i));
                    output.add(poi);
                }
            }
        }
        return output;
    }

    public Point deltaPoint(PImage image, Point p1, Point p2) {
        if(!isDifferent(image, p1, p2) || dist(p1.x, p1.y, p2.x, p2.y) < 2) {
            return p1;
        }
        Point mid = midpoint(p1, p2);
        boolean diff1 = isDifferent(image, mid, p1);
        boolean diff2 = isDifferent(image, mid, p2);
        if(diff1 == diff2)
            return mid;
        if(diff1)
            return deltaPoint(image, mid, p1);
        else
            return deltaPoint(image, mid, p2);
    }

    public Point midpoint(Point p1, Point p2) {
        return new Point(p1.x + (p2.x - p1.x)/2, p1.y + (p2.y - p1.y)/2);
    }

    public Point central(PImage image, Point p1, Point p2) {
        Point center = midpoint(new Point(0,0), new Point(image.width, image.height));
        if(dist(p1.x, p1.y, center.x, center.y) < dist(p2.x, p2.y, center.x, center.y))
            return p1;
        return p2;
    }

    public boolean isDifferent(PImage image, Point p1, Point p2) {
        final int SUM_THRESHOLD = 150;
        image.loadPixels();
        int c1 = image.pixels[((p1.y*image.width)+p1.x)];
        int c2 = image.pixels[((p2.y*image.width)+p2.x)];
        int sumDifference = Math.abs((c1>>16)&255-(c2>>16)&255)
                + Math.abs((c1>>8)&255-(c2>>8)&255)
                + Math.abs(c1&255-c2&255);
        return sumDifference > SUM_THRESHOLD;
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
                float thisDist = dist(pointsCopy.get(i).x, pointsCopy.get(i).y, origin.x, origin.y);
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