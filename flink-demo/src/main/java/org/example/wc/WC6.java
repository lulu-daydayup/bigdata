package org.example.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * description: 基于flink实现词频统计，使用对象来绑定数据
 * 使用默认并行度和设置并行度
 * 单次计数：
 * 1、使用默认并行度
 * 2、自定义并行度为2
 * 打开页面：http://localhost:8081
 *
 * TaskManager: 代表集群的每一台节点，TaskManager默认有2个TaskSlot, TaskSlot是运行task的。
 *
 * author: xinglu
 * date 2022/9/2 12:48 PM
 */
public class WC6 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        // 全局的并行度为2
//        env.setParallelism(2);

        // 使用下面的ParameterTool, 需要在最上面Configuration的参数里面配置--hostname:XX --port:XX
/*        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        String hostname = parameterTool.get("hostname");
        int port = parameterTool.getInt("port");*/

        // 在终端输入：nc -lk 9999
        String hostname = "127.0.0.1";
        int port = 9999;

        // socket无论如何就只能是一个并行度
        DataStreamSource<String> myDataStream = env.socketTextStream(hostname, port);

        // 修改算子的并行度，并在web页面观察。算子并行度为1，2，2，1
        SingleOutputStreamOperator<Word> result = myDataStream.flatMap(new StringSplitTask())
                .setParallelism(2)
                .keyBy("word")
                .sum("count")
                .setParallelism(2);

        result.print().setParallelism(1);
        env.execute("WC6...");

    }


    public static class StringSplitTask implements FlatMapFunction<String, Word> {
        @Override
        public void flatMap(String s, Collector<Word> collector) throws Exception {
            String[] values = s.split(" ");
            for (String value : values) {
                collector.collect(new Word(value, 1));
            }
        }
    }


    /**
     *
     */
    // 踩坑：使用对象的时候，必须有无参构造，且需要提供set/get方法。
    public static class Word {
        private String word;
        private Integer count;

        public Word() {
        }

        public Word(String word, int count) {
            this.word = word;
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "Word{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
