package icu.mhb.mybatisplus.plugln.config;

import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.enums.DefTableAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mahuibo
 * @Title: MpjConfig
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MpjConfig {

    /**
     * 列别名关键字
     */
    private String columnAliasKeyword;

    /**
     * 表别名关键字
     */
    private String tableAliasKeyword;

    /**
     * 是否使用MappedStatement缓存，如果使用在JoinInterceptor中就会更改
     * MappedStatement的id，导致mybatis-plus-mate 的某些拦截器插件报错，
     * 设置成false，代表不使用缓存则不会更改MappedStatement的id
     */
    private boolean isUseMsCache = true;

    /**
     * 是否开启子表的逻辑删除 默认开启，关闭后则不会拼接逻辑删除
     */
    private boolean isSubTableLogic = true;

    /**
     * 默认表别名映射
     */
    private String defTableAlias = StringPool.FORMAT_PLACEHOLDER;

    /**
     * 默认表别名生成方式
     */
    private DefTableAlias defTableAliasType = DefTableAlias.FULL_TABLE_NAME;

}
