package org.example.flinkstream.window.job5;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Description: 使用Event Time处理无序
 *
 * 打印结果：1，3，1。但是有数据的丢失。
 *
 * @date 2022/9/30 11:54 AM
 **/
public class WindowWordCountAndTime {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 步骤一：设置时间类型，默认的是ProcessTime
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStreamSource<String> dataStreamSource = env.addSource(new TestSource());
        dataStreamSource.map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String s) throws Exception {
                        String[] fields = s.split(",");
                        // 作用：指定时间字段
                        return new Tuple2<>(fields[0], Long.valueOf(fields[1]));
                    }
                }).assignTimestampsAndWatermarks(new EventTimeExtractor())
                .keyBy(0)
                .timeWindow(Time.seconds(10), Time.seconds(5))
                .process(new SumProcessFunction()).print().setParallelism(1);

        env.execute("WindowWordCountAndTime");
    }

    public static class TestSource implements SourceFunction<String> {

        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            String currentTime = String.valueOf(System.currentTimeMillis());
            while (Integer.valueOf(currentTime.substring(currentTime.length() - 4)) > 10) {
                currentTime = String.valueOf(System.currentTimeMillis());
                continue;
            }
            System.out.println("开始发送事件的时间： " + dateFormat.format(System.currentTimeMillis()));
            TimeUnit.SECONDS.sleep(3);

            // 13s 第一个事件
            String event = "flink," + System.currentTimeMillis();// 带时间戳的事件
            ctx.collect(event);

            // 16s 第2个事件
            TimeUnit.SECONDS.sleep(3);
            ctx.collect("flink," + System.currentTimeMillis());

            // 19s 第3个事件
            TimeUnit.SECONDS.sleep(3);
            ctx.collect(event);
        }

        @Override
        public void cancel() {

        }
    }

    private static class EventTimeExtractor implements AssignerWithPeriodicWatermarks<Tuple2<String, Long>> {
        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(System.currentTimeMillis());
        }

        // 指定时间，按照那个数据产生的时间进行处理
        @Override
        public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
            return element.f1;
        }
    }

    public static class SumProcessFunction extends ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Integer>, Tuple, TimeWindow> {
        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void process(Tuple key, ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Integer>, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Long>> elements, Collector<Tuple2<String, Integer>> out) throws Exception {
            int count = 0;
            for (Tuple2<String, Long> element : elements) {
                count++;
            }
            out.collect(Tuple2.of(key.getField(0), count));
        }
    }


}
