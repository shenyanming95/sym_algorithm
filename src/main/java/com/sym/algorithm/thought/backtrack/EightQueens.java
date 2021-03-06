package com.sym.algorithm.thought.backtrack;

import java.util.ArrayList;
import java.util.List;

/**
 * 回溯算法思想：
 * <pre>
 *  result = []
 *  def backtrack(路径, 选择列表):
 *     if 满足结束条件:
 *         result.add(路径)
 *         return
 *
 *     for 选择 in 选择列表:
 *         做选择
 *         backtrack(路径, 选择列表)
 *         撤销选择
 * </pre>
 * <p>
 * 八皇后问题. 在 8 x 8 格的国际象棋上摆放八个皇后, 使其不能相互攻击:
 * 任意两个皇后都不能处于同一行、同一列和同一斜线上.
 *
 * @author shenyanming
 * Created on 2021/5/5 20:29.
 */

public class EightQueens {

    public static void main(String[] args) {
        int n = 8;
        // new BoardV1(n).run();
        new BoardV2(n).run();
    }


    /**
     * N皇后解法1, 属于回溯算法思想, 但不是严格的回溯算法, 因为它没有涉及减枝
     */
    private static class BoardV1 implements Runnable {

        /**
         * 一维数组来表示N皇后棋盘, 数组下标表示行数, 数组元素值表示列号.
         */
        int[] boards;

        /**
         * 能够成功放置的摆法
         */
        int ways;

        /**
         * 初始化棋盘
         *
         * @param n N皇后
         */
        BoardV1(int n) {
            boards = new int[n];
        }

        /**
         * 开始回溯处理N皇后问题
         */
        @Override
        public void run() {
            // 从第一行开始
            place(0);
            // 给出所有结果
            System.out.printf("%d皇后共有%d种摆法", boards.length, ways);
        }

        private void place(int row) {
            if (row == boards.length) {
                // 到达最后一行, 说明可以满足所有摆放条件
                ways++;
                print();
                return;
            }
            for (int col = 0, len = boards.length; col < len; col++) {
                /*
                 * 这边有个细节要提醒下, 当row=0时, 也就是第一行时, 每一列都可以摆放
                 */
                if (isValid(row, col)) {
                    // 能够摆放到这一个点上
                    boards[row] = col;
                    // 同时处理下一行
                    place(row + 1);
                }
            }
        }

        private boolean isValid(int row, int col) {
            // 行号是递增的, 所以不需要判断行, 先来判断列
            for (int i = 0; i < row; i++) {
                if (boards[i] == col) {
                    // 前面几行, 第col列已经被摆放了
                    return false;
                }
                if ((row - i) == Math.abs(col - boards[i])) {
                    // 通过斜率来判断是否处于同一条斜线上
                    return false;
                }
            }
            return true;
        }

        private void print() {
            for (int row : boards) {
                for (int col = 0, len = boards.length; col < len; col++) {
                    if (boards[row] == col) {
                        System.out.print("1 ");
                    } else {
                        System.out.print("0 ");
                    }
                }
                System.out.println();
            }
            System.out.println("------------------------------------");
        }
    }

    /**
     * N皇后解法2, 使用回溯算法框架, 加入剪枝操作
     */
    private static class BoardV2 implements Runnable {

        /**
         * N皇后
         */
        private int n;

        /**
         * 棋盘
         */
        private int[][] board;

        /**
         * 摆放结果快照
         */
        private List<String> resultList;

        BoardV2(int n) {
            this.n = n;
            this.board = new int[n][n];
            this.resultList = new ArrayList<>();
        }


        @Override
        public void run() {
            place(0);
            resultList.forEach(System.out::println);
            System.out.printf("%d皇后共有%d种摆法", n, resultList.size());
        }

        private void place(int row) {
            if (row == n) {
                // 满足结束条件
                saveResult();
                return;
            }
            for (int col = 0; col < n; col++) {
                // 当前列没办法摆放, 选择另一条列
                if (!isValid(row, col)) {
                    continue;
                }
                // 做选择
                board[row][col] = 1;
                // 进入下一行选择
                place(row + 1);
                // 撤销选择
                board[row][col] = 0;
            }
        }

        private boolean isValid(int row, int col) {
            // 判断列是否有冲突
            for (int i = 0; i < row; i++) {
                if (board[i][col] == 1) {
                    return false;
                }
            }

            // 判断左上方斜线是否有冲突
            for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
                if (board[i][j] == 1) {
                    return false;
                }
            }

            // 判断右上方斜线是否有冲突
            for (int i = row - 1, j = col + 1; i >= 0 && j < n; i--, j++) {
                if (board[i][j] == 1)
                    return false;
            }

            return true;
        }

        /**
         * 类似数组快照, 保存摆放位置
         */
        private void saveResult() {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < n; row++) {
                for (int col = 0; col < n; col++) {
                    sb.append(board[row][col]).append(" ");
                }
                sb.append("\n");
            }
            resultList.add(sb.toString());
        }
    }

}
