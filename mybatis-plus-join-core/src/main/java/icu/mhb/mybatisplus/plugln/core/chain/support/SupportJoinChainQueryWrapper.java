package icu.mhb.mybatisplus.plugln.core.chain.support;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainCompareFunc;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainFunc;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainMethodFunc;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinWrapper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.tookit.StringUtils;
import icu.mhb.mybatisplus.plugln.tookit.*;
import icu.mhb.mybatisplus.plugln.tookit.fun.FunComm;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.SqlKeyword.ORDER_BY;
import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;
import static icu.mhb.mybatisplus.plugln.constant.StringPool.*;

/**
 * @author mahuibo
 * @Title: SupportJoinChainQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SuppressWarnings("all")
public abstract class SupportJoinChainQueryWrapper<T, Children extends SupportJoinChainQueryWrapper<T, Children>>
        extends SupportJoinWrapper<T, String, Children> implements Query<Children, T, String>, JoinChainCompareFunc<Children, BaseChainModel<?>>, JoinChainMethodFunc<T, Children>, JoinChainFunc<Children, BaseChainModel<?>> {

    /**
     * 主表 chain model
     */
    protected BaseChainModel<?> masterChainModel;


    /**
     * join sql和chain 别名映射
     */
    protected Map<String, SharedString> joinSqlMapping;

    /**
     * 初始化 模型中的实体查询条件
     *
     * @param models 模型
     */
    public Children initEntityCondition(BaseChainModel<?>... models) {
        for (BaseChainModel<?> chainModel : models) {
            Object entity = chainModel.getEntity();
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }

            Class<?> modelClass = chainModel.getModelClass();
            Map<String, Field> fieldMap = ReflectionKit.getFieldMap(modelClass);
            fieldMap.forEach((name, field) -> {
                Object val = ReflectionKit.getFieldValue(entity, name);
                if (ObjectUtils.isEmpty(val)) {
                    return;
                }
                TableFieldInfo tableFieldInfo = getTableFieldInfoByFieldName(name, modelClass);
                // 说明不是数据库字段
                if (null == tableFieldInfo) {
                    return;
                }

                TableField tableField = field.getAnnotation(TableField.class);
                String conditionSql = null != tableField ? tableField.condition() : EMPTY;
                if (StringUtils.isBlank(conditionSql)) {
                    conditionSql = SqlCondition.EQUAL;
                }
                String param = onlyFormatParam(val);
                String sql = String.format(conditionSql, getAliasAndField(chainModel.getAlias(), tableFieldInfo.getColumn()), param);
                super.apply(sql);
            });
        }
        return typedThis;
    }

    /**
     * 根据传入的chain类型来构建全部的字段
     */
    public <P> Children manyToManySelect(SFunction<P, ?> mappingColumn, BaseChainModel<?> model) {
        return manyToManySelect(mappingColumn, () -> ChainUtil.initAllChainFieldData(model));
    }

    /**
     * 多对多查询
     *
     * @param column   映射列
     * @param provider chain 提供數據
     */
    @SneakyThrows
    public <P> Children manyToManySelect(SFunction<P, ?> mappingColumn, Provider<BaseChainModel<?>> provider) {
        Class<?> manyToManyClass = provider.run().getModelClass();
        List<FieldMapping> belongsColumns = buildField(provider);

        LambdaMeta lambdaMeta = LambdaUtils.extract(mappingColumn);
        // 获取字段名
        String fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());

        Type[] actualTypeArguments = ((ParameterizedType) lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName).getGenericType()).getActualTypeArguments();

        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
            manyToManyClass = (Class<?>) actualTypeArguments[0];
        }

        ManyToManySelectBuild manySelectBuild = ManyToManySelectBuild
                .builder()
                .manyToManyField(fieldName)
                .manyToManyPropertyType(lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName).getType())
                .belongsColumns(belongsColumns)
                .manyToManyClass(manyToManyClass)
                .build();

        this.manyToManySelectBuildList.add(manySelectBuild);

        return selectByFieldMapping(belongsColumns, false);
    }


    /**
     * 根据传入的chain类型来构建全部的字段
     */
    public <P> Children oneToOneSelect(SFunction<P, ?> mappingColumn, BaseChainModel<?> model) {
        return oneToOneSelect(mappingColumn, () -> ChainUtil.initAllChainFieldData(model));
    }

    /**
     * 一对一查询
     *
     * @param column   映射列
     * @param provider chain 提供數據
     */
    @SneakyThrows
    public <P> Children oneToOneSelect(SFunction<P, ?> mappingColumn, Provider<BaseChainModel<?>> provider) {

        List<FieldMapping> belongsColumns = buildField(provider);

        LambdaMeta lambdaMeta = LambdaUtils.extract(mappingColumn);
        // 获取字段名
        String fieldName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());

        OneToOneSelectBuild oneToOneSelectBuild = OneToOneSelectBuild
                .builder()
                .oneToOneField(fieldName)
                .belongsColumns(belongsColumns)
                .oneToOneClass(lambdaMeta.getInstantiatedClass().getDeclaredField(fieldName).getType())
                .build();

        oneToOneSelectBuildList.add(oneToOneSelectBuild);

        return selectByFieldMapping(belongsColumns, false);
    }

    private <P> List<FieldMapping> buildField(Provider<BaseChainModel<?>> provider) {
        return useChainModel(provider, (model) -> {
            return model.getChainFieldDataList().stream()
                    .map(i -> {
                        String rawColumn = i.getColumn();
                        if (ObjectUtils.isEmpty(i.getVal())) {
                            i.setColumn(model.getAlias() + UNDERSCORE + rawColumn);
                        }
                        FieldMapping fieldMapping = getFieldMapping(i);
                        fieldMapping.setRawColumn(rawColumn);
                        return fieldMapping;
                    }).collect(Collectors.toList());
        });
    }


    @Override
    public Children in(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            Exceptions.throwMpje(!(data.getVal() instanceof Vals), "请使用 icu.mhb.mybatisplus.plugln.entity.Vals 类作为in方法 chain的入参");
            Vals vals = (Vals) data.getVal();
            super.in(flag, ChainUtil.getAliasColum(data), vals.getValList());
        });
    }

    @Override
    public Children notIn(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            Exceptions.throwMpje(!(data.getVal() instanceof Vals), "请使用 icu.mhb.mybatisplus.plugln.entity.Vals 类作为notIn方法 chain的入参");
            Vals vals = (Vals) data.getVal();
            super.notIn(flag, ChainUtil.getAliasColum(data), vals.getValList());
        });
    }

    @Override
    public Children isNull(boolean condition, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, false, provider, (flag, data) -> {
            super.isNull(flag, ChainUtil.getAliasColum(data));
        });
    }

    @Override
    public Children isNotNull(boolean condition, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, false, provider, (flag, data) -> {
            super.isNotNull(flag, ChainUtil.getAliasColum(data));
        });
    }

    @Override
    public Children groupBy(boolean condition, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, false, provider, (flag, data) -> {
            super.groupBy(flag, ChainUtil.getAliasColum(data));
        });
    }

    @Override
    public Children orderBySql(boolean condition, String sql) {
        return maybeDo(condition, () -> appendSqlSegments(ORDER_BY, columnToSqlSegment(sql)));
    }

    @Override
    public Children orderBy(boolean condition, boolean isAsc, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, false, provider, (flag, data) -> {
            super.orderBy(flag, isAsc, ChainUtil.getAliasColum(data));
        });
    }


    @Override
    public Children ne(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.ne(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children gt(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.gt(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children ge(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.ge(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children lt(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.lt(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children le(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.le(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children between(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            Exceptions.throwMpje(!(data.getVal() instanceof Vals), "请使用 icu.mhb.mybatisplus.plugln.entity.Vals 类作为between方法 chain入参");
            Vals vals = (Vals) data.getVal();
            super.between(flag, ChainUtil.getAliasColum(data), vals.get(0), vals.get(1));
        });
    }

    @Override
    public Children notBetween(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            Exceptions.throwMpje(!(data.getVal() instanceof Vals), "请使用 icu.mhb.mybatisplus.plugln.entity.Vals 类作为notBetween方法 chain入参");
            Vals vals = (Vals) data.getVal();
            super.notBetween(flag, ChainUtil.getAliasColum(data), vals.get(0), vals.get(1));
        });
    }

    @Override
    public Children like(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.like(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children notLike(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.notLike(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children notLikeLeft(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.notLikeLeft(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children notLikeRight(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.notLikeRight(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children likeLeft(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.likeLeft(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children likeRight(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.likeRight(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    @Override
    public Children eq(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider) {
        return addConditon(condition, ifNull, provider, (flag, data) -> {
            super.eq(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     */
    protected Children likeValue(boolean condition, SqlKeyword keyword, String column, Object val, SqlLike sqlLike) {
        return preChangeParam(val, (isSafetyParam, newVal) -> {
            maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), keyword,
                    () -> formatParam(null, SqlUtils.concatLike(newVal, sqlLike), isSafetyParam)));
        });
    }

    @Override
    protected Children addCondition(boolean condition, String column, SqlKeyword sqlKeyword, Object val) {
        return preChangeParam(val, (isSafetyParam, newVal) -> {
            maybeDo(condition, () -> appendSqlSegments(columnToSqlSegment(column), sqlKeyword,
                    () -> formatParam(null, newVal, isSafetyParam)));
        });
    }

    protected Children preChangeParam(Object val, BiConsumer<Boolean, Object> consumer) {
        Exceptions.throwMpje((val instanceof BaseChainModel), "入参不能直接是chain模型，如果是把字段作为值，请使用 _ 开头的方法");
        boolean isSafetyParam = isSafetyParam(val);
        val = preFieldDataVal(val);
        consumer.accept(isSafetyParam, val);
        return typedThis;
    }


    /**
     * 处理入参
     *
     * @param mapping 例如: "javaType=int,jdbcType=NUMERIC,typeHandler=xxx.xxx.MyTypeHandler" 这种
     * @param param   参数
     * @return value
     */
    protected String formatParam(String mapping, Object param, boolean isSafetyParam) {
        String paramStr = onlyFormatParam(param);
        return isSafetyParam ? SqlScriptUtils.safeParam(paramStr, mapping) : param.toString();
    }

    /**
     * 只是格式化了参数和增加入参，不增加括号
     *
     * @param param
     * @return
     */
    protected String onlyFormatParam(Object param) {
        final String genParamName = Constants.WRAPPER_PARAM + paramNameSeq.incrementAndGet();
        final String paramStr = getParamAlias() + Constants.WRAPPER_PARAM_MIDDLE + genParamName;
        paramNameValuePairs.put(genParamName, param);
        return paramStr;
    }

    @Override
    public Children joinAnd(boolean condition, BaseChainModel<?> model, Consumer<Children> consumer) {
        if (!condition) {
            return typedThis;
        }

        Exceptions.throwMpje(!joinSqlMapping.containsKey(model.getAlias()), "chain[%s] 模型,未找到对应的Join语句", model.getModelClass().getSimpleName());

        Children children = instance();
        children.paramNameSeq.set(this.paramNameSeq.intValue());

        consumer.accept(children);

        String conditonSql = children.getCustomSqlSegment();
        if (StringUtils.isNotBlank(conditonSql)) {
            conditonSql = conditonSql.replaceFirst(Constants.WHERE, SPACE);
            this.paramNameSeq.set(children.paramNameSeq.intValue());
            this.paramNameValuePairs.putAll(children.paramNameValuePairs);

            SharedString joinSql = joinSqlMapping.get(model.getAlias());
            joinSql.setStringValue(joinSql.getStringValue() + SPACE + AND + conditonSql);

            joinSqlMapping.put(model.getAlias(), joinSql);
        }

        return typedThis;
    }

    @Override
    public Children join(ChainFieldData leftField, ChainFieldData rightField, boolean logicDeleteIsApplyJoin, SqlExcerpt joinType) {

        Exceptions.throwMpje(leftField == null || rightField == null, "join的字段不能为空");

        SharedString sharedString = SharedString.emptyString();

        StringBuilder sb = new StringBuilder(String.format(joinType.getSql(), leftField.getTableName(), leftField.getAlias(), leftField.getAlias(), leftField.getColumn(), rightField.getAlias(), rightField.getColumn()));

        TableInfo tableInfo = TableInfoHelper.getTableInfo(leftField.getModelClass());
        if (null != tableInfo && logicDeleteIsApplyJoin) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            String logicDeleteSql = infoExt.getLogicDeleteSql(true, true, leftField.getAlias());
            sb.append(Constants.SPACE).append(Constants.NEWLINE).append(logicDeleteSql);
        }
        sharedString.setStringValue(sb.toString());

        joinSqlMapping.put(leftField.getAlias(), sharedString);

        return typedThis;
    }


    /**
     * 用户不要使用这个方法
     */
    @Override
    public Children select(String... columns) {
        if (ArrayUtils.isEmpty(columns)) {
            return typedThis;
        }
        select(Lists.newArrayList(columns));

        return typedThis;
    }

    public Children select(BaseChainModel<?> model, Predicate<TableFieldInfo> predicate) {
        ChainUtil.initAllChainFieldData(model, predicate);

        selectAs(() -> model);

        return typedThis;
    }

    protected void select(List<String> selectList) {
        if (CollectionUtils.isEmpty(selectList)) {
            return;
        }
        List<SharedString> list = Lists.changeList(selectList, SharedString::new);
        sqlSelect.addAll(list);
    }


    public Children selectAll(BaseChainModel<?> model) {
        ChainUtil.initAllChainFieldData(model);

        selectAs(() -> model);

        return typedThis;
    }


    public Children selectAs(String column, String alias) {
        column = String.format(SqlExcerpt.COLUMNS_AS.getSql(), column, alias);
        select(column);
        setFieldMappingList(alias, alias);
        return typedThis;
    }

    public <F> Children selectAs(String column, SFunction<F, ?> aliasFunc) {
        String alias = Lambdas.getSfuncName(aliasFunc);
        return selectAs(column, alias);
    }

    public Children selectAs(Provider<BaseChainModel<?>> provider) {
        return selectAs(provider, true);
    }


    protected Children selectAs(Provider<BaseChainModel<?>> provider, boolean saveMapping) {
        useChainModel(provider, (model) -> {
            List<ChainFieldData> fieldDataList = model.getChainFieldDataList();

            List<String> selectList = fieldDataList.stream()
                    .map(chainFieldData -> {
                        Object alias = chainFieldData.getVal();
                        String property = chainFieldData.getProperty();
                        String returnColumn = getAliasAndField(chainFieldData.getAlias(), chainFieldData.getColumn());

                        if (ObjectUtils.isNotEmpty(alias)) {
                            property = alias.toString();
                            returnColumn = String.format(SqlExcerpt.COLUMNS_AS.getSql(), returnColumn, property);
                        }

                        FunComm.isTrue(saveMapping, () -> setFieldMapping(chainFieldData));

                        return returnColumn;
                    }).collect(Collectors.toList());

            select(selectList);
        });

        return typedThis;
    }

    /**
     * 根据字段映射进行查询
     * <p>
     * 内部使用
     *
     * @param fieldMappingList 映射list
     * @param saveMapping
     * @return
     */
    protected Children selectByFieldMapping(List<FieldMapping> fieldMappingList, boolean saveMapping) {
        List<String> selectList = fieldMappingList.stream()
                .map(i -> {
                    String column = StringUtils.isBlank(i.getRawColumn()) ? i.getColumn() : i.getRawColumn();
                    String returnColumn = getAliasAndField(i.getTableAlias(), column);

                    returnColumn = String.format(SqlExcerpt.COLUMNS_AS.getSql(), returnColumn, i.getColumn());

                    FunComm.isTrue(saveMapping, () -> fieldMappingList.add(i));

                    return returnColumn;
                }).collect(Collectors.toList());
        select(selectList);
        return typedThis;
    }


    public Children addConditon(boolean condition, boolean ifNull, Provider<BaseChainModel<?>> provider, BiConsumer<Boolean, ChainFieldData> consumer) {
        if (condition) {
            useChainModel(provider, (model) -> {
                for (ChainFieldData fieldData : model.getChainFieldDataList()) {
                    consumer.accept(ifNull ? ObjectUtils.isNotEmpty(fieldData.getVal()) : true, fieldData);
                }
            });
        }
        return typedThis;
    }

    /**
     * 预处理字段数据值
     *
     * @param fieldData
     */
    private Object preFieldDataVal(Object data) {
        // 说明字段比对字段
        if (data instanceof ChainFieldData) {
            ChainFieldData chainModel = (ChainFieldData) data;
            String val = ChainUtil.getAliasColum(chainModel);
            return val;
        }
        return data;
    }

    private boolean isSafetyParam(Object val) {
        if (val instanceof ChainFieldData) {
            return false;
        }
        return true;
    }

    protected void useChainModel(Provider<BaseChainModel<?>> provider, Consumer<BaseChainModel<?>> consumer) {
        BaseChainModel<?> model = provider.run();
        consumer.accept(model);
        model.end();
    }

    protected <R> R useChainModel(Provider<BaseChainModel<?>> provider, Function<BaseChainModel<?>, R> consumer) {
        BaseChainModel<?> model = provider.run();
        R returnR = consumer.apply(model);
        model.end();
        return returnR;
    }

    @Override
    public String getJoinSql() {
        return super.getJoinSql();
    }


    @Override
    protected String columnToString(String column) {
        return column;
    }

    /**
     * 转换查询Wrapper 会把 查询条件，group，order by，having转换来
     * 注意该方法无法给 多个入参添加别名，例如 orderByDesc("id","id2")
     * 这种别名就会添加错误
     *
     * @param queryWrapper
     * @return
     */
    public Children changeQueryWrapper(BaseChainModel model, AbstractWrapper queryWrapper) {
        MergeSegments mergeSegments = queryWrapper.getExpression();

        String id = IdUtil.getSimpleUUID();
        final Children instance = instance();
        readWrapperInfo(model.getAlias(), mergeSegments, id, true);
        appendSqlSegments(APPLY, queryWrapper);

        getParamNameValuePairs().put(id, queryWrapper.getParamNameValuePairs());

        return typedThis;
    }


    protected String getAliasAndField(BaseChainModel model, String field) {
        return model.getAlias() + StringPool.DOT + field;
    }


    @Override
    protected void initNeed() {
        super.initNeed();
        this.joinSqlMapping = new HashMap<>();
    }

    protected FieldMapping getFieldMapping(ChainFieldData chainFieldData) {
        Object alias = chainFieldData.getVal();
        String property = chainFieldData.getProperty();
        String column = chainFieldData.getColumn();

        if (ObjectUtils.isNotEmpty(alias)) {
            property = alias.toString();
            column = property;
        }

        TableFieldInfoExt fieldInfoExt = null;
        if (null != chainFieldData.getModelClass()) {
            TableFieldInfo info = getTableFieldInfoByFieldName(chainFieldData.getProperty(), chainFieldData.getModelClass());
            if (null != info && (info.getTypeHandler() != null || info.getJdbcType() != null)) {
                fieldInfoExt = new TableFieldInfoExt(info);
                fieldInfoExt.setColumn(column);
                fieldInfoExt.setProperty(property);
            }
        }
        return new FieldMapping(column, chainFieldData.getColumn(), chainFieldData.getAlias(), property, fieldInfoExt);
    }

    @Override
    protected void setFieldMappingList(String fieldName, String columns) {
        setFieldMappingList(fieldName, columns, null);
    }

    protected void setFieldMapping(ChainFieldData chainFieldData) {
        fieldMappingList.add(getFieldMapping(chainFieldData));
    }

    protected void setFieldMappingList(String fieldName, String columns, Class<?> clz) {

        TableFieldInfoExt fieldInfoExt = null;
        if (null != clz) {
            TableFieldInfo info = getTableFieldInfoByFieldName(fieldName, clz);
            if (null != info && (info.getTypeHandler() != null || info.getJdbcType() != null)) {
                fieldInfoExt = new TableFieldInfoExt(info);
                fieldInfoExt.setColumn(columns);
                fieldInfoExt.setProperty(fieldName);
            }
        }

        fieldMappingList.add(new FieldMapping(columns, fieldName, fieldInfoExt));
    }


}
