package icu.mhb.mybatisplus.plugln.injector;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;

/**
 * @author mahuibo
 * @Title: JoinAbstractMethod
 * @time 8/27/21 3:53 PM
 */
public abstract class JoinAbstractMethod extends AbstractMethod {

    /**
     * 获取join 别名后的表名
     *
     * @param tableInfo 表信息
     * @return 表名
     */
    protected String getJoinTableName(TableInfo tableInfo) {
        return String.format(JoinConstant.AS, tableInfo.getTableName(), JoinConstant.TABLE_ALIAS_NAME);
    }

    /**
     * 获取表类信息
     *
     * @param clz 类信息
     * @return 转换后的class
     */
    protected Class<?> getTableClass(Class<?> clz) {
        return ClassUtils.getTableClass(clz);
    }


}
