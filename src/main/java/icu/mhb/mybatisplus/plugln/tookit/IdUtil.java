package icu.mhb.mybatisplus.plugln.tookit;

import java.util.UUID;

/**
 * @author mahuibo
 * @Title: IdUtil
 * @time 8/24/21 5:59 PM
 */
public class IdUtil {

    public static String getSimpleUUID() {
        return UUID.randomUUID().toString();
    }

}

