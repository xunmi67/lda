package matrix;
import org.apache.commons.math3.linear.*;

/**
 * Created by found on 1/17/16.
 */
public class TestMatrix {
    public static void test1(){
        double [][] d =new double[][]{{0.1,0.2,0.3,},{0.3,0.4,0.5}};
        double[] dd = {1,2,3};
        // Array2DRowRealMatrix mat = new Array2DRowRealMatrix(d);
        DiagonalMatrix dmat = new DiagonalMatrix(dd);
        System.out.println(dmat);
    }
    public static void main(String argss[]){
        test1();
    }
}
