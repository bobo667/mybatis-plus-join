package icu.mhb.mybatisplus.plugln.tookit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mahuibo
 * @Title: MappingUtil
 * @email mhb0409@qq.com
 * @time 2021/12/17
 */
public class MappingUtil {

    /**
     * 对传入的map进行一对一的映射
     *
     * @param maps                 结果映射
     * @param oneToOneSelectBuilds 一对一映射配置
     * @return 映射后的map
     */
    public static void oneToOneMapping(List<Map<String, Object>> maps, List<OneToOneSelectBuild> oneToOneSelectBuilds) {

        for (OneToOneSelectBuild oneSelectBuild : oneToOneSelectBuilds) {
            for (Map<String, Object> map : maps) {
                Map<String, Object> oneToOneMap = new HashMap<>();

                // 提取map中对应的一对一对应的值
                oneSelectBuild.getBelongsColumns()
                        .forEach((fieldMapping) -> MapUtil.notNullPut(StringUtils.isNotEmpty(fieldMapping.getFieldName()) ? fieldMapping.getFieldName() : fieldMapping.getColumn(), MapUtil.getAndRemove(fieldMapping.getColumn(), map), oneToOneMap));

                map.put(oneSelectBuild.getOneToOneField(), oneToOneMap);
            }
        }
    }

    public static <E> void wrapperOneToOneMapping(List<Map<String, Object>> objectMap, JoinLambdaWrapper<E> wrapper) {
        List<OneToOneSelectBuild> oneToOneSelectBuildList = wrapper.getOneToOneSelectBuildList();
        // 如果不为空就代表有一对一配置
        if (CollectionUtils.isNotEmpty(oneToOneSelectBuildList)) {
            oneToOneMapping(objectMap, oneToOneSelectBuildList);
        }
    }


}
