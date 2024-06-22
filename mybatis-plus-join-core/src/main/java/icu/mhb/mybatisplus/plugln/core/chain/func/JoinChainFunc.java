package icu.mhb.mybatisplus.plugln.core.chain.func;

import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;
import icu.mhb.mybatisplus.plugln.entity.ChainFieldData;
import icu.mhb.mybatisplus.plugln.tookit.ChainUtil;
import icu.mhb.mybatisplus.plugln.tookit.Provider;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author mahuibo
 * @Title: JoinChainFunc
 * @email mhb0409@qq.com
 * @time 2024/6/22
 */
@SuppressWarnings("all")
public interface JoinChainFunc<Children, E extends BaseChainModel<?>> {

    default Children isNull(boolean condition, ChainFieldData fieldData) {
        return isNull(condition, () -> (E) ChainUtil.formFieldChangeToModel(fieldData));
    }

    default Children isNull(ChainFieldData fieldData) {
        return isNull(true, fieldData);
    }

    default Children isNull(Provider<E> provider) {
        return isNull(true, provider);
    }

    /**
     * 字段 IS NULL
     * <p>例: isNull("name")</p>
     *
     * @param condition 执行条件
     * @param provider  执行函数
     * @return children
     */
    Children isNull(boolean condition, Provider<E> provider);

    default Children isNotNull(boolean condition, ChainFieldData fieldData) {
        return isNotNull(condition, () -> (E) ChainUtil.formFieldChangeToModel(fieldData));
    }

    default Children isNotNull(ChainFieldData fieldData) {
        return isNotNull(true, fieldData);
    }

    default Children isNotNull(Provider<E> provider) {
        return isNotNull(true, provider);
    }

    /**
     * 字段 IS NOT NULL
     * <p>例: isNotNull("name")</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @return children
     */
    Children isNotNull(boolean condition, Provider<E> provider);

    default Children groupBy(boolean condition, ChainFieldData fieldData) {
        return groupBy(condition, () -> (E) ChainUtil.formFieldChangeToModel(fieldData));
    }

    default Children groupBy(ChainFieldData fieldData) {
        return groupBy(true, fieldData);
    }

    default Children groupBy(Provider<E> provider) {
        return groupBy(true, provider);
    }

    /**
     * 分组：GROUP BY 字段, ...
     * <p>例: groupBy("id")</p>
     *
     * @param condition 执行条件
     * @param column    单个字段
     * @return children
     */
    Children groupBy(boolean condition, Provider<E> provider);

    default Children orderByAsc(boolean condition, ChainFieldData fieldData) {
        return orderByAsc(condition, () -> (E) ChainUtil.formFieldChangeToModel(fieldData));
    }

    default Children orderByAsc(ChainFieldData fieldData) {
        return orderByAsc(true, fieldData);
    }

    default Children orderByAsc(Provider<E> provider) {
        return orderByAsc(true, provider);
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     */
    default Children orderByAsc(boolean condition, Provider<E> provider) {
        return orderBy(condition, true, provider);
    }


    default Children orderByDesc(boolean condition, ChainFieldData fieldData) {
        return orderByDesc(condition, () -> (E) ChainUtil.formFieldChangeToModel(fieldData));
    }

    default Children orderByDesc(ChainFieldData fieldData) {
        return orderByDesc(true, fieldData);
    }

    default Children orderByDesc(Provider<E> provider) {
        return orderByDesc(true, provider);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     */
    default Children orderByDesc(boolean condition, Provider<E> provider) {
        return orderBy(condition, false, provider);
    }

    /**
     * 排序：ORDER BY 根据传入手写sql
     */
    default Children orderBySql(String sql) {
        return orderBySql(true, sql);
    }

    /**
     * 排序：ORDER BY 根据传入手写sql
     */
    Children orderBySql(boolean condition, String sql);

    /**
     * 排序：ORDER BY 字段, ...
     */
    Children orderBy(boolean condition, boolean isAsc, Provider<E> provider);


}
