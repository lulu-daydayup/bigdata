package org.example.flinkstream.state;

import akka.stream.impl.ReducerState;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * description: 聚合，不使用sum实现。
 * ReducingState<T>: 这个状态为每一个key保存一个聚合之后的值：
 * get()
 * add(): 更新状态值，将数据放到状态中
 * clear()
 * author: xinglu
 * date 2022/9/18 3:19 PM
 */
public class SumFuntion extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {

    private ReducingState<Long> sumState;

    @Override
    public void open(Configuration parameters) throws Exception {
        ReducingStateDescriptor<Long> descriptor = new ReducingStateDescriptor<>("sum", new ReduceFunction<Long>() {
            /**
             *
             * @param aLong 累积的值
             * @param t1 当前的值
             * @return
             * @throws Exception
             */
            @Override
            public Long reduce(Long aLong, Long t1) throws Exception {
                return aLong + t1;
            }
        }, Long.class);
        sumState = getRuntimeContext().getReducingState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<Long, Long> longLongTuple2, Collector<Tuple2<Long, Long>> collector) throws Exception {
        sumState.add(longLongTuple2.f1);
        collector.collect(Tuple2.of(longLongTuple2.f0, sumState.get()));
    }
}
