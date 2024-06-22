package icu.mhb.mybatisplus.plugln.entity;
import lombok.Builder;
import lombok.Data;

/**
 * having查询语句构建
 *
 * @author mahuibo
 * @Title: HavingBuild
 * @time 8/31/21 4:15 PM
 */
@Data
@Builder
public class HavingBuild {

    /**
     * 执行SQL
     */
    private String sql;

    /**
     * 是否执行
     */
    private boolean condition;

    /**
     * 参数值
     */
    private Object[] params;

}
