package icu.mhb.mybatisplus.plugln.enums;

import lombok.Getter;

/**
 * @author mahuibo
 * @Title: JoinSqlMethod
 * @time 8/25/21 10:49 AM
 */
@Getter
public enum JoinSqlMethod {


    /**
     * 查询
     */
    JOIN_SELECT_LIST("joinSelectList", "查询满足条件所有数据", "<script>%s SELECT %s FROM %s %s %s %s\n</script>"),
    JOIN_SELECT_COUNT("joinSelectCount", "查询满足条件的总数", "<script>%s SELECT COUNT(1) FROM %s %s %s %s\n</script>"),
    JOIN_SELECT_PAGE("joinSelectPage", "查询满足条件的列表分页", "<script>%s SELECT %s FROM %s %s %s %s\n</script>"),
    JOIN_SELECT_ONE("joinSelectOne", "查询满足条件的单个对象", "<script>%s SELECT %s FROM %s %s %s %s \n</script>");

    private final String method;

    private final String desc;

    private final String sql;

    JoinSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }


}
