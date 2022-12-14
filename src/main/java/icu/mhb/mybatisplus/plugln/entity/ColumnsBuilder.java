package icu.mhb.mybatisplus.plugln.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询列表构建实体
 *
 * @param <T>
 */
@Getter
public class ColumnsBuilder<T> {

    /**
     * 字段列表
     */
    private List<As<T>> columnsBuilderList;

    /**
     * 表名，用于自动映射
     */
    @Setter
    private String tableName;

    public ColumnsBuilder() {
        this.columnsBuilderList = new ArrayList<>();
    }

    public ColumnsBuilder<T> add(Collection<SFunction<T, ?>> columns) {

        if (CollectionUtils.isEmpty(columns)) {
            return this;
        }

        for (SFunction<T, ?> column : columns) {
            columnsBuilderList.add(new As<>(tableName, column));
        }

        return this;
    }

    @SafeVarargs
    public final ColumnsBuilder<T> add(SFunction<T, ?>... columns) {

        if (ArrayUtils.isEmpty(columns)) {
            return this;
        }

        return add(Arrays.asList(columns));
    }

    public ColumnsBuilder<T> add(SFunction<T, ?> column) {

        if (column == null) {
            return this;
        }

        return add(Collections.singletonList(column));
    }

    public <J> ColumnsBuilder<T> addFunAlias(SFunction<T, ?> column, SFunction<J, ?> alias) {

        if (column == null) {
            return this;
        }

        columnsBuilderList.add(new As<>(column, alias));
        return this;
    }

    public ColumnsBuilder<T> add(SFunction<T, ?> column, String alias) {
        columnsBuilderList.add(new As<>(column, alias));
        return this;
    }

    public ColumnsBuilder<T> add(Object columnStr, String alias) {
        columnsBuilderList.add(new As<>(columnStr, alias));
        return this;
    }

    public ColumnsBuilder<T> add(Object columnStr, String alias, boolean isQuotes) {
        columnsBuilderList.add(new As<>(columnStr, alias, isQuotes));
        return this;
    }

    public <F> ColumnsBuilder<T> add(SFunction<T, ?> column, String alias, SFunction<F, ?> fieldName) {
        columnsBuilderList.add(new As<>(column, alias, fieldName));
        return this;
    }

    public ColumnsBuilder<T> addAll(List<As<T>> columnsBuilderList) {

        if (CollectionUtils.isNotEmpty(columnsBuilderList)) {
            this.columnsBuilderList.addAll(columnsBuilderList);
        }
        return this;
    }

}
