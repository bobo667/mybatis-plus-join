package icu.mhb.mybatisplus.plugln.tookit;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import icu.mhb.mybatisplus.plugln.entity.BaseChainModel;
import icu.mhb.mybatisplus.plugln.entity.ChainFieldData;
import icu.mhb.mybatisplus.plugln.entity.TableInfoExt;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static icu.mhb.mybatisplus.plugln.constant.StringPool.DOT;

/**
 * @author mahuibo
 * @Title: ChainUtil
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
public final class ChainUtil {


    public static String getAliasColum(ChainFieldData fieldData) {
        return fieldData.getAlias() + DOT + fieldData.getColumn();
    }

    public static <E extends BaseChainModel<E>> void initAllChainFieldData(E model) {
        initAllChainFieldData(model, null);
    }

    /**
     * 初始化 model中的所有字段进去
     *
     * @param model model
     */
    public static <E extends BaseChainModel<E>> void initAllChainFieldData(E model, Predicate<TableFieldInfo> predicate) {
        Class<?> modelClass = model.getModelClass();
        TableInfoExt tableInfoExt = TableInfoExt.get(modelClass);

        List<ChainFieldData> fieldDataList = tableInfoExt.getTableInfo().getFieldList().stream().filter(Optional.ofNullable(predicate).orElseGet(() -> (i) -> true)).map(field -> {
            return new ChainFieldData(field.getProperty(), field.getColumn(), model.getAlias(), model.getTableName(), modelClass, null);
        }).collect(Collectors.toList());

        if (tableInfoExt.getTableInfo().havePK()) {
            fieldDataList.add(new ChainFieldData(tableInfoExt.getTableInfo().getKeyProperty(), tableInfoExt.getTableInfo().getKeyColumn(), model.getAlias(), model.getTableName(), modelClass, null));
        }

        model.getChainFieldDataList().addAll(fieldDataList);
    }

}
