package org.example.flinkstream.state;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * description: 需求：当接收到相同key的元素的个数等于3个或者超过3个的时候
 * 就计算这些元素的value的平均值
 * 计算keyed stream中每3个元素的value的平均值
 * author: xinglu
 * date 2022/9/18 11:44 AM
 */
public class TestKeyedStateMain {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple2<Long, Long>> dataStreamSource = env.fromElements(Tuple2.of(1L, 3L), Tuple2.of(1L, 5L), Tuple2.of(1L, 7L), Tuple2.of(2L, 4L), Tuple2.of(2L, 2L), Tuple2.of(2L, 5L));

        // 输出：
        // (1,5.0)
        // (2,3.6666666)
        dataStreamSource.keyBy(0)
//                .flatMap(new CountWindowAverageWithValueState())
//                .flatMap(new CountWindowAverageWithListState())
//                .flatMap(new CountWindowAverageWithMapState())
//                .flatMap(new SumFuntion())
                .flatMap(new ContainsValueFunction())
                .print();

        env.execute("TestKeyedStateMain");
    }
}
