package icu.mhb.mybatisplus.plugln.interceptor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import icu.mhb.mybatisplus.plugln.extend.Joins;
import org.springframework.beans.BeanUtils;
import icu.mhb.mybatisplus.plugln.annotations.FieldTrans;
import icu.mhb.mybatisplus.plugln.annotations.ManyToMany;
import icu.mhb.mybatisplus.plugln.annotations.OneToOne;
import icu.mhb.mybatisplus.plugln.config.ConfigUtil;
import icu.mhb.mybatisplus.plugln.config.MpjConfig;
import icu.mhb.mybatisplus.plugln.config.MybatisPlusJoinConfig;
import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.core.support.SupportJoinWrapper;
import icu.mhb.mybatisplus.plugln.entity.FieldMapping;
import icu.mhb.mybatisplus.plugln.entity.ManyToManySelectBuild;
import icu.mhb.mybatisplus.plugln.entity.OneToOneSelectBuild;
import icu.mhb.mybatisplus.plugln.enums.JoinSqlMethod;
import icu.mhb.mybatisplus.plugln.enums.PropertyType;
import icu.mhb.mybatisplus.plugln.exception.Exceptions;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultResultType;
import icu.mhb.mybatisplus.plugln.tookit.ClassUtils;
import icu.mhb.mybatisplus.plugln.tookit.Lambdas;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;
import java.util.*;
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

        if (!ms.getId().endsWith(JoinSqlMethod.JOIN_SELECT_COUNT.getMethod())
                && !ms.getId().endsWith(JoinSqlMethod.JOIN_SELECT_LIST.getMethod())
                && !ms.getId().endsWith(JoinSqlMethod.JOIN_SELECT_PAGE.getMethod())
                && !ms.getId().endsWith(JoinSqlMethod.JOIN_SELECT_ONE.getMethod())) {
            return invocation.proceed();
        }

        if (!(args[1] instanceof Map)) {
            return invocation.proceed();
        }

        Map<String, Object> paramMap = (Map<String, Object>) args[1];
        Object ew = paramMap.getOrDefault(Constants.WRAPPER, null);
        Object returnClass = paramMap.getOrDefault(JoinConstant.CLASS_PARAMS_NAME, null);

        if (null == ew) {
            return invocation.proceed();
        }

        // count查询不需要返回类型
        if (ms.getId().endsWith(JoinSqlMethod.JOIN_SELECT_COUNT.getMethod())) {
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

        args[0] = createMappedStatement(ms, joinWrapper, classType);
        Object proceed = invocation.proceed();

        if (ObjectUtils.isNotEmpty(proceed)) {
            processAnnotationMapping(proceed, classType);
        }

        return proceed;
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
        Map<Configuration, MappedStatement> statementMap = isUseMsCache()
                ? MS_CACHE.get(msId)
                : null;

        if (statementMap != null && statementMap.containsKey(ms.getConfiguration())) {
            return statementMap.get(ms.getConfiguration());
        }

        // 创建新的MappedStatement
        MappedStatement newMs = buildNewMappedStatement(ms, msId, joinWrapper, classType);

        // 缓存MappedStatement
        if (isUseMsCache()) {
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
        if (!isUseMsCache()) {
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

            // 使用getTargetColumn去除column-format格式化符号（如反引号）
            // 因为数据库返回的列名是原始字段名，不包含格式化符号
            String columnName = com.baomidou.mybatisplus.core.toolkit.StringUtils.getTargetColumn(fieldMapping.getColumn());

            return new ResultMapping.Builder(configuration, field.getName(),
                    columnName, field.getType()).build();
        } catch (Exception e) {
            throw Exceptions.mpje("Failed to build ResultMapping for field: %s", e,
                    fieldMapping.getFieldName());
        }
    }

    private boolean isUseMsCache() {
        if (this.mybatisPlusJoinConfig == null) {
            MpjConfig config = ConfigUtil.getConfig();
            return config.isUseMsCache();
        }

        return this.mybatisPlusJoinConfig.isUseMsCache();
    }

    /**
     * 处理注解映射 (OneToOne 和 ManyToMany)
     */
    private void processAnnotationMapping(Object result, Class<?> returnClass) {
        try {
            Collection<?> dataList = null;

            // 判断返回结果类型
            if (result instanceof Collection) {
                dataList = (Collection<?>) result;
            } else if (result instanceof IPage) {
                dataList = ((IPage<?>) result).getRecords();
            } else {
                // 单个对象,转为列表处理
                dataList = Collections.singletonList(result);
            }

            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }

            // 获取字段映射
            Map<String, Field> fieldMap = ReflectionKit.getFieldMap(returnClass);

            // 收集OneToOne、ManyToMany和FieldTrans注解
            Map<String, OneToOneAnnotationInfo> oneToOneMap = new HashMap<>();
            Map<String, ManyToManyAnnotationInfo> manyToManyMap = new HashMap<>();
            Map<String, FieldTransAnnotationInfo> fieldTransMap = new HashMap<>();

            fieldMap.forEach((fieldName, field) -> {
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                if (oneToOne != null) {
                    oneToOneMap.put(fieldName, new OneToOneAnnotationInfo(field, oneToOne));
                }

                ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                if (manyToMany != null) {
                    manyToManyMap.put(fieldName, new ManyToManyAnnotationInfo(field, manyToMany));
                }

                FieldTrans fieldTrans = field.getAnnotation(FieldTrans.class);
                if (fieldTrans != null) {
                    fieldTransMap.put(fieldName, new FieldTransAnnotationInfo(field, fieldTrans));
                }
            });

            // 处理OneToOne注解
            if (!oneToOneMap.isEmpty()) {
                processOneToOneAnnotation(dataList, oneToOneMap, fieldMap);
            }

            // 处理ManyToMany注解
            if (!manyToManyMap.isEmpty()) {
                processManyToManyAnnotation(dataList, manyToManyMap, fieldMap);
            }

            // 处理FieldTrans注解
            if (!fieldTransMap.isEmpty()) {
                processFieldTransAnnotation(dataList, fieldTransMap, fieldMap);
            }

        } catch (Exception e) {
            log.error("处理注解映射失败", e);
            throw Exceptions.mpje("处理注解映射失败", e);
        }
    }

    /**
     * 处理OneToOne注解
     */
    private void processOneToOneAnnotation(Collection<?> dataList,
                                           Map<String, OneToOneAnnotationInfo> oneToOneMap,
                                           Map<String, Field> fieldMap) {
        for (Map.Entry<String, OneToOneAnnotationInfo> entry : oneToOneMap.entrySet()) {
            String fieldName = entry.getKey();
            OneToOneAnnotationInfo info = entry.getValue();
            OneToOne oneToOne = info.annotation;
            Field annotatedField = info.field;  // 被标注注解的字段,用于取值

            try {
                // 获取ref字段(用于赋值结果)
                Field refField = fieldMap.get(oneToOne.ref());
                if (refField == null) {
                    log.warn("OneToOne注解的ref字段[{}]不存在", oneToOne.ref());
                    continue;
                }
                refField.setAccessible(true);
                annotatedField.setAccessible(true);

                // 收集所有被标注字段的值(用于查询)
                Set<Object> queryValues = new HashSet<>();
                for (Object data : dataList) {
                    Object value = annotatedField.get(data);
                    if (value != null) {
                        queryValues.add(value);
                    }
                }

                if (queryValues.isEmpty()) {
                    continue;
                }

                // 获取目标表信息
                TableInfo tableInfo = TableInfoHelper.getTableInfo(oneToOne.targetSub());
                String targetIdField = StringUtils.isNotBlank(oneToOne.targetSubId())
                        ? oneToOne.targetSubId()
                        : tableInfo.getKeyProperty();

                Class<?> refFieldType = refField.getType();

                // 查询关联数据
                List<?> subList = Joins.of(oneToOne.targetSub())
                        .in(Lambdas.getSFunction(oneToOne.targetSub(),
                                        getFieldType(oneToOne.targetSub(), targetIdField),
                                        targetIdField),
                                queryValues)
                        .joinList(refFieldType);

                if (CollectionUtils.isEmpty(subList)) {
                    continue;
                }

                // 获取目标表的ID字段
                Field targetIdFieldObj = ClassUtils.getDeclaredField(refFieldType, targetIdField);
                if (targetIdFieldObj == null) {
                    log.warn("目标表[{}]的ID字段[{}]不存在", refFieldType.getName(), targetIdField);
                    continue;
                }
                targetIdFieldObj.setAccessible(true);

                // 检查ref字段的类型

                boolean needConvert = !refFieldType.equals(oneToOne.targetSub());

                // 构建Map用于快速查找 (targetId -> subData)
                Map<Object, Object> subDataMap = new HashMap<>();
                for (Object subData : subList) {
                    Object subIdValue = targetIdFieldObj.get(subData);
                    if (subIdValue != null) {
                        // 如果ref字段类型与targetSub不一致，需要转换
                        if (needConvert) {
                            Object convertedData = convertToVO(subData, refFieldType);
                            if (convertedData != null) {
                                subDataMap.put(subIdValue, convertedData);
                            }
                        } else {
                            subDataMap.put(subIdValue, subData);
                        }
                    }
                }

                // 根据被标注字段的值匹配,将结果赋值到ref字段
                for (Object data : dataList) {
                    Object queryValue = annotatedField.get(data);
                    if (queryValue != null && subDataMap.containsKey(queryValue)) {
                        refField.set(data, subDataMap.get(queryValue));
                    }
                }

            } catch (Exception e) {
                log.error("处理OneToOne注解[{}]失败", fieldName, e);
            }
        }
    }

    /**
     * 处理ManyToMany注解
     */
    private void processManyToManyAnnotation(Collection<?> dataList,
                                             Map<String, ManyToManyAnnotationInfo> manyToManyMap,
                                             Map<String, Field> fieldMap) {
        for (Map.Entry<String, ManyToManyAnnotationInfo> entry : manyToManyMap.entrySet()) {
            String fieldName = entry.getKey();
            ManyToManyAnnotationInfo info = entry.getValue();
            ManyToMany manyToMany = info.annotation;
            Field annotatedField = info.field;  // 被标注注解的字段,用于取值

            try {
                // 获取ref字段(用于赋值结果)
                Field refField = fieldMap.get(manyToMany.ref());
                if (refField == null) {
                    log.warn("ManyToMany注解的ref字段[{}]不存在", manyToMany.ref());
                    continue;
                }
                refField.setAccessible(true);
                annotatedField.setAccessible(true);

                // 收集所有被标注字段的值(用于查询)
                Set<Object> queryValues = new HashSet<>();
                for (Object data : dataList) {
                    Object value = annotatedField.get(data);
                    if (value != null) {
                        queryValues.add(value);
                    }
                }

                if (queryValues.isEmpty()) {
                    continue;
                }

                // 获取目标表信息
                TableInfo tableInfo = TableInfoHelper.getTableInfo(manyToMany.targetSub());
                String targetIdField = StringUtils.isNotBlank(manyToMany.targetSubId())
                        ? manyToMany.targetSubId()
                        : tableInfo.getKeyProperty();

                Class<?> listGenericType = getListGenericType(refField);
                if (null == listGenericType) {
                    log.warn("请定义{}字段的List泛型！", refField.toString());
                    continue;
                }

                // 查询关联数据
                List<?> subList = Joins.of(manyToMany.targetSub())
                        .in(Lambdas.getSFunction(manyToMany.targetSub(),
                                        getFieldType(manyToMany.targetSub(), targetIdField),
                                        targetIdField),
                                queryValues)
                        .joinList(listGenericType);

                if (CollectionUtils.isEmpty(subList)) {
                    continue;
                }

                // 获取目标表的ID字段
                Field targetIdFieldObj = ClassUtils.getDeclaredField(listGenericType, targetIdField);
                if (targetIdFieldObj == null) {
                    log.warn("目标表[{}]的ID字段[{}]不存在", listGenericType.getName(), targetIdField);
                    continue;
                }
                targetIdFieldObj.setAccessible(true);

                // 获取List的泛型类型
                boolean needConvert = listGenericType != null && !listGenericType.equals(manyToMany.targetSub());

                // 构建Map用于快速查找 (targetId -> List<subData>)
                Map<Object, List<Object>> subDataMap = new HashMap<>();
                for (Object subData : subList) {
                    Object subIdValue = targetIdFieldObj.get(subData);
                    if (subIdValue != null) {
                        // 如果List泛型类型与targetSub不一致，需要转换
                        Object dataToAdd = subData;
                        if (needConvert) {
                            dataToAdd = convertToVO(subData, listGenericType);
                            if (dataToAdd == null) {
                                continue;
                            }
                        }
                        subDataMap.computeIfAbsent(subIdValue, k -> new ArrayList<>()).add(dataToAdd);
                    }
                }

                // 根据被标注字段的值匹配,将结果赋值到ref字段
                for (Object data : dataList) {
                    Object queryValue = annotatedField.get(data);
                    if (queryValue != null) {
                        List<Object> subDataList = subDataMap.get(queryValue);
                        if (subDataList != null) {
                            refField.set(data, subDataList);
                        } else {
                            refField.set(data, new ArrayList<>());
                        }
                    }
                }

            } catch (Exception e) {
                log.error("处理ManyToMany注解[{}]失败", fieldName, e);
            }
        }
    }

    /**
     * 处理FieldTrans注解
     * 从子表中查询数据，提取指定字段并映射到当前对象的字段上
     */
    private void processFieldTransAnnotation(Collection<?> dataList,
                                             Map<String, FieldTransAnnotationInfo> fieldTransMap,
                                             Map<String, Field> fieldMap) {
        for (Map.Entry<String, FieldTransAnnotationInfo> entry : fieldTransMap.entrySet()) {
            String fieldName = entry.getKey();
            FieldTransAnnotationInfo info = entry.getValue();
            FieldTrans fieldTrans = info.annotation;
            Field annotatedField = info.field;  // 被标注注解的字段,用于取值

            try {
                annotatedField.setAccessible(true);

                // 解析 tagetFiledOrRef 配置 {"userName:userNameStr", "age:userAge"}
                Map<String, String> fieldMappingConfig = new HashMap<>();
                for (String mapping : fieldTrans.tagetFiledOrRef()) {
                    String[] parts = mapping.split(":");
                    if (parts.length == 2) {
                        String sourceField = parts[0].trim();  // 子表字段
                        String targetField = parts[1].trim();  // 当前对象字段
                        fieldMappingConfig.put(sourceField, targetField);
                    } else {
                        log.warn("FieldTrans配置格式错误: {}", mapping);
                    }
                }

                if (fieldMappingConfig.isEmpty()) {
                    log.warn("FieldTrans注解[{}]没有有效的字段映射配置", fieldName);
                    continue;
                }

                // 收集所有被标注字段的值(用于查询)
                Set<Object> queryValues = new HashSet<>();
                for (Object data : dataList) {
                    Object value = annotatedField.get(data);
                    if (value != null) {
                        queryValues.add(value);
                    }
                }

                if (queryValues.isEmpty()) {
                    continue;
                }

                // 获取目标表信息
                TableInfo tableInfo = TableInfoHelper.getTableInfo(fieldTrans.targetSub());
                String targetIdField = StringUtils.isNotBlank(fieldTrans.targetSubId())
                        ? fieldTrans.targetSubId()
                        : tableInfo.getKeyProperty();

                // 查询关联数据
                List<?> subList = Db.lambdaQuery(fieldTrans.targetSub())
                        .in(Lambdas.getSFunction(fieldTrans.targetSub(),
                                        getFieldType(fieldTrans.targetSub(), targetIdField),
                                        targetIdField),
                                queryValues)
                        .list();

                if (CollectionUtils.isEmpty(subList)) {
                    continue;
                }

                // 获取目标表的ID字段
                Field targetIdFieldObj = ClassUtils.getDeclaredField(fieldTrans.targetSub(), targetIdField);
                if (targetIdFieldObj == null) {
                    log.warn("目标表[{}]的ID字段[{}]不存在", fieldTrans.targetSub().getName(), targetIdField);
                    continue;
                }
                targetIdFieldObj.setAccessible(true);

                // 构建Map用于快速查找 (targetId -> subData)
                Map<Object, Object> subDataMap = new HashMap<>();
                for (Object subData : subList) {
                    Object subIdValue = targetIdFieldObj.get(subData);
                    if (subIdValue != null) {
                        subDataMap.put(subIdValue, subData);
                    }
                }

                // 遍历每条数据,进行字段映射
                for (Object data : dataList) {
                    Object queryValue = annotatedField.get(data);
                    if (queryValue != null && subDataMap.containsKey(queryValue)) {
                        Object subData = subDataMap.get(queryValue);

                        // 遍历字段映射配置,从subData中提取字段值并设置到data中
                        for (Map.Entry<String, String> mappingEntry : fieldMappingConfig.entrySet()) {
                            String sourceFieldName = mappingEntry.getKey();  // 子表字段名
                            String targetFieldName = mappingEntry.getValue();  // 当前对象字段名

                            try {
                                // 从子表对象中获取源字段
                                Field sourceField = ClassUtils.getDeclaredField(fieldTrans.targetSub(), sourceFieldName);
                                if (sourceField == null) {
                                    log.warn("子表[{}]不存在字段[{}]", fieldTrans.targetSub().getName(), sourceFieldName);
                                    continue;
                                }
                                sourceField.setAccessible(true);
                                Object sourceValue = sourceField.get(subData);

                                // 设置到当前对象的目标字段
                                Field targetField = fieldMap.get(targetFieldName);
                                if (targetField == null) {
                                    log.warn("当前对象不存在字段[{}]", targetFieldName);
                                    continue;
                                }
                                targetField.setAccessible(true);

                                // 类型检查
                                if (sourceValue != null && targetField.getType().isAssignableFrom(sourceField.getType())) {
                                    targetField.set(data, sourceValue);
                                } else if (sourceValue != null) {
                                    log.debug("字段类型不匹配: {} -> {}", sourceField.getType(), targetField.getType());
                                }
                            } catch (Exception e) {
                                log.error("字段映射失败: {} -> {}", sourceFieldName, targetFieldName, e);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.error("处理FieldTrans注解[{}]失败", fieldName, e);
            }
        }
    }

    /**
     * 获取字段类型
     */
    private Class<?> getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = ClassUtils.getDeclaredField(clazz, fieldName);
            if (field != null) {
                return field.getType();
            }
        } catch (Exception e) {
            log.warn("获取字段类型失败: {}.{}", clazz.getName(), fieldName);
        }
        return Object.class;
    }

    /**
     * 获取List字段的泛型类型
     * 使用MyBatis-Plus的GenericTypeUtils获取泛型类型
     */
    private Class<?> getListGenericType(Field field) {
        try {
            if (field.getType().isAssignableFrom(List.class)) {
                java.lang.reflect.Type genericType = field.getGenericType();
                if (genericType instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType parameterizedType =
                            (java.lang.reflect.ParameterizedType) genericType;
                    java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        return (Class<?>) actualTypeArguments[0];
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取List泛型类型失败: {}", field.getName(), e);
        }
        return null;
    }


    /**
     * 将实体对象转换为VO对象
     * 使用Spring的BeanUtils进行属性拷贝
     */
    private Object convertToVO(Object source, Class<?> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }

        try {
            // 创建目标对象
            Object target = targetClass.getDeclaredConstructor().newInstance();

            // 使用Spring的BeanUtils拷贝属性
            BeanUtils.copyProperties(source, target);

            return target;
        } catch (Exception e) {
            log.error("转换对象失败: {} -> {}", source.getClass().getName(), targetClass.getName(), e);
            return null;
        }
    }

    /**
     * OneToOne注解信息
     */
    private static class OneToOneAnnotationInfo {
        Field field;
        OneToOne annotation;

        OneToOneAnnotationInfo(Field field, OneToOne annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }

    /**
     * ManyToMany注解信息
     */
    private static class ManyToManyAnnotationInfo {
        Field field;
        ManyToMany annotation;

        ManyToManyAnnotationInfo(Field field, ManyToMany annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }

    /**
     * FieldTrans注解信息
     */
    private static class FieldTransAnnotationInfo {
        Field field;
        FieldTrans annotation;

        FieldTransAnnotationInfo(Field field, FieldTrans annotation) {
            this.field = field;
            this.annotation = annotation;
        }
    }


}

