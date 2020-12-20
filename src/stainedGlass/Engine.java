package stainedGlass;

import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;
import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Engine extends PApplet {

    PImage input;
    final int GRID_SIZE = 10;

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
        noLoop();
    }

    public void draw() {
        noStroke();
        fill(color(255,0,0));
        Point[] points = getPoints(input);
        for(Point point : points) {
            circle(point.x, point.y, 3);
        }
        fill(color(0,255,0));
        ArrayList<Point> contourPoints = contourPoints(input);
//        for(Point point : contourPoints) {
//            circle(point.x, point.y, 3);
//        }
        ArrayList<Point[]> triangles = triangulation(contourPoints);
        stroke(0);
        noFill();
        for(Point[] triangle : triangles) {
            triangle(triangle[0].x, triangle[0].y, triangle[1].x, triangle[1].y, triangle[2].x, triangle[2].y);
        }
        noStroke();
        fill(0,255,0);
        for(Point point : contourPoints) {
            circle(point.x, point.y, 3);
        }
        System.out.println("Done.");
    }

//    public PImage transform(PImage image) {
//
//    }

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
        points = reducePoints(image, points, 10);
        points.addAll(edgePoints);
        return points;
    }

    private ArrayList<Point> reducePoints(PImage image, ArrayList<Point> points, int iteration) {
//        if(iteration<=0)
//            return points;
//        ArrayList<Point> midpoints = new ArrayList<Point>();
//        for(int i=0; i<points.size(); i++) {
//            Point[] near = nearPoints(points.get(i), points, 4);
//            for (Point point : near) {
//                if (isDifferent(image, points.get(i), point)) {
//                    Point mid = midpoint(points.get(i), point);
//                    if (!midpoints.contains(mid))
//                        midpoints.add(mid);
//                } else {
//                    Point central = central(image, points.get(i), point);
//                    if (!midpoints.contains(central))
//                        midpoints.add(central);
//                }
//            }
//        }
//        return reducePoints(image, midpoints, iteration-1);


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
        final int SUM_THRESHOLD = 250;
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

    public ArrayList<Point[]> triangulation(ArrayList<Point> points){
        ArrayList<Point[]> triangles = new ArrayList<Point[]>();
        for(int i = 0; i < points.size(); i++) {
            for(int j = i+1; j < points.size(); j ++) {
                for(int k = j+1; k < points.size(); k ++) {
                    boolean successfulTriangle = true;
                    Point[] currentTriangle = {points.get(i), points.get(j), points.get(k)};
                    for(int l = 0; l < points.size(); l ++) {
                        if(i == l || j == l || k == l)
                            continue;
                        if(triangleContainsPoint(points.get(l), currentTriangle)) {
                            successfulTriangle = false;
                            break;
                        }
                    }
                    if(successfulTriangle) {
                        triangles.add(currentTriangle);
                    }
                }
            }
        }

        return triangles;
    }

    public boolean triangleContainsPoint(Point p, Point[] vertices) {

        //Area of original triangle
        float areaOrig = triangleArea(vertices[0], vertices[1], vertices[2]);

        //Area of three new triangles
        float areaNew1 = triangleArea(p, vertices[0], vertices[1]);
        float areaNew2 = triangleArea(p, vertices[0], vertices[2]);
        float areaNew3 = triangleArea(p, vertices[1], vertices[2]);
        float sumNewAreas = areaNew1 + areaNew2 + areaNew3;

        if(abs(areaOrig-sumNewAreas) < 0.01)
            return true;

        return false;
    }

    public float triangleArea(Point p1, Point p2, Point p3) {
        float a, b, c, s;
        a = dist(p1.x, p1.y, p2.x, p2.y);
        b = dist(p1.x, p1.y, p3.x, p3.y);
        c = dist(p2.x, p2.y, p3.x, p3.y);
        s = (a+b+c)/2;
        return sqrt(s * (s-a) * (s-b) * (s-c));
    }
}
