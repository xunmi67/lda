package cf;
import myUtils.myArray.ArrayUtils;
import myUtils.myArray.MatrixUtils;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.*;

/**
 * Created by found on 1/21/16.
 */
public class Itemcf {
    private int __maxRelateRange = 2;
    private ArrayList<TreeMap<Double,Integer> >similarItems;
    private ArrayList<Set<Integer> > relateItems;
    private OpenMapRealMatrix  similarities;
    protected OpenMapRealMatrix records;
    private ArrayList<ArrayList<Integer>> listRec;
    public ArrayList<Set<Integer>> historyOfUser;
    public  ArrayList<Set<Integer>> historyOfItem;
    public int U,I,K;//number of user, item,top-K
    private Logger log = LogManager.getLogger();
    public Connection conn=null;
    public String tablename=null;

    /**
     * fit item cf model to data item-user
     * @param item array of items,item[i] was consumed by user[i]
     * @param user array of users
     */
    public void fit(int[] item,int[] user,int K){
        // TODO:prepareSimilarItems for {@getSimilarItems}
        this.U = user[ArrayUtils.array_max(user)]+1;
        this.I = item[ArrayUtils.array_max(item)]+1;
        this.K = K;

        this.records = MatrixUtils.convertRecord2Matrix(item,user);
        this.listRec = covertMatrix2List(records);
        this.similarities = new OpenMapRealMatrix(I,I);
        this.similarItems = new ArrayList<>();
        calSimilarities();
    }

