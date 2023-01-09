package com.bw.flink.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: 自定义读取Redis数据
 *
 * @date 2022/10/15 4:46 PM
 **/
public class CustomerRedisSource implements SourceFunction<Map<String, String>> {

    private Logger logger = LoggerFactory.getLogger(CustomerRedisSource.class);
    private Jedis jedis;
    private boolean isRunning = true;

    @Override
    public void run(SourceContext<Map<String, String>> ctx) throws Exception {
        this.jedis = new Jedis("hadoop104", 6379);
        Map<String, String> map = new HashMap<>();
        while (isRunning) {
            try {
                map.clear();
                Map<String, String> areas = jedis.hgetAll("area");
                for (Map.Entry<String, String> entry : areas.entrySet()) {
                    String area = entry.getKey();
                    String value = entry.getValue();
                    String[] fields = value.split(",");
                    for (String country : fields) {
                        // TW, AREA_CHINA
                        // HK, AREA_CHINA
                        map.put(country, area);
                    }
                }
                if (map.size() > 0) {
                    ctx.collect(map);
                }
                Thread.sleep(60000);
            } catch (JedisException e) {
                logger.error("redis连接异常");
            } catch (Exception e) {
                logger.error("");
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        if (jedis != null) {
            jedis.close();
        }
    }
}
