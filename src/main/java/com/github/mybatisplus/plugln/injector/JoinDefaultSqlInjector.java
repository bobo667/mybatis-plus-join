package com.github.mybatisplus.plugln.injector;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.github.mybatisplus.plugln.injector.methods.JoinSelectCount;
import com.github.mybatisplus.plugln.injector.methods.JoinSelectList;
import com.github.mybatisplus.plugln.injector.methods.JoinSelectOne;
import com.github.mybatisplus.plugln.injector.methods.JoinSelectPage;

import java.util.Arrays;
import java.util.List;

/**
 * @author mahuibo
 * @Title: JoinDefaultSqlInjector
 * @time 8/25/21 2:42 PM
 */
public class JoinDefaultSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        List<AbstractMethod> list = Arrays.asList(
                new JoinSelectList(),
                new JoinSelectCount(),
                new JoinSelectOne(),
                new JoinSelectPage()
        );
        methodList.addAll(list);
        return methodList;
    }

}
