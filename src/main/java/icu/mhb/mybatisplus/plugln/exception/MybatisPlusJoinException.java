package icu.mhb.mybatisplus.plugln.exception;

/**
 * mybatis plus join 异常类
 *
 * @author mahuibo
 * @Title: MybatisPlusJoinException
 * @time 2023/1/11
 */
public class MybatisPlusJoinException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MybatisPlusJoinException(String message) {
        super(message);
    }

    public MybatisPlusJoinException(Throwable throwable) {
        super(throwable);
    }

    public MybatisPlusJoinException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
