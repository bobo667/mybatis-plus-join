package icu.mhb.mybatisplus.plugln.enums;
/**
 * @author mahuibo
 * @Title: SqlExcerpt
 * @time 8/25/21 11:44 AM
 */
public enum SqlExcerpt {

    LEFT_JOIN(" LEFT JOIN %s  %s ON %s.%s = %s.%s", "左联SQL"),
    RIGHT_JOIN(" RIGHT JOIN %s  %s ON %s.%s = %s.%s", "右联SQL"),
    INNER_JOIN(" INNER JOIN %s  %s ON %s.%s = %s.%s", "内联SQL"),
    COLUMNS_AS(" %s  %s ", "AS"),
    TABLE_AS(" %s  %s ", "AS"),
    AND(" AND %s = %s ", "AND");


    private String sql;

    private String desc;

    SqlExcerpt(String sql, String desc) {
        this.sql = sql;
        this.desc = desc;
    }

    /**
     * 更改枚举的值，如果某个数据库字段有区别，调用该方法，更改全局的关键字
     *
     * @return
     */
    public SqlExcerpt updateValue(String sql, String desc) {
        this.sql = sql;
        this.desc = desc;
        return this;
    }

    public String getSql() {
        return this.sql;
    }

    public String getDesc() {
        return this.desc;
    }


}
