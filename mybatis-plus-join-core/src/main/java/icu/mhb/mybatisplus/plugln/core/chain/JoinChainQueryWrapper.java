package icu.mhb.mybatisplus.plugln.core.chain;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import icu.mhb.mybatisplus.plugln.constant.StringPool;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.chain.support.SupportJoinChainQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.func.JoinQueryFunc;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import icu.mhb.mybatisplus.plugln.tookit.Provider;
import icu.mhb.mybatisplus.plugln.tookit.fun.FunComm;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author mahuibo
 * @Title: ChainJoinQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SuppressWarnings("all")
public class JoinChainQueryWrapper<T> extends SupportJoinChainQueryWrapper<T, JoinChainQueryWrapper<T>>
        implements JoinQueryFunc<T, String, JoinChainQueryWrapper<T>> {


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
    private SharedString sqlSelectCahce = new SharedString();


    @Override
    public String getSqlSelect() {

        if (sqlSelectFlag) {
            return sqlSelectCahce.getStringValue();
        }

        if (CollectionUtils.isEmpty(sqlSelect) && !this.notDefaultSelectAll) {
            selectAll(masterChainModel);
        }

        StringBuilder stringValue = new StringBuilder(sqlSelect.stream().map(SharedString::getStringValue).distinct().collect(Collectors.joining(",")));

        sqlSelectFlag = true;
        if (hasDistinct) {
            stringValue.insert(0, getFuncKeyWord().distinct() + StringPool.SPACE);
        }
        String selectSql = stringValue.toString();

        sqlSelectCahce.setStringValue(selectSql);
        return selectSql;
    }


    @Override
    protected JoinChainQueryWrapper<T> instance() {
        return new JoinChainQueryWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }


    public JoinChainQueryWrapper(BaseChainModel model) {
        super.setEntityClass(model.getModelClass());
        this.masterChainModel = model;
        this.masterTableAlias = model.getAlias();
        super.initNeed();
    }


    JoinChainQueryWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
                          Map<String, Object> paramNameValuePairs, MergeSegments mergeSegments,
                          SharedString lastSql, SharedString sqlComment, SharedString sqlFirst) {
        initNeed();
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

    public JoinChainQueryWrapper<T> select(boolean condition, List<String> columns) {
        return condition ? super.select(columns) : typedThis;
    }

    @Override
    public JoinChainQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        throw Exceptions.mpje("请不要调用该方法！请使用 select(BaseChainModel<?> model, Predicate<TableFieldInfo> predicate)");
    }

    @Override
    public String getJoinSql() {
        Collection<SharedString> joinSqlList = joinSqlMapping.values();
        if (CollectionUtils.isNotEmpty(joinSqlList)) {
            return joinSqlList.stream().map(SharedString::getStringValue).collect(Collectors.joining(Constants.NEWLINE));
        }
        return "";
    }

}
