package org.example.flinkstream.statebackend;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackendFactory;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.concurrent.TimeUnit;

/**
 * description: 状态容错, 设置备份checkpoint方式
 * author: xinglu
 * date 2022/9/18 5:24 PM
 */
public class StateBackendTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 默认情况下就是MemoryStateBackend
//        MemoryStateBackend memoryStateBackend = new MemoryStateBackend();
//        env.setStateBackend(memoryStateBackend);

        // 指定FsStateBackend(推荐)
        FsStateBackend fsStateBackend = new FsStateBackend("hdfs://hadoop1:8020/flink/checkpoint");
        env.setStateBackend(fsStateBackend);

        // 指定RocksDB
//        RocksDBStateBackend rocksDBStateBackend = new RocksDBStateBackend("hdfs://hadoop1:8020/flink/checkpoint", true);
//        env.setStateBackend(rocksDBStateBackend);

        // 开启checkpoint机制，每隔10s执行一次，每隔10s保存一次state
        env.enableCheckpointing(10000);

        // 设置仅一次语义
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        // 确保检查点之间至少有500ms的间隔【checkpoint最小间隔】, 因为数据量大的话有积压
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        // 检查点必须在一分钟内完成，活着被丢弃【checkpoint的超时时间】
        env.getCheckpointConfig().setCheckpointTimeout(60000);

        // 同一时间只允许进行一个检查点
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        // 表示一旦Flink处理程序被cancel后，会保留Checkpoint数据，以便根据实际需要恢复到制定的checkpoint
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 如果数据量比较大，建议5分钟左右checkpoint一次，阿里他们使用的时候，也是这样建议的。

        // 重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // 尝试重启次数
                Time.of(10, TimeUnit.SECONDS) // 间隔
                ));

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        String hostname = parameterTool.get("hostname");
        int port = parameterTool.getInt("port");
        DataStreamSource<String> dataStreamSource = env.socketTextStream(hostname, port);
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] split = s.split(",");
                for (String s1 : split) {
                    collector.collect(new Tuple2<>(s1, 1));
                }
            }
        }).keyBy(0).sum(1);
        result.print();
        env.execute("StateBackendTest ...");
    }
}
