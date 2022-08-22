package org.example.wc;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Description:
 * 1.13版本的流式wordCount
 *
 * @date 2022/8/22 11:34 PM
 **/
public class StreamWordCount_13_version {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);

//        env.addSource()

        DataStreamSource<String> lineDSS = env.socketTextStream("hadoop102", 7777);
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = lineDSS.flatMap((String line, Collector<Tuple2<String, Integer>> out) -> {
            String[] words = line.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word, 1));
            }
            //当Lambda表达式 使用 Java 泛型的时候, 由于泛型擦除的存在, 需要显示的声明类型信息
        }).returns(Types.TUPLE(Types.STRING, Types.INT));
        SingleOutputStreamOperator<Tuple2<String, Integer>> result = wordAndOne.keyBy(t -> t.f0).sum(1);
        result.print();
        env.execute();
    }
}
