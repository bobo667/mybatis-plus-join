package icu.mhb.mybatisplus.plugln.extend;

import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.chain.JoinChainQueryWrapper;
import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;

/**
 * @author mahuibo
 * @Title: Joins
 * @email mhb0409@qq.com
 * @time 2022/12/22
 */
public class Joins {

    public static <T> JoinLambdaWrapper<T> of(Class<T> clz) {
        return new JoinLambdaWrapper<>(clz);
    }

    public static <T> JoinLambdaWrapper<T> of(Class<T> clz, String alias) {
        return new JoinLambdaWrapper<>(clz, alias);
    }

    public static <T> JoinLambdaWrapper<T> of(T entity, String alias) {
        return new JoinLambdaWrapper<>(entity, alias);
    }

    public static <T> JoinLambdaWrapper<T> of(T entity) {
        return new JoinLambdaWrapper<>(entity);
    }

    public static <T> JoinChainQueryWrapper<T> chain(BaseChainModel<?> model) {
        return new JoinChainQueryWrapper<>(model);
    }

}
