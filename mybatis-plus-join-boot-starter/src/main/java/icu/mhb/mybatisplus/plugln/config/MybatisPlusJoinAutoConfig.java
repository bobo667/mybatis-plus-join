package icu.mhb.mybatisplus.plugln.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultSqlInjector;
import icu.mhb.mybatisplus.plugln.interceptor.JoinInterceptor;
import icu.mhb.mybatisplus.plugln.interceptor.JoinInterceptorConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * @author mahuibo
 * @Title: MpjAutoConfig
 * @email mhb0409@qq.com
 * @time 2024/6/23
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisPlusJoinProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisPlusLanguageDriverAutoConfiguration.class})
public class MybatisPlusJoinAutoConfig {


    public MybatisPlusJoinAutoConfig(MybatisPlusJoinProperties properties) {
        ConfigUtil.setConfig(properties.getConfig());
    }

    /**
     * mybatis plus join 自定义方法
     */
    @Bean
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnBean(ISqlInjector.class)
    public JoinDefaultSqlInjector joinDefaultSqlInjector(ISqlInjector sqlInjector) {
        log.info("用户自定义了注入了 ISqlInjector，开始初始化...");
        return new JoinDefaultSqlInjector(sqlInjector);
    }

    /**
     * mybatis plus join 自定义方法
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(ISqlInjector.class)
    public JoinDefaultSqlInjector joinDefaultSqlInjectorOnMiss() {
        log.info("用户未定义 ISqlInjector，开始初始化默认数据...");
        return new JoinDefaultSqlInjector();
    }

    @Bean
    public JoinInterceptorConfig joinInterceptorConfig() {
        return new JoinInterceptorConfig();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public JoinInterceptor joinInterceptor() {
        return new JoinInterceptor();
    }

}
