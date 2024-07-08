package icu.mhb.mybatisplus.plugln.processor;

import icu.mhb.mybatisplus.plugln.constant.JoinConstant;
import icu.mhb.mybatisplus.plugln.tookit.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static icu.mhb.mybatisplus.plugln.constant.JoinConstant.*;
import static icu.mhb.mybatisplus.plugln.constant.StringPool.*;

/**
 * @author mahuibo
 * @Title: JoinChainModelProcessor
 * @email mhb0409@qq.com
 * @time 2024/6/21
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("icu.mhb.mybatisplus.plugln.annotations.JoinChainModel")
public class JoinChainModelProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "cssc");
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : annotatedElements) {
                if (element instanceof TypeElement) {
                    generateClass((TypeElement) element);
                }
            }
        }
        return true;
    }

    private void generateClass(TypeElement element) {
        String rawClassName = element.getSimpleName().toString();
        String rawPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String className = element.getSimpleName().toString() + "Chain";
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString() + DOT + CHAIN;

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className);
            try (PrintWriter writer = new PrintWriter(file.openWriter())) {
                StringBuilder sb = new StringBuilder();

                List<Element> elementList = getAllFields(element);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "所有的字段：：" + elementList.toString());
                // 生成基础信息
                buildClassBaseInfo(sb, packageName, rawPackageName, rawClassName, className);
                sb.append(NEWLINE);
                // 构建字段常量信息
                buildFieldConstantInfo(sb, elementList);
                sb.append(NEWLINE);
                // 构建有参数的信息
                buildConstructor(sb, className, rawClassName, true);
                sb.append(NEWLINE);
                // 构建无参数的信息
                buildConstructor(sb, className, rawClassName, false);
                sb.append(NEWLINE);
                // 生成setEntity 方法
                buildSetEntityMethod(sb, packageName, rawPackageName, rawClassName, className);
                sb.append(NEWLINE);
                // 构建无参数创建方法
                buildCreateMethod(sb, className, false);
                sb.append(NEWLINE);
                // 构建有参数创建方法
                buildCreateMethod(sb, className, true);
                sb.append(NEWLINE);

                // 构建方法
                buildFieldMethodInfo(sb, className, elementList);
                sb.append(RIGHT_BRACE);

                writer.println(sb.toString());
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to create source file for " + className);
        }
    }

    private List<Element> getAllFields(TypeElement element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "进入了getAllFields");
        List<Element> fields = element.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> !e.getModifiers().contains(Modifier.STATIC))
                .filter(e -> !e.getModifiers().contains(Modifier.TRANSIENT))
                .collect(Collectors.toList());
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, fields.toString());

        TypeMirror superClass = element.getSuperclass();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "superClass的類型是:" + superClass.toString());
        if (superClass instanceof DeclaredType) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "進去了，superClass的類型是:" + superClass.toString());
            TypeElement superTypeElement = (TypeElement) ((DeclaredType) superClass).asElement();
            fields.addAll(getAllFields(superTypeElement));
        }

        return fields;
    }

    /**
     * 构建字段方法
     */
    private void buildFieldMethodInfo(StringBuilder writer, String className, List<Element> elementList) {
        for (Element element : elementList) {
            String fieldName = element.getSimpleName().toString();
            HashMap<String, String> param = new HashMap<String, String>() {{
                put(Object.class.getSimpleName(), VAL);
            }};

            buildMethodHeader(writer, false, fieldName, className, param);
            writer.append(TAB).append(TAB).append(SUPER).append(DOT).append(ADD).append(LEFT_BRACKET).append(StringUtils.camelToUnderline(fieldName).toUpperCase()).append(COMMA).append(VAL).append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                    .append(TAB).append(TAB).append(RETURN_).append(SPACE).append(THIS).append(SEMICOLON).append(NEWLINE)
                    .append(TAB).append(RIGHT_BRACE).append(NEWLINE);

            // 无参
            buildMethodHeader(writer, false, fieldName, className, null);
            writer.append(TAB).append(TAB).append(RETURN_).append(SPACE).append(fieldName).append(LEFT_BRACKET).append(NULL).append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                    .append(TAB).append(RIGHT_BRACE).append(NEWLINE);

            // 函數方法
            buildMethodHeader(writer, false, UNDERSCORE + fieldName, CHAIN_FIELD_DATA, null);
            writer.append(TAB).append(TAB).append(RETURN_).append(SPACE).append(SUPER).append(DOT).append(BUILD_CHAIN_FIELD_DATA).append(LEFT_BRACKET).append(StringUtils.camelToUnderline(fieldName).toUpperCase()).append(COMMA).append(NULL).append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                    .append(TAB).append(RIGHT_BRACE).append(NEWLINE);

            // 函數方法
            buildMethodHeader(writer, false, UNDERSCORE + fieldName, CHAIN_FIELD_DATA, param);
            writer.append(TAB).append(TAB).append(RETURN_).append(SPACE).append(SUPER).append(DOT).append(BUILD_CHAIN_FIELD_DATA).append(LEFT_BRACKET).append(StringUtils.camelToUnderline(fieldName).toUpperCase()).append(COMMA).append(VAL).append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                    .append(TAB).append(RIGHT_BRACE).append(NEWLINE);
        }
    }

    /**
     * 构建字段常量
     */
    private void buildFieldConstantInfo(StringBuilder writer, List<Element> elements) {
        for (Element element : elements) {
            String fieldName = element.getSimpleName().toString();
            writer.append(TAB).append(PRIVATE).append(SPACE).append(STATIC).append(SPACE).append(FINAL).append(SPACE).append(STRING).append(SPACE).append(StringUtils.camelToUnderline(fieldName).toUpperCase()).append(SPACE).append(EQUALS).append(SPACE).append(QUOTE).append(fieldName).append(QUOTE).append(SEMICOLON).append(NEWLINE);
        }
    }

    /**
     * 构建基础信息
     *
     * @param writer      sb
     * @param packageName 包名
     * @param className   类名
     */
    private void buildClassBaseInfo(StringBuilder writer, String packageName, String rawPackageName, String rawClassName, String className) {
        writer.append(PACKAGE).append(SPACE).append(packageName).append(SEMICOLON).append(NEWLINE);
        writer.append(BASE_CHAIN_MODEL_PACKAGE).append(NEWLINE);
        writer.append(CHAIN_FIELD_DATA_PACKAGE).append(NEWLINE);
        writer.append(IMPORT).append(SPACE).append(rawPackageName).append(DOT).append(rawClassName).append(SEMICOLON).append(NEWLINE);
        writer.append(JoinConstant.GENERATION_DESC);
        writer.append(PUBLIC).append(SPACE).append(CLASS).append(SPACE).append(className).append(SPACE).append(EXTENDS).append(SPACE).append(GENERATION_BASE_MODEL_NAME).append(LEFT_CHEV).append(className).append(RIGHT_CHEV).append(LEFT_BRACE).append(NEWLINE);
        writer.append(NEWLINE);
    }

    /**
     * 构建 set实体方法
     *
     * @param writer      sb
     * @param packageName 包名
     * @param className   类名
     */
    private void buildSetEntityMethod(StringBuilder writer, String packageName, String rawPackageName, String rawClassName, String className) {
        buildMethodHeader(writer, false, SET_ENTITY, className, new HashMap<String, String>() {{
            put(rawClassName, VAL);
        }});

        writer.append(TAB).append(TAB).append(SUPER).append(DOT).append(SET_ENTITY).append(LEFT_BRACKET).append(VAL).append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                .append(TAB).append(TAB).append(RETURN_).append(SPACE).append(THIS).append(SEMICOLON).append(NEWLINE)
                .append(TAB).append(RIGHT_BRACE).append(NEWLINE);
    }

    /**
     * 构建无参数的创建方法
     */
    private void buildCreateMethod(StringBuilder writer, String className, boolean isParam) {
        Map<String, String> params = null;
        if (isParam) {
            params = new HashMap<String, String>() {{
                put(STRING, ALIAS);
            }};
        }
        buildMethodHeader(writer, true, CREATE, className, params);
        writer.append(TAB).append(TAB).append(RETURN_).append(SPACE).append(NEW).append(SPACE).append(className).append(LEFT_BRACKET);

        if (isParam) {
            writer.append(ALIAS);
        }

        writer.append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                .append(TAB).append(RIGHT_BRACE).append(NEWLINE);
    }

    /**
     * 构建有参数的信息
     */
    private void buildConstructor(StringBuilder writer, String className, String rawClassName, boolean isParam) {
        Map<String, String> params = null;
        if (isParam) {
            params = new HashMap<String, String>() {{
                put(STRING, ALIAS);
            }};
        }
        buildMethodHeader(writer, false, className, EMPTY, params);

        writer.append(TAB).append(TAB).append(SUPER).append(LEFT_BRACKET);

        if (isParam) {
            writer.append(ALIAS).append(COMMA);
        }
        writer.append(rawClassName).append(DOT).append(CLASS)
                .append(RIGHT_BRACKET).append(SEMICOLON).append(NEWLINE)
                .append(TAB).append(RIGHT_BRACE).append(NEWLINE);
    }


    /**
     * 构建方法头信息
     *
     * @param writer     sb
     * @param isStatic   是否静态方法
     * @param methodName 方法名
     * @param returnName 返回类型
     * @param params     参数
     */
    private void buildMethodHeader(StringBuilder writer, boolean isStatic, String methodName, String returnName, Map<String, String> params) {
        writer.append(TAB).append(PUBLIC).append(SPACE);
        if (isStatic) {
            writer.append(STATIC).append(SPACE);
        }
        writer.append(returnName).append(SPACE).append(methodName).append(LEFT_BRACKET);
        String paramsStr = "";
        if (null != params && !params.isEmpty()) {
            paramsStr = params.entrySet().stream()
                    .map(e -> e.getKey() + SPACE + e.getValue())
                    .collect(Collectors.joining(COMMA));
        }
        writer.append(paramsStr).append(RIGHT_BRACKET).append(SPACE).append(LEFT_BRACE).append(NEWLINE);
    }


    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
