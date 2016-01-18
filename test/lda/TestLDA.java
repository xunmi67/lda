package lda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by found on 1/18/16.
 */
public class TestLDA {
    public static void testFit() throws IOException {
        int [][] x = readX("tfs.txt");
        int K = 20;
        lda.LDA model = new LDA(10000,50.0/K,0.01,K);
        model.fit(x);
        double[][] phi = model.phi;
        for(double[] topic:phi){
            Arrays.sort(topic);
        }

    }
    public static int[][] readX(String filename) throws IOException {
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
        int m_ind = LDA.array_max(indices);
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

    public static String[] readW(String filename) throws IOException {
        ArrayList<String> wrods_str = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = null;
        while((line = br.readLine()) != null){
            wrods_str.add(line.trim().split("\\s+")[1]);
        }
        String [] words = new String[wrods_str.size()];
        return wrods_str.toArray(words);
    }

    private static HashMap<Integer,String> words(){
        return  null;

    }

    public static void main(String[] a) throws IOException {
        testFit();
    }
}
