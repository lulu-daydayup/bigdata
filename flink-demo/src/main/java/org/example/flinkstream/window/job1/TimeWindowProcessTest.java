package org.example.flinkstream.window.job1;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * description: 需求：每隔5秒，计算最近10s内单词出现的次数
 * author: xinglu
 * date 2022/9/21 10:56 AM
 */
public class TimeWindowProcessTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //老版需要设置时间语义:https://blog.csdn.net/LangLang1111111/article/details/121343530
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        DataStreamSource<String> dataStreamSource = env.socketTextStream("localhost", 9999);
        dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public void flatMap(String s, Collector<Tuple2<String, Long>> collector) throws Exception {
                        String[] split = s.split(" ");
                        for (String word : split) {
                            collector.collect(Tuple2.of(word, 1L));
                        }

                    }
                }).keyBy(0)
                .timeWindow(Time.seconds(10), Time.seconds(5))
                .sum(1)
                .print().setParallelism(1);

        env.execute("TimeWindowTest...");
    }
}
