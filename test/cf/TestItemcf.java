package cf;

import myUtils.io.ReadFile;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;

import static myUtils.io.ReadFile.*;

/**
 * Created by found on 1/27/16.
 */
public class TestItemcf {
    public static Logger log = LogManager.getLogger();
    public static int[][] ui = null;
    public static Itemcf model = null;
    @BeforeClass
    public static void readFile(){
        String filename = "res/cf.txt";
        try {
            ui = ReadFile.convert2Arrays(ReadFile.readSparseMatrix(filename));
        }catch (IOException ex){
            ex.printStackTrace();
            System.err.println(ex);
            ui = null;
            log.fatal("can't find file");
        }finally {
            assertNotNull(ui);
            model = new Itemcf();
            model.fit(ui[0], ui[1], 1);
        }
    }

    @Before
    public void testFit(){
        assertNotNull(ui);
        assertNotNull(model);
    }
    @Test
    public void testGetRelatedItems(){
        Set<Integer> relate0 = model.getRelateItems(0);
        HashSet<Integer> real0 = new HashSet<>(
                (List<Integer>) Arrays.asList(1, 2,3,0) );
        relate0.removeAll(real0);
        assertEquals(0,relate0.size());
    }
    @Test
    public void testSimilarity(){
        double sim = model.getSimilarity(0,1);
        ArrayRealVector v0 = new ArrayRealVector(new double[]{1,1,0});
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1,1,1});
        double realSim = v0.cosine(v1);
        assertEquals(realSim,sim,0.001);
    }
    @Test
    public void testRecords(){
        RealVector i0 = model.records.getColumnVector(0);
        double [] array_0 = i0.toArray();
        assertArrayEquals(new double[]{1,1,0},array_0,0.001);
    }

    @Test
    public void testGetSimilar(){
        log.fatal("warnning..");
        log.debug("user:{},item:{}",model.U,model.I);
        for(int i=0;i<model.I;i++){
            for( int j=0;j<model.I;j++){
                log.debug("{},{}:{}\t",i,j,model.getSimilarity(i,j));
            }
            log.debug("\n");
        }
    }
    @Test
    public void test1(){
        System.out.print("test1");
    }

    @Test
    public void testPreference(){
        int rec = model.getPreference(0,1).get(0);
        assertEquals(2,rec);
    }

}
