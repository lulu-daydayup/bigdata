package org.example.flinkstream.source.job2;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * description:
 * author: xinglu
 * date 2022/9/12 1:42 PM
 */
public class FlinkWithNotParallelSourceTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Long> longDataStreamSource = env.addSource(new NotParallelSource()).setParallelism(1);

        SingleOutputStreamOperator<Long> result = longDataStreamSource.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long aLong) throws Exception {
                System.out.println("接收到的数据: " + aLong);
                return aLong % 2 == 0;
            }
        });

        result.print().setParallelism(1);
        env.execute("FlinkWithNotParallelSourceTest..");

    }
}
