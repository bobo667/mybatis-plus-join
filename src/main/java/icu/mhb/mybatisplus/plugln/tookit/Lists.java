package icu.mhb.mybatisplus.plugln.tookit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author mahuibo
 * @Title: Lists
 * @email mhb0409@qq.com
 * @date 2022-02-17
 */
public final class Lists {

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }


    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... es) {
        return new ArrayList<E>(Arrays.asList(es));
    }

    public static <E> ArrayList<E> newArrayList(int initialCapacity) {
        return new ArrayList<E>(initialCapacity);
    }

    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<E>();
    }

}
