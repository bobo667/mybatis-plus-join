package icu.mhb.mybatisplus.plugln.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.base.service.JoinIService;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author mahuibo
 * @Title: JoinServiceImpl
 * @time 8/25/21 10:36 AM
 */
public class JoinServiceImpl<M extends JoinBaseMapper<T>, T> extends ServiceImpl<M, T> implements JoinIService<T> {

    @Autowired
    protected M joinMapper;

    @Override
    public M getBaseMapper() {
        return joinMapper;
    }

    @Override
    public <EV, E> List<EV> joinList(Wrapper<E> wrapper, Class<EV> clz) {
        return joinMapper.joinSelectList(wrapper, clz);
    }

    @Override
    public <E, EV> EV joinGetOne(Wrapper<E> wrapper, Class<EV> clz) {
        return joinMapper.joinSelectOne(wrapper, clz);
    }

    @Override
    public <E> int joinCount(Wrapper<E> wrapper) {
        return joinMapper.joinSelectCount(wrapper);
    }

    @Override
    public <E extends IPage<EV>, EV, C> E joinPage(E page, Wrapper<C> wrapper, Class<EV> clz) {
        return joinMapper.joinSelectPage(page, wrapper, clz);
    }

    protected LambdaQueryWrapper<T> lambdaQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(Class<J> clz) {
        return new JoinLambdaWrapper<>(clz);
    }

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(Class<J> clz, String alias) {
        return new JoinLambdaWrapper<>(clz, alias);
    }

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(J entity, String alias) {
        return new JoinLambdaWrapper<>(entity, alias);
    }

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(J entity) {
        return new JoinLambdaWrapper<>(entity);
    }

    protected LambdaUpdateWrapper<T> lambdaUpdateWrapper() {
        return new LambdaUpdateWrapper<>();
    }

}
