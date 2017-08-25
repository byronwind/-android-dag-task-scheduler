package com.dagtask.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务节点
 */
public abstract class TaskNode implements Runnable {

    // 任务分发线程类型
    public enum ExecuteType {
        SINGLE, // 固定单线程，在Android 上可以指定 MainThread
        PARALLEL, // 并发线程
    }

    String id;

    ExecuteType executeType;

    // 输入参数
    private Map<String, List<Object>> inputParameters = new HashMap<>();
    // 输出
    private Map<String, List<Object>> outputParameters = new HashMap<>();

    private TaskScheduler scheduler;

    public TaskNode(String id, ExecuteType executeType) {
        this.id = id;
        this.executeType = executeType;
    }

    public void setScheduler(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public abstract void execute();

    @Override
    public void run() {
        System.out.println("Starting Task:" + this.id);
        this.execute();
        System.out.println("Done Task:" + this.id);
        this.scheduler.notifyTaskExecuted(this);
    }


}
