package icu.mhb.mybatisplus.plugln.interceptor;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.ManyToManySelectBuild;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;
import icu.mhb.mybatisplus.plugln.enums.PropertyType;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultResultType;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.Lists;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * join返回类型拦截器
 *
 * @author mahuibo
 * @Title: JoinInterceptor
 * @email mhb0409@qq.com
 * @date 2022-02-15
 */
@SuppressWarnings("all")
@Order(Integer.MIN_VALUE)
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class JoinInterceptor implements Interceptor {

    /**
     * 缓存MappedStatement
     */
    private static final Map<String, Map<Configuration, MappedStatement>> MS_CACHE = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if (args[0] instanceof MappedStatement) {
            MappedStatement ms = (MappedStatement) args[0];
            if (args[1] instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) args[1];
                Object ew = map.containsKey(Constants.WRAPPER) ? map.get(Constants.WRAPPER) : null;
                Object returnClass = map.containsKey(JoinConstant.CLASS_PARAMS_NAME) ? map.get(JoinConstant.CLASS_PARAMS_NAME) : null;
                // 如果说 Wrapper 是JoinLambdaWrapper类型就代表可能需要解析多表映射
                if ((ew instanceof JoinLambdaWrapper) && returnClass != null) {
                    JoinLambdaWrapper joinLambdaWrapper = (JoinLambdaWrapper) ew;
                    Class<?> classType = (Class<?>) returnClass;
                    List<ResultMap> list = ms.getResultMaps();
                    if (CollectionUtils.isNotEmpty(list)) {
                        ResultMap resultMap = list.get(0);
                        if (resultMap.getType() == JoinDefaultResultType.class) {
                            args[0] = newMappedStatement(ms, joinLambdaWrapper, classType);
                        }
                    }
                }

            }
        }
        return invocation.proceed();
    }


    /**
     * 构建新的MappedStatement
     */
    private MappedStatement newMappedStatement(MappedStatement ms, JoinLambdaWrapper joinLambdaWrapper, Class<?> classType) {
        String id = ms.getId() + StringPool.UNDERSCORE + joinLambdaWrapper.getSqlSelect();
        Map<Configuration, MappedStatement> statementMap = MS_CACHE.get(id);

        if (CollectionUtils.isNotEmpty(statementMap) && Objects.nonNull(statementMap.get(ms.getConfiguration()))) {
            return statementMap.get(ms.getConfiguration());
        }
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, ms.getSqlSource(), ms.getSqlCommandType())
                .resource(ms.getResource())
                .fetchSize(ms.getFetchSize())
                .statementType(ms.getStatementType())
                .keyGenerator(ms.getKeyGenerator())
                .timeout(ms.getTimeout())
                .parameterMap(ms.getParameterMap())
                .resultSetType(ms.getResultSetType())
                .cache(ms.getCache())
                .flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache());

        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            builder.keyProperty(String.join(StringPool.COMMA, ms.getKeyProperties()));
        }

        builder.resultMaps(Lists.newArrayList(newResultMap(ms, joinLambdaWrapper, classType)));
        MappedStatement mappedStatement = builder.build();

        if (statementMap == null) {
            statementMap = new ConcurrentHashMap<>();
            MS_CACHE.put(id, statementMap);
        }
        statementMap.put(ms.getConfiguration(), mappedStatement);
        return mappedStatement;
    }

    /**
     * 构建resultMap
     */
    private ResultMap newResultMap(MappedStatement ms, JoinLambdaWrapper<?> joinLambdaWrapper, Class<?> classType) {
        Configuration configuration = ms.getConfiguration();
        String id = ms.getId() + StringPool.UNDERSCORE + joinLambdaWrapper.getSqlSelect();

        // 如果返回类型是基础类型或者包装类型，就直接返回基础映射，或者是map类型
        if (PropertyType.hasBaseType(classType) || classType.equals(Map.class)) {
            return new ResultMap.Builder(configuration, id, classType, Lists.newArrayList(0)).build();
        }

        Class<?>[] interfaces = classType.getInterfaces();
        if (ArrayUtils.isNotEmpty(interfaces)) {
            for (Class<?> aClass : interfaces) {
                // 代表是map类型
                if (aClass.equals(Map.class)) {
                    return new ResultMap.Builder(configuration, id, classType, Lists.newArrayList(0)).build();
                }
            }
        }

        if (configuration.hasResultMap(id)) {
            return configuration.getResultMap(id);
        }

        List<ResultMapping> resultMappings = buildResultMapping(configuration, joinLambdaWrapper.getFieldMappingList(), classType);


        List<OneToOneSelectBuild> oneToOneSelectBuildList = joinLambdaWrapper.getOneToOneSelectBuildList();
        // 不为空就代表有一对一映射
        if (CollectionUtils.isNotEmpty(oneToOneSelectBuildList)) {
            for (OneToOneSelectBuild oneToOneSelectBuild : oneToOneSelectBuildList) {
                // 构建ResultMap
                String oneToOneId = id + StringPool.UNDERSCORE + oneToOneSelectBuild.getOneToOneField();
                if (!configuration.hasResultMap(oneToOneId)) {
                    ResultMap oneToOneResultMap = new ResultMap.Builder(configuration, oneToOneId,
                                                                        oneToOneSelectBuild.getOneToOneClass(),
                                                                        buildResultMapping(configuration, oneToOneSelectBuild.getBelongsColumns(), oneToOneSelectBuild.getOneToOneClass())).build();
                    configuration.addResultMap(oneToOneResultMap);
                }
                resultMappings.add(new ResultMapping.Builder(configuration, oneToOneSelectBuild.getOneToOneField())
                                           .javaType(oneToOneSelectBuild.getOneToOneClass()).nestedResultMapId(oneToOneId).build());
            }
        }

        List<ManyToManySelectBuild> manyToManySelectBuildList = joinLambdaWrapper.getManyToManySelectBuildList();
        // 不为空就代表有多对多映射
        if (CollectionUtils.isNotEmpty(manyToManySelectBuildList)) {
            for (ManyToManySelectBuild manyToManySelectBuild : manyToManySelectBuildList) {
                // 构建ResultMap
                String manyToManyId = id + StringPool.UNDERSCORE + manyToManySelectBuild.getManyToManyField();
                if (!configuration.hasResultMap(manyToManyId)) {
                    ResultMap oneToOneResultMap = new ResultMap.Builder(configuration, manyToManyId, manyToManySelectBuild.getManyToManyClass(),
                                                                        buildResultMapping(configuration, manyToManySelectBuild.getBelongsColumns(),
                                                                                           manyToManySelectBuild.getManyToManyClass())).build();
                    configuration.addResultMap(oneToOneResultMap);
                }
                resultMappings.add(new ResultMapping.Builder(configuration, manyToManySelectBuild.getManyToManyField())
                                           .javaType(manyToManySelectBuild.getManyToManyPropertyType()).nestedResultMapId(manyToManyId).build());
            }
        }
        ResultMap resultMap = new ResultMap.Builder(configuration, id, classType, resultMappings).build();
        configuration.addResultMap(resultMap);

        return resultMap;
    }

    /**
     * 构建结果集映射
     *
     * @param clz           mapper方法返回的类型
     * @param fieldMappings 字段映射列表
     * @param configuration mybatis 配置类
     */
    private List<ResultMapping> buildResultMapping(Configuration configuration, List<FieldMapping> fieldMappings, Class<?> clz) {
        return fieldMappings.stream()
                .map(fieldMapping -> {
                    if (null != fieldMapping.getTableFieldInfoExt()) {
                        return fieldMapping.getTableFieldInfoExt().getResultMapping(configuration);
                    }
                    Class<?> propertyType = ClassUtils.getDeclaredField(clz, fieldMapping.getFieldName()).getType();
                    ResultMapping.Builder builder = new ResultMapping.Builder(configuration, fieldMapping.getFieldName(),
                                                                              fieldMapping.getColumn(), propertyType
                    );
                    return builder.build();
                })
                .collect(Collectors.toList());
    }

}
