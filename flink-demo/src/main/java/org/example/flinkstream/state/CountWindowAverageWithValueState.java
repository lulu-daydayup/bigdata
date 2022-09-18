package org.example.flinkstream.state;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

/**
 * description:
 * <p>
 * ValueState<T>: 这个状态为每一个key保存一个值
 * value():获取状态值
 * update():更新状态值
 * clear():清除状态
 * <p>
 * <p>
 * RichFlatMapFunction<IN, OUT>:
 * In:元素的输入类型
 * Out: 输出类型
 * author: xinglu
 * date 2022/9/18 11:50 AM
 */
public class CountWindowAverageWithValueState extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Double>> {

    /**
     * 用以保存每个key出现的次数，以及这个key对应的value的值(本需求为平均值)
     * managed keyed state
     * 1.valueState保存的是对应的一个key的一个状态值
     */
    private ValueState<Tuple2<Long, Long>> countAndSum;

    // 实现open方法，初始化状态的，只会被调用一次
    @Override
    public void open(Configuration parameters) throws Exception {
        // 注册状态，自定义状态类型
        // state里面存储的数据类型：Long; key出现的次数，Long: 相同key的值的累加
        ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<Tuple2<Long, Long>>(
                "average", // 状态的名字
                Types.TUPLE(Types.LONG, Types.LONG) // 状态存储的数据类型
        );
        countAndSum = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<Long, Long> longLongTuple2, Collector<Tuple2<Long, Double>> collector) throws Exception {
        // 拿到当前的key的状态值
        Tuple2<Long, Long> currentState = countAndSum.value();

        // 如果状态值还没有初始化，则初始化
        if (currentState == null) {
            currentState = Tuple2.of(0L, 0L);
        }

        // 更新状态值中的元素的个数
        currentState.f0 += 1;

        // 更新状态值中的总值
        currentState.f1 += longLongTuple2.f1;

        // 更新状态
        countAndSum.update(currentState);

        // 判断，如果当前的key出现了3次，则需要计算平均值，并且输出
        if (currentState.f0 >= 3) {
            double avg = (double) currentState.f1 / currentState.f0;
            // 输出key及其对应的平均值
            collector.collect(Tuple2.of(longLongTuple2.f0, avg));
            // 清空状态值
            countAndSum.clear();
        }

    }
}
