package org.example.flinkstream.window_2.job5;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;

import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

/**
 * Description: 自定义Window
 * global window + trigger一起配合才能使用
 * 需求：单词每出现三次统计一次
 *
 * @date 2022/10/6 9:24 PM
 **/
public class GlobalWindowTest {
    public static void main(String[] args) {
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
        stream.keyBy(0)
                .window(GlobalWindows.create())
                .trigger(new CountTrigger(3));
    }

    public static class CountTrigger extends Trigger {
        private Integer maxCount;

        public CountTrigger(Integer maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public TriggerResult onElement(Object element, long timestamp, Window window, TriggerContext ctx) throws Exception {
            return null;
        }

        @Override
        public TriggerResult onProcessingTime(long time, Window window, TriggerContext ctx) throws Exception {
            return null;
        }

        @Override
        public TriggerResult onEventTime(long time, Window window, TriggerContext ctx) throws Exception {
            return null;
        }

        @Override
        public void clear(Window window, TriggerContext ctx) throws Exception {

        }
    }
}
