package com.sym.structure.tree.bst;

import com.sym.structure.tree.ITraversal;
import com.sym.structure.tree.ITree;
import com.sym.util.printer.BinaryTreeInfo;

/**
 * 二叉搜索树接口定义, Binary Search Tree, 简称BST.
 * 是二叉树的一种, 也称为二叉查找树、二叉排序树.
 *
 * @author shenyanming
 * @date 2020/5/21 21:19.
 */
public interface IBinarySearchTree<E> extends ITree<E>, ITraversal<E>, BinaryTreeInfo {
    /**
     * 返回二叉搜索树的高度
     * @return height
     */
    int height();
}
