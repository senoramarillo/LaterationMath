package latmath.util;

/**
 * Matrix library.
 *
 * @version 1.0, 2011-07-26
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class Matrix {

    private final int rows;             // number of rows
    private final int cols;             // number of columns
    private final double[][] data;      // rows-by-cols array

    // Create a square matrix initialized to the identity
    public Matrix(int dimension) {
        this.rows = dimension;
        this.cols = dimension;
        data = new double[rows][cols];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                data[i][j] = ((i == j) ? 1.0 : 0.0);
            }
        }
    }

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];
    }

    public Matrix(double[][] data) {
        rows = data.length;
        cols = data[0].length;
        this.data = getArrayCopy(data);
    }

    public Matrix(double[][] data, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = data;
    }

    private Matrix(Matrix A) {
        this(A.data);
    }

    public int cols() {
        return cols;
    }

    public int rows() {
        return rows;
    }

    public double[][] getArray() {
        return data;
    }

    public double[][] getArrayCopy() {
        return getArrayCopy(data);
    }

    private double[][] getArrayCopy(double[][] data) {
        double[][] c = new double[data.length][data[0].length];
        for (int i = 0; i < c.length; i++) {
            System.arraycopy(data[i], 0, c[i], 0, c[0].length);
        }
        return c;
    }

    /**
     * Get a submatrix.
     *
     * @param i0   Initial row index
     * @param i1   Final row index
     * @param j0   Initial column index
     * @param j1   Final column index
     *
     * @return     A(i0:i1,j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int i0, int i1, int j0, int j1) {
        Matrix x = new Matrix(i1-i0+1,j1-j0+1);
        double[][] b = x.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    b[i-i0][j-j0] = data[i][j];
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return x;
    }

    /**
     * Get a submatrix.
     *
     * @param r    Array of row indices.
     * @param j0   Initial column index.
     * @param j1   Final column index.
     *
     * @return     A(r(:),j0:j1)
     * @exception  ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int[] r, int j0, int j1) {
        Matrix x = new Matrix(r.length, j1-j0+1);
        double[][] b = x.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    b[i][j-j0] = data[r[i]][j];
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return x;
    }

    public void copy(Matrix m) {
        if (m.rows() != rows() || m.cols() != cols()) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        subcopy(m);
    }

    private void subcopy(Matrix m) {
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                setCell(i, j, m.cell(i, j));
            }
        }
    }

    // return the cell at the ith row and jth column
    public double cell(int i, int j) {
        return data[i][j];
    }

    // set the value of the cell at the ith row and jth column
    public void setCell(int i, int j, double value) {
        data[i][j] = value;
    }

    public Matrix add(Matrix b) {
        Matrix a = this;
        if (a.cols != b.cols || a.rows != b.rows) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix c = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
           for (int j = 0; j < a.cols; j++) {
              c.data[i][j] = a.data[i][j] + b.data[i][j];
           }
        }
        return c;
    }

    public Matrix transpose() {
        Matrix a = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                a.data[j][i] = this.data[i][j];
            }
        }
        return a;
    }
    
    public Matrix times(double s) {
        Matrix a = this;
        Matrix c = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
           for (int j = 0; j < a.cols; j++) {
              c.data[i][j] = s * a.data[i][j];
           }
        }
        return c;
    }

    public Matrix times(Matrix b) {
        Matrix a = this;
        if (a.cols != b.rows) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix c = new Matrix(a.rows, b.cols);
        for (int i = 0; i < c.rows; i++) {
            for (int j = 0; j < c.cols; j++) {
                for (int k = 0; k < a.cols; k++) {
                    c.data[i][j] += (a.data[i][k] * b.data[k][j]);
                }
            }
        }
        return c;
    }

    // return the inverse or pseudoinverse of this matrix
    public Matrix inverse() {
        // if matrix is 2x2, which should be in our case, use direct computation
        if (rows == cols && rows == 2) {
            double det = (data[0][0]*data[1][1] - data[0][1]*data[1][0]);
            if (det == 0) {
                // matrix is singular => no inverse
                return null;
            }
            double one_div_det = 1 / det;
            Matrix m = new Matrix(2, 2);
            m.data[0][0] = one_div_det * data[1][1];
            m.data[0][1] = -one_div_det * data[0][1];
            m.data[1][0] = -one_div_det * data[1][0];
            m.data[1][1] = one_div_det * data[0][0];
            return m;
        } else {
            return solve(new Matrix(rows));
        }
    }

    /**
     * Solve A*X = b.
     *
     * @param b Right hand side.
     *
     * @return Solution if A is square, least squares solution otherwise.
     */
    private Matrix solve(Matrix b) {
        if (rows == cols) {
            return new LUDecomposition(this).solve(b);
        } else {
            System.out.println("QRDecomposition should actually not be used!");
            return new QRDecomposition(this).solve(b);
        }
    }

    // print matrix to standard output
    public void show() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%9.4f ", data[i][j]);
            }
            System.out.println();
        }
    }

}
