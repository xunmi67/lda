package lda;

import java.util.ArrayList;

/**
 * Created by found on 1/15/16.
 */
public class LDA {
    int[] wArray = null;
    int[] dArray = null;
    int[] zArray = null;
    int[][] k_v = null;
    int[][] m_k = null;
    int[][] n_k_w = null;
    int[][] n_d_k = null;
    int[] sumK = null;
    int nIter = 0;
    double alfa = 0;
    double beta = 0;
    int V,K,D;

    public LDA(int nIter,double alfa,double beta,int K){
        this.nIter = nIter;
        this.alfa = alfa;
        this.beta = beta;
        this.K = K;
    }

    /**
     * fit model to wArray and dArray
     * @param wArray all words of all doc wArray[i] is word of doc darray[i]
     * @param dArray
     */
    public void fit(int[] wArray,int[] dArray){
        V = wArray[array_max(wArray)]+1;
        D = dArray[array_max(dArray)]+1;

    }

    public double[][] getParam(String paramName){
        // TODO:waiting
        return null;
    }

    public static int array_max(Object array){
        if(array instanceof int[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else if(array instanceof double[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else if(array instanceof float[]){
            int maxi = 0;
            int[] iarray = (int[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else{
            return -1;
        }
    }
}
