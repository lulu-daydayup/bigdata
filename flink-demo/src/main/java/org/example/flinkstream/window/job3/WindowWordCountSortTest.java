package org.example.flinkstream.window.job3;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.concurrent.TimeUnit;

/**
 * description: 自定义source模拟：第13秒的时候连续发送2个事件，第16秒的时候再发送1个事件
 * Process Time Window 有序
 * author: xinglu
 * date 2022/9/24 11:14 AM
 */
public class WindowWordCountSortTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        DataStreamSource<String> dataStreamSource = env.addSource(new TestSource());
        dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String line, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        String[] split = line.split(",");
                        for (String word : split) {
                            collector.collect(Tuple2.of(word, 1));
                        }
                    }
                }).keyBy(0)
                .timeWindow(Time.seconds(10), Time.seconds(5))
                .process(new SumProcessFunction()).print().setParallelism(1);
        env.execute("WindowWordCountSortTest");

    }

    public static class TestSource implements SourceFunction<String> {

        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void run(SourceContext<String> sourceContext) throws Exception {
            String currentTime = String.valueOf(System.currentTimeMillis());
            System.out.println(currentTime);

            // 这个操作是我为了保证10秒的背数。1000ms等于1s.
            while (Integer.parseInt(currentTime.substring(currentTime.length() - 4)) > 10) {
                currentTime = String.valueOf(System.currentTimeMillis());
                continue;
            }

            System.out.println("开始发送事件的事件： " + dateFormat.format(System.currentTimeMillis()));


            TimeUnit.SECONDS.sleep(3);

            // 实际上我们的数据是在13秒的时候生成的，只是19的时候被发送出去。
            String event = "flink";
            // 13s发送第一个事件
            sourceContext.collect(event);

            // 16s发送第二个事件
            TimeUnit.SECONDS.sleep(3);
            sourceContext.collect(event);

            // 19s发送第二个事件
            TimeUnit.SECONDS.sleep(3);
            sourceContext.collect(event);

            // 程序等待
            TimeUnit.SECONDS.sleep(50000);
        }

        @Override
        public void cancel() {

        }
    }

    /**
     * IN
     * OUT
     * KEY -> is Tuple
     * W extends Window
     */
    public static class SumProcessFunction extends ProcessWindowFunction<Tuple2<String, Integer>,
            Tuple2<String, Integer>, Tuple, TimeWindow> {
        @Override
        public void process(Tuple key, ProcessWindowFunction<Tuple2<String, Integer>,
                Tuple2<String, Integer>, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Integer>> allElements,
                            Collector<Tuple2<String, Integer>> collector) throws Exception {
            int count = 0;
            for (Tuple2<String, Integer> allElement : allElements) {
                count++;
            }
            collector.collect(Tuple2.of(key.getField(0), count));
        }
    }
}
