package icu.mhb.mybatisplus.plugln.core.support;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.*;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import icu.mhb.mybatisplus.plugln.annotations.conditions.*;
import icu.mhb.mybatisplus.plugln.annotations.order.AliasMapping;
import icu.mhb.mybatisplus.plugln.annotations.order.OrderBy;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.config.ConfigUtil;
import icu.mhb.mybatisplus.plugln.config.MpjConfig;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.func.IfCompareFun;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.ConditionType;
import icu.mhb.mybatisplus.plugln.keyword.DefaultFuncKeyWord;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import icu.mhb.mybatisplus.plugln.tookit.*;
import icu.mhb.mybatisplus.plugln.tookit.ArrayUtils;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.*;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.*;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.SPACE;
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
@Slf4j
@SuppressWarnings("all")
public abstract class SupportJoinWrapper<T, R, Children extends SupportJoinWrapper<T, R, Children>> extends AbstractWrapper<T, R, Children> implements IfCompareFun<Children, R> {

    protected MpjConfig mpjConfig = ConfigUtil.getConfig();

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
     * 自定义别名map
     */
    protected Map<R, String> customAliasMap = Maps.newHasMap();

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
                sql.append(sharedString.getStringValue()).append(SPACE);
            }
        }
        return sql.toString();
    }


    /**
     * 读取和处理Wrapper信息
     */
    protected int readWrapperInfo(String alias, MergeSegments mergeSegments, String id, boolean isAdd) {
        int conditionCount = 0;

        // 处理普通SQL片段
        conditionCount += processNormalSegments(alias, mergeSegments, id);

        // 处理分组SQL
        processGroupBySegments(alias, mergeSegments);
        if (isAdd) {
            expressionAdd(mergeSegments.getGroupBy(), GROUP_BY);
            mergeSegments.getGroupBy().clear();
        }

        // 处理Having条件
        processHavingSegments(mergeSegments, id);
        if (isAdd) {
            expressionAdd(mergeSegments.getHaving(), HAVING);
            mergeSegments.getHaving().clear();
        }

        // 处理排序SQL
        processOrderBySegments(alias, mergeSegments);
        if (isAdd) {
            expressionAdd(mergeSegments.getOrderBy(), ORDER_BY);
            mergeSegments.getOrderBy().clear();
        }

        return conditionCount;
    }

    /**
     * 处理普通SQL片段
     */
    private int processNormalSegments(String alias, MergeSegments mergeSegments, String id) {
        int conditionCount = 0;
        List<ISqlSegment> normalSegments = mergeSegments.getNormal();

        for (int i = 0; i < normalSegments.size(); i++) {
            ISqlSegment segment = normalSegments.get(i);

            if (segment instanceof SqlKeyword) {
                continue;
            }

            if (segment instanceof AbstractWrapper) {
                conditionCount += processAbstractWrapper(alias, (AbstractWrapper) segment, id);
                continue;
            }

            String sqlSegment = segment.getSqlSegment();
            normalSegments.remove(segment);

            if (!sqlSegment.contains("#{")) {
                conditionCount++;
                String sql = getAliasAndField(alias, sqlSegment);
                normalSegments.add(i, () -> sql);
            } else {
                String sql = sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME,
                        JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id);
                normalSegments.add(i, () -> sql);
            }
        }
        return conditionCount;
    }

    /**
     * 处理AbstractWrapper类型的片段
     */
    private int processAbstractWrapper(String alias, AbstractWrapper wrapper, String id) {
        return readWrapperInfo(alias, wrapper.getExpression(), id, false);
    }

    /**
     * 处理分组SQL片段
     */
    private void processGroupBySegments(String alias, MergeSegments mergeSegments) {
        GroupBySegmentList groupBy = mergeSegments.getGroupBy();
        for (int i = 0; i < groupBy.size(); i++) {
            ISqlSegment segment = groupBy.get(i);
            if (segment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = segment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                groupBy.remove(segment);
                groupBy.add(i, () -> getAliasAndField(alias, sqlSegment));
            }
        }
    }

    /**
     * 处理Having条件片段
     */
    private void processHavingSegments(MergeSegments mergeSegments, String id) {
        HavingSegmentList having = mergeSegments.getHaving();
        for (int i = 0; i < having.size(); i++) {
            ISqlSegment segment = having.get(i);
            if (segment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = segment.getSqlSegment();
            if (sqlSegment.contains("#{")) {
                having.remove(segment);
                having.add(i, () -> sqlSegment.replaceAll(JoinConstant.MP_PARAMS_NAME,
                        JoinConstant.MP_PARAMS_NAME + StringPool.DOT + id));
            }
        }
    }

    /**
     * 处理排序SQL片段
     */
    private void processOrderBySegments(String alias, MergeSegments mergeSegments) {
        OrderBySegmentList orderBy = mergeSegments.getOrderBy();
        for (int i = 0; i < orderBy.size(); i++) {
            ISqlSegment segment = orderBy.get(i);
            if (segment instanceof SqlKeyword) {
                continue;
            }
            String sqlSegment = segment.getSqlSegment();
            if (!sqlSegment.contains("#{")) {
                orderBy.remove(segment);
                orderBy.add(i, () -> getAliasAndField(alias, sqlSegment));
            }
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
        if (StringUtils.isBlank(alias)) {
            return fieldName;
        }
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

    public Children addObjConditions(Object obj) {
        return addObjConditions(obj, null);
    }

    /**
     * 添加实体构建基础条件
     *
     * @param entity 实体对象
     * @return Children
     */
    public Children addObjConditions(Object obj, Class<?> groupClass) {
        Class<?> objClass = obj.getClass();
        List<Field> fieldList = ReflectionKit.getFieldList(obj.getClass());
        for (Field field : fieldList) {
            ConditionAnnoVal conditionAnnoVal = getConditionAnnoVal(field);
            if (conditionAnnoVal == null) {
                continue;
            }
            if (ArrayUtils.isNotEmpty(conditionAnnoVal.getGroup())) {
                if (groupClass != null && !ArrayUtils.contains(conditionAnnoVal.getGroup(), groupClass)) {
                    continue;
                }
            }

            R r = getConditionR(obj.getClass(), field);
            String tableAlias = StringUtils.isNotBlank(conditionAnnoVal.getTableAlias()) ? conditionAnnoVal.getTableAlias() : getMasterTableAlias();
            String columnAlias = StringUtils.isNotBlank(conditionAnnoVal.getMappingColum()) ? conditionAnnoVal.getMappingColum() : StringUtils.camelToUnderline(field.getName());
            customAliasMap.put(r, tableAlias + StringPool.DOT + columnAlias);
            Object fieldValue = ReflectionKit.getFieldValue(obj, field.getName());

            switch (conditionAnnoVal.getType()) {
                case EQ:
                    eqIfNull(r, fieldValue);
                    break;
                case GT:
                    gtIfNull(r, fieldValue);
                    break;
                case GE:
                    geIfNull(r, fieldValue);
                    break;
                case LT:
                    ltIfNull(r, fieldValue);
                    break;
                case LE:
                    leIfNull(r, fieldValue);
                    break;
                case NE:
                    neIfNull(r, fieldValue);
                    break;
                case IN:
                    if (ObjectUtils.isNotEmpty(fieldValue)) {
                        if (fieldValue instanceof Collection) {
                            // 如果是集合类型，直接传递
                            in(r, (Collection<?>) fieldValue);
                        } else if (fieldValue.getClass().isArray()) {
                            // 如果是数组类型，转换为集合
                            in(r, Arrays.asList((Object[]) fieldValue));
                        } else {
                            // 如果是单个值，包装为集合
                            in(r, Collections.singletonList(fieldValue));
                        }
                    }
                    break;
                case NOT_IN:
                    if (ObjectUtils.isNotEmpty(fieldValue)) {
                        if (fieldValue instanceof Collection) {
                            // 如果是集合类型，直接传递
                            notIn(r, (Collection<?>) fieldValue);
                        } else if (fieldValue.getClass().isArray()) {
                            // 如果是数组类型，转换为集合
                            notIn(r, Arrays.asList((Object[]) fieldValue));
                        } else {
                            // 如果是单个值，包装为集合
                            notIn(r, Collections.singletonList(fieldValue));
                        }
                    }
                    break;
                case LIKE:
                    likeIfNull(r, fieldValue);
                    break;
                case LIKE_LEFT:
                    likeLeftIfNull(r, fieldValue);
                    break;
                case LIKE_RIGHT:
                    likeRightIfNull(r, fieldValue);
                    break;
                case NOT_LIKE:
                    notLikeIfNull(r, fieldValue);
                    break;
                case NOT_LIKE_LEFT:
                    notLikeLeftIfNull(r, fieldValue);
                    break;
                case NOT_LIKE_RIGHT:
                    notLikeRightIfNull(r, fieldValue);
                    break;
                case BETWEEN:
                    if (ObjectUtils.isNotEmpty(fieldValue)) {
                        if (fieldValue instanceof Collection) {
                            Collection collection = (Collection) fieldValue;
                            betweenIfNull(r, CollUtil.get(collection, 0), CollUtil.get(collection, 1));
                        } else if (fieldValue.getClass().isArray()) {
                            Object[] vals = (Object[]) fieldValue;
                            // 如果是数组类型，转换为集合
                            betweenIfNull(r, ArrayUtils.get(vals, 0), ArrayUtils.get(vals, 1));
                        } else if (fieldValue instanceof String) {
                            // 如果是String则 用逗号分割
                            String[] vals = ((String) fieldValue).split(",");
                            betweenIfNull(r, ArrayUtils.get(vals, 0), ArrayUtils.get(vals, 1));
                        } else {
                            log.warn("@Between The type of the passed value {} is not supported. Please use Array, List, or a comma-separated string.", fieldValue);
                        }
                    }
                    break;
                case ORDER_BY:
                    OrderBy orderBy = field.getAnnotation(OrderBy.class);

                    AliasMappingAnnotUtil.parsing(objClass, field, orderBy.aliasMapping());

                    if (ObjectUtils.isNotEmpty(fieldValue)) {
                        if (fieldValue instanceof Collection) {
                            Collection<Object> collection = (Collection) fieldValue;
                            collection.stream()
                                    .filter(ObjectUtils::isNotEmpty)
                                    .map(i -> String.valueOf(i))
                                    .forEach(is -> {
                                        runOrderBy(field, is, objClass, tableAlias);
                                    });
                        } else if (fieldValue.getClass().isArray()) {
                            Object[] vals = (Object[]) fieldValue;
                            Arrays.stream(vals)
                                    .filter(ObjectUtils::isNotEmpty)
                                    .map(i -> String.valueOf(i))
                                    .forEach(is -> {
                                        runOrderBy(field, is, objClass, tableAlias);
                                    });
                        } else if (fieldValue instanceof String) {
                            runOrderBy(field, String.valueOf(fieldValue), objClass, tableAlias);
                        } else {
                            log.warn("@OrderBy The type of the passed value {} is not supported. Please use Array, List, or a comma-separated string.", fieldValue);
                        }
                    }
                    break;
            }
        }

        return typedThis;
    }

    private void runOrderBy(Field field, String is, Class<?> objClass, String tableAlias) {
        String[] split = is.split(StringPool.COMMA);
        if (ArrayUtils.len(split) != 2) {
            return;
        }
        String fieldName = split[0];
        String orderType = split[1];
        R conditionR = getConditionR(objClass, field);
        AliasMapping aliasMapping = AliasMappingAnnotUtil.get(objClass, field, fieldName);
        String tableAlias_ = null != aliasMapping ? aliasMapping.tableAlias() : tableAlias;
        String columnAlias_ = null != aliasMapping ? aliasMapping.columnName() : StringUtils.camelToUnderline(fieldName);
        customAliasMap.put(conditionR, tableAlias_ + StringPool.DOT + columnAlias_);

        if (ASC.getSqlSegment().equals(orderType.toUpperCase())) {
            orderByAsc(conditionR);
        } else if (DESC.getSqlSegment().equals(orderType.toUpperCase())) {
            orderByDesc(conditionR);
        }
    }

    private ConditionAnnoVal getConditionAnnoVal(Field field) {
        // 等于条件
        if (field.isAnnotationPresent(Eq.class)) {
            Eq annotation = field.getAnnotation(Eq.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.EQ);
        }

        // 大于条件
        if (field.isAnnotationPresent(Gt.class)) {
            Gt annotation = field.getAnnotation(Gt.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.GT);
        }

        // 大于等于条件
        if (field.isAnnotationPresent(Ge.class)) {
            Ge annotation = field.getAnnotation(Ge.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.GE);
        }

        // 小于条件
        if (field.isAnnotationPresent(Lt.class)) {
            Lt annotation = field.getAnnotation(Lt.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.LT);
        }

        // 小于等于条件
        if (field.isAnnotationPresent(Le.class)) {
            Le annotation = field.getAnnotation(Le.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.LE);
        }

        // 不等于条件
        if (field.isAnnotationPresent(Ne.class)) {
            Ne annotation = field.getAnnotation(Ne.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.NE);
        }

        // IN条件
        if (field.isAnnotationPresent(In.class)) {
            In annotation = field.getAnnotation(In.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.IN);
        }

        // Not IN条件
        if (field.isAnnotationPresent(NotIn.class)) {
            NotIn annotation = field.getAnnotation(NotIn.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.NOT_IN);
        }

        // LIKE条件
        if (field.isAnnotationPresent(Like.class)) {
            Like annotation = field.getAnnotation(Like.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.LIKE);
        }

        // 左LIKE条件
        if (field.isAnnotationPresent(LikeLeft.class)) {
            LikeLeft annotation = field.getAnnotation(LikeLeft.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.LIKE_LEFT);
        }

        // 右LIKE条件
        if (field.isAnnotationPresent(LikeRight.class)) {
            LikeRight annotation = field.getAnnotation(LikeRight.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.LIKE_RIGHT);
        }

        // NOT LIKE条件
        if (field.isAnnotationPresent(NotLike.class)) {
            NotLike annotation = field.getAnnotation(NotLike.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.NOT_LIKE);
        }

        // NOT LIKE LEFT条件
        if (field.isAnnotationPresent(NotLikeLeft.class)) {
            NotLikeLeft annotation = field.getAnnotation(NotLikeLeft.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.NOT_LIKE_LEFT);
        }

        // NOT LIKE RIGHT条件
        if (field.isAnnotationPresent(NotLikeRight.class)) {
            NotLikeRight annotation = field.getAnnotation(NotLikeRight.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.NOT_LIKE_RIGHT);
        }

        // Between 条件
        if (field.isAnnotationPresent(Between.class)) {
            Between annotation = field.getAnnotation(Between.class);
            return new ConditionAnnoVal(annotation.tableAlias(), annotation.mappingColum(), annotation.group(), ConditionType.BETWEEN);
        }

        // OrderBy 条件
        if (field.isAnnotationPresent(OrderBy.class)) {
            return new ConditionAnnoVal(null, null, null, ConditionType.ORDER_BY);
        }

        return null;
    }

    /**
     * 只为避开类型检查
     */
    public abstract R getConditionR(Class<?> entityClass, Field field);

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
