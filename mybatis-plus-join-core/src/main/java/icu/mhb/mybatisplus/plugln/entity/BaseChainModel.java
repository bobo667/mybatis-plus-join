package icu.mhb.mybatisplus.plugln.entity;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mahuibo
 * @Title: BaseChainModel
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
public abstract class BaseChainModel<Children> {

    protected final Children typedThis = (Children) this;

    @Getter
    private Object entity;

    private final SharedString alias = SharedString.emptyString();

    private final SharedString tableName = SharedString.emptyString();

    @Getter
    private final Class<?> modelClass;

    @Getter
    private final List<ChainFieldData> chainFieldDataList = new ArrayList<>();

    protected void setAlias(String alias) {
        this.alias.setStringValue(alias);
    }

    protected void setEntity(Object entity){
        this.entity = entity;
    }

    public String getAlias() {
        return alias.getStringValue();
    }


    protected BaseChainModel(String alias, Class<?> modelClass) {
        this.modelClass = modelClass;
        init(alias);
    }

    protected BaseChainModel(Class<?> modelClass) {
        this(null, modelClass);
    }

    private void init(String alias) {
        TableInfoExt tableInfo = getTableInfo();
        if (StringUtils.isNotBlank(alias)) {
            setAlias(alias);
        } else {
            setAlias(tableInfo.getTableInfo().getTableName());
        }
        this.tableName.setStringValue(tableInfo.getTableInfo().getTableName());
    }

    protected void add(ChainFieldData chainFieldData) {
        chainFieldDataList.add(chainFieldData);
    }

    protected void add(List<ChainFieldData> chainFieldDataList) {
        this.chainFieldDataList.addAll(chainFieldDataList);
    }

    protected void add(String property, Object val) {
        chainFieldDataList.add(buildChainFieldData(property, val));
    }

    protected ChainFieldData buildChainFieldData(String property, Object val) {
        String colum = getTableInfo().getColumByProperty(property);
        Exceptions.throwMpje(StringUtils.isBlank(colum), "在 mybatis-plus FieldList 字段缓存中找不到属性： %s", property);

        return new ChainFieldData(property, colum, alias.getStringValue(), tableName.getStringValue(), modelClass, val);
    }

    public <T extends BaseChainModel<T>> T to(T t) {
        t.add(chainFieldDataList);
        chainFieldDataList.clear();
        return t;
    }

    public Children clear() {
        chainFieldDataList.clear();
        return typedThis;
    }

    public Children end() {
        clear();
        return typedThis;
    }

    /**
     * 获取表名
     */
    public String getTableName() {
        return tableName.getStringValue();
    }

    public TableInfoExt getTableInfo() {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(getModelClass());
        return new TableInfoExt(tableInfo);
    }

}
