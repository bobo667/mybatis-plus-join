package icu.mhb.mybatisplus.plugln.core.str.func;

/**
 * String类型Join查询函数接口
 *
 * @author mahuibo
 * @Title: JoinStrQueryFunc
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public interface JoinStrQueryFunc<T, R, Children> {

    /**
     * 获取SELECT SQL
     *
     * @return SELECT SQL
     */
    String getSqlSelect();

    /**
     * 获取JOIN SQL
     *
     * @return JOIN SQL
     */
    String getJoinSql();
} 