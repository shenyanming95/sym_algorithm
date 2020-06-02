package com.sym.util;

import com.sym.structure.tree.bst.BinarySearchTree;

/**
 * @author shenyanming
 * @date 2020/5/31 15:01.
 */

public class BinaryTreeUtil {

    public static BinarySearchTree<Integer> newBinarySearchTree(String nodeString){
        BinarySearchTree<Integer> bst = new BinarySearchTree<>();
        String[] nodeArray = nodeString.split(",");
        for (String nodeValue : nodeArray) {
            if("".equals(nodeValue)){
                continue;
            }
            bst.add(Integer.parseInt(nodeValue));
        }
        return bst;
    }
}