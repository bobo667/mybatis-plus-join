package icu.mhb.mybatisplus.plugln.core.func;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * join查询条件封装
 * <p>比较值</p>
 *
 * @author mahuibo
 * @Title: JoinCompareFun
 * @email mhb0409@qq.com
 * @time 2022/12/30
 */
public interface IfCompareFun<Children, R> extends Compare<Children, R> {

    /**
     * 当值不为空时才执行eq操作
     */
    default Children eqIfNull(R column, Object val) {
        return eq(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行ne操作
     */
    default Children neIfNull(R column, Object val) {
        return ne(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行gt操作
     */
    default Children gtIfNull(R column, Object val) {
        return gt(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行ge操作
     */
    default Children geIfNull(R column, Object val) {
        return ge(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行lt操作
     */
    default Children ltIfNull(R column, Object val) {
        return lt(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行le操作
     */
    default Children leIfNull(R column, Object val) {
        return le(ObjectUtils.isNotEmpty(val), column, val);
    }


    /**
     * 当值不为空时才执行like操作
     */
    default Children likeIfNull(R column, Object val) {
        return like(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行notLike操作
     */
    default Children notLikeIfNull(R column, Object val) {
        return notLike(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行notLikeLeft操作
     */
    default Children notLikeLeftIfNull(R column, Object val) {
        return notLikeLeft(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行notLikeRight操作
     */
    default Children notLikeRightIfNull(R column, Object val) {
        return notLikeRight(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行likeLeft操作
     */
    default Children likeLeftIfNull(R column, Object val) {
        return likeLeft(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当值不为空时才执行likeRight操作
     */
    default Children likeRightIfNull(R column, Object val) {
        return likeRight(ObjectUtils.isNotEmpty(val), column, val);
    }

    /**
     * 当两个值都不为空时才执行between操作
     */
    default Children betweenIfNull(R column, Object val1, Object val2) {
        return between(ObjectUtils.isNotEmpty(val1) && ObjectUtils.isNotEmpty(val2), column, val1, val2);
    }

    /**
     * 当两个值都不为空时才执行notBetween操作
     */
    default Children notBetweenIfNull(R column, Object val1, Object val2) {
        return notBetween(ObjectUtils.isNotEmpty(val1) && ObjectUtils.isNotEmpty(val2), column, val1, val2);
    }

}
