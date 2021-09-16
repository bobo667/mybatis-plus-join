package com.github.mybatisplus.plugln.base.service;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sun.istack.internal.NotNull;

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
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @param <E>     返回泛型
     * @return 返回E 类型的列表
     */
    <EV, E> List<EV> joinList(@NotNull Wrapper<E> wrapper, @NotNull Class<EV> clz);

    /**
     * 查询单个对象
     *
     * @param wrapper 实体对象封装操作类
     * @param clz     返回对象
     * @param <E>     包装泛型类型
     * @param <EV>    返回类型泛型
     * @return EV
     */
    <E, EV> EV joinGetOne(@NotNull Wrapper<E> wrapper, @NotNull Class<EV> clz);


    /**
     * 查询count
     *
     * @param wrapper 实体对象封装操作类（可以为 null）
     * @param <E>     返回泛型
     * @return 总数
     */
    <E> int joinCount(@NotNull Wrapper<E> wrapper);


    /**
     * 翻页查询
     *
     * @param page    翻页对象
     * @param wrapper 实体对象封装操作类
     */
    <EV, E extends IPage<EV>, C> IPage<EV> joinPage(@NotNull E page, @NotNull Wrapper<C> wrapper, @NotNull Class<EV> clz);

}
