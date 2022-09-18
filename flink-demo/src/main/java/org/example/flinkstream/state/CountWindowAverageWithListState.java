package org.example.flinkstream.state;


import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Collections;

/**
 * description:
 * ListState<T> : 这个状态为每一个key保存集合的值
 * get(): 获取状态值
 * add()/ addAll() 更新状态值，将数据放到状态中
 * clear(): 清除状态
 * author: xinglu
 * date 2022/9/18 2:51 PM
 */
public class CountWindowAverageWithListState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {

    // managed keyed state
    // ListState保存的是对应的一个key的出现的所有的元素
    private ListState<Tuple2<Long, Long>> elementsByKey;

    @Override
    public void open(Configuration parameters) throws Exception {
        ListStateDescriptor<Tuple2<Long, Long>> descriptor = new ListStateDescriptor<>("average", Types.TUPLE(Types.LONG, Types.LONG));
        elementsByKey = getRuntimeContext().getListState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<Long, Long> longLongTuple2, Collector<Tuple2<Long, Double>> collector) throws Exception {
        Iterable<Tuple2<Long, Long>> currentState = elementsByKey.get();
        if (currentState == null) {
            elementsByKey.addAll(Collections.emptyList());
        }
        elementsByKey.add(longLongTuple2);
        Lists.newArrayList(elementsByKey.get());
        ArrayList<Tuple2<Long, Long>> allElements = Lists.newArrayList(elementsByKey.get());
        if (allElements.size() == 3) {
            long count = 3;
            double sum = 0;
            for (Tuple2<Long, Long> e : allElements) {
                count++;
                sum += e.f1;

            }
            double avg = sum / count;
            collector.collect(Tuple2.of(longLongTuple2.f0, avg));
            elementsByKey.clear();
        }

    }
}
