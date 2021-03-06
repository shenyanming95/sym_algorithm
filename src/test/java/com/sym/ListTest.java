package com.sym;

import com.sym.structure.list.IList;
import com.sym.structure.list.array.ArrayList;
import com.sym.structure.list.linked.DoublyLinkedList;
import com.sym.structure.list.linked.SingleLinkedList;
import com.sym.structure.list.skip.SkipList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * 请注意，无论是顺序表还是链表，一般的插入方法add()、取值方法get()和删除方法delete()
 * 这些方法的参数都是下标(注意若把i当下标，则循环应该从0开始，若i-1当下标，循环从-1开始)
 *
 * @author ym.shen
 */
public class ListTest {

    @Test
    public void arrayListTest() {
        Random r = new Random();
        // 创建容量为3的集合, 向里面插入5个数据
        IList<Integer> list = new ArrayList<>(3);
        for (int i = 0; i < 5; i++) {
            // 往数据表添加数据
            list.add(r.nextInt(100));
        }
        System.out.println("列表大小：" + list.size());
        System.out.println(list);
        System.out.println("下标为3, 新增数据：");
        list.add(3, 1914);
        System.out.println(list);
        System.out.println("下标为0,修改数据：");
        list.update(0, 9527);
        System.out.println(list);
        System.out.println("删除下标为1的数据：");
        list.delete(1);
        System.out.println(list);
        System.out.println("数值为4的下标：" + list.indexOf(4));
    }

    @Test
    public void singleLinkedListTest(){
        Random r = new Random();
        SingleLinkedList<Integer> list = new SingleLinkedList<>();
        for (int i = 0; i < 5; i++) {
            // 往数据表添加数据
            list.add(r.nextInt(100));
        }
        System.out.println(list);
        list.add(3, 100);
        System.out.println("下标为3新增数据：" + list);
        list.add(6, 69);
        System.out.println("下标为6新增数据：" + list);
        System.out.println("下标为5的值：" + list.get(5));
    }

    @Test
    public void doublyLinkedListTest() {
        System.out.println("===================测试链表=================");
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        for (int i = 0; i < 5; i++) {
            list.add("测试数据_" + i);
        }
        System.out.println(list);
        list.add(list.size(), "新插入1号");
        list.add(3, "新插入2号");
        System.out.println(list);
        System.out.printf("下标为4的元素为：%s\r\n", list.get(4));
        System.out.printf("数据【111】在链表的位置：%s\r\n", list.indexOf("测试111"));
        System.out.printf("数据【测试数据_1】在链表的位置：%s\n\r", list.indexOf("测试数据_1"));
        list.delete(2);
        System.out.println(list);
    }


    /**
     * 测试：单向链表的反转
     */
    @Test
    public void testSingleLinkedList() {
        Random random = new Random();
        SingleLinkedList<Integer> list = new SingleLinkedList<>();
        for (int i = 0; i < 9; i++) {
            list.add(random.nextInt(100));
        }
        System.out.println("原链表：");
        System.out.println(list);

        System.out.println("链表反转：");
        list.reverse();
        System.out.println(list);
    }

    /**
     * 跳表测试类
     */
    @Test
    public void testSkipList() {
        int count = 100_0000;
        int delta = 10;

        SkipList<Integer, Integer> list = new SkipList<>();
        for (int i = 0; i < count; i++) {
            list.put(i, i + delta);
        }
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(list.get(i).intValue(), i + delta);
        }
        Assert.assertEquals(count, list.size());
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(list.remove(i).intValue(), i + delta);
        }
        Assert.assertEquals(0, list.size());
    }
}
