package cf;

import static cf.Itemcf2.*;
import static org.junit.Assert.*;
import myUtils.db.Conns;
import myUtils.db.Redis;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by found on 2/18/16.
 */
public class TestItemcf2 {
    public static Connection conn = null;
    public static Jedis jd = null;
    @BeforeClass
    public static void init(){
        conn = Conns.getConnections("ml");
        jd = Redis.getJedis("localhost");
    }
    @Test
    public void testItemcf2() throws SQLException {
        initRedis("ml","ml_50k");
        int I = 799;
        try {
            assertEquals(I, getItemN(conn,"ml_50k"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertEquals(0, getNotUpdated(jd,"ml:ml_50k"));
        double calsim = 0.0;
        double realsim = 97.0/(Math.sqrt(239)*Math.sqrt(144));
        assertEquals(Itemcf2.calSim(conn,"ml_50k",1,11),realsim,0.0001);
        Itemcf2.updateSimilarity(jd,conn,"ml:ml_50k");
        log.debug("sim:{}",Itemcf2.getSimilarity(jd,1,11,"ml:ml_50k") );
        log.debug("preference:{}",Itemcf2.getPreference(jd,conn,"ml:ml_50k",1,10));
    }
}
