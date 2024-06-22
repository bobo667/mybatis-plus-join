package icu.mhb.mybatisplus.plugln.core.chain.func;

import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;
import icu.mhb.mybatisplus.plugln.entity.ChainFieldData;
import icu.mhb.mybatisplus.plugln.tookit.ChainUtil;
import icu.mhb.mybatisplus.plugln.tookit.Provider;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author mahuibo
 * @Title: JoinChainCompareFun
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SuppressWarnings("all")
public interface JoinChainCompareFunc<Children, E extends BaseChainModel<?>> {

    default Children eq(ChainFieldData chainFieldData) {
        return eq(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children eqIfNull(ChainFieldData chainFieldData) {
        return eqIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children eq(boolean condition, Provider<E> provider) {
        return eq(condition, false, provider);
    }

    default Children eqIfNull(boolean condition, Provider<E> provider) {
        return eq(condition, true, provider);
    }

    default Children eqIfNull(Provider<E> provider) {
        return eq(true, true, provider);
    }

    default Children eq(Provider<E> provider) {
        return eq(true, false, provider);
    }

    /**
     * = 查詢  eq
     *
     * @param condition 是否执行
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children eq(boolean condition, boolean ifNull, Provider<E> provider);


    default Children ne(ChainFieldData chainFieldData) {
        return ne(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children neIfNull(ChainFieldData chainFieldData) {
        return neIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children neIfNull(boolean condition, Provider<E> provider) {
        return ne(condition, true, provider);
    }

    default Children neIfNull(Provider<E> provider) {
        return ne(true, true, provider);
    }

    default Children ne(Provider<E> provider) {
        return ne(true, false, provider);
    }

    default Children ne(boolean condition, Provider<E> provider) {
        return ne(condition, false, provider);
    }

    /**
     * 不等于 &lt;&gt;
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children ne(boolean condition, boolean ifNull, Provider<E> provider);

    default Children gt(ChainFieldData chainFieldData) {
        return gt(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children gtIfNull(ChainFieldData chainFieldData) {
        return gtIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children gtIfNull(boolean condition, Provider<E> provider) {
        return gt(condition, true, provider);
    }

    default Children gtIfNull(Provider<E> provider) {
        return gt(true, true, provider);
    }

    default Children gt(Provider<E> provider) {
        return gt(true, false, provider);
    }

    default Children gt(boolean condition, Provider<E> provider) {
        return gt(condition, false, provider);
    }

    /**
     * 大于 &gt;
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children gt(boolean condition, boolean ifNull, Provider<E> provider);


    default Children ge(ChainFieldData chainFieldData) {
        return ge(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children geIfNull(ChainFieldData chainFieldData) {
        return geIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children geIfNull(boolean condition, Provider<E> provider) {
        return ge(condition, true, provider);
    }

    default Children geIfNull(Provider<E> provider) {
        return ge(true, true, provider);
    }

    default Children ge(Provider<E> provider) {
        return ge(true, false, provider);
    }

    default Children ge(boolean condition, Provider<E> provider) {
        return ge(condition, false, provider);
    }


    /**
     * 大于等于 &gt;=
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children ge(boolean condition, boolean ifNull, Provider<E> provider);

    default Children lt(ChainFieldData chainFieldData) {
        return lt(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children ltIfNull(ChainFieldData chainFieldData) {
        return ltIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }


    default Children ltIfNull(boolean condition, Provider<E> provider) {
        return lt(condition, true, provider);
    }

    default Children ltIfNull(Provider<E> provider) {
        return lt(true, true, provider);
    }

    default Children lt(Provider<E> provider) {
        return lt(true, false, provider);
    }

    default Children lt(boolean condition, Provider<E> provider) {
        return lt(condition, false, provider);
    }


    /**
     * 小于 &lt;
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children lt(boolean condition, boolean ifNull, Provider<E> provider);


    default Children le(ChainFieldData chainFieldData) {
        return le(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children leIfNull(ChainFieldData chainFieldData) {
        return leIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children leIfNull(boolean condition, Provider<E> provider) {
        return lt(condition, true, provider);
    }

    default Children leIfNull(Provider<E> provider) {
        return lt(true, true, provider);
    }

    default Children le(Provider<E> provider) {
        return lt(true, false, provider);
    }

    default Children le(boolean condition, Provider<E> provider) {
        return lt(condition, false, provider);
    }


    /**
     * 小于等于 &lt;=
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children le(boolean condition, boolean ifNull, Provider<E> provider);


    default Children between(ChainFieldData chainFieldData) {
        return between(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children betweenIfNull(ChainFieldData chainFieldData) {
        return betweenIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children betweenIfNull(boolean condition, Provider<E> provider) {
        return between(condition, true, provider);
    }

    default Children betweenIfNull(Provider<E> provider) {
        return between(true, true, provider);
    }

    default Children between(Provider<E> provider) {
        return between(true, false, provider);
    }

    default Children between(boolean condition, Provider<E> provider) {
        return between(condition, false, provider);
    }


    /**
     * 需要使用 icu.mhb.mybatisplus.plugln.entity.Vals 作为chain的值
     * <p>
     * BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children between(boolean condition, boolean ifNull, Provider<E> provider);

    default Children notBetween(ChainFieldData chainFieldData) {
        return notBetween(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notBetweenIfNull(ChainFieldData chainFieldData) {
        return notBetweenIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notBetweenIfNull(boolean condition, Provider<E> provider) {
        return notBetween(condition, true, provider);
    }

    default Children notBetweenIfNull(Provider<E> provider) {
        return notBetween(true, true, provider);
    }

    default Children notBetween(Provider<E> provider) {
        return notBetween(true, false, provider);
    }

    default Children notBetween(boolean condition, Provider<E> provider) {
        return notBetween(condition, false, provider);
    }


    /**
     * 需要使用 icu.mhb.mybatisplus.plugln.entity.Vals 作为chain的值
     * <p>
     * NOT BETWEEN 值1 AND 值2
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children notBetween(boolean condition, boolean ifNull, Provider<E> provider);


    default Children like(ChainFieldData chainFieldData) {
        return like(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeIfNull(ChainFieldData chainFieldData) {
        return likeIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeIfNull(boolean condition, Provider<E> provider) {
        return like(condition, true, provider);
    }

    default Children likeIfNull(Provider<E> provider) {
        return like(true, true, provider);
    }

    default Children like(Provider<E> provider) {
        return like(true, false, provider);
    }

    default Children like(boolean condition, Provider<E> provider) {
        return like(condition, false, provider);
    }


    /**
     * LIKE '%值%'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children like(boolean condition, boolean ifNull, Provider<E> provider);

    default Children notLike(ChainFieldData chainFieldData) {
        return notLike(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeIfNull(ChainFieldData chainFieldData) {
        return notLikeIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeIfNull(boolean condition, Provider<E> provider) {
        return notLike(condition, true, provider);
    }

    default Children notLikeIfNull(Provider<E> provider) {
        return notLike(true, true, provider);
    }

    default Children notLike(Provider<E> provider) {
        return notLike(true, false, provider);
    }

    default Children notLike(boolean condition, Provider<E> provider) {
        return notLike(condition, false, provider);
    }


    /**
     * NOT LIKE '%值%'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children notLike(boolean condition, boolean ifNull, Provider<E> provider);

    default Children notLikeLeft(ChainFieldData chainFieldData) {
        return notLikeLeft(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeLeftIfNull(ChainFieldData chainFieldData) {
        return notLikeLeftIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeLeftIfNull(boolean condition, Provider<E> provider) {
        return notLikeLeft(condition, true, provider);
    }

    default Children notLikeLeftIfNull(Provider<E> provider) {
        return notLikeLeft(true, true, provider);
    }

    default Children notLikeLeft(Provider<E> provider) {
        return notLikeLeft(true, false, provider);
    }

    default Children notLikeLeft(boolean condition, Provider<E> provider) {
        return notLikeLeft(condition, false, provider);
    }


    /**
     * NOT LIKE '%值'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children notLikeLeft(boolean condition, boolean ifNull, Provider<E> provider);

    default Children notLikeRight(ChainFieldData chainFieldData) {
        return notLikeRight(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeRightIfNull(ChainFieldData chainFieldData) {
        return notLikeRightIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notLikeRightIfNull(boolean condition, Provider<E> provider) {
        return notLikeRight(condition, true, provider);
    }

    default Children notLikeRightIfNull(Provider<E> provider) {
        return notLikeRight(true, true, provider);
    }

    default Children notLikeRight(Provider<E> provider) {
        return notLikeRight(true, false, provider);
    }

    default Children notLikeRight(boolean condition, Provider<E> provider) {
        return notLikeRight(condition, false, provider);
    }


    /**
     * NOT LIKE '值%'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children notLikeRight(boolean condition, boolean ifNull, Provider<E> provider);

    default Children likeLeft(ChainFieldData chainFieldData) {
        return likeLeft(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeLeftIfNull(ChainFieldData chainFieldData) {
        return likeLeftIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeLeftIfNull(boolean condition, Provider<E> provider) {
        return likeLeft(condition, true, provider);
    }

    default Children likeLeftIfNull(Provider<E> provider) {
        return likeLeft(true, true, provider);
    }

    default Children likeLeft(Provider<E> provider) {
        return likeLeft(true, false, provider);
    }

    default Children likeLeft(boolean condition, Provider<E> provider) {
        return likeLeft(condition, false, provider);
    }


    /**
     * LIKE '%值'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children likeLeft(boolean condition, boolean ifNull, Provider<E> provider);


    default Children likeRight(ChainFieldData chainFieldData) {
        return likeRight(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeRightIfNull(ChainFieldData chainFieldData) {
        return likeRightIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children likeRightIfNull(boolean condition, Provider<E> provider) {
        return likeRight(condition, true, provider);
    }

    default Children likeRightIfNull(Provider<E> provider) {
        return likeRight(true, true, provider);
    }

    default Children likeRight(Provider<E> provider) {
        return likeRight(true, false, provider);
    }

    default Children likeRight(boolean condition, Provider<E> provider) {
        return likeRight(condition, false, provider);
    }


    /**
     * LIKE '值%'
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children likeRight(boolean condition, boolean ifNull, Provider<E> provider);


    default Children inIfNull(ChainFieldData chainFieldData) {
        return inIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children inIfNull(boolean condition, Provider<E> provider) {
        return in(condition, true, provider);
    }

    default Children inIfNull(Provider<E> provider) {
        return in(true, true, provider);
    }

    default Children in(ChainFieldData chainFieldData) {
        return in(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children in(Provider<E> provider) {
        return in(true, false, provider);
    }

    default Children in(boolean condition, Provider<E> provider) {
        return in(condition, false, provider);
    }


    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 注意！集合为空若存在逻辑错误，请在 condition 条件中判断 </li>
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param condition 执行条件
     * @param ifNull    是否判空
     * @param provider  执行函数
     * @return children
     */
    Children in(boolean condition, boolean ifNull, Provider<E> provider);


    default Children notInIfNull(ChainFieldData chainFieldData) {
        return inIfNull(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notInIfNull(boolean condition, Provider<E> provider) {
        return notIn(condition, true, provider);
    }

    default Children notInIfNull(Provider<E> provider) {
        return notIn(true, true, provider);
    }

    default Children notIn(ChainFieldData chainFieldData) {
        return notIn(() -> (E) ChainUtil.formFieldChangeToModel(chainFieldData));
    }

    default Children notIn(Provider<E> provider) {
        return notIn(true, false, provider);
    }

    default Children notIn(boolean condition, Provider<E> provider) {
        return notIn(condition, false, provider);
    }


    /**
     * 字段 NOT IN (v0, v1, ...)
     * <p>例: notIn("id", 1, 2, 3, 4, 5)</p>
     *
     * @param condition 执行条件
     * @param column    字段
     * @param values    数据数组
     * @return children
     */
    Children notIn(boolean condition, boolean ifNull, Provider<E> provider);


    default Children joinAnd(E model, Consumer<Children> consumer) {
        return joinAnd(true, model, consumer);
    }

    /**
     * join and 参数添加
     *
     * @param condition 是否执行
     * @param model     chain
     * @param consumer  消费者
     */
    Children joinAnd(boolean condition, E model, Consumer<Children> consumer);
}
