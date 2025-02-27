package icu.mhb.mybatisplus.plugln.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author mahuibo
 * @Title: MybatisPlusJoinProperties
 * @email mhb0409@qq.com
 * @time 2025/2/27
 */
@ConfigurationProperties(
        prefix = "mybatis-plus-join"
)
@Data
public class MybatisPlusJoinProperties {

    @NestedConfigurationProperty
    private MpjConfig config;

}
