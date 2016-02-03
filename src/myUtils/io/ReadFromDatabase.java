package myUtils.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by found on 1/28/16.
 */
public class ReadFromDatabase {
    private static Logger log = LogManager.getLogger();
    /**
     * read from database into int[] user,int[] items,
     * database def:(user int,item int,rating float,time long);
     * @param conn connection
     * @param tablename table name
     * @return records of item and user,int[0] is items,int[1] is users
     */
    public static int[][] readFromDb(Connection conn,String tablename) throws SQLException {
        String sql = "select user,item from "+tablename+" order by time";
        Statement st = conn.createStatement();
        ArrayList<Integer> users = new ArrayList<>();
        ArrayList<Integer> items = new ArrayList<>();
        ResultSet rs = null;
        try{
            rs = st.executeQuery(sql);
            while(rs.next()){
                users.add(rs.getInt(1));
                items.add(rs.getInt(2));
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            rs.close();
        }
        int [][] result = new int[2][];
        int[] itemsArray = new int[items.size()];
        int[] usersArray = new int[users.size()];
        for(int i=0;i<users.size();i++){
            itemsArray[i] = items.get(i);
            usersArray[i] = users.get(i);
        }
        result[0] = itemsArray;
        result[1] = usersArray;
        return result;
    }

}
