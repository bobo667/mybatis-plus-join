package icu.mhb.mybatisplus.plugln.tookit;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import icu.mhb.mybatisplus.plugln.exception.Exceptions;

/**
 * @author mahuibo
 * @Title: Lambdas
 * @email mhb0409@qq.com
 * @time 2022/12/29
 */
public class Lambdas {

    /**
     * 可序列化
     */
    private static final int FLAG_SERIALIZABLE = 1;

    /**
     * 获取一个字段的sfunction
     *
     * @param model     实体类
     * @param fieldType 字段类型
     * @param fieldName 字段名称
     * @param <T>       实体类型
     * @return 字段get方法的的sfunction
     */
    public static <T> SFunction getSFunction(Class<T> model, Class<?> fieldType, String fieldName) {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(fieldType, model);
        final CallSite site;
        SFunction func = null;
        try {
            //方法名叫做:getSecretLevel  转换为 SFunction function interface对象
            site = LambdaMetafactory.altMetafactory(lookup,
                                                    "invoke",
                                                    MethodType.methodType(SFunction.class),
                                                    methodType,
                                                    lookup.findVirtual(model, "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1),
                                                                       MethodType.methodType(fieldType)),
                                                    methodType, FLAG_SERIALIZABLE);
            func = (SFunction) site.getTarget().invokeExact();
        } catch (Throwable e) {
            throw Exceptions.mpje(e);
        }
        return func;
    }

}
