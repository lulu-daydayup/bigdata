package org.example.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * description: 基于flink实现词频统计
 * author: xinglu
 * date 2022/9/11 12:21 PM
 */
public class WC5 {
    public static void main(String[] args) throws Exception {
        // 1、创建程序入口，无论是本地，还是集群模式入口都是一样的
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2、指定数据源：socketTestStream
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);

        // 3、处理了数据
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            /**
             * @param s         数据源输入的数据
             * @param collector 将数据写到下游
             * @throws Exception
             */
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] split = s.split(" ");
                for (String s1 : split) {
                    collector.collect(new Tuple2<>(s1, 1));
                }
            }
        }).keyBy(0).sum(1);

        // 4、输出数据
        result.print();

        // 5、程序执行
        env.execute("word count job start ...");

    }
}


