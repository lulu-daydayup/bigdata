package com.bw.flink.consumer;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

/**
 * Description:
 *
 * @date 2022/10/15 5:17 PM
 **/
public class FlinkKafkaConsumer implements SourceFunction<String> {
    private String topic;
    private Properties properties;
    private SimpleStringSchema simpleStringSchema;

    public FlinkKafkaConsumer(String topic, SimpleStringSchema simpleStringSchema, Properties properties) {
        this.topic = topic;
        this.properties = properties;
        this.simpleStringSchema = simpleStringSchema;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {

    }

    @Override
    public void cancel() {

    }
}
