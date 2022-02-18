package icu.mhb.mybatisplus.plugln.constant;
import com.baomidou.mybatisplus.core.toolkit.Constants;

/**
 * 常用常量
 *
 * @author mahuibo
 * @Title: JoinConstant
 * @time 8/27/21 3:50 PM
 */
public final class JoinConstant {

    private JoinConstant() {
    }


    /**
     * join SQL片段xml名称
     */
    public static final String JOIN_SQL_NAME = "${" + Constants.WRAPPER + ".joinSql}";

    /**
     * 表别名 SQL片段 xml名称
     */
    public static final String TABLE_ALIAS_NAME = "${" + Constants.WRAPPER + ".alias}";

    /**
     * as关键字
     */
    public static final String AS = "%s AS %s";

    /**
     * mybatis plus 参数名字
     */
    public static final String MP_PARAMS_NAME = Constants.WRAPPER + ".paramNameValuePairs";

    /**
     * mapper class参数名
     */
    public static final String CLASS_PARAMS_NAME = "returnClassType0000000001111";

}
