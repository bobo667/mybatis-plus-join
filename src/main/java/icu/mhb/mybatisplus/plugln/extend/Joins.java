package icu.mhb.mybatisplus.plugln.extend;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;

/**
 * @author mahuibo
 * @Title: Joins
 * @email mhb0409@qq.com
 * @time 2022/12/22
 */
public class Joins {

    public static <T> JoinLambdaWrapper<T> of(Class<T> clz) {
        return new JoinLambdaWrapper<>(clz);
    }

}
