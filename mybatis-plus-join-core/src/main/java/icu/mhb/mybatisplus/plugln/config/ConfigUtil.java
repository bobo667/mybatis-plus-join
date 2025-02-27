package icu.mhb.mybatisplus.plugln.config;

import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.enums.DefTableAlias;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.tookit.StringUtils;

/**
 * @author mahuibo
 * @Title: ConfigUtil
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
public class ConfigUtil {

    private static MpjConfig CONFIG = new MpjConfig(null, null, true, true, StringPool.FORMAT_PLACEHOLDER, DefTableAlias.FULL_TABLE_NAME);

    public static void setConfig(MpjConfig mpjConfig) {
        if (null == mpjConfig) {
            return;
        }

        if (StringUtils.isNotBlank(mpjConfig.getTableAliasKeyword())) {
            SqlExcerpt.TABLE_AS.updateValue(" %s " + mpjConfig.getTableAliasKeyword() + " %s ", "");
            SqlExcerpt.LEFT_JOIN.updateValue(" LEFT JOIN %s " + mpjConfig.getTableAliasKeyword() + " %s ON %s.%s = %s.%s", "");
            SqlExcerpt.RIGHT_JOIN.updateValue(" RIGHT JOIN %s " + mpjConfig.getTableAliasKeyword() + " %s ON %s.%s = %s.%s", "");
            SqlExcerpt.INNER_JOIN.updateValue(" INNER JOIN %s " + mpjConfig.getTableAliasKeyword() + " %s ON %s.%s = %s.%s", "");
        }

        if (StringUtils.isNotBlank(mpjConfig.getColumnAliasKeyword())) {
            SqlExcerpt.COLUMNS_AS.updateValue(" %s " + mpjConfig.getColumnAliasKeyword() + " %s ", "");
        }

        if (StringUtils.isBlank(mpjConfig.getDefTableAlias())) {
            mpjConfig.setDefTableAlias(StringPool.FORMAT_PLACEHOLDER);
        }

        if (null == mpjConfig.getDefTableAliasType()) {
            mpjConfig.setDefTableAliasType(DefTableAlias.FULL_TABLE_NAME);
        }

        CONFIG = mpjConfig;
    }

    public static MpjConfig getConfig() {
        return CONFIG;
    }
}
