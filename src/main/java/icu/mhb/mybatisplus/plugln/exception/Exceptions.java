package icu.mhb.mybatisplus.plugln.exception;

/**
 * mybatis plus join 异常辅助工具类
 * copy mybatis-plus ExceptionUtils
 *
 * @author mahuibo
 * @Title: ExceptionUtils
 * @time 2023/1/11
 */
public final class Exceptions {

    private Exceptions() {
    }

    /**
     * 返回一个新的异常，统一构建，方便统一处理
     *
     * @param msg 消息
     * @param t   异常信息
     * @return 返回异常
     */
    public static MybatisPlusJoinException mpje(String msg, Throwable t, Object... params) {
        return new MybatisPlusJoinException(String.format(msg, params), t);
    }

    /**
     * 重载的方法
     *
     * @param msg 消息
     * @return 返回异常
     */
    public static MybatisPlusJoinException mpje(String msg, Object... params) {
        return new MybatisPlusJoinException(String.format(msg, params));
    }

    /**
     * 重载的方法
     *
     * @param t 异常
     * @return 返回异常
     */
    public static MybatisPlusJoinException mpje(Throwable t) {
        return new MybatisPlusJoinException(t);
    }

    public static void throwMpje(boolean condition, String msg, Object... params) {
        if (condition) {
            throw mpje(msg, params);
        }
    }
}
