package matrix;
import org.apache.commons.math3.linear.*;

/**
 * Created by found on 1/17/16.
 */
public class TestMatrix {
    public static void test1(){
        double [][] d ={{0.1,0.2},{0.3,0.4}};
        Array2DRowRealMatrix mat = new Array2DRowRealMatrix(d,false);
        RealVector vec = mat.getRowVector(0);
        vec.setEntry(0,9.0);
        System.out.println(d.toString());
        System.out.println(mat);
        System.out.println(vec);
    }
    public static void main(String argss[]){
        int a = 9;
        a /= 4-1;
        System.out.println(a);
    }
}
