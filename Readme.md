# Dag task scheduler for android app

一个大型APP在启动过程中，会有很多的任务执行，通常这些任务分配在UI线程或者后台线程中执行。并且，有些任务会存在依赖关系。为了更好的管理这些任务的调度，我们在这个示例中使用一个 [DAG（有向无环图）](https://en.wikipedia.org/wiki/Directed_acyclic_graph)对这些存在依赖关系的任务进行建模。

DAG 的实现，可以利用[jgrapht](http://www.jgrapht.org)或者[guava](https://github.com/google/guava)这两个类库。为了简单起见，我们使用android.support.utils 中的简单实现。