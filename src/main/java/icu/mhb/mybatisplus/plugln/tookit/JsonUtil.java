package icu.mhb.mybatisplus.plugln.tookit;
import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * 序列化工具
 *
 * @author mahuibo
 * @Title: JsonUtil
 * @email mhb0409@qq.com
 * @time 2021/11/30
 */
public class JsonUtil {

    /**
     * 获取 clz类型的list集合
     *
     * @param value 需要转换的数据
     * @param clz   class类型
     * @param <T>   泛型
     * @return 转换后的集合
     */
    public static <T> List<T> getClzTypeList(Object value, Class<T> clz) {
        return JSON.parseArray(JSON.toJSONString(value), clz);
    }

    /**
     * 获取 clz类型的对象数据
     *
     * @param value 需要转换的数据
     * @param clz   class类型
     * @param <T>   泛型
     * @return 转换后的对象
     */
    public static <T> T getClzType(Object value, Class<T> clz) {
        return JSON.parseObject(JSON.toJSONString(value), clz);
    }


}
