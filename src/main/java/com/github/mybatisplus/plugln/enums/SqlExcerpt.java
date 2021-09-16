package com.github.mybatisplus.plugln.enums;
/**
 * @author mahuibo
 * @Title: SqlExcerpt
 * @time 8/25/21 11:44 AM
 */
public enum SqlExcerpt {

    LEFT_JOIN(" LEFT JOIN %s AS %s ON %s.%s = %s.%s", "左联SQL"),
    RIGHT_JOIN(" RIGHT JOIN %s AS %s ON %s.%s = %s.%s", "右联SQL"),
    JOIN(" JOIN %s AS %s ON %s.%s = %s.%s", "内联SQL"),
    AS(" %s AS %s ", "AS"),
    AND(" AND %s = %s ", "AND");


    private final String sql;

    private final String desc;

    SqlExcerpt(String sql, String desc) {
        this.sql = sql;
        this.desc = desc;
    }

    public String getSql() {
        return this.sql;
    }

    public String getDesc() {
        return this.desc;
    }


}
