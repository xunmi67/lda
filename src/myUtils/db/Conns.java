package myUtils.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by found on 2/3/16.
 */
public class Conns {
    public static Connection getConnections(String dbname){
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String connStr = "jdbc:mysql://localhost/" + dbname+
                     "?user=root&password=123456";
            conn = DriverManager.getConnection(connStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    public static void closeConnection(Connection conn){
        try {
            conn.close();
        }catch (Exception e){
            conn=null;
        }

    }
}
