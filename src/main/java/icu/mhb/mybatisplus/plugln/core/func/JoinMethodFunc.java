package icu.mhb.mybatisplus.plugln.core.func;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.annotations.JoinField;
import icu.mhb.mybatisplus.plugln.constant.RelevancyType;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.JoinWrapper;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.Lambdas;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;

/**
 * @author mahuibo
 * @Title: JoinMethodFunc
 * @email mhb0409@qq.com
 * @time 2022/11/16
 */
public interface JoinMethodFunc<T> {


    default <J> JoinLambdaWrapper<T> pushLeftJoin(SFunction<J, Object> pushJoinField) {
        return pushJoin(pushJoinField, null, SqlExcerpt.LEFT_JOIN).end();
    }

    default <J> JoinLambdaWrapper<T> pushRightJoin(SFunction<J, Object> pushJoinField) {
        return pushJoin(pushJoinField, null, SqlExcerpt.RIGHT_JOIN).end();
    }

    default <J> JoinLambdaWrapper<T> pushInnerJoin(SFunction<J, Object> pushJoinField) {
        return pushJoin(pushJoinField, null, SqlExcerpt.INNER_JOIN).end();
    }

    default <J> JoinLambdaWrapper<T> pushLeftJoin(SFunction<J, Object>... pushJoinFields) {
        return pushJoin(SqlExcerpt.LEFT_JOIN, pushJoinFields);
    }

    default <J> JoinLambdaWrapper<T> pushRightJoin(SFunction<J, Object>... pushJoinFields) {
        return pushJoin(SqlExcerpt.RIGHT_JOIN, pushJoinFields);
    }

    default <J> JoinLambdaWrapper<T> pushInnerJoin(SFunction<J, Object>... pushJoinFields) {
        return pushJoin(SqlExcerpt.INNER_JOIN, pushJoinFields);
    }

    default <F> JoinLambdaWrapper<T> pushJoin(SqlExcerpt sqlExcerpt, SFunction<F, Object>... pushJoinFields) {
        if (ArrayUtils.isEmpty(pushJoinFields)) {
            return (JoinLambdaWrapper<T>) this;
        }

        JoinLambdaWrapper<T> joinWrapper = null;
        for (SFunction<F, Object> pushJoinField : pushJoinFields) {
            joinWrapper = pushJoin(pushJoinField, null, sqlExcerpt).end();
        }
        return joinWrapper;
    }

    @SuppressWarnings("all")
    default <J, F> JoinWrapper<J, T> pushJoin(SFunction<F, Object> pushJoinField, Class<J> clz, SqlExcerpt sqlExcerpt) {
        LambdaMeta lambdaMeta = LambdaUtils.extract(pushJoinField);
        String fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());

        Field field = ClassUtils.getDeclaredField(lambdaMeta.getInstantiatedClass(), fieldName);
        JoinField joinField = field.getAnnotation(JoinField.class);
        Assert.isFalse(joinField == null, "There is no @JoinField annotation for this property, please add..");

        Field sunField = ClassUtils.getDeclaredField(joinField.sunModelClass(), joinField.sunModelField());
        Field masterField = ClassUtils.getDeclaredField(joinField.masterModelClass(), joinField.masterModelField());
        return (JoinWrapper<J, T>) join(joinField.sunModelClass(), joinField.sunAlias()).func(w -> {
            // 一对一
            if (RelevancyType.ONT_TO_ONE.equals(joinField.relevancyType())) {
                w.oneToOneSelect(pushJoinField, joinField.sunModelClass());
            } else if (RelevancyType.MANY_TO_MANY.equals(joinField.relevancyType())) {
                w.manyToManySelect(pushJoinField, joinField.sunModelClass());
            }
            if (sqlExcerpt.equals(SqlExcerpt.LEFT_JOIN)) {
                w.leftJoin(Lambdas.getSFunction(joinField.sunModelClass(), sunField.getType(), joinField.sunModelField()), Lambdas.getSFunction(joinField.masterModelClass(), masterField.getType(), joinField.masterModelField()));
            } else if (sqlExcerpt.equals(SqlExcerpt.RIGHT_JOIN)) {
                w.rightJoin(Lambdas.getSFunction(joinField.sunModelClass(), sunField.getType(), joinField.sunModelField()), Lambdas.getSFunction(joinField.masterModelClass(), masterField.getType(), joinField.masterModelField()));
            } else {
                w.innerJoin(Lambdas.getSFunction(joinField.sunModelClass(), sunField.getType(), joinField.sunModelField()), Lambdas.getSFunction(joinField.masterModelClass(), masterField.getType(), joinField.masterModelField()));
            }
        });
    }


    /**
     * 实用化leftJoin
     *
     * @param clz              关联的实体
     * @param joinTableField   连接参数
     * @param masterTableField 被关联的参数
     * @param alias            别名
     * @param logicDelete      是否逻辑删除 如果为true就代表执行逻辑删除 默认true
     * @return
     */
    default <J, F> JoinWrapper<J, T> leftJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, boolean logicDelete) {
        return join(clz, alias, logicDelete).leftJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> leftJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, boolean logicDelete) {
        return join(clz, logicDelete).leftJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> leftJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField) {
        return leftJoin(clz, joinTableField, masterTableField, null);
    }

    default <J, F> JoinWrapper<J, T> leftJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias) {
        return join(clz, alias).leftJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> rightJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, boolean logicDelete) {
        return join(clz, alias, logicDelete).rightJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> rightJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, boolean logicDelete) {
        return join(clz, logicDelete).rightJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> rightJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField) {
        return rightJoin(clz, joinTableField, masterTableField, null);
    }

    default <J, F> JoinWrapper<J, T> rightJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias) {
        return join(clz, alias).rightJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> innerJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, boolean logicDelete) {
        return join(clz, alias, logicDelete).innerJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> innerJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, boolean logicDelete) {
        return join(clz, logicDelete).innerJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> innerJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias) {
        return join(clz, alias).innerJoin(joinTableField, masterTableField);
    }

    default <J, F> JoinWrapper<J, T> innerJoin(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField) {
        return innerJoin(clz, joinTableField, masterTableField, null);
    }


    default <J> JoinWrapper<J, T> join(Class<J> clz) {
        return join(clz, null);
    }

    default <J> JoinWrapper<J, T> join(Class<J> clz, String alias) {
        return join(clz, alias, true);
    }

    default <J> JoinWrapper<J, T> join(Class<J> clz, boolean logicDelete) {
        return join(clz, null, logicDelete);
    }


    /**
     * 进行join操作
     *
     * @param clz         外联表class
     * @param <J>         泛型
     * @param alias       别名
     * @param logicDelete 是否查询进行逻辑删除
     * @return JoinWrapper join条件
     */
    <J> JoinWrapper<J, T> join(Class<J> clz, String alias, boolean logicDelete);

}
