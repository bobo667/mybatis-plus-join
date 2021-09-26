package icu.mhb.mybatisplus.plugln.tookit;
/**
 * @author mahuibo
 * @Title: StringUtil
 * @time 9/25/21 5:32 PM
 */
public class StringUtil {

    private StringUtil() {
    }

    public static boolean isBlank(final CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int l = cs.length();
        if (l > 0) {
            for (int i = 0; i < l; i++) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }


}
