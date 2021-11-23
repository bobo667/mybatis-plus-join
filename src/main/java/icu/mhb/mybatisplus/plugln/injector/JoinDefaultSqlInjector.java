package icu.mhb.mybatisplus.plugln.injector;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import icu.mhb.mybatisplus.plugln.injector.methods.JoinSelectCount;
import icu.mhb.mybatisplus.plugln.injector.methods.JoinSelectList;
import icu.mhb.mybatisplus.plugln.injector.methods.JoinSelectOne;
import icu.mhb.mybatisplus.plugln.injector.methods.JoinSelectPage;

import java.util.Arrays;
import java.util.List;

/**
 * @author mahuibo
 * @Title: JoinDefaultSqlInjector
 * @time 8/25/21 2:42 PM
 */
public class JoinDefaultSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
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
