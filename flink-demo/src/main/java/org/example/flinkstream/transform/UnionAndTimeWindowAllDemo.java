package org.example.flinkstream.transform;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.example.flinkstream.source.job2.NotParallelSource;

/**
 * description: union
 * author: xinglu
 * date 2022/9/12 5:14 PM
 */
public class UnionAndTimeWindowAllDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //老版需要设置时间语义:https://blog.csdn.net/LangLang1111111/article/details/121343530
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        //数据源
        DataStreamSource<Long> data1 = env.addSource(new NotParallelSource()).setParallelism(1);
        DataStreamSource<Long> data2 = env.addSource(new NotParallelSource()).setParallelism(1);

        // 把data1和data2合并到一起，数据类型必须一致
        DataStream<Long> dataAll = data1.union(data2);
        SingleOutputStreamOperator<Long> result = dataAll.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long aLong) throws Exception {
                System.out.println("接受到的数据： " + aLong);
                return aLong;
            }
        });


        // 每2秒处理一次数据
        SingleOutputStreamOperator<Long> sum = result.timeWindowAll(Time.seconds(2)).sum(0);
        // 打印处理
        sum.print().setParallelism(1);

        env.execute("UnionAndTimeWindowAllDemo");
    }
}
