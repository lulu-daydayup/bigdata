package org.example.flinkstream.window.job6;

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
import org.example.flinkstream.window.job5.WindowWordCountAndTime;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Description: 使用WaterMark机制解决无序, 结果：解决了数据丢失的情况。
 * 开始发送事件的时间： 11:21:50
 * (flink,2)
 * (flink,3)
 * (flink,1)
 *
 * @date 2022/10/3 11:18 AM
 **/
public class WindowWordCountAndTime2 {
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

        env.execute("WindowWordCountAndTime2");
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

    /**
     * 使用WaterMark需要实现AssignerWithPeriodicWatermarks接口
     * 重写方法。
     */
    private static class EventTimeExtractor implements AssignerWithPeriodicWatermarks<Tuple2<String, Long>> {
        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        // 设置5s的延迟(乱序)
        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
//            System.out.println("water mark:......" + System.currentTimeMillis());
            return new Watermark(System.currentTimeMillis() - 5000);
        }

        // 指定时间，按照那个数据产生的时间进行处理
        @Override
        public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
            System.out.println("event: " + dateFormat.format(element.f1) + ", currentWaterMark: " + dateFormat.format(getCurrentWatermark().getTimestamp()));
            return element.f1;
        }
    }

    public static class SumProcessFunction extends ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Integer>, Tuple, TimeWindow> {
        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void process(Tuple key, ProcessWindowFunction<Tuple2<String, Long>, Tuple2<String, Integer>, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Long>> elements, Collector<Tuple2<String, Integer>> out) throws Exception {
            System.out.println("处理时间： " + dateFormat.format(context.currentProcessingTime()));
            System.out.println("Window Start Time: " + dateFormat.format(context.window().getStart()));
            int count = 0;
            for (Tuple2<String, Long> element : elements) {
                count++;
            }
            out.collect(Tuple2.of(key.getField(0), count));
            System.out.println("Window End Time: " + dateFormat.format(context.window().getEnd()));
        }
    }
}
