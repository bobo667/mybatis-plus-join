package icu.mhb.mybatisplus.plugln.base.mapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用于 join连接所继承的基础mapper
 *
 * @author mahuibo
 * @Title: JoinBaseMapper
 * @time 8/25/21 9:51 AM
 */
public interface JoinBaseMapper<T> extends BaseMapper<T> {

    /**
     * 多表查询列表
     *
     * @param wrapper 条件包装
     * @param <E>     适配各种传入类型
     * @return 返回包装类型的对象list
     */
    <E, EV> List<EV> joinSelectList(@Param(Constants.WRAPPER) Wrapper<E> wrapper, @Param(JoinConstant.CLASS_PARAMS_NAME) Class<EV> clz);

    /**
     * 多表查询单个
     *
     * @param wrapper 条件包装
     * @param <E>     传入类型
     * @return 返回包装类型的对象
     */
    <E, EV> EV joinSelectOne(@Param(Constants.WRAPPER) Wrapper<E> wrapper, @Param(JoinConstant.CLASS_PARAMS_NAME) Class<EV> clz);

    /**
     * 多表查询count
     *
     * @param wrapper 条件包装
     * @param <E>     返回类型
     * @return 总数
     */
    <E> int joinSelectCount(@Param(Constants.WRAPPER) Wrapper<E> wrapper);

    /**
     * 多表查询分页
     *
     * @param page         分页参数
     * @param queryWrapper 条件包装
     * @param <E>          返回类型
     * @param <C>          传入Wrapper类型
     * @return E
     */
    <E extends IPage<EV>, EV, C> E joinSelectPage(E page, @Param(Constants.WRAPPER) Wrapper<C> queryWrapper, @Param(JoinConstant.CLASS_PARAMS_NAME) Class<EV> clz);


}
