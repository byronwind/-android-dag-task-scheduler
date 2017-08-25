package com.dagtask.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.dagtask.scheduler.TaskGraph;
import com.dagtask.scheduler.TaskNode;
import com.dagtask.scheduler.TaskScheduler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TaskGraph graph = new TaskGraph();
        SampleTask s1 = new SampleTask("s1", TaskNode.ExecuteType.SINGLE);
        SampleTask s2 = new SampleTask("s2", TaskNode.ExecuteType.SINGLE);
        SampleTask s3 = new SampleTask("s3", TaskNode.ExecuteType.PARALLEL);
        SampleTask s4 = new SampleTask("s4", TaskNode.ExecuteType.PARALLEL);
        SampleTask s5 = new SampleTask("s5", TaskNode.ExecuteType.SINGLE);

        // s1->s2
        // s1->s3
        // s2->s4
        // s4->s5
        // s3->s5
        graph.addTaskNode(s1);
        graph.addTaskNode(s2, s1);
        graph.addTaskNode(s3, s1);
        graph.addTaskNode(s4, s2);
        graph.addTaskNode(s5, s4);
        graph.addTaskNode(s5, s3);

        final TaskScheduler scheduler = new TaskScheduler(graph, 3);

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                scheduler.execute();

            }
        });

        background.start();

        //        List<TaskNode> s1Outgoing = graph.getDependsTasks().getOutgoingEdges(s1);
        //
        //        List<TaskNode> s2Incoming = graph.getDependsTasks().getIncomingEdges(s2);
    }
}
