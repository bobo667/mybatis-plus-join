package icu.mhb.mybatisplus.plugln.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.mhb.mybatisplus.plugln.base.mapper.JoinBaseMapper;
import icu.mhb.mybatisplus.plugln.base.service.JoinIService;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;
import icu.mhb.mybatisplus.plugln.enums.PropertyType;
import icu.mhb.mybatisplus.plugln.tookit.JsonUtil;
import icu.mhb.mybatisplus.plugln.tookit.MappingUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Map<String, Object>> objectMap = joinMapper.joinSelectList(wrapper);

        if (CollectionUtils.isEmpty(objectMap)) {
            return new ArrayList<>();
        }

        // 判断是否是基础类型
        if (PropertyType.hasBaseType(clz)) {
            // 获取到每个map的第一位数据然后返回
            List<Object> list = objectMap.stream()
                    .map(i -> i.values().stream().findFirst())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            return JsonUtil.getClzTypeList(list, clz);
        }

        if (wrapper instanceof JoinLambdaWrapper) {
            MappingUtil.wrapperOneToOneMapping(objectMap, (JoinLambdaWrapper<E>) wrapper);
        }

        return JsonUtil.getClzTypeList(objectMap, clz);
    }

    @Override
    public <E, EV> EV joinGetOne(Wrapper<E> wrapper, Class<EV> clz) {
        Map<String, Object> objectMap = joinMapper.joinSelectOne(wrapper);

        if (null == objectMap) {
            return null;
        }

        // 判断是否是基础类型
        if (PropertyType.hasBaseType(clz)) {
            return JsonUtil.getClzType(objectMap.entrySet().stream().findFirst().map(Map.Entry::getValue).get(), clz);
        }

        if (wrapper instanceof JoinLambdaWrapper) {
            MappingUtil.wrapperOneToOneMapping(Collections.singletonList(objectMap), (JoinLambdaWrapper<E>) wrapper);
        }

        return JsonUtil.getClzType(objectMap, clz);
    }

    @Override
    public <E> int joinCount(Wrapper<E> wrapper) {
        return joinMapper.joinSelectCount(wrapper);
    }

    @Override
    public <EV, E extends IPage<EV>, C> Page<EV> joinPage(E page, Wrapper<C> wrapper, Class<EV> clz) {

        IPage<Map<String, Object>> selectPage = joinMapper.joinSelectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        // 获取列表
        List<Map<String, Object>> pageRecords = selectPage.getRecords();

        List<EV> parseArray = JsonUtil.getClzTypeList(pageRecords, clz);
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

    protected <J> JoinLambdaWrapper<J> joinLambdaQueryWrapper(J entity) {
        return new JoinLambdaWrapper<>(entity);
    }

    protected LambdaUpdateWrapper<T> lambdaUpdateWrapper() {
        return new LambdaUpdateWrapper<>();
    }

}
