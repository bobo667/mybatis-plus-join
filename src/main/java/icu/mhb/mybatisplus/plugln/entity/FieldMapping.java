package icu.mhb.mybatisplus.plugln.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段映射
 *
 * @author mahuibo
 * @Title: FieldMapping
 * @email mhb0409@qq.com
 * @time 2021/12/17
 */
@Data
@AllArgsConstructor
public class FieldMapping {

    /**
     * 字段名
     */
    private String column;

    /**
     * 属性名
     */
    private String fieldName;

}
