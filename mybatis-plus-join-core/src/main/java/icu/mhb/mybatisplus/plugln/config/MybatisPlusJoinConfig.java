package icu.mhb.mybatisplus.plugln.config;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 此类废弃，请使用
 * @author mahuibo
 * @Title: MybatisPlusJoinConfig
 * @email mhb0409@qq.com
 * @time 3/17/22
 */
@Deprecated
@Getter
@Slf4j
public class MybatisPlusJoinConfig {

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

    private MybatisPlusJoinConfig() {

    }

    private MybatisPlusJoinConfig(String columnAliasKeyword, String tableAliasKeyword, boolean isUseMsCache) {
        this.columnAliasKeyword = columnAliasKeyword;
        this.tableAliasKeyword = tableAliasKeyword;
        this.isUseMsCache = isUseMsCache;
    }

    public static MybatisPlusJoinConfigBuilder builder() {
        log.warn("该类已被废弃，请使用Spring boot 自动装配 icu.mhb.mybatisplus.plugln.config.MybatisPlusJoinProperties");
        return new MybatisPlusJoinConfigBuilder();
    }

    public static class MybatisPlusJoinConfigBuilder {

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

        public MybatisPlusJoinConfigBuilder columnAliasKeyword(String columnAliasKeyword) {
            this.columnAliasKeyword = columnAliasKeyword;
            return this;
        }

        public MybatisPlusJoinConfigBuilder isUseMsCache(boolean isUseMsCache) {
            this.isUseMsCache = isUseMsCache;
            return this;
        }

        public MybatisPlusJoinConfigBuilder tableAliasKeyword(String tableAliasKeyword) {
            this.tableAliasKeyword = tableAliasKeyword;
            return this;
        }

        public MybatisPlusJoinConfig build() {
            if (StringUtils.isNotBlank(columnAliasKeyword)) {
                SqlExcerpt.COLUMNS_AS.updateValue(" %s " + columnAliasKeyword + " %s ", "");
            }

            if (StringUtils.isNotBlank(tableAliasKeyword)) {
                SqlExcerpt.TABLE_AS.updateValue(" %s " + tableAliasKeyword + " %s ", "");
                SqlExcerpt.LEFT_JOIN.updateValue(" LEFT JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
                SqlExcerpt.RIGHT_JOIN.updateValue(" RIGHT JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
                SqlExcerpt.INNER_JOIN.updateValue(" INNER JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
            }

            return new MybatisPlusJoinConfig(columnAliasKeyword, tableAliasKeyword, isUseMsCache);
        }

    }

}
