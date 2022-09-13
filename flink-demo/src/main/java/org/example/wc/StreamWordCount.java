package org.example.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Description:
 *
 * @date 2022/8/22 11:33 PM
 **/
public class StreamWordCount {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 环境的并行度为1和打印时的并行度为1，处理的过程是不一样的。
//        env.setParallelism(1);

        String inputFile = "data/hello.txt";
        DataStream<String> inputStream = env.readTextFile(inputFile);

        // 从socket文本流读取数据
        // 测试：控制台nc -lk 7777。然后在控制台输入测试参数hello flink进行测试
//        DataStream<String> inputStream = env.socketTextStream("localhost", 7777);
        // 用parameter tool工具从程序启动参数中提取配置项
//        ParameterTool parameterTool = ParameterTool.fromArgs(args);
//        String host = parameterTool.get("host");
//        int port = parameterTool.getInt("port");
//        System.out.println(port);
//        DataStream<String> inputStream = env.socketTextStream(host, port);

        DataStream<Tuple2<String, Integer>> wordCountDataStream = inputStream.flatMap(new WordCount.MyFlatMapper())
                .keyBy(0)
                .sum(1);

        wordCountDataStream.print().setParallelism(1);

        // 执行任务
        env.execute();
    }
}
