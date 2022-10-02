package org.example.flinkstream.window.job4;

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
import org.example.flinkstream.window.job3.WindowWordCountSortTest;

import java.util.concurrent.TimeUnit;

/**
 * description:自定义source模拟：第 13 秒的时候连续发送2个事件，第一个事件在第13秒的时候发送出去了，
 * 第二个事件在13的时候产生因为网络延迟等原因，在19秒的时候才发送出去，第三个事件16 秒的时候发送了出去。
 * <p>
 * 按照的是事件达到Flink的时间处理的，不是事件的发生的时间。
 * <p>
 * author: xinglu
 * date 2022/9/24 11:34 AM
 */
public class WindowBySort2 {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        DataStreamSource<String> dataStreamSource = env.addSource(new WindowWordCountSortTest.TestSource());
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
                .process(new WindowWordCountSortTest.SumProcessFunction()).print().setParallelism(1);
        env.execute("WindowBySort2");

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

            // 13s
            TimeUnit.SECONDS.sleep(3);
            sourceContext.collect("flink");
            sourceContext.collect("flink");

            // 16s
            TimeUnit.SECONDS.sleep(3);
            sourceContext.collect("flink");

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
