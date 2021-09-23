package icu.mhb.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;

import java.util.ArrayList;
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

    public ColumnsBuilder<T> add(SFunction<T, ?> column) {
        columnsBuilderList.add(new As<>(column));
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

}
