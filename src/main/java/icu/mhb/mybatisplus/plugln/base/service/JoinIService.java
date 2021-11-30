package icu.mhb.mybatisplus.plugln.base.service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * join 基础service接口
 *
 * @author mahuibo
 * @Title: JoinIService
 * @time 8/25/21 10:21 AM
 */
public interface JoinIService<T> extends IService<T> {


    /**
     * 查询列表
     *
     * @param wrapper 实体对象封装操作类
     * @param <E>     返回泛型（如果只查询一个字段可以传递String Int之类的类型）
     * @return 返回E 类型的列表
     */
    <EV, E> List<EV> joinList(Wrapper<E> wrapper, Class<EV> clz);

    /**
     * 查询单个对象
     *
     * @param wrapper 实体对象封装操作类
     * @param clz     返回对象 （如果只查询一个字段可以传递String Int之类的类型）
     * @param <E>     包装泛型类型
     * @param <EV>    返回类型泛型
     * @return EV
     */
    <E, EV> EV joinGetOne(Wrapper<E> wrapper, Class<EV> clz);


    /**
     * 查询count
     *
     * @param wrapper 实体对象封装操作类
     * @param <E>     返回泛型
     * @return 总数
     */
    <E> int joinCount(Wrapper<E> wrapper);


    /**
     * 翻页查询
     *
     * @param page    翻页对象
     * @param wrapper 实体对象封装操作类
     */
    <EV, E extends IPage<EV>, C> IPage<EV> joinPage(E page, Wrapper<C> wrapper, Class<EV> clz);

}
