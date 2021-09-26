package icu.mhb.mybatisplus.plugln.entity;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * having查询语句构建
 *
 * @author mahuibo
 * @Title: HavingBuild
 * @time 8/31/21 4:15 PM
 */
@Data
@Builder
public class OrderByBuild {

    /**
     * 执行SQL
     */
    private List<ISqlSegment> sqlSegmentList;

    /**
     * 是否执行
     */
    private boolean condition;

}
