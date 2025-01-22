package icu.mhb.mybatisplus.plugln.interceptor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import icu.mhb.mybatisplus.plugln.config.MybatisPlusJoinConfig;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.JoinLambdaWrapper;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinWrapper;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.ManyToManySelectBuild;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;
import icu.mhb.mybatisplus.plugln.entity.TableFieldInfoExt;
import icu.mhb.mybatisplus.plugln.enums.JoinSqlMethod;
import icu.mhb.mybatisplus.plugln.enums.PropertyType;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultResultType;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.Lists;

import icu.mhb.mybatisplus.plugln.tookit.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.Collections;
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
@Slf4j
@SuppressWarnings("all")
@Order(Integer.MIN_VALUE)
@Intercepts(@Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class JoinInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(JoinInterceptor.class);

    @Autowired(required = false)
    private MybatisPlusJoinConfig mybatisPlusJoinConfig;

    /**
     * 缓存MappedStatement
     */
    private static final Map<String, Map<Configuration, MappedStatement>> MS_CACHE = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];

        if (!(args[1] instanceof Map)) {
            return invocation.proceed();
        }

        Map<String, Object> paramMap = (Map<String, Object>) args[1];
        Object ew = paramMap.get(Constants.WRAPPER);
        Object returnClass = paramMap.getOrDefault(JoinConstant.CLASS_PARAMS_NAME, null);

        // count查询不需要返回类型
        if (ms.getId().endsWith("joinSelectCount")) {
            if (!(ew instanceof SupportJoinWrapper)) {
                return invocation.proceed();
            }
        } else {
            // 其他查询需要校验返回类型
            if (!(ew instanceof SupportJoinWrapper) || returnClass == null) {
                return invocation.proceed();
            }
        }

        SupportJoinWrapper joinWrapper = (SupportJoinWrapper) ew;
        Class<?> classType = (Class<?>) returnClass;

        List<ResultMap> resultMaps = ms.getResultMaps();
        if (CollectionUtils.isEmpty(resultMaps)) {
            return invocation.proceed();
        }

        ResultMap resultMap = resultMaps.get(0);
        if (resultMap.getType() != JoinDefaultResultType.class) {
            return invocation.proceed();
        }

        try {
            args[0] = createMappedStatement(ms, joinWrapper, classType);
            return invocation.proceed();
        } catch (Exception e) {
            throw Exceptions.mpje("Failed to process join query for method: %s", e, ms.getId());
        }
    }

    /**
     * 创建新的MappedStatement
     */
    private MappedStatement createMappedStatement(MappedStatement ms,
                                                  SupportJoinWrapper joinWrapper,
                                                  Class<?> classType) {
        if (ms == null || joinWrapper == null || classType == null) {
            throw Exceptions.mpje("Invalid parameters for creating MappedStatement");
        }

        String msId = buildMappedStatementId(ms, joinWrapper, classType);

        // 从缓存获取MappedStatement
        Map<Configuration, MappedStatement> statementMap = getMybatisPlusJoinConfig().isUseMsCache()
                ? MS_CACHE.get(msId)
                : null;

        if (statementMap != null && statementMap.containsKey(ms.getConfiguration())) {
            return statementMap.get(ms.getConfiguration());
        }

        // 创建新的MappedStatement
        MappedStatement newMs = buildNewMappedStatement(ms, msId, joinWrapper, classType);

        // 缓存MappedStatement
        if (getMybatisPlusJoinConfig().isUseMsCache()) {
            synchronized (MS_CACHE) {
                MS_CACHE.computeIfAbsent(msId, k -> new ConcurrentHashMap<>())
                        .put(ms.getConfiguration(), newMs);
            }
        }

        return newMs;
    }

    /**
     * 构建MappedStatement ID
     */
    private String buildMappedStatementId(MappedStatement ms,
                                          SupportJoinWrapper joinWrapper,
                                          Class<?> classType) {
        if (!getMybatisPlusJoinConfig().isUseMsCache()) {
            return ms.getId();
        }
        return (ms.getId() + StringPool.COLON + classType.getName() +
                StringPool.UNDERSCORE + joinWrapper.getSqlSelect())
                .replaceAll("\\s+", "");
    }

    /**
     * 构建新的MappedStatement
     */
    private MappedStatement buildNewMappedStatement(MappedStatement ms, String id,
                                                    SupportJoinWrapper joinWrapper,
                                                    Class<?> classType) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id,
                ms.getSqlSource(), ms.getSqlCommandType())
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

        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(String.join(StringPool.COMMA, ms.getKeyProperties()));
        }

        ResultMap resultMap = createResultMap(ms, joinWrapper, classType);
        builder.resultMaps(Lists.newArrayList(resultMap));

        return builder.build();
    }

    /**
     * 创建ResultMap，处理并发情况
     */
    private ResultMap createResultMap(MappedStatement ms,
                                      SupportJoinWrapper joinWrapper,
                                      Class<?> classType) {
        if (ms == null || joinWrapper == null || classType == null) {
            throw Exceptions.mpje("Invalid parameters for creating ResultMap");
        }

        String resultMapId = buildResultMapId(ms, joinWrapper, classType);
        Configuration configuration = ms.getConfiguration();

        try {
            // 基础类型或Map类型的处理
            if (PropertyType.hasBaseType(classType) || ClassUtils.hasIncludeClass(classType, Map.class)) {
                return new ResultMap.Builder(configuration, resultMapId, classType,
                        Collections.emptyList()).build();
            }

            // 检查已存在的ResultMap
            if (configuration.hasResultMap(resultMapId)) {
                return configuration.getResultMap(resultMapId);
            }

            // 创建新的ResultMap
            synchronized (configuration) {
                if (configuration.hasResultMap(resultMapId)) {
                    return configuration.getResultMap(resultMapId);
                }

                List<ResultMapping> resultMappings = buildResultMappings(configuration,
                        joinWrapper.getFieldMappingList(),
                        classType);

                // 处理一对一映射
                processOneToOneMapping(configuration, resultMapId, joinWrapper, resultMappings);

                // 处理多对多映射
                processManyToManyMapping(configuration, resultMapId, joinWrapper, resultMappings);

                ResultMap resultMap = new ResultMap.Builder(configuration, resultMapId,
                        classType, resultMappings).build();

                try {
                    configuration.addResultMap(resultMap);
                } catch (IllegalArgumentException e) {
                    log.debug("ResultMap [{}] already exists, using existing one", resultMapId);
                    return configuration.getResultMap(resultMapId);
                }

                return resultMap;
            }
        } catch (Exception e) {
            throw Exceptions.mpje("Failed to create ResultMap for id: %s", e, resultMapId);
        }
    }

    /**
     * 处理一对一映射
     */
    private void processOneToOneMapping(Configuration configuration, String baseId,
                                        SupportJoinWrapper joinWrapper,
                                        List<ResultMapping> resultMappings) {
        List<OneToOneSelectBuild> oneToOneBuilds = joinWrapper.getOneToOneSelectBuildList();
        if (CollectionUtils.isEmpty(oneToOneBuilds)) {
            return;
        }

        for (OneToOneSelectBuild build : oneToOneBuilds) {
            String oneToOneId = baseId + StringPool.UNDERSCORE + build.getOneToOneField();
            oneToOneId = oneToOneId.replaceAll("\\s+", "");

            if (!configuration.hasResultMap(oneToOneId)) {
                ResultMap oneToOneResultMap = new ResultMap.Builder(configuration, oneToOneId,
                        build.getOneToOneClass(),
                        buildResultMappings(configuration, build.getBelongsColumns(),
                                build.getOneToOneClass())).build();
                addResultMapSafely(configuration, oneToOneResultMap, oneToOneId);
            }

            resultMappings.add(new ResultMapping.Builder(configuration, build.getOneToOneField())
                    .javaType(build.getOneToOneClass())
                    .nestedResultMapId(oneToOneId)
                    .build());
        }
    }

    /**
     * 处理多对多映射
     */
    private void processManyToManyMapping(Configuration configuration, String baseId,
                                          SupportJoinWrapper joinWrapper,
                                          List<ResultMapping> resultMappings) {
        List<ManyToManySelectBuild> manyToManyBuilds = joinWrapper.getManyToManySelectBuildList();
        if (CollectionUtils.isEmpty(manyToManyBuilds)) {
            return;
        }

        for (ManyToManySelectBuild build : manyToManyBuilds) {
            String manyToManyId = baseId + StringPool.UNDERSCORE + build.getManyToManyField();
            manyToManyId = manyToManyId.replaceAll("\\s+", "");

            if (!configuration.hasResultMap(manyToManyId)) {
                ResultMap manyToManyResultMap = new ResultMap.Builder(configuration, manyToManyId,
                        build.getManyToManyClass(),
                        buildResultMappings(configuration, build.getBelongsColumns(),
                                build.getManyToManyClass())).build();
                addResultMapSafely(configuration, manyToManyResultMap, manyToManyId);
            }

            resultMappings.add(new ResultMapping.Builder(configuration, build.getManyToManyField())
                    .javaType(build.getManyToManyPropertyType())
                    .nestedResultMapId(manyToManyId)
                    .build());
        }
    }

    /**
     * 安全地添加ResultMap
     */
    private synchronized void addResultMapSafely(Configuration configuration,
                                                 ResultMap resultMap,
                                                 String id) {
        if (configuration == null || resultMap == null || StringUtils.isBlank(id)) {
            throw Exceptions.mpje("Invalid parameters for adding ResultMap");
        }

        try {
            if (!configuration.hasResultMap(id)) {
                configuration.addResultMap(resultMap);
            }
        } catch (IllegalArgumentException e) {
            log.debug("ResultMap [{}] already exists, ignore", id);
        } catch (Exception e) {
            throw Exceptions.mpje("Failed to add ResultMap: %s", e, id);
        }
    }

    /**
     * 构建ResultMap ID
     */
    private String buildResultMapId(MappedStatement ms,
                                    SupportJoinWrapper joinWrapper,
                                    Class<?> classType) {
        return (ms.getId() + StringPool.COLON + classType.getName() +
                StringPool.UNDERSCORE + joinWrapper.getSqlSelect())
                .replaceAll("\\s+", "");
    }

    /**
     * 构建结果映射
     */
    private List<ResultMapping> buildResultMappings(Configuration configuration,
                                                    List<FieldMapping> fieldMappings,
                                                    Class<?> clz) {
        if (CollectionUtils.isEmpty(fieldMappings)) {
            throw Exceptions.mpje("No field mappings found for class: %s", clz.getName());
        }

        return fieldMappings.stream()
                .map(fieldMapping -> {
                    try {
                        return buildResultMapping(configuration, fieldMapping, clz);
                    } catch (Exception e) {
                        throw Exceptions.mpje("Failed to build ResultMapping", e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 构建单个结果映射
     */
    private ResultMapping buildResultMapping(Configuration configuration,
                                             FieldMapping fieldMapping,
                                             Class<?> clz) {
        try {
            Field field = ClassUtils.getDeclaredField(clz, fieldMapping.getFieldName());

            if (field == null) {
                return null;
            }

            if (fieldMapping.getTableFieldInfoExt() != null) {
                return fieldMapping.getTableFieldInfoExt().getResultMapping(configuration);
            }

            return new ResultMapping.Builder(configuration, field.getName(),
                    fieldMapping.getColumn(), field.getType()).build();
        } catch (Exception e) {
            throw Exceptions.mpje("Failed to build ResultMapping for field: %s", e,
                    fieldMapping.getFieldName());
        }
    }

    public MybatisPlusJoinConfig getMybatisPlusJoinConfig() {
        if (this.mybatisPlusJoinConfig == null) {
            this.mybatisPlusJoinConfig = MybatisPlusJoinConfig.builder().build();
        }
        return this.mybatisPlusJoinConfig;
    }

}

