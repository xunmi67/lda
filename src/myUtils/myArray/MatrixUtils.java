package myUtils.myArray;

import org.apache.commons.math3.linear.OpenMapRealMatrix;

/**
 * Created by found on 1/21/16.
 */
public class MatrixUtils {
    /**
     * convert record to apache sparse matrix
     * @param items items of record
     * @param users items[i] was history of users[i]
     * @return an equivalent matrix of history
     */
    public static OpenMapRealMatrix convertRecord2Matrix(int[] items,int[] users){
        // TODO:to complete
        int row = users[ArrayUtils.array_max(users)]+1;
        int column = items[ArrayUtils.array_max(items)]+1;
        OpenMapRealMatrix op = new OpenMapRealMatrix(row,column);
        for(int i=0;i< items.length;i++){
            op.setEntry(users[i],items[i],1);
        }
        return op;
    }
}
