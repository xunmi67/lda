package myUtils.io;

import myUtils.db.Conns;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by found on 2/18/16.
 */
public class TestConn {
    @Test
    public void testConn(){
        Connection conn = Conns.getConnections("ml");
        try {
            System.out.print(conn.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
