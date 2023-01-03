package icu.mhb.mybatisplus.plugln.core.func;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinLambdaWrapper;

import java.util.List;

/**
 * @author mahuibo
 * @Title: JoinQueryFunc
 * @email mhb0409@qq.com
 * @time 2022/12/29
 */
public interface JoinQueryFunc<T, Children extends SupportJoinLambdaWrapper<T, Children>> {

    default <EV> List<EV> joinList(Class<EV> clz) {
        return executeQuery(mapper -> mapper.joinSelectList(getSunWrapper(), clz));
    }

    default <EV> EV joinGetOne(Class<EV> clz) {
        return executeQuery(mapper -> mapper.joinSelectOne(getSunWrapper(), clz));
    }

    default int joinCount() {
        return executeQuery(mapper -> mapper.joinSelectCount(getSunWrapper()));
    }

    default <E extends IPage<EV>, EV> E joinPage(E page, Class<EV> clz) {
        return executeQuery(mapper -> mapper.joinSelectPage(page, getSunWrapper(), clz));
    }

    /**
     * 执行查询
     *
     * @return
     */
    <R> R executeQuery(SFunction<JoinBaseMapper<T>, R> function);


    default Children getSunWrapper() {
        return (Children) this;
    }

}
