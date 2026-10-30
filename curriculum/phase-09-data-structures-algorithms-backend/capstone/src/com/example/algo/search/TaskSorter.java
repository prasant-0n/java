package com.example.algo.search;

import com.example.algo.model.Task;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sorting engine implementing stable MergeSort and in-place Dual-Pivot QuickSort.
 */
public class TaskSorter {

    /**
     * Stable MergeSort guaranteeing preservation of relative order for composite records.
     */
    public static void mergeSort(List<Task> list, Comparator<Task> comparator) {
        if (list.size() <= 1) return;
        List<Task> aux = new ArrayList<>(list);
        mergeSortInternal(list, aux, 0, list.size() - 1, comparator);
    }

    private static void mergeSortInternal(List<Task> list, List<Task> aux, int low, int high, Comparator<Task> comparator) {
        if (low >= high) return;
        int mid = (low + high) >>> 1;
        mergeSortInternal(list, aux, low, mid, comparator);
        mergeSortInternal(list, aux, mid + 1, high, comparator);
        merge(list, aux, low, mid, high, comparator);
    }

    private static void merge(List<Task> list, List<Task> aux, int low, int mid, int high, Comparator<Task> comparator) {
        for (int k = low; k <= high; k++) {
            aux.set(k, list.get(k));
        }

        int i = low;
        int j = mid + 1;
        for (int k = low; k <= high; k++) {
            if (i > mid) {
                list.set(k, aux.get(j++));
            } else if (j > high) {
                list.set(k, aux.get(i++));
            } else if (comparator.compare(aux.get(j), aux.get(i)) < 0) {
                list.set(k, aux.get(j++));
            } else {
                // Stable: when elements are equal, take left side
                list.set(k, aux.get(i++));
            }
        }
    }

    /**
     * Dual-Pivot QuickSort on an array of primitive long timestamps.
     */
    public static void dualPivotQuickSort(long[] a, int left, int right) {
        if (right <= left) return;

        if (a[left] > a[right]) {
            swap(a, left, right);
        }

        long p = a[left];
        long q = a[right];

        int l = left + 1;
        int g = right - 1;
        int k = l;

        while (k <= g) {
            if (a[k] < p) {
                swap(a, k, l);
                l++;
            } else if (a[k] >= q) {
                while (a[g] > q && k < g) {
                    g--;
                }
                swap(a, k, g);
                g--;
                if (a[k] < p) {
                    swap(a, k, l);
                    l++;
                }
            }
            k++;
        }
        l--;
        g++;

        swap(a, left, l);
        swap(a, right, g);

        dualPivotQuickSort(a, left, l - 1);
        dualPivotQuickSort(a, l + 1, g - 1);
        dualPivotQuickSort(a, g + 1, right);
    }

    private static void swap(long[] a, int i, int j) {
        long temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
