package icu.mhb.mybatisplus.plugln.tookit.fun;
/**
 * @author mahuibo
 * @Title: FunComm
 * @email mhb0409@qq.com
 * @time 2022/11/22
 */
public class FunComm {

    public static void isTrue(boolean flag, Runnable runnable) {
        if (flag) {
            runnable.run();
        }
    }

}
