package com.github.mybatisplus.plugln.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import com.github.mybatisplus.plugln.base.service.JoinIService;
import com.github.mybatisplus.plugln.core.JoinLambdaWrapper;
import com.sun.istack.internal.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mahuibo
 * @Title: JoinServiceImpl
 * @time 8/25/21 10:36 AM
 */
public class JoinServiceImpl<M extends JoinBaseMapper<T>, T> extends ServiceImpl<M, T> implements JoinIService<T> {

    @Autowired
    protected M joinMapper;

    @Override
    public <EV, E> List<EV> joinList(@NotNull Wrapper<E> wrapper, @NotNull Class<EV> clz) {
        List<Map<String, Object>> objectMap = joinMapper.joinSelectList(wrapper);

        if (CollectionUtils.isEmpty(objectMap)) {
            return new ArrayList<>();
        }

        return JSONArray.parseArray(JSON.toJSONString(objectMap), clz);
    }

    @Override
    public <E, EV> EV joinGetOne(@NotNull Wrapper<E> wrapper, @NotNull Class<EV> clz) {
        Map<String, Object> objectMap = joinMapper.joinSelectOne(wrapper);

        if (null == objectMap) {
            return null;
        }

        return JSON.parseObject(JSON.toJSONString(objectMap), clz);
    }

    @Override
    public <E> int joinCount(@NotNull Wrapper<E> wrapper) {
        return joinMapper.joinSelectCount(wrapper);
    }

    @Override
    public <EV, E extends IPage<EV>, C> Page<EV> joinPage(@NotNull E page, @NotNull Wrapper<C> wrapper, @NotNull Class<EV> clz) {

        IPage<Map<String, Object>> selectPage = joinMapper.joinSelectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        // 获取列表
        List<Map<String, Object>> pageRecords = selectPage.getRecords();

        List<EV> parseArray = JSON.parseArray(JSON.toJSONString(pageRecords), clz);
        selectPage.setRecords(null);

        Page<EV> returnPage = new Page<>();
        BeanUtils.copyProperties(selectPage, returnPage);

        return returnPage.setRecords(parseArray);
    }


    protected LambdaQueryWrapper<T> lambdaQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(Class<J> clz) {
        return new JoinLambdaWrapper<>(clz);
    }

    protected LambdaUpdateWrapper<T> lambdaUpdateWrapper() {
        return new LambdaUpdateWrapper<>();
    }

}
