package icu.mhb.mybatisplus.plugln.core.func;
/**
 * @author mahuibo
 * @Title: JoinOrderFunc
 * @email mhb0409@qq.com
 * @time 2022/11/16
 */
public interface JoinOrderFunc<Children, R> {

    default Children orderByAsc(R column, int index) {
        return orderByAsc(true, column, index);
    }

    /**
     * 排序：ORDER BY 字段, ... ASC
     */
    default Children orderByAsc(boolean condition, R column, int index) {
        return orderBy(condition, true, column, index);
    }


    default Children orderByDesc(R column, int index) {
        return orderByDesc(true, column, index);
    }

    /**
     * 排序：ORDER BY 字段, ... DESC
     */
    default Children orderByDesc(boolean condition, R column, int index) {
        return orderBy(condition, false, column, index);
    }

    /**
     * 根据index下标进行排列排序顺序
     *
     * @param condition 是否执行
     * @param isAsc     是否正序
     * @param index     下标
     * @param column    列
     * @return Children
     */
    Children orderBy(boolean condition, boolean isAsc, R column, int index);

    default Children orderBySql(String sql, int index) {
        return this.orderBySql(true, sql, index);
    }

    /**
     * 手写排序SQL
     *
     * @param condition 是否执行
     * @param sql       SQL
     * @return Children
     */
    Children orderBySql(boolean condition, String sql, int index);

}
