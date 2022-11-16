package icu.mhb.mybatisplus.plugln.core.func;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.core.JoinWrapper;

/**
 * @author mahuibo
 * @Title: JoinMethodFunc
 * @email mhb0409@qq.com
 * @time 2022/11/16
 */
public interface JoinMethodFunc<T> {

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
