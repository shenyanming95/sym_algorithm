package com.sym.structure.tree.rbt;

import com.sym.structure.queue.IQueue;
import com.sym.structure.queue.linked.LinkedQueue;
import com.sym.structure.tree.ITree;
import com.sym.structure.tree.traversal.IVisitor;
import java.util.Comparator;

/**
 * 红黑树的实现, 它其实可以继承{@link com.sym.structure.tree.bst.BinarySearchTree}做延伸,这里为了实现一套完整的逻辑, 单独实现.
 * 红黑树也是一颗二叉搜索树, 在二叉搜索树的基础上加了5个限制：{@link IRedBlackTree},其中最特色的就是引入了节点颜色的概念.红黑树的出现
 * 会为了补足AVL树在极端情况下的删除操作会触发O(logn)次的旋转, 所以红黑树的平衡操作相较于AVL树可能会复杂一点, 操作更多.
 *
 * @author shenyanming
 * @date 2020/6/27 10:31.
 */
public class RedBlackTree<E> implements IRedBlackTree<E> {

    /**
     * 红黑树节点
     *
     * @param <E> 存储的元素类型
     */
    private static class RbtNode<E> {
        E element;
        int color;
        RbtNode<E> parent;
        RbtNode<E> left;
        RbtNode<E> right;

        RbtNode(E e, RbtNode<E> parent) {
            // 默认创建红色节点, 因为在添加的时候, 用红色节点更容易满足红黑树的性质,
            // 所以统一规定默认的红黑树节点创建出来就是红色的
            this(e, parent, RED);
        }

        RbtNode(E e, RbtNode<E> parent, int color) {
            this.element = e;
            this.parent = parent;
            this.color = color;
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

        /**
         * 判断当前节点是否为其父节点的左子节点
         *
         * @return true-是左子节点
         */
        public boolean isParentLeftChild() {
            return parent != null && parent.left == this;
        }

        /**
         * 判断当前节点是否为其父节点的右子节点
         *
         * @return true-是右子节点
         */
        public boolean isParentRightChild() {
            return parent != null && parent.right == this;
        }

        /**
         * 获取当前节点的兄弟节点
         *
         * @return 可能为Null
         */
        public RbtNode<E> sibling() {
            if (isParentLeftChild()) {
                // 说明当前节点是否父节点的左子节点, 那么兄弟节点就是父节点的右子节点
                return parent.right;
            }
            if (isParentRightChild()) {
                // 说明当前节点是否父节点的右子节点, 那么兄弟节点就是父节点的左子节点
                return parent.left;
            }
            // 没有父节点, 即根节点
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            // 节点自身的信息
            sb.append(element.toString());
            // 父节点的信息
            if (parent == null) {
                sb.append("(null)");
            } else {
                sb.append("(").append(parent.element.toString()).append(")");
            }
            if (color == RED) {
                sb.append("_R");
            }
            return sb.toString();
        }
    }

    /**
     * 这里统一：红色节点用0表示, 黑色结点用1表示
     */
    private final static int RED = 0;
    private final static int BLACK = 1;

    /**
     * 根节点
     */
    private RbtNode<E> root;

    /**
     * 红黑树的元素数量
     */
    private int size;

    /**
     * 用于元素比较大小
     */
    private Comparator<E> comparator;

    /**
     * 无参构造器, 要求元素E自己必须实现{@link Comparable}
     */
    public RedBlackTree() {
    }

