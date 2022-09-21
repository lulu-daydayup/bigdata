package org.example.flinkstream.window;


import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * description: 每隔5秒计算统计最近10s单词出现的次数，要在输出窗口打印更详细的信息
 * 自定义sum方法的逻辑。
 * author: xinglu
 * date 2022/9/21 10:57 AM
 */
public class TimeWindowCountTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        DataStreamSource<String> dataStreamSource = env.socketTextStream("localhost", 9999);
        dataStreamSource.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                        String[] split = s.split(" ");
                        for (String word : split) {
                            collector.collect(Tuple2.of(word, 1));
                        }
                    }
                }).keyBy(0).timeWindow(Time.seconds(10), Time.seconds(5))
                .process(new MySumProcessWindowFunction()) // 相当于spark里面的foreach
                .print().setParallelism(1);
        env.execute("TimeWindowCountTest...");
    }

    /**
     * <IN, OUT, KEY, W extends Window>
     * IN: 输入的数据类型
     * OUT：输出的数据类型
     * KEY：Key的数据类型
     * W: 窗口的数据类型
     */
    public static class MySumProcessWindowFunction
            extends ProcessWindowFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple, TimeWindow> {

        FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");

        // iterable: 当前窗口里面的数据
        @Override
        public void process(Tuple key, ProcessWindowFunction<Tuple2<String, Integer>,
                Tuple2<String, Integer>, Tuple, TimeWindow>.Context context, Iterable<Tuple2<String, Integer>> iterable,
                            Collector<Tuple2<String, Integer>> collector) throws Exception {

            FastDateFormat dateFormat = FastDateFormat.getInstance("HH:mm:ss");
            System.out.println("当前系统时间：" + dateFormat.format(System.currentTimeMillis()));
            System.out.println("窗口处理时间：" + dateFormat.format(context.currentProcessingTime()));
            System.out.println("窗口开始时间：" + dateFormat.format(context.window().getStart()));

            // 业务逻辑
            int sum = 0;
            for (Tuple2<String, Integer> element : iterable) {
                sum += 1;
            }
            collector.collect(Tuple2.of(key.getField(0), sum));
            System.out.println("窗口结束时间：" + dateFormat.format(context.window().getEnd()));
            System.out.println("===========================================================");

        }
    }
}
