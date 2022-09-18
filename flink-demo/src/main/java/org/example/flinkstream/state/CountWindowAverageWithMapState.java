package org.example.flinkstream.state;


import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.UUID;

/**
 * description:
 * author: xinglu
 * date 2022/9/18 2:51 PM
 */
public class CountWindowAverageWithMapState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {

    private MapState<String, Long> mapState;

    @Override
    public void open(Configuration parameters) throws Exception {
        MapStateDescriptor<String, Long> descriptor = new MapStateDescriptor<>("average", String.class, Long.class);
        mapState = getRuntimeContext().getMapState(descriptor);

    }

    @Override
    public void flatMap(Tuple2<Long, Long> longLongTuple2, Collector<Tuple2<Long, Double>> collector) throws Exception {
        mapState.put(UUID.randomUUID().toString(), longLongTuple2.f1);
        ArrayList<Long> arrayList = Lists.newArrayList(mapState.values());
        if (arrayList.size() == 3) {
            long count = 0;
            long sum = 0;
            for (Long ele : arrayList) {
                count++;
                sum += ele;
                double avg = (double) sum / count;
                collector.collect(Tuple2.of(longLongTuple2.f0, avg));
                mapState.clear();
            }
        }
    }
}
