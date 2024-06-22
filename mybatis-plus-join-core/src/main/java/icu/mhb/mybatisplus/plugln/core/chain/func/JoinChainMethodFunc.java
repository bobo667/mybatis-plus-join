package icu.mhb.mybatisplus.plugln.core.chain.func;

import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;
import icu.mhb.mybatisplus.plugln.entity.ChainFieldData;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;

/**
 * @author mahuibo
 * @Title: JoinChainMethodFunc
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
public interface JoinChainMethodFunc<T, Children> {

    /**
     * inner join
     *
     * @param leftTableField         左表字段
     * @param rightTableField        右表字段
     * @param logicDeleteIsApplyJoin 逻辑删除是否加入join
     * @return Children
     */
    default Children innerJoin(ChainFieldData leftTableField, ChainFieldData rightTableField, boolean logicDeleteIsApplyJoin) {
        return join(leftTableField, rightTableField, logicDeleteIsApplyJoin, SqlExcerpt.INNER_JOIN);
    }


    /**
     * inner join
     *
     * @param leftTableField  左表字段
     * @param rightTableField 右表字段
     * @return Children
     */
    default Children innerJoin(ChainFieldData leftTableField, ChainFieldData rightTableField) {
        return join(leftTableField, rightTableField, true, SqlExcerpt.INNER_JOIN);
    }

    /**
     * right join
     *
     * @param leftTableField         左表字段
     * @param rightTableField        右表字段
     * @param logicDeleteIsApplyJoin 逻辑删除是否加入join
     * @return Children
     */
    default Children rightJoin(ChainFieldData leftTableField, ChainFieldData rightTableField, boolean logicDeleteIsApplyJoin) {
        return join(leftTableField, rightTableField, logicDeleteIsApplyJoin, SqlExcerpt.RIGHT_JOIN);
    }

    /**
     * right join
     *
     * @param leftTableField  左表字段
     * @param rightTableField 右表字段
     * @return Children
     */
    default Children rightJoin(ChainFieldData leftTableField, ChainFieldData rightTableField) {
        return join(leftTableField, rightTableField, true, SqlExcerpt.RIGHT_JOIN);
    }

    /**
     * left join
     *
     * @param leftTableField         左表字段
     * @param rightTableField        右表字段
     * @param logicDeleteIsApplyJoin 逻辑删除是否加入join
     * @return Children
     */
    default Children leftJoin(ChainFieldData leftTableField, ChainFieldData rightTableField, boolean logicDeleteIsApplyJoin) {
        return join(leftTableField, rightTableField, logicDeleteIsApplyJoin, SqlExcerpt.LEFT_JOIN);
    }


    /**
     * left join
     *
     * @param leftTableField  左表字段
     * @param rightTableField 右表字段
     * @return Children
     */
    default Children leftJoin(ChainFieldData leftTableField, ChainFieldData rightTableField) {
        return join(leftTableField, rightTableField, true, SqlExcerpt.LEFT_JOIN);
    }


    /**
     * join
     *
     * @param leftTableField         左表字段
     * @param rightTableField        右表字段
     * @param joinType               连接类型
     * @param logicDeleteIsApplyJoin 逻辑删除是否加入join
     * @return Children
     */
    Children join(ChainFieldData leftTableField, ChainFieldData rightTableField, boolean logicDeleteIsApplyJoin, SqlExcerpt joinType);

}
