package org.example.flinkstream.window_2.job2;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * Description: Time Window和 Count Window的区别
 *
 * @date 2022/10/6 9:08 PM
 **/
public class WordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> dataStreamSource = env.socketTextStream("localhost", 9999);
        SingleOutputStreamOperator<Tuple2<String, Integer>> stream = dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] fields = s.split(",");
                for (String word : fields) {
                    collector.collect(Tuple2.of(word, 1));
                }
            }
        });
        /**
         * 滚动窗口和滑动窗口的区别：参数是1个的是滚动，参数是2个的是滑动。
         * timeWindow(Time.seconds(3)) == window(TumblingProcessingTimeWindows.of(Time.seconds(2)))
         * timeWindow(Time.second(6), Time.second(4))
         */
        stream.keyBy(0)
                // 每隔10个元素，统计最近100个元素的情况
//                .countWindow(100, 10)
                // 每100个元素统计一次
//                .countWindow(100)
//                .timeWindow(Time.seconds(5))
//                .timeWindow(Time.seconds(3), Time.seconds(5))
                .sum(1)
                .print();

        // 滚动窗口
        stream.keyBy(0)
                // .timeWindow(Time.seconds(2))
                .window(TumblingProcessingTimeWindows.of(Time.seconds(2)))
                .sum(1).print();

        // 滑动窗口
        stream.keyBy(0)
//                .window(SlidingProcessingTimeWindows.of(Time.seconds(6), Time.seconds(4)))
                .timeWindow(Time.seconds(10), Time.seconds(5))
                .sum(1)
                .print();
        env.execute("Word Count");
    }
}
