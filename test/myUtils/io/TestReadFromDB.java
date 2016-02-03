package myUtils.io;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by found on 2/2/16.
 */
public class TestReadFromDB {
    public static Connection conn = null;
    @BeforeClass
    public static void makeConn(){
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String connStr = "jdbc:mysql://localhost/ml?" +
                     "user=root&password=123456";
            conn = DriverManager.getConnection(connStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Before
    public void testConn(){
        assertNotNull(conn);
    }

    @Test
    public void testReadFromDB(){
        try {
            int[][] record = ReadFromDatabase.readFromDb(conn,"test");
            int[] items = record[0];
            int[] users = record[1];
            assertEquals(items[0],377);
            assertEquals(items[9],302);
            assertEquals(users[0],22);
            assertEquals(users[9],186);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
