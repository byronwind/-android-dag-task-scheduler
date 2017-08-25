package com.dagtask.sample;

import com.dagtask.scheduler.TaskNode;

/**
 * SampleTask
 *
 * @author liguoqing
 * @version 1.0 24/08/2017
 */

class SampleTask extends TaskNode {
    public SampleTask(String s1, TaskNode.ExecuteType type) {
        super(s1,type);
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
