package com.dagtask.scheduler;


import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 任务图
 */
public class TaskGraph<T extends TaskNode> {

    private DirectedAcyclicGraph<T> dependsTasks = new DirectedAcyclicGraph<>();
    private DirectedAcyclicGraph<T> shadowGraph  = new DirectedAcyclicGraph<>();

    private Set<T> runningTasks = new HashSet<>();

    private Set<T> executedTasks = new HashSet<>();

    /**
     * 添加任务节点
     */
    public TaskGraph addTaskNode(T taskNode) {
        this.dependsTasks.addNode(taskNode);
        this.shadowGraph.addNode(taskNode);
        return this;
    }

    /**
     * 添加节点依赖，taskNode depends on dependsOnTask
     * A-->B  addTaskNode(B, A)
     *
     * @param taskNode
     * @param dependsOnTask
     */
    public TaskGraph addTaskNode(T taskNode, T... dependsOnTask) {
        this.dependsTasks.addNode(taskNode);
        this.shadowGraph.addNode(taskNode);
        for (T task : dependsOnTask) {
            this.dependsTasks.addEdge(task, taskNode);
            this.shadowGraph.addEdge(task, taskNode);
        }
        return this;
    }

    public DirectedAcyclicGraph<T> getDependsTasks() {
        return dependsTasks;
    }

    public void markRunningTask(final T taskNode) {
        assert taskNode != null;
        if (runningTasks.contains(taskNode)) {
            throw new IllegalStateException(String.format("Task %s is already started", taskNode.id));
        }
        this.runningTasks.add(taskNode);
    }

    public void markTaskExecuted(final T taskNode) {
        if (!this.runningTasks.contains(taskNode)) {
            throw new IllegalStateException(
                    String.format("Task %s hasn't been started, or already executed", taskNode.id));
        }

        this.runningTasks.remove(taskNode);
        this.dependsTasks.removeNode(taskNode);
        this.executedTasks.add(taskNode);
    }

    public boolean isAllTasksDone() {
        return this.dependsTasks.size() == 0;
    }

    // 提取图中可以被执行的任务
    // 条件：任务没有开始执行，入度为0（没有前置依赖）
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized TaskNode[] getTasksToBeExecuted() {
//        return this.dependsTasks.getSortedList().stream().filter(
//                task -> !this.runningTasks.contains(task) && (this.dependsTasks.getIncomingEdges(task) == null ||
//                        this.dependsTasks.getIncomingEdges(task).size() == 0)).toArray(
//                size -> new TaskNode[size]);

        ArrayList<TaskNode> nodes = (ArrayList<TaskNode>) this.dependsTasks.getSortedList();
        ArrayList<TaskNode> result = new ArrayList<>();
        for (TaskNode node : nodes) {
            if(!this.runningTasks.contains(node) && !this.dependsTasks.hasOutgoingEdges((T) node)) {
                result.add(node);
            }
        }
        TaskNode[] nodesArray = new TaskNode[result.size()];
        return result.toArray(nodesArray);
    }


}
