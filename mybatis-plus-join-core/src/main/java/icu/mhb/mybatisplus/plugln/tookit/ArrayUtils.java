/*
 * Copyright [2024] [https://nblowcode.aicats.cc]
 *
 * Nb-LowCode 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Nb-LowCode源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://nblowcode.aicats.cc
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队mhb0409@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Nb-LowCode商业授权许可，请在官网购买授权，地址为 https://nblowcode.aicats.cc
 */

package icu.mhb.mybatisplus.plugln.tookit;

import java.util.Objects;

/**
 * @author mahuibo
 * @Title: ArrayUtils
 * @email mhb0409@qq.com
 * @time 2025/1/22
 */
public final class ArrayUtils {


    private ArrayUtils() {
    }

    /**
     * 判断数据是否为空
     *
     * @param array 长度
     * @return 数组对象为null或者长度为 0 时，返回 false
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return 数组对象内含有任意对象时返回 true
     * @see com.baomidou.mybatisplus.core.toolkit.ArrayUtils#isEmpty(Object[])
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean contains(Object[] array, Object o) {
        if (isEmpty(array)) {
            return false;
        }
        for (Object e : array) {
            if (Objects.equals(o, e)) {
                return true;
            }
        }
        return false;
    }
}
