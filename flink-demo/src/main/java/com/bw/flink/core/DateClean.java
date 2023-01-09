package com.bw.flink.core;

import com.bw.flink.consumer.FlinkKafkaConsumer;
import com.bw.flink.source.CustomerRedisSource;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.core.fs.CloseableRegistry;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.runtime.execution.Environment;
import org.apache.flink.runtime.query.TaskKvStateRegistry;
import org.apache.flink.runtime.state.*;
import org.apache.flink.runtime.state.ttl.TtlTimeProvider;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Description: 读取Redis数据和Kafka的数据
 *
 * @date 2022/10/15 4:48 PM
 **/
public class DateClean {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 从Kafka读取数据，所以你得看读取得topic有几个分区，那么就设置几个并行度
        env.setParallelism(4);

        // 开启checkPoint
        env.enableCheckpointing(600000);
        // 仅一次语义
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(10000);

        // 保留一个checkPoint结果就可以了
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setCheckpointTimeout(60000);

        // 程序退出checkPoint数据要保存
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 设置state保存策略
        env.setStateBackend(new RocksDBStateBackend("hdfs://hadoop104/flink/checkpoint"));


        // 读取Kafka里面的数据
        String topic = "flink-01";
        Properties properties = new Properties();
        properties.setProperty("bootstrap.server", "hadoop4:9002");
        properties.setProperty("group.id", "flinkETL_consumer");
        properties.setProperty("enable.auto.commit", "false");// flink自己管理
        // 第一次读取的时候，从头开始
        properties.setProperty("auto.offset.reset", "earliest");

        FlinkKafkaConsumer kafkaConsumer = new FlinkKafkaConsumer(topic, new SimpleStringSchema(), properties);
        DataStreamSource<String> dataStreamSource = env.addSource(kafkaConsumer);
        DataStreamSource<Map<String, String>> redisSource = env.addSource(new CustomerRedisSource());


    }

    private static class RocksDBStateBackend implements StateBackend {
        private String checkPointAddress;

        public RocksDBStateBackend(String checkPointAddress) {
            this.checkPointAddress = checkPointAddress;
        }

        @Override
        public <K> CheckpointableKeyedStateBackend<K> createKeyedStateBackend(Environment environment, JobID jobID, String s, TypeSerializer<K> typeSerializer, int i, KeyGroupRange keyGroupRange, TaskKvStateRegistry taskKvStateRegistry, TtlTimeProvider ttlTimeProvider, MetricGroup metricGroup, @Nonnull Collection<KeyedStateHandle> collection, CloseableRegistry closeableRegistry) throws Exception {
            return null;
        }

        @Override
        public OperatorStateBackend createOperatorStateBackend(Environment environment, String s, @Nonnull Collection<OperatorStateHandle> collection, CloseableRegistry closeableRegistry) throws Exception {
            return null;
        }
    }
}
