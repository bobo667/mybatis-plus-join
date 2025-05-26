package icu.mhb.mybatisplus.plugln.core.str;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.core.str.support.SupportJoinStrQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.str.func.JoinStrQueryFunc;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.tookit.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;

/**
 * String类型的Join构造器
 *
 * @author mahuibo
 * @Title: JoinStrQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/27
 */
@SuppressWarnings("all")
public class JoinStrQueryWrapper<T> extends SupportJoinStrQueryWrapper<T, JoinStrQueryWrapper<T>>
        implements JoinStrQueryFunc<T, String, JoinStrQueryWrapper<T>> {

    /**
     * 判断SQL是否缓存过
     */
    private boolean sqlCacheFlag;

    /**
     * SQL缓存
     */
    private SharedString sqlCache = new SharedString();

    /**
     * 查询字段是否缓存过
     */
    private boolean sqlSelectFlag;

    /**
     * 查询字段缓存
     */
    private SharedString sqlSelectCache = new SharedString();

    @Override
    public String getSqlSelect() {
        if (sqlSelectFlag) {
            return sqlSelectCache.getStringValue();
        }

        if (CollectionUtils.isEmpty(sqlSelect) && !this.notDefaultSelectAll) {
            selectAll();
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.stream()
                .map(SharedString::getStringValue)
                .distinct()
                .collect(Collectors.joining(",")));

        sqlSelectFlag = true;
        if (hasDistinct) {
            stringValue.insert(0, getFuncKeyWord().distinct() + StringPool.SPACE);
        }
        String selectSql = stringValue.toString();

        sqlSelectCache.setStringValue(selectSql);
        return selectSql;
    }

    @Override
    protected JoinStrQueryWrapper<T> instance() {
        return new JoinStrQueryWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }

    /**
     * 构造函数
     */
    public JoinStrQueryWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 构造函数
     */
    public JoinStrQueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    /**
     * 构造函数
     *
     * @param entity 实体
     * @param alias  别名
     */
    public JoinStrQueryWrapper(T entity, String alias) {
        super.setEntity(entity);
        super.initNeed();
        this.setAlias(alias);
    }

    /**
     * 构造函数
     *
     * @param entityClass 实体类
     * @param alias       别名
     */
    public JoinStrQueryWrapper(Class<T> entityClass, String alias) {
        super.setEntityClass(entityClass);
        super.initNeed();
        this.setAlias(alias);
    }

    /**
     * 内部构造函数
     */
    JoinStrQueryWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                        Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                        SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.sqlSelect = sqlSelect;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    @Override
    public JoinStrQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        throw Exceptions.mpje("请不要调用该方法！请使用 select(Predicate<TableFieldInfo> predicate)");
    }

    @Override
    public String getJoinSql() {
        if (CollectionUtils.isEmpty(joinSqlMapping)) {
            return "";
        }
        return joinSqlMapping.values().stream()
                .map(SharedString::getStringValue)
                .collect(Collectors.joining(Constants.NEWLINE));
    }

    @Override
    public JoinStrQueryWrapper<T> select(Predicate<TableFieldInfo> predicate) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getEntityOrMasterClass());
        if (tableInfo != null) {
            TableInfoExt tableInfoExt = new TableInfoExt(tableInfo);
            List<String> columns = tableInfoExt.chooseSelect(predicate, getAlias());
            List<SharedString> strings = Lists.changeList(columns, SharedString::new);
            this.sqlSelect.addAll(strings);
        }
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> select(String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        // 为没有点号的字段添加主表别名前缀
        List<String> prefixedColumns = new java.util.ArrayList<>();
        for (String column : columns) {
            prefixedColumns.add(checkAndHandleColumn(column));
        }

        List<SharedString> list = Lists.changeList(prefixedColumns, SharedString::new);
        this.sqlSelect.addAll(list);
        return typedThis;
    }

    /**
     * 带别名前缀的查询字段
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias   表别名
     * @param columns 字段列表
     * @return this
     */
    public JoinStrQueryWrapper<T> selectWithAlias(String alias, String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }

        List<String> prefixedColumns = new java.util.ArrayList<>();
        for (String column : columns) {
            prefixedColumns.add(handleColumnPrefix(alias, column));
        }

        List<SharedString> list = Lists.changeList(prefixedColumns, SharedString::new);
        this.sqlSelect.addAll(list);
        return typedThis;
    }

    public JoinStrQueryWrapper<T> selectAs(String column, String alias) {
        String columnWithAlias = String.format(SqlExcerpt.COLUMNS_AS.getSql(), column, alias);
        select(columnWithAlias);
        return typedThis;
    }

    public JoinStrQueryWrapper<T> orderBySql(String orderBySql) {
        maybeDo(true, () -> appendSqlSegments(ORDER_BY, columnToSqlSegment(orderBySql)));
        return typedThis;
    }

    public JoinStrQueryWrapper<T> selectSub(String selectSql, String alias) {
        String subQuery = String.format("(%s) %s", selectSql, alias);
        SharedString sharedString = new SharedString(subQuery);
        this.sqlSelect.add(sharedString);
        return typedThis;
    }

    public JoinStrQueryWrapper<T> alias(String alias) {
        this.setAlias(alias);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> leftJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.LEFT_JOIN);
    }

    @Override
    public JoinStrQueryWrapper<T> leftJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<JoinStrQueryWrapper<T>> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.LEFT_JOIN, consumer);
    }

    @Override
    public JoinStrQueryWrapper<T> rightJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.RIGHT_JOIN);
    }

    @Override
    public JoinStrQueryWrapper<T> rightJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<JoinStrQueryWrapper<T>> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.RIGHT_JOIN, consumer);
    }


    @Override
    public JoinStrQueryWrapper<T> innerJoin(String joinTable, String joinTableField, String masterTableField, String alias) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.INNER_JOIN);
    }

    @Override
    public JoinStrQueryWrapper<T> innerJoin(String joinTable, String joinTableField, String masterTableField, String alias, Consumer<JoinStrQueryWrapper<T>> consumer) {
        return join(joinTable, joinTableField, masterTableField, alias, SqlExcerpt.INNER_JOIN, consumer);
    }

    @Override
    public JoinStrQueryWrapper<T> join(String joinTableField, String masterTableField, SqlExcerpt joinType) {
        buildJoinSql(joinTableField, masterTableField, joinType);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType) {
        buildJoinSql(joinTable, joinTableField, masterTableField, alias, joinType);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> join(String joinTable, String joinTableField, String masterTableField, String alias, SqlExcerpt joinType, Consumer<JoinStrQueryWrapper<T>> consumer) {
        buildJoinSql(joinTable, joinTableField, masterTableField, alias, joinType, consumer);
        return typedThis;
    }

    /**
     * 检查并处理字段名
     * 如果字段名不包含点号，则自动添加主表别名前缀
     *
     * @param column 字段名
     * @return 处理后的字段名
     */
    private String checkAndHandleColumn(String column) {
        if (StringUtils.isNotBlank(column) && !column.contains(StringPool.DOT)) {
            return handleColumnPrefix(masterTableAlias, column);
        }
        return column;
    }

    // 以下是解决方法冲突的实现

    @Override
    public JoinStrQueryWrapper<T> eq(String column, Object val) {
        super.eq(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> ne(String column, Object val) {
        super.ne(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> like(String column, Object val) {
        super.like(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> notLike(String column, Object val) {
        super.notLike(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> groupBy(String column) {
        super.groupBy(true, checkAndHandleColumn(column));
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> having(String havingSql, Object... params) {
        super.having(true, havingSql, params);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> notBetween(String column, Object val1, Object val2) {
        super.notBetween(true, checkAndHandleColumn(column), val1, val2);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> in(String column, Collection<?> coll) {
        super.in(true, checkAndHandleColumn(column), coll);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> notIn(String column, Collection<?> coll) {
        super.notIn(true, checkAndHandleColumn(column), coll);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> isNull(String column) {
        super.isNull(true, checkAndHandleColumn(column));
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> isNotNull(String column) {
        super.isNotNull(true, checkAndHandleColumn(column));
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> between(String column, Object val1, Object val2) {
        super.between(true, checkAndHandleColumn(column), val1, val2);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> gt(String column, Object val) {
        super.gt(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> ge(String column, Object val) {
        super.ge(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> lt(String column, Object val) {
        super.lt(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> le(String column, Object val) {
        super.le(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> likeLeft(String column, Object val) {
        super.likeLeft(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> likeRight(String column, Object val) {
        super.likeRight(true, checkAndHandleColumn(column), val);
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> orderByAsc(String column) {
        maybeDo(true, () -> {
            ISqlSegment[] segments = new ISqlSegment[2];
            segments[0] = ORDER_BY;
            segments[1] = () -> columnToString(checkAndHandleColumn(column)) + " ASC";
            appendSqlSegments(segments);
        });
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> orderByDesc(String column) {
        maybeDo(true, () -> {
            ISqlSegment[] segments = new ISqlSegment[2];
            segments[0] = ORDER_BY;
            segments[1] = () -> columnToString(checkAndHandleColumn(column)) + " DESC";
            appendSqlSegments(segments);
        });
        return typedThis;
    }

    @Override
    public JoinStrQueryWrapper<T> joinAnd(String alias, Consumer<JoinStrQueryWrapper<T>> consumer) {
        super.joinAnd(true, alias, consumer);
        return typedThis;
    }

    /**
     * 带别名前缀的等于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> eq(String alias, String column, Object val) {
        super.eq(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的不等于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> ne(String alias, String column, Object val) {
        super.ne(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的LIKE条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> like(String alias, String column, Object val) {
        super.like(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的NOT LIKE条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> notLike(String alias, String column, Object val) {
        super.notLike(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的分组
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return this
     */
    public JoinStrQueryWrapper<T> groupBy(String alias, String column) {
        super.groupBy(true, handleColumnPrefix(alias, column));
        return typedThis;
    }

    /**
     * 带别名前缀的NOT BETWEEN条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val1   值1
     * @param val2   值2
     * @return this
     */
    public JoinStrQueryWrapper<T> notBetween(String alias, String column, Object val1, Object val2) {
        super.notBetween(true, handleColumnPrefix(alias, column), val1, val2);
        return typedThis;
    }

    /**
     * 带别名前缀的IN条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param coll   集合
     * @return this
     */
    public JoinStrQueryWrapper<T> in(String alias, String column, Collection<?> coll) {
        super.in(true, handleColumnPrefix(alias, column), coll);
        return typedThis;
    }

    /**
     * 带别名前缀的NOT IN条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param coll   集合
     * @return this
     */
    public JoinStrQueryWrapper<T> notIn(String alias, String column, Collection<?> coll) {
        super.notIn(true, handleColumnPrefix(alias, column), coll);
        return typedThis;
    }

    /**
     * 带别名前缀的IS NULL条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return this
     */
    public JoinStrQueryWrapper<T> isNull(String alias, String column) {
        super.isNull(true, handleColumnPrefix(alias, column));
        return typedThis;
    }

    /**
     * 带别名前缀的IS NOT NULL条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return this
     */
    public JoinStrQueryWrapper<T> isNotNull(String alias, String column) {
        super.isNotNull(true, handleColumnPrefix(alias, column));
        return typedThis;
    }

    /**
     * 带别名前缀的BETWEEN条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val1   值1
     * @param val2   值2
     * @return this
     */
    public JoinStrQueryWrapper<T> between(String alias, String column, Object val1, Object val2) {
        super.between(true, handleColumnPrefix(alias, column), val1, val2);
        return typedThis;
    }

    /**
     * 带别名前缀的大于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> gt(String alias, String column, Object val) {
        super.gt(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的大于等于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> ge(String alias, String column, Object val) {
        super.ge(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的小于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> lt(String alias, String column, Object val) {
        super.lt(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的小于等于条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> le(String alias, String column, Object val) {
        super.le(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的LIKE LEFT条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> likeLeft(String alias, String column, Object val) {
        super.likeLeft(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的LIKE RIGHT条件
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @param val    值
     * @return this
     */
    public JoinStrQueryWrapper<T> likeRight(String alias, String column, Object val) {
        super.likeRight(true, handleColumnPrefix(alias, column), val);
        return typedThis;
    }

    /**
     * 带别名前缀的升序排序
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return this
     */
    public JoinStrQueryWrapper<T> orderByAsc(String alias, String column) {
        String prefixedColumn = handleColumnPrefix(alias, column);
        maybeDo(true, () -> {
            ISqlSegment[] segments = new ISqlSegment[2];
            segments[0] = ORDER_BY;
            segments[1] = () -> columnToString(prefixedColumn) + " ASC";
            appendSqlSegments(segments);
        });
        return typedThis;
    }

    /**
     * 带别名前缀的降序排序
     * 如果字段名不包含点号，则自动添加别名前缀
     *
     * @param alias  表别名
     * @param column 字段名
     * @return this
     */
    public JoinStrQueryWrapper<T> orderByDesc(String alias, String column) {
        String prefixedColumn = handleColumnPrefix(alias, column);
        maybeDo(true, () -> {
            ISqlSegment[] segments = new ISqlSegment[2];
            segments[0] = ORDER_BY;
            segments[1] = () -> columnToString(prefixedColumn) + " DESC";
            appendSqlSegments(segments);
        });
        return typedThis;
    }
}
