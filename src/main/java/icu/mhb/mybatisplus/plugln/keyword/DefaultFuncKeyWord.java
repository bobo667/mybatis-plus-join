package icu.mhb.mybatisplus.plugln.keyword;
/**
 * @author mahuibo
 * @Title: DefaultFuncKeyWord
 * @email mhb0409@qq.com
 * @time 2022/11/16
 */
public class DefaultFuncKeyWord implements IFuncKeyWord {

    @Override
    public String distinct() {
        return "DISTINCT";
    }

}
