package cf;

import myUtils.db.Conns;
import myUtils.db.Redis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by found on 2/18/16.
 */
public class Itemcf2 {
    public static Logger log = LogManager.getLogger();
    public static List<Integer> getPreference(Jedis jd, Connection conn,
                                              String table_key, int user,int K){
        String tablename = table_key.split(":")[1];
        Set<Integer> history = getHistory(conn,tablename,user);
        HashSet<Integer> candidate = new HashSet<>();
        for(Integer hist:history){
            try {
                candidate.addAll(getRelateItems(conn,tablename,hist));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        candidate.removeAll(history);
        //treemap record candidate-history similarity,use for first K history-candidate
        //
        HashMap<Integer,TreeMap<Double,Integer>> preferOfCand = new HashMap<>();
        for(Integer cand:candidate){
            TreeMap<Double,Integer> simWithHist = new TreeMap<>(new Comparator<Double>() {
                @Override
                public int compare(Double o, Double t1) {
                    return 0-o.compareTo(t1);
                }
            });
            for(Integer hist:history){
                simWithHist.put(getSimilarity(jd,hist,cand,table_key),hist);
            }
            preferOfCand.put(cand,simWithHist);
        }
        TreeMap<Double,Integer> rank = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o, Double t1) {
                return 0-o.compareTo(t1);
            }
        });

        for(Integer cand:candidate){
            TreeMap<Double,Integer> preferList = preferOfCand.get(cand);
            double score = 0.0;
            int i = 0;
            for(Map.Entry<Double,Integer> entry:preferList.entrySet()){
                score += entry.getKey();
                if(++i >= K ) break;
            }
            rank.put(score,cand);
        }
        List<Integer> recList = new ArrayList<>(rank.values());
        return recList;
    }

    public static Set<Integer> getHistory(Connection conn,String tablename,int user)  {
        HashSet<Integer> hist = new HashSet<>();
        String sql = "select distinct(item) from "+tablename+" where user=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1,user);
            ResultSet rs = pst.executeQuery();
            while(rs.next()){
                hist.add(rs.getInt(1));
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return  hist;
    }


    /**
     * init redis from mysql
     * @param dbname database name
     * @param tableName table name
     */
    public static void initRedis(String dbname,String tableName) throws SQLException {
        Connection conn = Conns.getConnections(dbname);
        if(conn == null) return ;
        //init waiting to update item list
        Jedis jd = Redis.getJedis("localhost");
        String table_key = dbname+":"+tableName;
        if( jd.sismember("initialed_table",table_key) ) return ;
        int I = getItemN(conn,tableName)+1;
        for(int i=0;i<I;i++){
            jd.sadd(table_key+":waitingUpdate",""+i);
        }
        //init similarities
        updateSimilarity(jd,conn,table_key);
        jd.sadd("initialed_table",table_key);
        conn.close();
        jd.close();
    }

    public static void updateSimilarity(Jedis jd,Connection conn,String table_key){
        int count =0;
        while(getNotUpdated(jd,table_key) > 0){
            int up = Integer.parseInt(jd.spop(table_key+":waitingUpdate"));
            updateSimilarityOfItem(jd,conn,table_key,up);
            if(count++ % 100 == 0) log.debug("update similarity of user:{}",count);
        }
    }

    protected static void updateSimilarityOfItem(Jedis jd,Connection conn,String table_key,int up){
        String tablename = table_key.split(":")[1];
        Set<Integer> relateItems = null;
        try {
            relateItems = getRelateItems(conn,tablename,up);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(Integer relateItem:relateItems){
            String simKey = getSimilarityKey(table_key,up,relateItem);
            double sim = calSim(conn,tablename,up,relateItem);
            jd.set(simKey,""+sim);
        }
    }
    public static double calSim(Connection conn,String tablename,int item1,int item2){
        double sim = 0.0;
        String sql_itemCount =
                "select count(distinct user) from "+tablename+" where item=?";
        String sql_innerJoin =
                "select count(*) from (select distinct user from ml.ml_50k where item=?) t1 " +
                        "inner join (select distinct user from ml.ml_50k " +
                        "where item=?) t2 on t1.user=t2.user;";
        try {
            PreparedStatement pst1 = conn.prepareStatement(sql_itemCount);
            PreparedStatement pst2 = conn.prepareStatement(sql_innerJoin);
            pst1.setInt(1,item1);
            ResultSet rs = pst1.executeQuery();
            double item1Count = 0.0001;
            double item2Count = 0.0001;
            double item1_item2Count = 0.0001;
            if(rs.next()) item1Count = rs.getInt(1);
            rs.close();
            pst1.setInt(1,item2);
            rs = pst1.executeQuery();
            if(rs.next()) item2Count = rs.getInt(1);
            rs.close();
            pst1.close();
            pst2.setInt(1,item1);
            pst2.setInt(2,item2);
            rs = pst2.executeQuery();
            if(rs.next()) item1_item2Count = rs.getInt(1);
            rs.close();
            pst2.close();
            sim = item1_item2Count / (Math.sqrt(item1Count)*Math.sqrt(item2Count) );
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return sim;
    }
    private static String getSimilarityKey(String table_key,int item1,int item2){
        int small = item1<item2?item1:item2;
        int big = item1>item2?item1:item2;
        return "similarity:"+table_key+":"+small+":"+big;
    }
    public static Set<Integer> getRelateItems(Connection conn,String tablename,int item) throws SQLException {
        HashSet<Integer> relateItems = new HashSet<>();
        String sql = "select distinct(item) from "+tablename+
                " where user in (select distinct user from "+tablename+
                " where item=?)";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1,item);
        ResultSet rs = pst.executeQuery();
        while(rs.next()){
            relateItems.add(rs.getInt(1));
        }
        return relateItems;
    }
    private static double getSimilarity(Jedis jd,String key){
        return Double.parseDouble(jd.get(key));

    }
    public static double getSimilarity(Jedis jd,int item1,int item2,String table_key){
        if(item1 == item2) return 1.0;
        String simKey = getSimilarityKey(table_key,item1,item2);
        return getSimilarity(jd,simKey);
    }
    /**
     * return not updated items list
     * @param jd
     * @param table_key
     * @return
     */
    public static long getNotUpdated(Jedis jd, String table_key){
        return jd.scard(table_key+":waitingUpdate");
    }

    /**
     * get max item number of tablename
     * @param conn sql connections
     * @param tablename tablename to query
     * @return max item number
     * @throws SQLException
     */
    public static int getItemN(Connection conn,String tablename) throws SQLException {
        String sql = "select max(item) from "+tablename;
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        int itemN = -1;
        if(rs.next()){
            itemN = rs.getInt(1);
        }
        rs.close();
        return  itemN;
    }
}
