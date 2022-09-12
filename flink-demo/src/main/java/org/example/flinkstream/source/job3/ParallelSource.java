package org.example.flinkstream.source.job3;

import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * description: 自定义数据源，支持并行度
 * author: xinglu
 * date 2022/9/12 1:54 PM
 */
public class ParallelSource implements ParallelSourceFunction<Long> {

    private long number = 1L;
    private boolean isRunning = true;

    @Override
    public void run(SourceContext<Long> sourceContext) throws Exception {
        while (isRunning) {
            sourceContext.collect(number);
            number++;
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;

    }
}
