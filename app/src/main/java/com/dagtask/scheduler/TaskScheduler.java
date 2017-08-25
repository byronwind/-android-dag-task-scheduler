package com.dagtask.scheduler;


import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * task scheduler
 * 任务图调度器
 */
public class TaskScheduler {
    private TaskGraph taskGraph;
    private ExecutorService parallelExecutor;
    private ExecutorService singleThreadExecutor;
    private final int maxConcurrentTasksNums;
    private AtomicInteger curRunningTasks = new AtomicInteger(0);


    public TaskScheduler(TaskGraph taskGraph, int maxConcurrentTasksNums) {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        parallelExecutor = Executors.newCachedThreadPool();
        this.taskGraph = taskGraph;
        this.maxConcurrentTasksNums = maxConcurrentTasksNums;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void execute() {
        assert taskGraph != null;

        while (!taskGraph.isAllTasksDone()) {
            synchronized (this) {
                if (curRunningTasks.get() < maxConcurrentTasksNums) {
                    TaskNode[] toBeExecutedTasks = taskGraph.getTasksToBeExecuted();
                    if (toBeExecutedTasks.length == 0) {
                        break;
                    }

                    TaskNode task = toBeExecutedTasks[0];
                    this.curRunningTasks.incrementAndGet();
                    task.setScheduler(this);
                    taskGraph.markRunningTask(task);
                    if (task.executeType == TaskNode.ExecuteType.SINGLE) {
                        singleThreadExecutor.execute(task);
                    } else if (task.executeType == TaskNode.ExecuteType.PARALLEL) {
                        parallelExecutor.execute(task);
                    }
                } else {
                    Set<TaskNode> readyTasks = new HashSet<>();
                    TaskNode[] toBeExecutedTasks = taskGraph.getTasksToBeExecuted();
                    readyTasks.addAll(Arrays.asList(toBeExecutedTasks));
                    while (!readyTasks.isEmpty()) {
                        TaskNode task = toBeExecutedTasks[0];
                        readyTasks.remove(task);
                        this.curRunningTasks.incrementAndGet();
                        task.setScheduler(this);
                        this.taskGraph.markRunningTask(task);
                        if (task.executeType == TaskNode.ExecuteType.SINGLE) {
                            singleThreadExecutor.execute(task);
                        } else if (task.executeType == TaskNode.ExecuteType.PARALLEL) {
                            parallelExecutor.execute(task);
                        }
                    }
                }

                if (!this.taskGraph.isAllTasksDone()) {
                    try {
                        System.out.println("Waiting called from: " + Thread.currentThread().getName());
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // cleanup
        synchronized (this) {
            this.taskGraph = null;
        }

        this.singleThreadExecutor.shutdown();
        this.parallelExecutor.shutdown();
    }


    public synchronized void notifyTaskExecuted(TaskNode taskNode) {
        this.curRunningTasks.decrementAndGet();
        this.taskGraph.markTaskExecuted(taskNode);
        System.out.println("Waking up scheduler from: " + Thread.currentThread().getName());
        this.notifyAll();
        System.out.println("Task " + taskNode.id + " is done!");
    }
}
