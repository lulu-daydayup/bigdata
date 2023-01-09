package com.bw.flink.source;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @date 2022/10/15 5:27 PM
 **/
public class Jedis {
    private String hostname;
    private Integer port;

    public Jedis(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public Map<String, String> hgetAll(String key) throws JedisException{
        return new HashMap<>();
    }

    public void close() {

    }
}
