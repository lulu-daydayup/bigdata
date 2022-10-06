package org.example.flinkstream.window.job7;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: WaterMark + Window处理乱序时间。
 * 默认：直接丢弃。
 * 测试数据：
 * 输入数据：
 * 000001,1461756870000
 * 000001,1461756883000
 * <p>
 * 000001,1461756870000
 * 000001,1461756871000
 * 000001,1461756872000
 * <p>
 * 发现迟到太多数据就会被丢弃
 *
 * @date 2022/10/4 5:01 PM
 **/
public class WindowCountAndTimeByWaterMark {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 多并行度的测试
        // env.setParallelism(2);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // 收集迟到的数据，推荐
        OutputTag<Tuple2<String, Long>> outputTag = new OutputTag<Tuple2<String, Long>>("late-date") {
        };

        DataStreamSource<String> dataStreamSource = env.socketTextStream("127.0.0.1", 9999);
        SingleOutputStreamOperator<String> result = dataStreamSource.map(new MapFunction<String, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(String s) throws Exception {
                        String[] fields = s.split(",");
                        return new Tuple2<>(fields[0], Long.valueOf(fields[1]));
                    }
                }).assignTimestampsAndWatermarks(new EventTimeExtractor())
                .keyBy(0)
                .timeWindow(Time.seconds(3))
//                .allowedLateness(Time.seconds(2)) // 不推荐，允许时间吃到2s。
                .sideOutputLateData(outputTag) // 推荐，保留迟到太多的数据
                .process(new SumProcessWindowFunction());

        result.print().setParallelism(1);

        // 输出迟到的数据。然后把收集到迟到的数据通过redis, kafka保存下来。
        result.getSideOutput(outputTag).map(new MapFunction<Tuple2<String, Long>, String>() {
            @Override
            public String map(Tuple2<String, Long> stringLongTuple2) throws Exception {
                return "迟到数据：" + stringLongTuple2.toString();
            }
        }).print();

        env.execute("WindowCountAndTimeByWaterMark");
    }

    private static class EventTimeExtractor implements AssignerWithPeriodicWatermarks<Tuple2<String, Long>> {
        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");
        private long currentMaxEventTime = 0L;

        // 设置10s的延迟(乱序)
        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxEventTime - 10000);
        }

        // 每次过来一条数据，就会调用该方法
        @Override
        public long extractTimestamp(Tuple2<String, Long> element, long recordTimestamp) {
            Long currentElementTime = element.f1;
            currentMaxEventTime = Math.max(currentMaxEventTime, currentElementTime);

            long id = Thread.currentThread().getId();

            System.out.println("当前线程id=" + id + " event = " + element
                    + " Event Time: " + dateFormat.format(element.f1)
                    + " Max Event Time: " + dateFormat.format(currentMaxEventTime)
                    + " Current Water Mark: " + dateFormat.format(getCurrentWatermark().getTimestamp()));
            return currentElementTime;
        }
    }

    public static class SumProcessWindowFunction extends ProcessWindowFunction<Tuple2<String, Long>, String, Tuple, TimeWindow> {
        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        @Override
        public void process(Tuple tuple, ProcessWindowFunction<Tuple2<String, Long>, String, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Long>> elements, Collector<String> out) throws Exception {
            System.out.println("处理时间： " + dateFormat.format(context.currentProcessingTime()));
            System.out.println("Window Start Time: " + dateFormat.format(context.window().getStart()));
            List<String> list = new ArrayList<>();
            for (Tuple2<String, Long> element : elements) {
                list.add(element.toString() + "|" + dateFormat.format(element.f1));
            }
            out.collect(list.toString());
            System.out.println("Window End Time: " + dateFormat.format(context.window().getEnd()));
        }
    }
}
