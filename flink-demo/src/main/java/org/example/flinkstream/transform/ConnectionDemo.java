package org.example.flinkstream.transform;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.example.flinkstream.source.job2.NotParallelSource;

/**
 * Description: connect
 *
 * @author xinglu
 * @date 2022/9/13 10:36 PM
 **/
public class ConnectionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Long> data1 = env.addSource(new NotParallelSource()).setParallelism(1);
        DataStreamSource<Long> data2 = env.addSource(new NotParallelSource()).setParallelism(1);

        SingleOutputStreamOperator<String> data_str = data2.map(new MapFunction<Long, String>() {
            @Override
            public String map(Long aLong) throws Exception {
                return "str_" + aLong;
            }
        });

        // connect: 可以两边数据不一致
        ConnectedStreams<Long, String> connect = data1.connect(data_str);

        /**
         * Long:数据源1的类型
         * String: 数据源2的类型
         * Object：输出的类型
         */
        SingleOutputStreamOperator<Object> result = connect.map(new CoMapFunction<Long, String, Object>() {
            // 这个方法处理的是数据源1
            @Override
            public Object map1(Long value) throws Exception {
                return value;
            }

            // 这个方法处理的是数据源2
            @Override
            public Object map2(String value) throws Exception {
                return value;
            }
        });

        result.print().setParallelism(1);
        String jobName = ConnectionDemo.class.getSimpleName();
        env.execute(jobName);
    }
}
