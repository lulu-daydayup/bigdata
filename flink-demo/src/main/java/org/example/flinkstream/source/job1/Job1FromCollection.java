package org.example.flinkstream.source.job1;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * author: xinglu
 * date 2022/9/12 1:46 PM
 */
public class Job1FromCollection {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 数据源
        List<String> data = new ArrayList<>();
        data.add("flink1");
        data.add("flink2");
        data.add("flink3");

        DataStreamSource<String> stringDataStreamSource = env.fromCollection(data);
        SingleOutputStreamOperator<String> result = stringDataStreamSource.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                return s + "map operator";
            }
        });
        result.print();
        env.execute("Job1FromCollection");


    }
}
