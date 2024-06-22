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
public class ManyToManySelectBuild {

    /**
     * 多对多所属字段名
     */
    private String manyToManyField;

    /**
     * 多对多所属类
     */
    private Class<?> manyToManyClass;

    /**
     * 多对多属性类型
     */
    private Class<?> manyToManyPropertyType;

    /**
     * 多对多所属列列表
     */
    private List<FieldMapping> belongsColumns;


}
