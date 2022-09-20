package org.example.flinkstream.transform;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.example.flinkstream.source.job2.NotParallelSource;

/**
 * Description: 了解
 * 根据规则把一个数据流切分为多个流
 * 应用场景：
 * 可能在实际工作中，源数据流中混合了多种类似的数据，多种类型的数据处理规则不一样，所以就可以在根据一定的规则，
 * 把一个数据流切分成多个数据流，这样每个数据流就可以使用不同的处理逻辑了。
 *
 * @date 2022/9/13 10:43 PM
 **/
public class SplitAndSelectDemo {
    public static void main(String[] args) {
        // 获取Flink的运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 获取数据源，注意，针对此source, 并行度只能设置为1
        DataStreamSource<Long> text = env.addSource(new NotParallelSource()).setParallelism(1);

        // 对流进行切分，按照数据的奇偶性进行区分
//        text.split
    }

}
