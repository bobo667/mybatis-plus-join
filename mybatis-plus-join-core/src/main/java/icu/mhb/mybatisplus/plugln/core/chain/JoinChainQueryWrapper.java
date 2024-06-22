package icu.mhb.mybatisplus.plugln.core.chain;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.chain.func.JoinChainCompareFun;
import icu.mhb.mybatisplus.plugln.core.chain.support.SupportJoinChainQueryWrapper;
import icu.mhb.mybatisplus.plugln.core.func.JoinMethodFunc;
import icu.mhb.mybatisplus.plugln.core.func.JoinQueryFunc;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.*;
import icu.mhb.mybatisplus.plugln.keyword.IFuncKeyWord;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.COMMA;

/**
 * @author mahuibo
 * @Title: ChainJoinQueryWrapper
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SuppressWarnings("all")
public class JoinChainQueryWrapper<T> extends SupportJoinChainQueryWrapper<T, JoinChainQueryWrapper<T>>
        implements JoinQueryFunc<T, JoinLambdaWrapper<T>> {


    /**
     * 主表别名
     */
    @Getter
    private String masterTableAlias;

    /**
     * 主表class
     */
    private Class<T> masterClass;

    /**
     * 关键字获取
     */
    private IFuncKeyWord funcKeyWord;


    /**
     * 一对一 构建列表
     */
    @Getter
    private List<OneToOneSelectBuild> oneToOneSelectBuildList = null;

    /**
     * 多对多 构建列表
     */
    @Getter
    private List<ManyToManySelectBuild> manyToManySelectBuildList = null;

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



//
//    public String getSqlSelect() {
//
//        if (sqlSelectFlag) {

//            return sqlSelectCahce.getStringValue();
//        }
//
//        if (CollectionUtils.isEmpty(sqlSelect) && !this.notDefaultSelectAll) {
//            selectAll();
//        }
//
//        StringBuilder stringValue = new StringBuilder(sqlSelect.stream().map(SharedString::getStringValue).distinct().collect(Collectors.joining(",")));
//
//        String joinSelectSql = joinSqlSelect.stream()
//                .map(SharedString::getStringValue)
//                .filter(StringUtils::isNotBlank)
//                .collect(Collectors.joining(COMMA));
//
//        // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
//        if (stringValue.length() > 0 && StringUtils.isNotBlank(joinSelectSql)) {
//            stringValue.append(COMMA);
//        }
//
//        stringValue.append(joinSelectSql);
//
//        if (CollectionUtils.isNotEmpty(sunQueryList)) {
//            // 只有在拥有主表查询字段并且有子表查询的时候才需要加上','分隔符
//            if (stringValue.length() > 0) {
//                stringValue.append(COMMA);
//            }
//            stringValue.append(sunQueryList.stream().map(SharedString::getStringValue).collect(Collectors.joining(",")));
//        }
//
//        String selectSql = stringValue.toString();
//
//        sqlSelectFlag = true;
//        if (hasDistinct) {
//            selectSql = getFuncKeyWord().distinct() + " " + selectSql;
//        }
//        sqlSelectCahce.setStringValue(selectSql);
//
//        return selectSql;
//    }




    @Override
    protected JoinChainQueryWrapper<T> instance() {
        return new JoinChainQueryWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq, paramNameValuePairs,
                new MergeSegments(), SharedString.emptyString(), SharedString.emptyString(), SharedString.emptyString());
    }


    public JoinChainQueryWrapper(BaseChainModel model) {
        super.setEntityClass(model.getModelClass());
        super.initNeed();
    }

    public JoinChainQueryWrapper(Class<T> cla) {
        super.setEntityClass(cla);
        super.initNeed();
    }


    JoinChainQueryWrapper(T entity, Class<T> entityClass, List<SharedString> sqlSelect, AtomicInteger paramNameSeq,
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
    public JoinChainQueryWrapper<T> select(String... columns) {
        return null;
    }

    @Override
    public JoinChainQueryWrapper<T> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        return null;
    }

}
