package org.example.flinkstream.source.job3;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * description:
 * author: xinglu
 * date 2022/9/12 1:57 PM
 */
public class FlinkWithParallelSourceTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 数据源
        DataStreamSource<Long> longDataStreamSource = env.addSource(new ParallelSource()).setParallelism(2);
        SingleOutputStreamOperator<Long> result = longDataStreamSource.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long aLong) throws Exception {
                System.out.println("接收到的数据: " + aLong);
                return aLong % 2 == 0;
            }
        });

        result.print().setParallelism(1);
        env.execute("FlinkWithParallelSourceTest..");
    }

    /**
     * 接收到的数据: 1
     * 接收到的数据: 1
     * 接收到的数据: 2
     * 接收到的数据: 2
     * 2
     * 2
     * 接收到的数据: 3
     * 接收到的数据: 3
     * 接收到的数据: 4
     * 接收到的数据: 4
     * 4
     * 4
     */
}
