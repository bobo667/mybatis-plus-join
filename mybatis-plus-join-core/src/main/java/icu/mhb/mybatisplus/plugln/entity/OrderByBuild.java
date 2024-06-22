package icu.mhb.mybatisplus.plugln.entity;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import lombok.Data;

/**
 * @author mahuibo
 * @Title: OrderByBuild
 * @email mhb0409@qq.com
 * @time 2022/11/16
 */
@Data
public class OrderByBuild {

    /**
     * 是否执行
     */
    boolean condition;

    /**
     * 是否正序
     */
    boolean isAsc;

    /**
     * 列
     */
    ISqlSegment column;

    /**
     * 下标
     */
    private int index;

    /**
     * 是否是手写的SQL
     */
    private boolean isSql;

}
