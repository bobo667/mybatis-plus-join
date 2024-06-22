package icu.mhb.mybatisplus.plugln.tookit;

/**
 * @author mahuibo
 * @Title: Consume
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@FunctionalInterface
public interface Provider<T> {

    T run();

}
