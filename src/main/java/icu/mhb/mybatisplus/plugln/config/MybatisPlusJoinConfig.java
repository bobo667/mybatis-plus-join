package icu.mhb.mybatisplus.plugln.config;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import lombok.Getter;

/**
 * @author mahuibo
 * @Title: MybatisPlusJoinConfig
 * @email mhb0409@qq.com
 * @time 3/17/22
 */
public class MybatisPlusJoinConfig {

    /**
     * 列别名关键字
     */
    @Getter
    private String columnAliasKeyword;

    /**
     * 表别名关键字
     */
    @Getter
    private String tableAliasKeyword;

    private MybatisPlusJoinConfig() {

    }

    private MybatisPlusJoinConfig(String columnAliasKeyword, String tableAliasKeyword) {
        this.columnAliasKeyword = columnAliasKeyword;
        this.tableAliasKeyword = tableAliasKeyword;
    }

    public static MybatisPlusJoinConfigBuilder builder() {
        return new MybatisPlusJoinConfig.MybatisPlusJoinConfigBuilder();
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

        public MybatisPlusJoinConfigBuilder columnAliasKeyword(String columnAliasKeyword) {
            this.columnAliasKeyword = columnAliasKeyword;
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
                SqlExcerpt.TABLE_AS.updateValue(" %s " + columnAliasKeyword + " %s ", "");
                SqlExcerpt.LEFT_JOIN.updateValue(" LEFT JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
                SqlExcerpt.RIGHT_JOIN.updateValue(" RIGHT JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
                SqlExcerpt.INNER_JOIN.updateValue(" INNER JOIN %s " + tableAliasKeyword + " %s ON %s.%s = %s.%s", "");
            }

            return new MybatisPlusJoinConfig(columnAliasKeyword, tableAliasKeyword);
        }

    }

}
