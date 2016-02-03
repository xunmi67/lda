package myUtils.io;

import myUtils.myArray.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by found on 1/26/16.
 */
public class ReadFile {
    /**
     * read scipy sparse file, convert into int[item],and int [user]
     * @param filename file to read
     * @return int[0] is item list,int[1] is user list,means that item[i]
     * is read by user[i]
     * indice,indptr,data three lines consist of a file
     */
    private static Logger log = LogManager.getLogger();
    public static int[][] readSparseMatrix(String filename) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = null;
        line = br.readLine();
        //line is indices
        String[] ind_str = line.trim().split("\\s+");
        int ind_len = ind_str.length;

        line = br.readLine();
        String[] ptr_str = line.trim().split("\\s+");
        int ptr_len = ptr_str.length;

        line = br.readLine();
        String[]  data_str = line.trim().split("\\s+");
        int data_len = data_str.length;
        assert ind_len == data_len;
        int[] indices = new int[ind_len];
        int[] indptr = new int[ptr_len];
        int[] data = new int[data_len];
        for(int i=0;i<ind_len;i++){
            indices[i] = Integer.parseInt(ind_str[i]);
            data[i] = Integer.parseInt(data_str[i]);
        }
        for(int i=0;i<indptr.length;i++)
            indptr[i] = Integer.parseInt(ptr_str[i]);
        int m_ind = ArrayUtils.array_max(indices);
        int n_sample = ptr_len -1 ;
        int n_word = indices[m_ind] + 1;
        int[][] result = new int[n_sample][n_word];
        for(int i = 0;i<n_sample;i ++){
            for(int j=indptr[i];j<indptr[i+1];j++){
                result[i][indices[j]] = data[j];
            }
        }
        return result;
    }
    /**
     * convert X into int[2][],int[0] is wArray,int[1] is dArray[]
     * @param X X[d][i] represent word i in document d
     * @return int[0] is wArray,int[1] is dArray[]
     */
    public static int[][] convert2Arrays(int[][] X){
        ArrayList<Integer> wArrayList = new ArrayList<>();
        ArrayList<Integer> dArrayList = new ArrayList<>();
        for(int d = 0;d<X.length;d++)
            for(int w=0;w<X[0].length;w++)
                for(int count=0;count<X[d][w];count++)
                {
                    wArrayList.add(w);
                    dArrayList.add(d);
                }
        int[][] result = new int[2][];
        result[0] = new int[wArrayList.size()];
        result[1] = new int[dArrayList.size()];
        for(int i=0;i<wArrayList.size();i++)
            result[0][i] = wArrayList.get(i);
        for(int i=0;i<dArrayList.size();i++)
            result[1][i] = dArrayList.get(i);
        return result;
    }
}
