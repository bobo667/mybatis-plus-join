package icu.mhb.mybatisplus.plugln.core.support;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.*;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import icu.mhb.mybatisplus.plugln.annotations.TableAlias;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.func.JoinCompareFun;
import icu.mhb.mybatisplus.plugln.core.func.JoinOrderFunc;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import icu.mhb.mybatisplus.plugln.keyword.DefaultFuncKeyWord;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import icu.mhb.mybatisplus.plugln.tookit.TableAliasCache;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.*;
import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.NEWLINE;
import static java.util.stream.Collectors.joining;

/**
 * Join lambda解析
 * 重写于mybatis plus 中的LambdaQueryWrapper
 *
 * @author mahuibo
 * @Title: SupportJoinLambdaWrapper
 * @time 8/24/21 6:28 PM
 * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 */
@SuppressWarnings("all")
public abstract class SupportJoinWrapper<T, R, Children extends SupportJoinWrapper<T, R, Children>> extends AbstractWrapper<T, R, Children> {

    /**
     * 一对一 构建列表
     */
    @Getter
    protected List<OneToOneSelectBuild> oneToOneSelectBuildList;

    /**
     * 多对多 构建列表
     */
    @Getter
    protected List<ManyToManySelectBuild> manyToManySelectBuildList;

    /**
     * 查询的字段映射列表
     */
    @Getter
    protected List<FieldMapping> fieldMappingList;

    /**
     * 查询字段
     */
    protected List<SharedString> sqlSelect;

    /**
     * 关联表SQL
     */
    protected List<SharedString> joinSql;

    @Getter
    protected boolean masterLogicDelete;

    /**
     * 主表别名
     */
    @Getter
    protected String masterTableAlias;


    /**
     * 是否查询主表全部字段 该条件是在没有指定查询字段的时候生效
     */
    protected boolean notDefaultSelectAll;

    /**
     * 是否添加去重关键字
     */
    protected boolean hasDistinct;


    /**
     * 关键字获取
     */
    @Autowired(required = false)
    private IFuncKeyWord funcKeyWord;

    public Children setFuncKeyWord(IFuncKeyWord funcKeyWord) {
        this.funcKeyWord = funcKeyWord;
        return typedThis;
    }


    public IFuncKeyWord getFuncKeyWord() {
        if (this.funcKeyWord == null) {
            this.funcKeyWord = new DefaultFuncKeyWord();
        }
        return funcKeyWord;
    }

    /**
     * 设置主表逻辑删除
     *
     * @param masterLogicDelete 是否设置逻辑删除，如果为false则主表不加入逻辑删除
     * @return JoinLambdaWrapper<T>
     */
    public Children masterLogicDelete(boolean masterLogicDelete) {
        this.masterLogicDelete = masterLogicDelete;
        return typedThis;
    }

    /**
     * 添加去重函数
     */
    public Children distinct() {
        this.hasDistinct = true;
        return typedThis;
    }


