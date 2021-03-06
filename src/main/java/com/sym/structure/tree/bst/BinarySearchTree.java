package com.sym.structure.tree.bst;

import com.sym.structure.queue.IQueue;
import com.sym.structure.queue.linked.LinkedQueue;
import com.sym.structure.tree.ITree;

import java.util.Comparator;
import java.util.Objects;

/**
 * 二叉搜索树 ( Binary Search Tee) 的链表实现,
 * 它是实现AVL树和红黑树的基础数据结构.
 *
 * @param <E> 要么通过{@link java.util.Comparator}比较, 要么E需要实现{@link Comparable}
 * @author shenyanming
 * @date 2020/5/21 22:51.
 */
public class BinarySearchTree<E> implements IBinarySearchTree<E> {

    /**
     * 没有指定比较器, 则需要泛型E实现{@link Comparable}
     */
    public BinarySearchTree() {
    }

    public BinarySearchTree(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * 二叉搜索树的节点实体
     *
     * @param <E> 类型
     */
    protected static class BstNode<E> {
        protected E element;
        protected BstNode<E> left;
        protected BstNode<E> right;
        protected BstNode<E> parent;

        public BstNode<E> left() {
            return this.left;
        }

        public BstNode<E> right() {
            return this.right;
        }

        public BstNode<E> parent() {
            return this.parent;
        }

        public E element() {
            return this.element;
        }

        public BstNode(E e, BstNode<E> p) {
            this.element = e;
            this.parent = p;
        }

        /**
         * 获取节点的度
         *
         * @return 0 or 1 or 2
         */
        public int degree() {
            int result = 0;
            if (left != null) {
                result++;
            }
            if (right != null) {
                result++;
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(element.toString());
            if (parent == null) {
                sb.append("(null)");
            } else {
                sb.append("(").append(parent.element.toString()).append(")");
            }
            return sb.toString();
        }
    }

    /**
     * 根结点
     */
    protected BstNode<E> root;

    /**
     * 元素总数量
     */
    protected int size;

    /**
     * 元素比较器
     */
    protected Comparator<E> comparator;

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * 添加新元素, 其实就是二分查找过程, 定位到元素实际需要添加的位置(是位于父节点的左?还是右?)
     *
     * @param e 元素
     */
    @Override
    public void add(E e) {
        // 待添加的新节点
        BstNode<E> newNode;
        if (null == root) {
            // 初始化根节点
            newNode = node(e, null);
            root = newNode;
        } else {
            // 当比较结束后, 需要知道节点e放在父节点的左边还是右边, 所以需要记录最后一次比较的值
            int compareResult = 0;
            // 原二叉树的当前比较节点, 由它从root开始, 一层一层地比较
            BstNode<E> temp = root;
            // 当比较结束后, 需要知道节点e的父节点是谁, 所以需要记录当前比较节点temp的父节点
            BstNode<E> currentParentNode = root;
            while (temp != null) {
                // 每次循环开启, 就记录父节点
                currentParentNode = temp;
                // 进行比较
                compareResult = doCompare(e, temp.element);
                // 根据二叉搜索树性质, 若比根节点值大, 则取右节点比较; 若比根节点值小, 则取左节点比较;
                // 若相等, 则覆盖原节点的值, 方法返回.
                if (compareResult > 0) {
                    temp = temp.right;
                } else if (compareResult < 0) {
                    temp = temp.left;
                } else {
                    temp.element = e;
                    return;
                }
            }
            // 当循环退出, 表示已经找到一个合适的位置, 通过判断result正负形, 来决定位于 父节点 的左边还是右边
            newNode = node(e, currentParentNode);
            if (compareResult > 0) {
                currentParentNode.right = newNode;
            } else {
                currentParentNode.left = newNode;
            }
        }
        // 添加新节点的后置处理
        afterAdd(newNode);
        size++;
    }

    /**
     * 找到对应的元素, 删除它, 一个删除涉及到3种不同的节点：
     * 1)、删除度为0的节点, 由于没有左右子树, 直接删除即可(若该节点为根节点, 直接将根节点置为null);
     * 2)、删除度为1的节点, 说明要么只有左子树, 要么只有右子树, 就让它的子节点来替代它的位置;
     * 3)、删除度为2的节点, 可以取它的前驱节点或后继节点的值来替换它的值, 然后将前驱节点或后继节点删除掉.
     *
     * @param e 待删除元素
     */
    @Override
    public void remove(E e) {
        // 定位到对应的节点
        BstNode<E> target = doSearch(e);
        if (target == null) {
            return;
        }
        // 总数量减一
        size--;
        // 获取节点的度, 处理思路是这样：
        // - 如果是度为0的节点, 直接删掉;
        // - 如果是度为1的节点, 用它的子节点替代它;
        // - 如果是度为2的节点, 用它的前驱节点(或后继节点)的值替换它的值, 然后删除前驱节点(或后继节点);
        // 由于删除度为2的节点, 需要删除一个节点(前驱 or 后继), 跟度为0和1的节点处理方式一样, 所说优先处理度为2的节点.
        int degree = target.degree();
        if (degree == ITree.DEGREE_TWO) {
            // 处理度为2的节点, 使用它的前驱节点的值来替换它的值, 然后将它的前驱节点删除
            BstNode<E> predecessor = predecessor(target);
            target.element = predecessor.element;
            target = predecessor;
        }
        // 如果删除的是根节点, 且根节点没有任何子节点(表示这棵树只剩一个节点)
        if (degree == ITree.DEGREE_ZERO && root == target) {
            root = null;
            return;
        }
        // 不管是度为0还是度为1, 都需要将父节点的左指针或者右指针清空, 所以可以放到一个逻辑一起操作
        BstNode<E> child = target.left == null ? target.right : target.left;
        if (child != null) {
            // 说明是度为1的节点
            child.parent = target.parent;
            if (child.parent == null) {
                // 说明删除的target, 就是根节点, 所以child成为新的根节点
                root = child;
            } else if (target.parent.left == target) {
                // 让子节点成为父节点的左子树
                target.parent.left = child;
            } else {
                // 让子节点成为父节点的右子树
                target.parent.right = child;
            }
        } else {
            // 说明是度为0的节点, 隔断父节点到子节点的引用即可
            if (target == target.parent.left) {
                target.parent.left = null;
            } else { // node == node.parent.right
                target.parent.right = null;
            }
        }
        // 删除节点的后置处理
        afterRemove(target);

    }

    /**
     * 查找某个元素是否存在于二叉搜索树中
     *
     * @param e 元素
     * @return true-exist
     */
    @Override
    public boolean contains(E e) {
        if (isEmpty() || e == null) {
            return false;
        }
        return null != doSearch(e);
    }

    /**
     * 返回二叉搜索树的高度
     *
     * @return 高度
     */
    @Override
    public int height() {
        // 递归的方式
        // return doComputeHeightV2(root);

        // 遍历的方式
        return doComputeHeight(root);
    }

    /**
     * 前序遍历, 可以有两种方式实现：栈或递归
     *
     * @param visitor 访问者
     */
    @Override
    public void preOrder(IVisitor<E> visitor) {
        if (Objects.isNull(root) || Objects.isNull(visitor)) {
            return;
        }
        // 通过递归的方式
        this.preOrder(root, visitor);
    }


    /**
     * 中序遍历, 可以有两种方式实现：栈或递归
     *
     * @param visitor 访问者
     */
    @Override
    public void inOrder(IVisitor<E> visitor) {
        if (Objects.isNull(root) || Objects.isNull(visitor)) {
            return;
        }
        // 通过递归的方式
        this.inOrder(root, visitor);
    }

    /**
     * 后序遍历, 可以有两种方式实现：栈或递归
     *
     * @param visitor 访问者
     */
    @Override
    public void postOrder(IVisitor<E> visitor) {
        if (Objects.isNull(root) || Objects.isNull(visitor)) {
            return;
        }
        // 通过递归的方式
        this.postOrder(root, visitor);
    }

    /**
     * 层次遍历, 通过循环+队列的方式
     *
     * @param visitor 访问者
     */
    @Override
    public void levelOrder(IVisitor<E> visitor) {
        if (Objects.isNull(root)) {
            return;
        }
        // 创建一个队列, 没访问到一个节点, 就将它的左右非空子节点入队,
        // 然后遍历这个队列, 直至队列为空, 整个循环过程就是一棵树的层序遍历！
        IQueue<BstNode<E>> queue = new LinkedQueue<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            // 当前访问节点
            BstNode<E> curNode = queue.poll();
            visitor.visit(curNode.element);
            // 依次获取当前节点的左右子节点, 非空时入队
            BstNode<E> left;
            BstNode<E> right;
            if (Objects.nonNull((left = curNode.left))) {
                queue.offer(left);
            }
            if (Objects.nonNull((right = curNode.right))) {
                queue.offer(right);
            }
        }
    }

    /* 借助外部工具类, 打印二叉树的结构图 - start*/

    @Override
    public Object printRoot() {
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object printLeft(Object node) {
        return ((BstNode<E>) node).left;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object printRight(Object node) {
        return ((BstNode<E>) node).right;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object printNodeString(Object node) {
        return ((BstNode<E>) node).toString();
    }

    /* 借助外部工具类, 打印二叉树的结构图 - end*/

    /**
     * 递归实现的前序遍历
     *
     * @param node    节点
     * @param visitor 访问者
     */
    private void preOrder(BstNode<E> node, IVisitor<E> visitor) {
        // 递归终止条件, 节点为null
        if (node == null) {
            return;
        }
        // 先访问根节点
        visitor.visit(node.element);
        // 再访问左子树
        preOrder(node.left, visitor);
        // 最后访问右子树
        preOrder(node.right, visitor);
    }

    /**
     * 递归实现的中序遍历
     *
     * @param node    节点
     * @param visitor 访问者
     */
    private void inOrder(BstNode<E> node, IVisitor<E> visitor) {
        // 递归终止条件, 节点为null
        if (node == null) {
            return;
        }
        // 先访问左子树
        inOrder(node.left, visitor);
        // 再访问根节点
        visitor.visit(node.element);
        // 最后访问右子树
        inOrder(node.right, visitor);
    }

    /**
     * 递归实现的后序遍历
     *
     * @param node    节点
     * @param visitor 访问者
     */
    private void postOrder(BstNode<E> node, IVisitor<E> visitor) {
        // 递归终止条件, 节点为null
        if (node == null) {
            return;
        }
        // 先访问左子树
        postOrder(node.left, visitor);
        // 再访问右子树
        postOrder(node.right, visitor);
        // 最后访问根节点
        visitor.visit(node.element);
    }

    /**
     * 二叉搜索树的元素比较
     *
     * @param first  元素1
     * @param second 元素2
     * @return 返回1, 0,-1分别表示元素1大于元素2, 元素1等于元素2, 元素1小于元素2
     */
    @SuppressWarnings("unchecked")
    private int doCompare(E first, E second) {
        // 如果有指定比较器, 通过比较器比较大小;
        // 如果没有指定比较器, 默认E可比较(即实现Comparable接口)
        return null != this.comparator ? comparator.compare(first, second) :
                ((Comparable<E>) first).compareTo(second);
    }

    /**
     * 二叉搜素树的搜索逻辑, 一个简单的二分查找算法
     *
     * @param e 元素
     * @return null-不存在, 反之返回元素所在的节点
     */
    private BstNode<E> doSearch(E e) {
        BstNode<E> temp = root;
        while (null != temp) {
            int result = doCompare(e, temp.element);
            if (result > 0) {
                temp = temp.right;
            } else if (result < 0) {
                temp = temp.left;
            } else {
                return temp;
            }
        }
        return null;
    }

    /**
     * 通过遍历（层序遍历）的方式计算高度
     *
     * @return 高度
     */
    private int doComputeHeight(BstNode<E> root) {
        if (null == root) {
            return 0;
        }
        IQueue<BstNode<E>> queue = new LinkedQueue<>();
        queue.offer(root);
        // 表示每层需要访问的个数
        int levelCount = 1;
        // 树的高度
        int height = 0;
        while (!queue.isEmpty()) {
            BstNode<E> node = queue.poll();
            // 没访问一个节点, 就将它所在的层次的总访问个数减一
            levelCount--;
            if (null != node.left) {
                queue.offer(node.left);
            }
            if (null != node.right) {
                queue.offer(node.right);
            }
            if (levelCount == 0) {
                // 如果一层都访问完了, 将高度加一, 并且队列中元素的个数就是下一层需要访问的个数
                height++;
                levelCount = queue.size();
            }
        }
        return height;
    }

    /**
     * 通过递归的方式计算二叉搜索树的高度
     *
     * @param root 根节点
     * @return 高度
     */
    private int doComputeHeightV2(BstNode<E> root) {
        if (null == root) {
            return 0;
        }
        // 一旦能递归调用, 说明这一层的节点存在, 说明树的高度就要加1;
        // 一棵树的高度就由它的左子树和右子树高度的最大值来决定
        return 1 + Math.max(doComputeHeightV2(root.left), doComputeHeightV2(root.right));
    }

    /**
     * 获取指定节点的前驱节点, 所谓前驱节点, 即一颗二叉树通过中序遍历(先左再根后右)后指定节点的前一个节点.
     * 在二叉搜索树中, 节点的前驱节点就是比它小的那个节点, 有3种情况查询前驱节点：
     * 1)、若指定节点的左子树不为空, 则左子树的最右边的节点就是它的前驱节点, 即node.left.right.right.right...(把当前节点当做根节点, 要等左子树遍历完后才能轮到它, 而左子树遍历完就是它的最右节点访问完)
     * 2)、若指定节点的左子树为空, 但是父节点不为空, 则要找最小的祖父节点, 换句话说就是找到当前节点在其父节点的右子树中, 即node.parent.parent.parent...直至node = parent.right
     * 3)、若指定节点的左子树为空, 并且父节点也为空, 则当前节点就没有前驱节点, 其实当前节点也是根节点
     *
     * @param node 指定节点
     * @return 指定节点的前驱节点
     */
    private BstNode<E> predecessor(BstNode<E> node) {
        if (null == node) {
            return null;
        }
        if (node.left != null) {
            // 左子树不为空, 就一直找到左子树的最右节点, 直至为null
            BstNode<E> p = node.left;
            while (p.right != null) {
                p = p.right;
            }
            return p;
        }
        // 如果代码能来到这, 说明左子树为空, 所以要从它的父节点和祖父节点向上找, 直到找到处于祖父节点的右子树部分
        BstNode<E> p = node;
        while (p.parent != null && p == p.parent.left) {
            p = p.parent;
        }
        // 循环终止有两个条件：
        // 其一：父节点或者祖父节点已经为null
        // 其二：节点p位于其父节点的右子树部分
        // 两种情况都返回p.parent, 假设第一种情况, 那就说明它没有前驱节点, 所以返回null; 假设第二种, 那父节点就是node的前驱节点, 返回它即可
        return p.parent;
    }

    /**
     * 获取指定节点的后继节点, 所谓后继节点, 即一颗二叉树通过中序遍历后当前节点的后一个节点, 它的定位方式跟前驱节点相反,
     * 在二叉搜索树中, 节点的后继节点就是下一个比它大的那个节点, 有3种情况查询后继节点：
     * 1)、若指定节点的右子树不为空, 则右子树的最左边的节点就是它的后继节点, 即node.right.left.left.left...(当前节点作为根节点, 下一个临近它的节点必是右子树的第一个访问节点, 这个节点就是左子树最左的那个接)
     * 2)、若指定节点的右子树为空, 但是父节点不为空, 则要找最大的祖父节点, 换句话说就是找到当前节点在其父节点的左子树中, 即node.parent.parent.parent...直至node = parent.left
     * 3)、若指定节点的右子树为空, 并且父节点也为空, 则当前节点就没有后继节点, 其实当前节点也是根节点
     *
     * @param node 指定节点
     * @return 指定节点的前驱节点
     */
    private BstNode<E> successor(BstNode<E> node) {
        if (node == null) {
            return null;
        }
        if (node.right != null) {
            // 右子树不为null, 就找右子树当中的最小的一个, 就是跟node相邻的并且大于它的节点, 即后继节点
            BstNode<E> s = node.right;
            while (s.left != null) {
                s = s.left;
            }
            return s;
        }
        // 如果右子树为null, 就从它的父节点和祖父节点开始找, 直至找到处于祖父节点的左子树部分
        BstNode<E> s = node;
        while (s.parent != null && s == s.parent.right) {
            s = s.parent;
        }
        return s.parent;
    }


    /**
     * 创建一个节点实体, 子类覆盖它, 以便创建子类自己定义的节点实体
     *
     * @param e     元素
     * @param pNode 父节点
     * @return 新节点
     */
    protected BstNode<E> node(E e, BstNode<E> pNode) {
        return new BstNode<>(Objects.requireNonNull(e), pNode);
    }

    /**
     * 调用{@link #add(Object)}添加新节点的后置处理, 用于
     * AVL树和红黑树的重平衡.
     *
     * @param newNode 新添加节点
     */
    protected void afterAdd(BstNode<E> newNode) {
        // 二叉搜索树没有平衡操作
    }

    /**
     * 调用{@link #remove(Object)}删除节点的后置处理, 用于
     * AVL树和红黑树的重平衡
     *
     * @param deleteNode 被删除节点
     */
    protected void afterRemove(BstNode<E> deleteNode) {
        // 二叉搜索树没有平衡操作
    }
}
