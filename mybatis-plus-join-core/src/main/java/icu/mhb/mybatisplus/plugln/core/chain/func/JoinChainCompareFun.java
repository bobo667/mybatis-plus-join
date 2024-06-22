package icu.mhb.mybatisplus.plugln.core.chain.func;

import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;
import icu.mhb.mybatisplus.plugln.tookit.Provider;

/**
 * @author mahuibo
 * @Title: JoinChainCompareFun
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
public interface JoinChainCompareFun<Children> {

    default <E extends BaseChainModel<E>> Children eq(boolean condition, Provider<E> provider) {
        return eq(condition, false, provider);
    }

    default <E extends BaseChainModel<E>> Children eqIfNull(boolean condition, Provider<E> provider) {
        return eq(condition, true, provider);
    }

    default <E extends BaseChainModel<E>> Children eqIfNull(Provider<E> provider) {
        return eq(true, true, provider);
    }

    default <E extends BaseChainModel<E>> Children eq(Provider<E> provider) {
        return eq(true, false, provider);
    }

    <E extends BaseChainModel<E>> Children eq(boolean condition, boolean ifNull, Provider<E> provider);

}
