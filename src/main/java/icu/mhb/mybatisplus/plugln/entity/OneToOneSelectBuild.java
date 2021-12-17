package icu.mhb.mybatisplus.plugln.entity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 一对一对应构建
 *
 * @author mahuibo
 * @Title: OneToOneSelectBuild
 * @email mhb0409@qq.com
 * @time 2021/12/17
 */
@Data
@Builder
public class OneToOneSelectBuild {

    /**
     * 一对一所属字段名
     */
    private String oneToOneField;

    /**
     * 所属列列表
     */
    private List<FieldMapping> belongsColumns;

}
