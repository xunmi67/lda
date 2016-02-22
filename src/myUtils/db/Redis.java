package myUtils.db;

import redis.clients.jedis.Jedis;

/**
 * Created by found on 2/18/16.
 */
public class Redis {
    static public Jedis getJedis(String ip){
        return new Jedis(ip);
    }
}