    /**
     * 获取join SQL语句
     *
     * @return 构建好的SQL
     */
    public String getJoinSql() {
        StringBuilder sql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(joinSql)) {
            for (SharedString sharedString : joinSql) {
                sql.append(sharedString.getStringValue()).append(NEWLINE);
            }
        }
        return sql.toString();
    }


    protected void readWrapperInfo(String alias, MergeSegments mergeSegments, String id, boolean isAdd) {
        for (int i = 0; i < mergeSegments.getNormal().size(); i++) {
            ISqlSegment iSqlSegment = mergeSegments.getNormal().get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }

//            新版本中 and 就会是 QueryWrapper 这种类型
            if (iSqlSegment instanceof AbstractWrapper) {
                AbstractWrapper wrapper = (AbstractWrapper) iSqlSegment;
                readWrapperInfo(alias, wrapper.getExpression(), id, false);
                continue;
            }

            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getNormal().remove(iSqlSegment);
                String sql = getAliasAndField(alias, sqlSegment);
                mergeSegments.getNormal().add(i, () -> sql);
            } else {
                // 替换外联表中的参数名字为唯一的
                mergeSegments.getNormal().remove(iSqlSegment);
                String sql = sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id);
                mergeSegments.getNormal().add(i, () -> sql);
            }
        }


        GroupBySegmentList groupBy = mergeSegments.getGroupBy();
        for (int i = 0; i < groupBy.size(); i++) {
            ISqlSegment iSqlSegment = groupBy.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getGroupBy().remove(iSqlSegment);
                mergeSegments.getGroupBy().add(i, () -> getAliasAndField(alias, sqlSegment));
            }
        }

        if (isAdd) {
            expressionAdd(mergeSegments.getGroupBy(), GROUP_BY);
            mergeSegments.getGroupBy().clear();
        }

        HavingSegmentList having = mergeSegments.getHaving();
        for (int i = 0; i < having.size(); i++) {
            ISqlSegment iSqlSegment = having.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (sqlSegment.contains("#{")) {
                // 替换外联表中的参数名字为唯一的
                mergeSegments.getHaving().remove(iSqlSegment);
                mergeSegments.getHaving().add(i, () -> sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME, JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id));
            }
        }

        if (isAdd) {
            expressionAdd(mergeSegments.getHaving(), HAVING);
            mergeSegments.getHaving().clear();
        }


        OrderBySegmentList orderBy = mergeSegments.getOrderBy();
        for (int i = 0; i < orderBy.size(); i++) {
            ISqlSegment iSqlSegment = orderBy.get(i);
            if (iSqlSegment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = iSqlSegment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                mergeSegments.getOrderBy().remove(iSqlSegment);
                mergeSegments.getOrderBy().add(i, () -> getAliasAndField(alias, sqlSegment));
            }
        }

        if (isAdd) {
            expressionAdd(mergeSegments.getOrderBy(), ORDER_BY);
            mergeSegments.getOrderBy().clear();
        }

    }

    private void expressionAdd(AbstractISegmentList list, SqlKeyword sqlKeyword) {
        if (!list.isEmpty()) {
            if (null != sqlKeyword) {
                list.add(0, sqlKeyword);
            }
            ISqlSegment[] iSqlSegmentArrays = new ISqlSegment[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ISqlSegment sqlSegment = list.get(i);
                iSqlSegmentArrays[i] = sqlSegment;
            }
            getExpression().add(iSqlSegmentArrays);
        }
    }

    /**
     * 获取 增加别名后的字段
     *
     * @param fieldName 字段
     * @return 别名 + 字段
     */
    protected String getAliasAndField(String alias, String fieldName) {
        return alias + StringPool.DOT + fieldName;
    }


    /**
     * 获取表对应的class  主要用于 vo dto这种对应实体
     *
     * @param clz 类
     * @return 对应后的类
     */
    protected Class<?> getTableClass(Class<?> clz) {
        return ClassUtils.getTableClass(clz);
    }


    /**
     * 获取实体并关联的
     *
     * @return Class<?>
     */
    protected Class<?> getEntityOrMasterClass() {
        Class<T> aClass = getEntityClass();

        if (null != aClass) {
            return getTableClass(aClass);
        }

        return null;
    }

    public <R> R executeQuery(SFunction<JoinBaseMapper<T>, R> function) {
        SqlSession sqlSession = SqlHelper.sqlSession(getEntityOrMasterClass());
        try {
            return function.apply((JoinBaseMapper<T>) SqlHelper.getMapper(getEntityOrMasterClass(), sqlSession));
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, GlobalConfigUtils.currentSessionFactory(getEntityOrMasterClass()));
        }
    }

    @Override
    protected void initNeed() {
        super.initNeed();
        this.fieldMappingList = Lists.newArrayList();
        this.sqlSelect = Lists.newArrayList();
        this.joinSql = Lists.newArrayList();
        this.masterLogicDelete = true;
        this.notDefaultSelectAll = false;
        this.hasDistinct = false;
        this.oneToOneSelectBuildList = Lists.newArrayList();
        this.manyToManySelectBuildList = Lists.newArrayList();
        final Class<?> entityClass = getEntityOrMasterClass();
    }

    protected void setFieldMappingList(String fieldName, String columns) {
        TableFieldInfo info = getTableFieldInfoByFieldName(fieldName);
        if (null != info && (info.getTypeHandler() != null || info.getJdbcType() != null)) {
            TableFieldInfoExt fieldInfoExt = new TableFieldInfoExt(info);
            fieldInfoExt.setColumn(columns);
            fieldInfoExt.setProperty(fieldName);
            fieldMappingList.add(new FieldMapping(columns, fieldName, fieldInfoExt));
            return;
        }
        fieldMappingList.add(new FieldMapping(columns, fieldName, null));
    }

    protected TableFieldInfo getTableFieldInfoByFieldName(String fieldName, Class<?> clz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clz);
        if (null == tableInfo) {
            return null;
        }
        Optional<TableFieldInfo> fieldInfoOpt = tableInfo.getFieldList().stream().filter(i -> i.getProperty().equals(fieldName)).findFirst();
        return fieldInfoOpt.orElse(null);
    }

    protected TableFieldInfo getTableFieldInfoByFieldName(String fieldName) {
        return getTableFieldInfoByFieldName(fieldName, getEntityClass());
    }


}
