package com.sym.algorithm.example.sort.impl;

import com.sym.algorithm.example.sort.AbstractIntegerSort;
import com.sym.util.ArrayUtil;
import com.sym.util.TimeUtil;

import java.util.Arrays;

/**
 * 选择排序, 每一轮比较, 选出最小/最大元素, 将其移动到数组的极端位置上,
 * 以选最小元素为例, 执行流程如下：
 * 1.循环第一次，遍历数组(0~n)，将最小值放在数组下标为0的位置上
 * 2.循环第二次，遍历数组(1~n)，将最小值放在数组下标为1的位置上
 * 3.循环第三次，遍历数组(2~n)，将最小值放在数组下标为2的位置上
 * 4....
 * 5.循环n-1次，遍历数组(n-1~n)，将最小值放在数组下标为n-1的位置上，完成排序
 *
 * @author shenyanming
 * Created on 2020/9/2 11:31
 */
public class SelectionSort extends AbstractIntegerSort {

    public static void main(String[] args) {
        int[] arr1 = ArrayUtil.getRandomArray(10);
        int[] arr2 = Arrays.copyOf(arr1, arr1.length);
        TimeUtil.execute("寻找最小值", () -> ArrayUtil.print(minimum(arr1)));
        TimeUtil.execute("寻找最大值", () -> ArrayUtil.print(maximum(arr2)));
    }

    public SelectionSort(int[] array) {
        super(array, "选择排序");
    }

    @Override
    protected void internalSort(int[] array) {
        // 跟 maximum() 方法逻辑一样
        // 至少需要比较n-1轮
        for (int end = array.length - 1; end > 0; end--) {
            // 记录最小值的下标
            int maximumIndex = 0;
            // 1轮比较, 需要遍历[0, end]
            for (int start = 0; start <= end; start++) {
                // 这边用 <= 是有讲究的, 假设: [10,30,30,20], 数组内有相等元素, 为了不交换它们的位置,
                // 用<= 比较, 可以保证原先靠前的元素可以继续靠前排列. 这样可以让选择排除处于稳定状态.
                if (compareByIndex(maximumIndex, start) <= 0) {
                    maximumIndex = start;
                }
            }
            // 每轮比较完后, 可以得到一个最大值即array[maximumIndex], 将其与array[end]互换位置
            swap(maximumIndex, end);
        }
    }

    /**
     * 每轮比较的是最小元素
     */
    private static int[] minimum(int[] array) {
        // 至少需要比较n-1轮
        for (int start = 0, length = array.length; start < length - 1; start++) {
            // 记录最小值的下标, 每进入一轮循环, 假设第一位就是最小值
            int minimumIndex = start;
            // 1轮比较, 需要遍历[start, array.length-1]
            for (int j = start + 1; j < array.length; j++) {
                // 这边用 >= 是有讲究的, 假设: [10,3,3,20], 数组内有相等元素, 为了不交换它们的位置,
                // 用 >= 比较, 可以保证原先靠前的元素可以继续靠前排列. 这样可以让选择排除处于稳定状态.
                if (array[minimumIndex] >= array[j]) {
                    minimumIndex = j;
                }
            }
            // 每轮比较完后, 可以得到一个最小值即array[minimumIndex],
            // 将其与array[start]互换位置
            int temp = array[minimumIndex];
            array[minimumIndex] = array[start];
            array[start] = temp;
        }
        return array;
    }

    /**
     * 每轮比较的是最大元素
     */
    private static int[] maximum(int[] array) {
        // 至少需要比较n-1轮
        for (int end = array.length - 1; end > 0; end--) {
            // 记录最小值的下标
            int maximumIndex = 0;
            // 1轮比较, 需要遍历[0, end]
            for (int start = 0; start <= end; start++) {
                // 这边用 <= 是有讲究的, 假设: [10,30,30,20], 数组内有相等元素, 为了不交换它们的位置,
                // 用<= 比较, 可以保证原先靠前的元素可以继续靠前排列. 这样可以让选择排除处于稳定状态.
                if (array[maximumIndex] <= array[start]) {
                    maximumIndex = start;
                }
            }
            // 每轮比较完后, 可以得到一个最大值即array[maximumIndex],
            // 将其与array[end]互换位置
            int temp = array[maximumIndex];
            array[maximumIndex] = array[end];
            array[end] = temp;
        }
        return array;
    }
}
