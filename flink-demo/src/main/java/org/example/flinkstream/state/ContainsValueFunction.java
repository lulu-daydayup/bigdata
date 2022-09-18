package org.example.flinkstream.state;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.AggregatingState;
import org.apache.flink.api.common.state.AggregatingStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * description: ("zhangsan", 20), ("zhangsan", 30) => ("zhangsan", 20-30)
 * author: xinglu
 * date 2022/9/18 4:44 PM
 */
public class ContainsValueFunction extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, String>> {

    private AggregatingState<Long, String> totalStr;


    @Override
    public void open(Configuration parameters) throws Exception {

        AggregatingStateDescriptor<Long, String, String> descriptor = new AggregatingStateDescriptor<>("totalStr", new AggregateFunction<Long, String, String>() {
            @Override
            public String createAccumulator() {
                return "Contains: ";
            }

            // 同一分区数据合并
            @Override
            public String add(Long value, String accumulator) {
                if ("Contains: ".equals(accumulator)) {
                    return accumulator + value;
                }
                return accumulator + " and " + value;
            }

            @Override
            public String getResult(String accumulator) {
                return accumulator;
            }

            //  不同分区的数据合并
            @Override
            public String merge(String s, String acc1) {
                return s +  " and " + acc1;
            }
        }, String.class);

        totalStr = getRuntimeContext().getAggregatingState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<Long, Long> longLongTuple2, Collector<Tuple2<Long, String>> collector) throws Exception {
        totalStr.add(longLongTuple2.f1);
        collector.collect(Tuple2.of(longLongTuple2.f1, totalStr.get()));
    }
}
