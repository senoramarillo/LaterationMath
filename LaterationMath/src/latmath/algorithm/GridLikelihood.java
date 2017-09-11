package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * bounding box with one iteration
 *
 * @version 1.0, 2014-07-6
 * @author yuan
 * @since LatMath 1.0
 */
public class GridLikelihood extends BasicLaterationAlgorithm implements Releasable {

    private static int gridsize;

    public GridLikelihood() {
        gridsize = 6;
    }

    @Override
    public String getName() {
        return "GridLikelihood";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges);
    }

    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not the
     * additional features of the <code>BasicLaterationAlgorithm</code> class.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     *
     * @return The estimated position of the mobile node to be located or
     * <code>null</code> if no position could be calculated, e.g. localization
     * failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges) {
        // step 0: sanity check
        double[] bb = new double[4];

        Point2d pos = new Point2d(0, 0);
        Point2d pos_2 = new Point2d(0, 0);

        if (anchors.length != ranges.length) {
            return null;
        }

        double north = anchors[0].y - ranges[0],
                west = anchors[0].x - ranges[0],
                east = anchors[0].x + ranges[0],
                south = anchors[0].y + ranges[0];

        for (int i = 1; i < anchors.length; i++) {
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

        //bb[1 2 3 4] = [l,r,b,t]
        double area_width = bb[1] - bb[0];
        double area_heigth = bb[3] - bb[2];

        Point2d grid[][] = new Point2d[gridsize][gridsize];
        double dist_anc_grid_sum[][] = new double[gridsize][gridsize];
        double weight_grid[][] = new double[gridsize][gridsize];
        double weight_sum = 0;
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                grid[i][j] = new Point2d();
                for (int k = 0; k < anchors.length; k++) {
                    grid[i][j].x = bb[0] + i * area_width / (gridsize - 1);
                    grid[i][j].y = bb[2] + j * area_heigth / (gridsize - 1);
                    dist_anc_grid_sum[i][j] += Math.abs(grid[i][j].distance(anchors[k]) - ranges[k]);
                }
                weight_grid[i][j] = 1.0 / dist_anc_grid_sum[i][j];
                weight_sum += weight_grid[i][j];
            }
        }

        double weight_mean = weight_sum / gridsize / gridsize;
        double weight_sum_2 = 0;
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                weight_grid[i][j] = Math.max(0, weight_grid[i][j] - weight_mean);
                weight_sum_2 += weight_grid[i][j];
            }
        }

        // initial
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                weight_grid[i][j] /= weight_sum_2;
                pos.x += weight_grid[i][j] * grid[i][j].x;
                pos.y += weight_grid[i][j] * grid[i][j].y;
            }
        }

        // adaptive  
        double anchor2pos_dist = 0;
        for (int k = 0; k < anchors.length; k++) {
            anchor2pos_dist += Math.abs(pos.distance(anchors[k]) - ranges[k]);
        }
        //    mean(abs(diff_r2dist)) -3 
        double mean_diff_r2dist = anchor2pos_dist / anchors.length - 3;
        area_width = Math.max(1, mean_diff_r2dist) * 2 + 1;
        area_heigth = Math.max(1, mean_diff_r2dist) + 1;

        // added lines
        bb[0] = pos.x - area_width / 2;
        bb[2] = pos.y - area_heigth / 2;
        
        // iteration
        weight_sum = 0;
        double dist_anc_grid_sum2[][] = new double[gridsize][gridsize];
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                for (int k = 0; k < anchors.length; k++) {
                    grid[i][j].x = bb[0] + i * area_width / (gridsize - 1);
                    grid[i][j].y = bb[2] + j * area_heigth / (gridsize - 1);
                    dist_anc_grid_sum2[i][j] += Math.abs(grid[i][j].distance(anchors[k]) - ranges[k]);
                }
                weight_grid[i][j] = 1.0 / dist_anc_grid_sum2[i][j];
                weight_sum += weight_grid[i][j];
            }
        }

        weight_mean = weight_sum / gridsize / gridsize;
        weight_sum_2 = 0;
        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                weight_grid[i][j] = Math.max(0, weight_grid[i][j] - weight_mean);
                weight_sum_2 += weight_grid[i][j];
            }
        }

        for (int i = 0; i < gridsize; i++) {
            for (int j = 0; j < gridsize; j++) {
                weight_grid[i][j] /= weight_sum_2;
                pos_2.x += weight_grid[i][j] * grid[i][j].x;
                pos_2.y += weight_grid[i][j] * grid[i][j].y;
            }
        }

        return pos_2;
    }

}
