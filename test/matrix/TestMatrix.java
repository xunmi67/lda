package matrix;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.special.Gamma;

import java.util.Arrays;

/**
 * Created by found on 1/17/16.
 */
public class TestMatrix {
    public static void test1(){
        int[][] wd = new int[2][];
        wd[0]= new int[] {1,2};
        wd[1] = new int[] {3,4};
    }
    public static void main(String argss[]){
        double ga = Gamma.gamma(3);
        double log_ga = Gamma.logGamma(3);
        System.out.print("ga:"+ga+"\tlog:"+log_ga);

    }
}
