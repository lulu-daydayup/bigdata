package org.example.flinkstream.transform;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * description:
 * author: xinglu
 * date 2022/9/12 5:14 PM
 */
public class UnionAndTimeWindowAllDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //数据源1

        // 数据源2

        // 把data1和data2合并到一起，数据类型必须一致

        // 每2秒处理一次数据

        // 打印处理
    }
}