    /**
     * get user preferences ,i.e. the recommendations
     * @param user get user's recommendations
     * @return int[topItem] items
     */
    public ArrayList<Integer> getPreference(int user,int recListLen){
        ArrayList<Integer> recommend = new ArrayList<>();
        ArrayList<Integer> history = getRecOfUser(user);
        HashSet<Integer> relateItemsMap = new HashSet<>();
        for(Integer historyItem:history){
            relateItemsMap.addAll(getSimilarItems(historyItem,__maxRelateRange*K));
        }
        //remove all seen items
        relateItemsMap.removeAll( history );

        ArrayList<Integer> relateItems = new ArrayList<>(relateItemsMap);
        /**
         * preferences[0] are the scores it get from history items.
         */
        ArrayList<TreeMap<Double,Integer>> preferences = new ArrayList<>();
        for(Integer relateItem:relateItems){
            // TODO:try use lamda instead
            TreeMap<Double,Integer> prefence = new TreeMap<>(new Comparator<Double>() {
                @Override
                public int compare(Double o, Double t1) {
                    return 0-o.compareTo(t1);
                }
            });
            for(Integer historyItem:history){
                prefence.put(getSimilarity(historyItem,relateItem),historyItem);
            }
            preferences.add(prefence);
        }
        TreeMap<Double,Integer> sumScores = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o, Double t1) {
                return 0-o.compareTo(t1);
            }
        });

        for(int i = 0;i<preferences.size();++i){
            Integer relateItem = relateItems.get(i);
            double sum = 0.0;
            int count = 0;
            for(Map.Entry<Double,Integer> entry:preferences.get(i).entrySet()){
                sum += entry.getKey();
                if(++count >= K) break;
            }
            sumScores.put(sum,relateItem);
        }
        int count = 0;
        for(Map.Entry<Double,Integer> entry:sumScores.entrySet()){
            recommend.add(entry.getValue());
            if(++count <= recListLen) break;
        }
        if(recommend.size()< recListLen) log.warn("user {}'s reclist len is less than" +
                " required {}",user,recListLen);
        return recommend;
    }

    /**
     * get all similar items of item
     * @param item target item
     * @return list of similar items
     */
    public ArrayList<Integer> getSimilarItems(int item){
        ArrayList<Integer> itemList = new ArrayList<>();
        int i = 0;
        for(Map.Entry<Double,Integer> entry:similarItems.get(item).entrySet()){
            itemList.add(entry.getValue() );
        }
        return itemList;
    }

    /**
     * get n_similar similar items of target item
     * @param item target item
     * @param n_similar number of similar items
     * @return arraylist of items of n_similar or less than n_similar items
     */
    public ArrayList<Integer> getSimilarItems(int item,int n_similar){
        ArrayList<Integer> itemList = new ArrayList<>();
        int i = 0;
        for(Map.Entry<Double,Integer> entry:similarItems.get(item).entrySet()){
            itemList.add(entry.getValue() );
            if(++i >= n_similar) break;
        }
        return itemList;
    }

    /**
     * return related items of item
     * @param item
     * @return
     */
    protected Set<Integer> getRelateItems(int item){
        if(this.relateItems == null){
            initHistory();
            initRelateItems();
        }
        return relateItems.get(item);
    }
    private void initHistory(){
        historyOfItem = new ArrayList<>();
        historyOfUser = new ArrayList<>();
        for(int i = 0;i< I;i++){
            historyOfItem.add(new HashSet<>());
        }
        for(int j = 0;j<U;j++){
            historyOfUser.add(new HashSet<>());
        }
        if(conn == null) {
            records.walkInColumnOrder(new DefaultRealMatrixPreservingVisitor() {
                @Override
                public void visit(int row, int colomn, double value) {
                    historyOfUser.get(row).add(colomn);
                    historyOfItem.get(colomn).add(row);
                }
            });
        }else{
            String sql_getHisOfItem = "select distinct user from "+tablename+" where item=?";
            for(int i=0;i<I;i++){

            }
        }
    }
    /**
     *
     */
    private void initRelateItems(){
        relateItems = new ArrayList<>();
        for(int i=0;i<this.I;i++)
            relateItems.add(new HashSet<>());
        for(int i=0;i<I;i++){
            for(Integer relateUser:historyOfItem.get(i)){
                relateItems.get(i).addAll(
                        historyOfUser.get(relateUser)
                );
            }
        }
    }
    protected double getSimilarity(int item1,int item2){
        return similarities.getEntry(item1,item2);
    }


    private ArrayList<ArrayList<Integer>> covertMatrix2List(RealMatrix mat){
        ArrayList<ArrayList<Integer>> lists = new ArrayList<>();
        for(int i = 0;i < mat.getRowDimension();i++){
            lists.add(new ArrayList<>());
        }
        mat.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor(){
            @Override
            public void visit(int row,int column,double value){
                if(value>0) lists.get(row).add(column);
            }
        });
        return lists;

    }
    protected ArrayList<Integer> getRecOfUser(int u){
        return this.listRec.get(u);
    }

    protected double  similarity(int item1,int item2){
        RealVector v1 = records.getColumnVector(item1);
        RealVector v2 = records.getColumnVector(item2);
        if(Math.abs(v1.getMaxValue() - 0.0) < 0.01 ||
                Math.abs(v2.getMaxValue()-0.0)<0.01)
            return 0.0;
        else
            return records.getColumnVector(item1).cosine(records.getColumnVector(item2));

    }
    /**
     * 1.cal item-vs-item similarites
     * 2.complete similarItems treemap list
     *
     */
    protected void calSimilarities(){
        //complete similarities matrix
        log.debug("starting calculate similar matrix");
        for(int i=0;i<records.getColumnDimension();i++){
            Set<Integer> relateItems = getRelateItems(i);
            for(int relateItem:relateItems){
                similarities.setEntry(i,relateItem,similarity(i,relateItem) );
            }
            if(i % 1 == 0)
                log.debug("cal similar matrix of user"+i);
        }
        //compute similarItems list,use vistor
        for(int i=0;i<I;i++){
            similarItems.add(new TreeMap<Double,Integer>(new Comparator<Double>() {
                @Override
                public int compare(Double o, Double t1) {
                    return 0-o.compareTo(t1);
                }
            }));
        }

        similarities.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor(){
            @Override
            public void visit(int row,int column,double value){
                if(value > 0.0)
                    similarItems.get(row).put(value,column);
            }
        });
        log.debug("cal similarity over.");
    }



}
