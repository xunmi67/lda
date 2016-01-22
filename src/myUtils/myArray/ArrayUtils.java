package myUtils.myArray;

/**
 * Created by found on 1/22/16.
 */
public class ArrayUtils {
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
            double[] iarray = (double[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else if(array instanceof float[]){
            int maxi = 0;
            float[] iarray = (float[]) array;
            for(int i=0;i<iarray.length;i++){
                maxi = iarray[i]>iarray[maxi]?i:maxi;
            }
            return maxi;
        }else{
            return -1;
        }
    }
}
