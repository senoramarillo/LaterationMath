/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 *
 * @author adler
 */
public class GridBasedSimplify extends BasicLaterationAlgorithm implements Releasable {

    /*
     * mean and var of measurement model between xk and xk-1
     */
    private double mu_mea;
    private double std_mea;

    /*
     mu_sys=0.01 mean and var of system model as the bound is already tight, no need to adaptive the system variance
     */
    private double mu_sys;
    private double std_sys;

    /*
     5th percentile of the ranging error (negative) 1.28; 10th 0.88 (negative)
     to avoid the area too small  0.3
     */
    private double side_minus;
    private double side_robust;

    private double[] pre_bb;
    private Point2d pre_pos;
    private int gridsize;
    
    public GridBasedSimplify() {

        mu_mea = 2.0;
        std_mea = 3.0;
        mu_sys = 0.02;
        std_sys = 1.0;

        side_minus = 1.28;
        side_robust = 0.3;

        pre_bb = new double[4];

        gridsize = 10;
    }

    @Override
    public String getName() {
        return "GridBasedSimplify";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void reset() {
        super.reset(); //To change body of generated methods, choose Tools | Templates.
        pre_bb = new double[4];
        pre_pos = null;
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges, Point2d actualPosition, ErrorModel errorModel, int width, int height) {

        double[] bb = new double[4];
        Point2d pos = new Point2d(0,0);
        // c&p from MinMax Class

        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }
        
        if (ranges.length <= 2) {
            return pre_pos;
        }

        double north = anchors[0].y - ranges[0],
                west = anchors[0].x - ranges[0],
                east = anchors[0].x + ranges[0],
                south = anchors[0].y + ranges[0];

        for (int i = 1; i < anchors.length; ++i) {
            north = Math.max(north, anchors[i].y - ranges[i]);
            west = Math.max(west, anchors[i].x - ranges[i]);
            east = Math.min(east, anchors[i].x + ranges[i]);
            south = Math.min(south, anchors[i].y + ranges[i]);
        }

        if (west < east) {
            bb[0] = west;
            bb[1] = east;
        } else {
            bb[0] = east;
            bb[1] = west;
        }

        if (north < south) {
            bb[2] = north;
            bb[3] = south;
        } else {
            bb[2] = south;
            bb[3] = north;
        }

        if (null == pre_bb) {
            pre_bb = bb;
        }

        if (null == pre_pos) {
            GeolaterationN geon = new GeolaterationN();
            pre_pos = geon.localize(anchors, ranges, actualPosition, errorModel, width, height);
            assert pre_pos!=null;
        }

        // if something goes wrong - look here
        // Bt= (Rt+minus_range) n Bt-1 n entrie area
        bb[0] = Math.max(bb[0] - side_minus, pre_bb[0]);
        bb[1] = Math.min(bb[1] + side_minus, pre_bb[1]);
        bb[2] = Math.max(bb[2] - side_minus, pre_bb[2]);
        bb[3] = Math.min(bb[3] + side_minus, pre_bb[3]);

        // Bt= Bt u Robust 
        bb[0] = Math.min(bb[0], pre_pos.getX() - side_robust);
        bb[1] = Math.max(bb[1], pre_pos.getX() + side_robust);
        bb[2] = Math.min(bb[2], pre_pos.getY() - side_robust);
        bb[3] = Math.max(bb[3], pre_pos.getY() + side_robust);

        // maybe additional improvement if we know size of the map here (see
        // yuan's original algorithm
        pre_bb = bb;

        double area_width = bb[1] - bb[0];
        double area_heigth = bb[3] - bb[2];

        Point2d grid[][] = new Point2d[gridsize][gridsize];
        double distance_grid[][] = new double[gridsize][gridsize];
        double weight_grid[][] = new double[gridsize][gridsize];
        double prop_grid[][] = new double[gridsize][gridsize];
        double cell_size_x = area_width / gridsize;
        double cell_size_y = area_heigth / gridsize;

        double weight_sum = 0;
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                grid[i][j] = new Point2d();
                grid[i][j].y = bb[2] - cell_size_y / 2 + cell_size_y * (i + 1);
                grid[i][j].x = bb[0] - cell_size_x / 2 + cell_size_x * (j + 1);

                distance_grid[i][j] = Math.sqrt(Math.pow(grid[i][j].x - pre_pos.getX(), 2) + Math.pow(grid[i][j].y - pre_pos.getY(), 2));
                prop_grid[i][j] = 1 / (Math.sqrt(2 * Math.PI) * std_sys) * Math.exp(-1 * (Math.pow(distance_grid[i][j] - mu_sys, 2) / (2 * std_sys * std_sys)));

                double likelyhood = 1.0;

                for (int k = 0; k < anchors.length; k++) {
                    double anchor_dist = Math.sqrt(Math.pow(grid[i][j].x - anchors[k].getX(), 2) + Math.pow(grid[i][j].y - anchors[k].getY(), 2));
                    likelyhood *= 1 / (Math.sqrt(2 * Math.PI) * std_mea) * (Math.exp(-1 * (Math.pow(ranges[k] - anchor_dist - mu_mea, 2) / (2 * std_mea * std_mea))));
                }

                weight_grid[i][j] = likelyhood * prop_grid[i][j];
                weight_sum += weight_grid[i][j];
            }
        }

        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                weight_grid[i][j] /= weight_sum;
                pos.x += weight_grid[i][j] * grid[i][j].x;
                pos.y += weight_grid[i][j] * grid[i][j].y;
            }
        }
        pre_pos = pos;
        return pos;
    }

}