    /**
     * 指定比较器, 元素E可以不用实现{@link Comparable}, 通过指定比较器{@link Comparator}
     *
     * @param comparator 比较器
     */
    public RedBlackTree(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /**
     * 通过层序遍历的方式计算红黑树的高度
     * @return 高度
     */
    @Override
    public int height() {
        if(isEmpty()){
            return 0;
        }
        IQueue<RbtNode<E>> queue = new LinkedQueue<>();
        queue.offer(root);
        // 每一层需要访问的节点个数, 第一层肯定就只需要访问根节点就行了
        int level = 1;
        // 记录高度
        int height = 0;
        // 层序遍历
        while(!queue.isEmpty()){
            // 取出队列的节点
            RbtNode<E> node = queue.poll();
            // 将它的非空左右子节点入队
            if(node.left != null){
                queue.offer(node.left);
            }
            if(node.right != null){
                queue.offer(node.right);
            }
            // 将当前访问层数减一
            level--;
            // 如果level访问完了, 说明这一层访问完毕, 此时队列的大小就是下一层需要访问的节点个数
            if(level == 0){
                level = queue.size();
                height++;
            }
        }
        return height;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return null == root;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public void add(E e) {
        if (root == null) {
            // 红黑树的根节点必定是黑色的.
            root = new RbtNode<>(e, null, BLACK);
        } else {
            // 添加其它非根节点
            this.doAdd(e);
        }
        size++;
    }

    @Override
    public void remove(E e) {
        // 先找到该元素对应的节点
        RbtNode<E> rbtNode = this.doSearch(e);
        if(rbtNode == null){
            return;
        }
        // 真正要被删除的节点
        RbtNode<E> deleteNode = rbtNode;
        // 如果节点的度为2, 那就找到它的后继节点的值来取代它, 然后删除它的后继节点就行
        if(rbtNode.degree() == ITree.DEGREE_TWO){
            RbtNode<E> successor = this.successor(rbtNode);
            rbtNode.element = successor.element;
            deleteNode = successor;
        }
        // 节点的度1, 拿它子节点的值来替换, 然后删除它的子节点即可
        if(rbtNode.degree() == ITree.DEGREE_ONE){
            RbtNode<E> child = rbtNode.left == null ? rbtNode.right : rbtNode.left;
            rbtNode.element = child.element;
            deleteNode = child;
        }
        // 节点的度为0, 或者是真正要被删除的节点, 因为红黑树的删除逻辑需要借助兄弟来判断, 所以先获取它的兄弟结点
        RbtNode<E> sibling = deleteNode.sibling();
        if(deleteNode.isParentRightChild()){
            deleteNode.parent.right = null;
        }else if(deleteNode.isParentLeftChild()){
            deleteNode.parent.left = null;
        }else{
            // 根节点
            root = null;
        }
        // 数量减一
        size--;
        // 自平衡处理, 红黑树的删除逻辑绝对是超级复杂的一种, 但是它相比较于AVL树, 删除导致的旋转操作
        // 不会达到O(logn)级别, 有学者统计红黑树删除节点导致的旋转操作, 最多不超过3次, 所以是O(1)复杂度
        this.reBalanceAfterRemove(deleteNode, sibling);
    }

    @Override
    public boolean contains(E e) {
        return this.doSearch(e) != null;
    }

    @Override
    public void preorder(IVisitor<E> visitor) {
    }

    @Override
    public void inorder(IVisitor<E> visitor) {
    }

    @Override
    public void postorder(IVisitor<E> visitor) {
    }

    @Override
    public void levelorder(IVisitor<E> visitor) {
    }

    /**
     * 添加非根节点
     *
     * @param e 元素
     */
    private void doAdd(E e) {
        // 记录比较结果
        int result = 0;
        // 当前遍历节点
        RbtNode<E> cur = root;
        // 当前遍历节点的父节点
        RbtNode<E> par = root;
        // 从根节点开始, 进行二分查找, 直到找到为null的节点, 那里就是新节点的位置.
        // 这一步是二叉搜索树的添加逻辑, 红黑树跟AVL数一样, 都得先添加再平衡.
        while (cur != null) {
            // 每次遍历前, 先记录父节点, 供下一次遍历使用
            par = cur;
            // 比较元素大小
            result = this.doCompare(e, cur.element);
            if (result > 0) {
                // 说明新添加的元素比当前节点大, 从当前节点右边再遍历
                cur = cur.right;
            } else if (result < 0) {
                // 说明新添加的元素比当前节点小, 从当期节点左边再遍历
                cur = cur.left;
            } else {
                // 元素值相等, 直接覆盖, 方法返回
                cur.element = e;
                return;
            }
        }
        // 循环终止, 说明找到一个节点为null, 此时父节点为par, 元素值比较结果为result.
        // 创建这个新结点, 默认让它为红色
        RbtNode<E> newNode = new RbtNode<>(e, par);
        if (result > 0) {
            // 说明比父节点大, 放在父节点的右边
            par.right = newNode;
        } else {
            // 说明比父节点小, 放在父节点的左边
            par.left = newNode;
        }
        // 红黑树自平衡处理, 传入新添加的节点
        this.reBalanceAfterAdd(newNode);
    }

    /**
     * 红黑树添加节点后的自平衡处理.
     * 红黑树可以等价于一颗4阶B树, 变换逻辑是：红黑树的一个黑色节点连同它的左右红色节点(如果有), 可以组成一个4阶B树节点.
     * 因此再判断红黑树添加失衡情况时结合4阶B树的性质, 节点有4种排列分布式：红黑红、黑红、红黑、黑, 这样新节点插入的时候
     * 就会有12种情况(具体要画图演示), 在这12种情况中：
     * - 有4种不需要平衡, 那就是父节点为黑色, 因为直接并过去还是可以保持4阶B树节点的性质;
     * - 有4种只需要染色, 那就是父节点为红色且叔父节点为红色, 因为发生上溢了, 将祖父节点向上合并到更高层的父节点
     * - 有4中既要染色也要旋转, 那就是父节点为红色且叔父节点为黑色, 需要旋转变色, 以满足[红-黑-红]分布
     *
     * @param newNode 新加入的节点
     */
    private void reBalanceAfterAdd(RbtNode<E> newNode) {
        // 获取父结点
        RbtNode<E> parent = newNode.parent;
        if (parent == null) {
            // 说明红黑树上溢持续到根节点, 将其染成黑色即可
            black(newNode);
            return;
        }
        if (isBlack(parent)) {
            // 父节点为黑色, 不需要平衡, 直接返回即可
            return;
        }
        // 获取祖父节点
        RbtNode<E> grandParent = parent.parent;

        // 父节点为红色, 需要判断叔父节点的颜色
        boolean isUncleBlack = isBlack(parent.sibling());
        if (isUncleBlack) {
            //【父节点为红色-叔父节点为黑色】
            // 判断新添加节点相较于祖父节点是处于何种分布：LL、RR、LR、RL
            if (parent.isParentLeftChild()) {
                if (newNode.isParentLeftChild()) {
                    // LL分布, 父节点染成黑色, 祖父节点染成红色, 同时祖父节点右旋转
                    black(parent);
                    red(grandParent);
                    rotateRight(grandParent);
                } else {
                    // LR分布, 自己染成黑色, 祖父节点染成红色, 同时父节点左旋转, 再让祖父节点右旋转
                    black(newNode);
                    red(grandParent);
                    rotateLeft(parent);
                    rotateRight(grandParent);
                }
            } else {
                if (newNode.isParentLeftChild()) {
                    // RL分布, 自己染成黑色, 祖父节点染成红色, 同时父节点右旋转, 再让祖父节点左旋
                    black(newNode);
                    red(grandParent);
                    rotateRight(parent);
                    rotateLeft(grandParent);
                } else {
                    // RR分布, 父节点染成黑色, 祖父节点染成红色, 祖父节点左旋转
                    black(parent);
                    red(grandParent);
                    rotateLeft(grandParent);
                }
            }
        } else {
            //【父节点为红色-叔父节点为红色】
            // 让父节点和叔父节点变为黑色, 独立为一个4阶B树节点
            black(parent);
            black(parent.sibling());
            // 让祖父节点变红, 然后向上层父节点合并, 即把祖父节点重新当成一个新添加的节点, 再调用一次doReBalance()方法
            this.reBalanceAfterAdd(red(grandParent));
        }
    }

    /**
     * 红黑树删除节点后的自平衡处理.
     * 删除度为2或者度为1的节点, 实际上会拿其它节点的值来替换它的值, 然后把其它节点删除, 因此红黑树节点删除也是发生在叶子节点上, 所以删除逻辑先分为两种：
     * - 如果删除的是红色节点, 直接删除, 红黑树的性质可以得到保证;
     * - 如果删除的是黑色节点, 那就复杂了, 因为黑色节点一删除, 红黑树"任意节点到其子节点路径上的黑色节点个数一致"这条性质无法保证,
     *   若以B树角度来看, 黑色节点被删除, 意味着B树节点被删除, 那就会发生"下溢", 所以黑色节点删除又分为三种：
     *   ◦ 如果兄弟结点为黑色, 并且有红色子节点, 那可以向兄弟结点借元素;
     *   ◦ 如果兄弟结点为黑色, 但没有红色子节点, 那只能父节点向下合并;
     *   ◦ 如果兄弟结点为红色, 那说明必有黑色侄子结点, 让黑色侄子节点变为兄弟结点, 然后重复上面两个判断逻辑.
     * 另外地, 由于被删除节点可能是右子节点, 也可能是左子节点, 所以在判断左右和处理旋转时, 要考虑两种情况, 而且这两种情况是完美对称的,
     * 这里先暂时考虑被删除节点都位于它父节点的右子树, 最后将代码复制一份进行左右互换, 以满足被删除节点在左边的情况
     *
     * @param deleteNode 实际被删除的节点
     * @param sibling 实际被删节点的兄弟结点
     */
    private void reBalanceAfterRemove(RbtNode<E> deleteNode, RbtNode<E> sibling) {
        if(isRed(deleteNode)){
            // 实际被删除的节点是红色, 直接删除, 不需要再做平衡.
            return;
        }
        // 获取父节点信息
        RbtNode<E> parent = deleteNode.parent;
        if(parent == null) {
            // 如果删除的是根节点, 那肯定是整棵树删除到只剩根节点了, 直接返回.
            return;
        }
        /*
         * 先考虑被删除节点位于其父节点的右子树中.
         */
        if(deleteNode.isParentRightChild()){
            // 兄弟节点为黑色 (注意因为当前被删节点已经保证是黑色了, 所以它必定会有兄弟节点, 不然红黑树性质保证不了!!! )
            if(isBlack(sibling)){
                if(sibling.degree() == ITree.DEGREE_ZERO){
                    // 兄弟结点没有子节点, 只能让父节点下来合并, 所以判断关键转到了父节点：
                    // - 若父节点为红色, 那么父节点下来以后, 父节点原先那一层还有节点, 就不会发生下溢;
                    // - 若父节点为黑色, 那么父节点下来以后, 父节点原先那一层没节点了, 也会发生下溢.
                    if(isRed(parent)){
                        // 父节点红色, 将它染黑, 弥补当前下溢情况, 同时兄弟节点染红.
                        black(parent);
                        red(sibling);
                    }else{
                        // 父节点自己就是黑色, 那它下来合并弥补下溢后, 它自己原来那一层也会发生下溢, 递归调用当前方法, 让它重新平衡.
                        // 注意兄弟还是要染成红色的.
                        red(sibling);
                        this.reBalanceAfterRemove(parent, parent.sibling());
                    }
                }else{
                    // 一旦兄弟(黑色)节点有子节点, 那么必定为红色(因为当前被删除节点肯定是叶子节点, 如果它兄弟节点还有黑色子节点, 红黑树的性质根本保证不了).
                    // 这时候要做的就是类似B树下溢时的“借元素”, 让父节点旋转下来替代被删节点的位置. 注意当前if语句块考虑的是被删除节点位于其父节点的右边,
                    // 最好自己画图理清.
                    if(sibling.left != null){
                        // 如果兄弟节点有左子树(可能是只有左子树即度为1, 也可能有左右子树即度为2, 当兄弟节点度为2时, 可以有两种处理, 这边采用简单点的).
                        // 直接让父节点右旋转, 同时兄弟节点继承父节点的颜色(可能红可能黑), 然后父节点和兄弟节点的子节点都变黑
                        sibling.color = parent.color;
                        black(sibling.left);
                        black(parent);
                        rotateRight(parent);
                    }else{
                        // 如果兄弟节点只有右子树, 那就是LR分布了, 需要让兄弟节点先左旋转, 再让父节点右旋转, 其结果就是让兄弟节点的子节点(侄子节点)成为这棵子树新的根节点.
                        // 同时兄弟节点的子节点继承原先父节点的颜色, 父节点和兄弟节点各自变黑
                        sibling.right.color = parent.color;
                        black(sibling);
                        black(parent);
                        rotateLeft(sibling);
                        rotateRight(parent);
                    }
                }
            // 兄弟结点为红色, 要让侄子节点变为兄弟结点, 所以需要对父节点右旋转
            }else{
                // 兄弟节点为红色, 意味着父节点为黑色, 那就意味着兄弟节点必定拥有2个红色子节点(否则父节点到其叶子节点的路径上黑色节点的个数就不符合了).
                // 此时让兄弟节点变为黑色, 父节点变为红色, 同时父节点右旋转, 让侄子节点升级为兄弟节点
                black(sibling);
                red(parent);
                rotateRight(parent);
                // 最后就回到兄弟节点为黑色, 父节点为红色的平衡逻辑, 递归调用当前方法重新平衡即可
                this.reBalanceAfterRemove(deleteNode, parent.left);
            }
        /*
         * 再考虑被删除节点位于其父节点的左子树中. (与上面的思路完全一样, 只不过旋转方向变了)
         */
        }else{
            // 兄弟节点为黑色 (注意因为当前被删节点已经保证是黑色了, 所以它必定会有兄弟节点, 不然红黑树性质保证不了!!! )
            if(isBlack(sibling)){
                if(sibling.degree() == ITree.DEGREE_ZERO){
                    // 兄弟结点没有子节点, 只能让父节点下来合并, 所以判断关键转到了父节点：
                    // - 若父节点为红色, 那么父节点下来以后, 父节点原先那一层还有节点, 就不会发生下溢;
                    // - 若父节点为黑色, 那么父节点下来以后, 父节点原先那一层没节点了, 也会发生下溢.
                    if(isRed(parent)){
                        // 父节点红色, 将它染黑, 弥补当前下溢情况, 同时兄弟节点染红.
                        black(parent);
                        red(sibling);
                    }else{
                        // 父节点自己就是黑色, 那它下来合并弥补下溢后, 它自己原来那一层也会发生下溢, 递归调用当前方法, 让它重新平衡.
                        // 注意兄弟还是要染成红色的.
                        red(sibling);
                        this.reBalanceAfterRemove(parent, parent.sibling());
                    }
                }else{
                    // 一旦兄弟(黑色)节点有子节点, 那么必定为红色(因为当前被删除节点肯定是叶子节点, 如果它兄弟节点还有黑色子节点, 红黑树的性质根本保证不了).
                    // 这时候要做的就是类似B树下溢时的“借元素”, 让父节点旋转下来替代被删节点的位置. 注意当前if语句块考虑的是被删除节点位于其父节点的左边,
                    // 最好自己画图理清.
                    if(sibling.right != null){
                        // 如果兄弟节点有右子树(可能是只有右子树即度为1, 也可能有左右子树即度为2, 当兄弟节点度为2时, 可以有两种处理, 这边采用简单点的).
                        // 直接让父节点左旋转, 同时兄弟节点继承父节点的颜色(可能红可能黑), 然后父节点和兄弟节点的子节点都变黑
                        sibling.color = parent.color;
                        black(sibling.right);
                        black(parent);
                        rotateLeft(parent);
                    }else{
                        // 如果兄弟节点只有左子树, 那就是RL分布了, 需要让兄弟节点先右旋转, 再让父节点左旋转, 其结果就是让兄弟节点的子节点(侄子节点)成为这棵子树新的根节点.
                        // 同时兄弟节点的子节点继承原先父节点的颜色, 父节点和兄弟节点各自变黑
                        sibling.left.color = parent.color;
                        black(sibling);
                        black(parent);
                        rotateRight(sibling);
                        rotateLeft(parent);
                    }
                }
            // 兄弟结点为红色, 要让侄子节点变为兄弟结点, 所以需要对父节点左旋转
            }else{
                // 兄弟节点为红色, 意味着父节点为黑色, 那就意味着兄弟节点必定拥有2个红色子节点(否则父节点到其叶子节点的路径上黑色节点的个数就不符合了).
                // 此时让兄弟节点变为黑色, 父节点变为红色, 同时父节点左旋转, 让侄子节点升级为兄弟节点
                black(sibling);
                red(parent);
                rotateLeft(parent);
                // 最后就回到兄弟节点为黑色, 父节点为红色的平衡逻辑, 递归调用当前方法重新平衡即可
                this.reBalanceAfterRemove(deleteNode, parent.right);
            }
        }
    }

    /**
     * 让节点执行左旋转
     *        |
     *        o  <---- 待旋转节点node
     *      /   \
     *     o     o
     *         /   \
     *        o     o
     *
     * @param node 待旋转节点
     */
    private void rotateLeft(RbtNode<E> node) {
        //node的父节点、右子节点
        RbtNode<E> parentNode = node.parent, rightChildNode = node.right;

        // rightChildNode的左子树, 移动到node的右子节点上
        node.right = rightChildNode.left;
        // node 作为 rightChildNode 的左子树
        rightChildNode.left = node;

        // 维护 parentNode 的子节点指针, rightChildNode 要作为这个子树的新根节点
        if (node.isParentLeftChild()) {
            parentNode.left = rightChildNode;
        } else if (node.isParentRightChild()) {
            parentNode.right = rightChildNode;
        } else {
            // // 说明 node 此时是根节点, 那么 rightChildNode 作为新的根节点
            root = rightChildNode;
        }
        node.parent = rightChildNode;
        rightChildNode.parent = parentNode;
        if (node.right != null) {
            node.right.parent = node;
        }
    }

    /**
     * 让节点执行右旋转
     *          |
     *          o  <--- 待旋转节点node
     *        /   \
     *       o     o
     *      / \
     *     o   o
     *
     * @param node 待旋转节点
     */
    private void rotateRight(RbtNode<E> node) {
        // 待旋转节点的父节点和左子节点
        RbtNode<E> parentNode = node.parent, leftChildNode = node.left;

        // leftChildNode 的右子树变为 node 的左子树
        node.left = leftChildNode.right;
        // node 作为 leftChildNode 的右子树
        leftChildNode.right = node;

        // 维护 parentNode 的子节点指针, leftChildNode 要作为这个子树的新根节点
        if (node.isParentLeftChild()) {
            parentNode.left = leftChildNode;
        } else if (node.isParentRightChild()) {
            parentNode.right = leftChildNode;
        } else {
            // 说明 node 此时是根节点, 那么 leftChildNode 作为新的根节点
            root = leftChildNode;
        }

        // 维护移动节点的父引用
        node.parent = leftChildNode;
        leftChildNode.parent = parentNode;
        if (node.left != null) {
            node.left.parent = node;
        }
    }

    /**
     * 查找某个元素是否存在于红黑树中
     * @param e 元素
     * @return 元素所在的节点, 可能为null
     */
    private RbtNode<E> doSearch(E e){
        RbtNode<E> cur = root;
        while(cur != null){
            int result = doCompare(e, cur.element);
            if(result > 0){
                cur = cur.right;
            }else if(result < 0){
                cur = cur.left;
            }else{
                return cur;
            }
        }
        return null;
    }

    /**
     * 比较两个元素的大小
     *
     * @param e1 第一个元素
     * @param e2 第二个元素
     * @return 若>0说明e1>e2, 若<0说明e1<e2, 若=0说明e1==e2
     */
    @SuppressWarnings("unchecked")
    private int doCompare(E e1, E e2) {
        return comparator != null ? comparator.compare(e1, e2) :
                ((Comparable<E>) e1).compareTo(e2);
    }

    /**
     * 判断节点是否为黑色
     *
     * @return true-黑色
     */
    private boolean isBlack(RbtNode<?> node){
        return node == null || node.color == BLACK;
    }

    /**
     * 判断节点是否为红色
     *
     * @return true-红色
     */
    private boolean isRed(RbtNode<?> node){
        return node != null && node.color == RED;
    }

    private RbtNode<E> red(RbtNode<E> node){
        return color(node, RED);
    }

    private RbtNode<E> black(RbtNode<E> node){
        return color(node, BLACK);
    }

    private RbtNode<E> color(RbtNode<E> node, int color){
        if(node != null){
            node.color = color;
        }
        return node;
    }

    /**
     * 查找某个节点的后继节点
     */
    private RbtNode<E> successor(RbtNode<E> node){
        // 如果节点的右子树不为空, 则找到它右子树的最左边的节点
        if(node.right != null){
            RbtNode<E> successor = node.right;
            while(successor.left != null){
                successor = successor.left;
            }
            return successor;
        }
        // 如果节点的右子树为空, 那就要往祖先节点找, 直到找到位于祖先节点的左边
        RbtNode<E> n = node;
        while(n.isParentRightChild()){
            // 如果父节点不为null, 并且一直位于父节点的右边, 就继续循环
            n = n.parent;
        }
        return n.parent;
    }

    /* 借助外部工具类, 打印二叉树的结构图 - start*/
    @Override
    public Object root() {
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object left(Object node) {
        return ((RbtNode<E>) node).left;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object right(Object node) {
        return ((RbtNode<E>) node).right;
    }

    @Override
    public Object string(Object node) {
        return node.toString();
    }
    /* 借助外部工具类, 打印二叉树的结构图 - end*/
}
