package org.example.flinkstream.transform;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * description: 滑动窗口实现单词计数
 * 数据源：socket
 * 需求：每隔1秒计算最近2秒单词出现的次数
 * author: xinglu
 * date 2022/9/12 4:59 PM
 * <p>
 * flatMap
 * keyBy:
 * dataStream.keyBy("someKey") 指定对象中的"someKey"字段作为key
 * dataStream.keyBy(0) 指定Tuple中的第一个元素作为分组key
 */
public class FlatMapAndTimeWindowDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> textStream = env.socketTextStream("localhost", 9999);
        // 执行transform操作
        SingleOutputStreamOperator<WordCount> wordCountStream = textStream.flatMap(new FlatMapFunction<String, WordCount>() {
                    @Override
                    public void flatMap(String s, Collector<WordCount> collector) throws Exception {
                        String[] fields = s.split(" ");
                        for (String word : fields) {
                            collector.collect(new WordCount(word, 1L));
                        }
                    }
                }).keyBy("word")
                .timeWindow(Time.seconds(2), Time.seconds(1)) // 每隔1秒计算最近2秒
                .sum("count");

        wordCountStream.print().setParallelism(1);
        env.execute("FlatMapAndTimeWindowDemo..");


    }

    public static class WordCount {
        private String word;
        private Long count;

        public WordCount() {
        }

        public WordCount(String word, Long count) {
            this.word = word;
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "WordCount{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
