package icu.mhb.mybatisplus.plugln.entity;

import icu.mhb.mybatisplus.plugln.tookit.Lists;
import lombok.Data;

import java.util.List;

/**
 * 多个值所用容器
 *
 * @author mahuibo
 * @Title: Vals
 * @email mhb0409@qq.com
 * @time 2024/6/22
 */
@Data
public class Vals {

    private List<Object> valList;

    public Vals(List<Object> valList) {
        this.valList = valList;
    }

    public static Vals of(Object... objs) {
        return new Vals(Lists.newArrayList(objs));
    }

    public static Vals of(List<Object> valList) {
        return new Vals(valList);
    }

    public final Vals add(Object... objs) {
        valList.addAll(Lists.newArrayList(objs));
        return this;
    }

    public Object get(int index) {
        return (valList.size() <= index) ? null : valList.get(index);
    }


}
