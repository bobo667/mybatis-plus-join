package icu.mhb.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public ColumnsBuilder() {
        this.columnsBuilderList = new ArrayList<>();
    }


    public ColumnsBuilder<T> add(Collection<SFunction<T, ?>> columns) {

        if (null == columns || columns.isEmpty()) {
            return this;
        }

        for (SFunction<T, ?> column : columns) {
            columnsBuilderList.add(new As<>(column));
        }

        return this;
    }

    public ColumnsBuilder<T> add(SFunction<T, ?> column) {

        if (column == null) {
            return this;
        }

        columnsBuilderList.add(new As<>(column));
        return this;
    }

    public ColumnsBuilder<T> add(Object columnStr, String alias) {
        columnsBuilderList.add(new As<>(columnStr, alias));
        return this;
    }

}
