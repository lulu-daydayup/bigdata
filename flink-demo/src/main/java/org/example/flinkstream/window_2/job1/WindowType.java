package org.example.flinkstream.window_2.job1;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * Description:
 *
 * @date 2022/10/6 9:02 PM
 **/
public class WindowType {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);
        SingleOutputStreamOperator<Tuple2<String, Integer>> streamResult = dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] fields = s.split(",");
                for (String word : fields) {
                    collector.collect(Tuple2.of(word, 1));
                }
            }
        });
        // Non keyed Stream
        AllWindowedStream<Tuple2<String, Integer>, TimeWindow> nonKeyedStream = streamResult.timeWindowAll(Time.seconds(3));
        nonKeyedStream.sum(1).print();

        // Keyed Stream
        streamResult.keyBy(0)
                .timeWindow(Time.seconds(3))
                .sum(1)
                .print();
        env.execute("WindowType");
    }
}
