package icu.mhb.mybatisplus.plugln.core.chain.support;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.core.chain.JoinChainQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainCompareFun;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainMethodFunc;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinWrapper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.enums.SqlExcerpt;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.tookit.ChainUtil;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import icu.mhb.mybatisplus.plugln.tookit.Provider;
import icu.mhb.mybatisplus.plugln.tookit.IdUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.enums.WrapperKeyword.APPLY;

/**
 * @author mahuibo
 * @Title: SupportJoinChainQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SuppressWarnings("all")
public abstract class SupportJoinChainQueryWrapper<T, Children extends SupportJoinChainQueryWrapper<T, Children>>
        extends SupportJoinWrapper<T, String, Children> implements Query<Children, T, String>, JoinChainCompareFun<Children>, JoinChainMethodFunc<T, Children> {

    /**
     * 逻辑删除是否拼接到Join后面
     * true 拼接
     * false 拼接到join 后面
     */
    private boolean logicDeleteIsApplyJoin = true;


    @Override
    public <L extends BaseChainModel<L>, R extends BaseChainModel<R>> Children join(L leftTableField, R rightTableField, boolean logicDeleteIsApplyJoin, SqlExcerpt joinType) {

        Exceptions.throwMpje(CollectionUtils.isEmpty(leftTableField.getChainFieldDataList()) || CollectionUtils.isEmpty(rightTableField.getChainFieldDataList()),
                "join的字段不能为空");

        ChainFieldData leftField = leftTableField.getChainFieldDataList().get(0);
        ChainFieldData rightField = rightTableField.getChainFieldDataList().get(0);

        SharedString sharedString = SharedString.emptyString();

        StringBuilder sb = new StringBuilder(String.format(joinType.getSql(), leftField.getTableName(), leftField.getAlias(), leftField.getAlias(), leftField.getColumn(), rightField.getAlias(), rightField.getColumn()));

        TableInfo tableInfo = TableInfoHelper.getTableInfo(leftField.getModelClass());
        if (null != tableInfo && logicDeleteIsApplyJoin) {
            TableInfoExt infoExt = new TableInfoExt(tableInfo);
            String logicDeleteSql = infoExt.getLogicDeleteSql(true, true, leftField.getAlias());
            sb.append(Constants.SPACE).append(Constants.NEWLINE).append(logicDeleteSql);
        }
        sharedString.setStringValue(sb.toString());

        leftTableField.end();
        rightTableField.end();
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

    public <E extends BaseChainModel<E>> Children select(E model, Predicate<TableFieldInfo> predicate) {
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


    public <E extends BaseChainModel<E>> Children selectAll(E model) {
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

    public <E extends BaseChainModel<E>> Children selectAs(Provider<E> provider) {
        useChainModel(provider, (model) -> {
            List<ChainFieldData> fieldDataList = model.getChainFieldDataList();

            List<String> selectList = fieldDataList.stream()
                    .map(chainFieldData -> {
                        Object alias = chainFieldData.getVal();
                        String property = chainFieldData.getProperty();
                        String returnColumn = getAliasAndField(chainFieldData.getAlias(), chainFieldData.getColumn());
                        String column = chainFieldData.getColumn();

                        if (ObjectUtils.isNotEmpty(alias)) {
                            property = alias.toString();
                            column = property;
                            returnColumn = String.format(SqlExcerpt.COLUMNS_AS.getSql(), returnColumn, property);
                        }
                        setFieldMappingList(property, column, chainFieldData.getModelClass());

                        return returnColumn;
                    }).collect(Collectors.toList());

            select(selectList);
        });

        return typedThis;
    }


    @Override
    public <E extends BaseChainModel<E>> Children eq(boolean condition, boolean ifNull, Provider<E> provider) {
        addConditon(condition, ifNull, provider, (flag, data) -> {
            eq(flag, ChainUtil.getAliasColum(data), data.getVal());
        });
        return typedThis;
    }


    public <E extends BaseChainModel<E>> void addConditon(boolean condition, boolean ifNull, Provider<E> provider, BiConsumer<Boolean, ChainFieldData> consumer) {
        if (condition) {
            useChainModel(provider, (model) -> {
                for (ChainFieldData fieldData : model.getChainFieldDataList()) {
                    consumer.accept(ifNull ? ObjectUtils.isNotEmpty(fieldData.getVal()) : true, fieldData);
                }
            });
        }
    }

    protected <E extends BaseChainModel<E>> void useChainModel(Provider<E> provider, Consumer<E> consumer) {
        E model = provider.run();
        consumer.accept(model);
        model.end();
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
    }

    @Override
    protected void setFieldMappingList(String fieldName, String columns) {
        setFieldMappingList(fieldName, columns, null);
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
