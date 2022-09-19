package org.example.flinkstream.statebackend;

import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackendFactory;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * description: 状态容错, 设置备份checkpoint方式
 * author: xinglu
 * date 2022/9/18 5:24 PM
 */
public class StateBackendTest {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 默认情况下就是MemoryStateBackend
        MemoryStateBackend memoryStateBackend = new MemoryStateBackend();
        env.setStateBackend(memoryStateBackend);

        // 指定FsStateBackend(推荐)
        FsStateBackend fsStateBackend = new FsStateBackend("hdfs://hadoop1:8020/flink/checkpoint");
        env.setStateBackend(fsStateBackend);

        // 指定RocksDB
//        RocksDBStateBackend rocksDBStateBackend = new RocksDBStateBackend("hdfs://hadoop1:8020/flink/checkpoint", true);
//        env.setStateBackend(rocksDBStateBackend);

    }
}
