package icu.mhb.mybatisplus.plugln.core.func;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
        return getJoinBaseMapper().joinSelectList(getSunWrapper(), clz);
    }

    default <EV> EV joinGetOne(Class<EV> clz) {
        return getJoinBaseMapper().joinSelectOne(getSunWrapper(), clz);
    }

    default int joinCount() {
        return getJoinBaseMapper().joinSelectCount(getSunWrapper());
    }

    default <E extends IPage<EV>, EV> E joinPage(E page, Class<EV> clz) {
        return getJoinBaseMapper().joinSelectPage(page, getSunWrapper(), clz);
    }

    /**
     * 获取baseMapper
     *
     * @return
     */
    JoinBaseMapper<T> getJoinBaseMapper();

    default Children getSunWrapper() {
        return (Children) this;
    }

}
