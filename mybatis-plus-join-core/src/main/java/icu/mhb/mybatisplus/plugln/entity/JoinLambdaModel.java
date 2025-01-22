package icu.mhb.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.core.JoinWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Consumer;

/**
 * @author mahuibo
 * @Title: JoinModel
 * @email mhb0409@qq.com
 * @time 2025/1/22
 */
@Data
@AllArgsConstructor
public class JoinLambdaModel<J, F> {

    private Class<J> clz;

    private SFunction<J, Object> joinTableField;

    private SFunction<F, Object> masterTableField;

    private String alias;

    private Boolean logicDelete;

    private Consumer<JoinWrapper<J, F>> consumer;

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, boolean logicDelete, Consumer<JoinWrapper<J, F>> consumer) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, alias, logicDelete, consumer);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, boolean logicDelete) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, alias, logicDelete, null);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, boolean logicDelete) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, null, logicDelete, null);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, boolean logicDelete, Consumer<JoinWrapper<J, F>> consumer) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, null, logicDelete, consumer);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, alias, null, null);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, String alias, Consumer<JoinWrapper<J, F>> consumer) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, alias, null, consumer);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, null, null, null);
    }

    public static <J, F> JoinLambdaModel<J, F> of(Class<J> clz, SFunction<J, Object> joinTableField, SFunction<F, Object> masterTableField, Consumer<JoinWrapper<J, F>> consumer) {
        return new JoinLambdaModel<>(clz, joinTableField, masterTableField, null, null, consumer);
    }

}
