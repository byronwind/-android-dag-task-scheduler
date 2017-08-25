package com.dagtask.scheduler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SimplePool;
import android.support.v4.util.SimpleArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * DAG IMPL from android.support.v4.util
 *
 * @param <T>
 */
final class DirectedAcyclicGraph<T> {
    private final Pool<ArrayList<T>>              mListPool      = new SimplePool(10);
    private final SimpleArrayMap<T, ArrayList<T>> mGraph         = new SimpleArrayMap();
    private final ArrayList<T>                    mSortResult    = new ArrayList();
    private final HashSet<T>                      mSortTmpMarked = new HashSet();

    public DirectedAcyclicGraph() {
    }

    public void addNode(@NonNull T node) {
        if (!this.mGraph.containsKey(node)) {
            this.mGraph.put(node, (ArrayList<T>) null);
        }

    }

    public boolean contains(@NonNull T node) {
        return this.mGraph.containsKey(node);
    }

    public void addEdge(@NonNull T node, @NonNull T incomingEdge) {
        if (this.mGraph.containsKey(node) && this.mGraph.containsKey(incomingEdge)) {
            ArrayList<T> edges = (ArrayList) this.mGraph.get(node);
            if (edges == null) {
                edges = this.getEmptyList();
                this.mGraph.put(node, edges);
            }

            edges.add(incomingEdge);
        } else {
            throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
        }
    }

    @Nullable
    public List getIncomingEdges(@NonNull T node) {
        return (List) this.mGraph.get(node);
    }

    @Nullable
    public List getOutgoingEdges(@NonNull T node) {
        ArrayList<T> result = null;
        int i = 0;

        for (int size = this.mGraph.size(); i < size; ++i) {
            ArrayList<T> edges = (ArrayList) this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                if (result == null) {
                    result = new ArrayList();
                }

                result.add(this.mGraph.keyAt(i));
            }
        }

        return result;
    }

    public void removeNode(@NonNull T node) {
        if (this.mGraph.containsKey(node)) {
            this.mGraph.remove(node);
        }
    }

    public boolean hasOutgoingEdges(@NonNull T node) {
        int i = 0;

        for (int size = this.mGraph.size(); i < size; ++i) {
            ArrayList<T> edges = (ArrayList) this.mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }

        return false;
    }

    public void clear() {
        int i = 0;

        for (int size = this.mGraph.size(); i < size; ++i) {
            ArrayList<T> edges = (ArrayList) this.mGraph.valueAt(i);
            if (edges != null) {
                this.poolList(edges);
            }
        }

        this.mGraph.clear();
    }

    @NonNull
    public ArrayList<T> getSortedList() {
        this.mSortResult.clear();
        this.mSortTmpMarked.clear();
        int i = 0;

        for (int size = this.mGraph.size(); i < size; ++i) {
            this.dfs(this.mGraph.keyAt(i), this.mSortResult, this.mSortTmpMarked);
        }

        Collections.reverse(this.mSortResult);
        return this.mSortResult;
    }

    private void dfs(T node, ArrayList<T> result, HashSet<T> tmpMarked) {
        if (!result.contains(node)) {
            if (tmpMarked.contains(node)) {
                throw new RuntimeException("This graph contains cyclic dependencies");
            } else {
                tmpMarked.add(node);
                ArrayList<T> edges = (ArrayList) this.mGraph.get(node);
                if (edges != null) {
                    int i = 0;

                    for (int size = edges.size(); i < size; ++i) {
                        this.dfs(edges.get(i), result, tmpMarked);
                    }
                }

                tmpMarked.remove(node);
                result.add(node);
            }
        }
    }

    public int size() {
        return this.mGraph.size();
    }

    @NonNull
    private ArrayList<T> getEmptyList() {
        ArrayList<T> list = (ArrayList) this.mListPool.acquire();
        if (list == null) {
            list = new ArrayList();
        }

        return list;
    }

    private void poolList(@NonNull ArrayList<T> list) {
        list.clear();
        this.mListPool.release(list);
    }
}
