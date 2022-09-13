package org.example.flinkstream.source.job2;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * description: 自定义数据源，不支持并行。
 * author: xinglu
 * date 2022/9/12 1:39 PM
 */
public class NotParallelSource implements SourceFunction<Long> {
    private long number = 1L;
    private boolean isRunning = true;

    @Override
    public void run(SourceContext<Long> sourceContext) throws Exception {
        while (isRunning) {
            // 把数据写到下游
            sourceContext.collect(number);
            number++;
            // 每秒写一次
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
