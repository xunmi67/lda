package exp;

import cf.Itemcf;
import myUtils.db.Conns;
import myUtils.io.ReadFromDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by found on 2/3/16.
 */
public class Recommand {
    public static Logger log = LogManager.getLogger();
    /**
     * get hits of rec list and history
     * @param map1
     * @param map2
     * @return double of hit
     */
    public static double getHitNum(Map<Integer,ArrayList<Integer>> map1,
                         Map<Integer,ArrayList<Integer>> map2){
        double count = 0.0;
        for(Integer key:map1.keySet()){
            if(map2.containsKey(key)){
                ArrayList<Integer> list1 = (ArrayList<Integer>) map1.get(key).clone();
                ArrayList<Integer> list2 = (ArrayList<Integer>) map2.get(key).clone();
                list1.retainAll(list2);
                count += list1.size();
            }
        }
        return count;
    }

    public static HashMap<Integer,ArrayList<Integer>> convertList2Map(int[] items,int[] users ){
        HashMap<Integer,ArrayList<Integer>> map = new HashMap<>();
        for(int i = 0;i<users.length;i++){
            if(map.containsKey(i)) {
                ArrayList<Integer> list = map.get(i);
                if (!list.contains(items[i])) {
                    list.add(items[i]);
                }
            }else{
                ArrayList<Integer> list = new ArrayList<>();
                list.add(items[i]);
            }
        }
        return map;
    }
    public static void exp1(int l) throws SQLException {
        HashMap<Integer,ArrayList<Integer>> history = new HashMap<>();
        HashMap<Integer,ArrayList<Integer>> recList = new HashMap<>();
        Connection conn = Conns.getConnections("ml");
        int[][] recs = ReadFromDatabase.readFromDb(conn,"ml_100k");
        int train_len = (int) (recs[0].length / 10.0 * 9.0);
        int[] train_items = Arrays.copyOfRange(recs[0],0,train_len);
        int[] train_users = Arrays.copyOfRange(recs[1],0,train_len);
        int[] test_items = Arrays.copyOfRange(recs[0],train_len,recs[0].length);
        int[] test_users = Arrays.copyOfRange(recs[1],train_len,recs[1].length);
        Itemcf model = new Itemcf();
        model.fit(train_items,train_users,20);
        int U = model.U;
        for(int i = 0;i<U;i++){
           recList.put(i, model.getPreference(i,l) );
        }
        recList = convertList2Map(test_items,test_users);
        double hits = getHitNum(history,recList);
        log.debug("hits {},recall:{},precision:{}",hits,
                hits/history.size(),hits/recList.size());

    }

    public static void main(String[] a) throws SQLException {
        exp1(10);
    }
}
