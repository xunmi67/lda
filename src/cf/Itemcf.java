package cf;
import myUtils.myArray.ArrayUtils;
import myUtils.myArray.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TreeMap;

/**
 * Created by found on 1/21/16.
 */
public class Itemcf {
    private TreeMap<Double,Integer> similarItems;
    private OpenMapRealMatrix  similarities;
    private OpenMapRealMatrix records;
    private int[] itemsOfRec;
    private int[] usersOfRec;
    private int U,I;//number of user, item
    private Logger log = LogManager.getLogger();

    /**
     * fit item cf model to data item-user
     * @param item array of items,item[i] was consumed by user[i]
     * @param user array of users
     */
    public void fit(int[] item,int[] user){
        // TODO:prepareSimilarItems for {@getSimilarItems}
        this.U = user[ArrayUtils.array_max(user)];
        this.I = item[ArrayUtils.array_max(item)];
        this.records = MatrixUtils.convertRecord2Matrix(this.itemsOfRec,this.usersOfRec);
        this.similarities = new OpenMapRealMatrix(U,I);
        
        calSimilarities();
    }

    /**
     * get user preferences ,i.e. the recommendations
     * @param user get user's recommendations
     * @param topItem return int[topItem] items.
     * @return int[topItem] items
     */
    public int[] getPreference(int user,int topItem){
        return null;
    }
    public int[] getSimilarItems(int item){
        return null;
    }

    /**
     * cal item-vs-item similarites,use cosin similarity
     */
    protected void calSimilarities(){
        for(int i=0;i<records.getRowDimension();i++){
            int[] relateItems = getSimilarItems(i);
            for(int relateItem:relateItems){
                similarities.setEntry(i,relateItem,
                        records.getRowVector(i).cosine(records.getRowVector(relateItem)));
            }
        }
    }


}
