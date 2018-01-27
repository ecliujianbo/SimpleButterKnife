package com.liu.simple_butterknife_compiler;

import com.google.auto.service.AutoService;
import com.liu.simple_butterknife_annotation.SimpleBindString;
import com.liu.simple_butterknife_annotation.SimpleBindView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class SimpleButterKnifeProcessor extends AbstractProcessor {
    /**
     * 用于生成java文件
     */
    private Filer filer;

    /**
     * 被注解处理工具调用.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //提供了Element，Filer，Messager等工具
        filer = processingEnv.getFiler();
    }

    /**
     * 支持的JDK版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 确认我们处理的注解
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(SimpleBindView.class.getCanonicalName());
        return types;
    }

    /**
     * 核心类，在每个activity生成内部类，并初始化控件
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取所有使用SimpleBindView的集合
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(SimpleBindView.class);
        //key是activity的名字
        //value是这个activity里面所有使用SimpleBindView注解的集合
        Map<String, List<VariableElement>> elementMap = handleElementIntoMap(elementSet);

        //开始准备写XXActivity$ViewBinder内部类的java文件
        Iterator<String> iterator = elementMap.keySet().iterator();
        while (iterator.hasNext()) {
            String activityName = iterator.next();
            //获取activity的所有元素
            List<VariableElement> variableElements = elementMap.get(activityName);
            //获取包名
            String packageName = getPackageName(variableElements.get(0));
            //获取我们要写的内部类java文件名
            String newActivityName = activityName + "$" + getInnerClassName();

            //用流来写内部类的文件
            createInnerClassFile(activityName, packageName, newActivityName, variableElements);
        }

        return false;
    }

    /**
     * 按照格式写内部类。
     * package xxx;
     * import xxx.SimpleViewBinder;
     * public class XXXActivity$SimpleViewBinder<T extends XXXActivity> implements SimpleViewBinder<T> {
     * public void bind(XXXActivity target){
     * //初始化控件
     * target.xxx = (xxx)target.findViewById(id);
     * ...
     * }
     * }
     */
    private void createInnerClassFile(String activityName, String packageName, String newActivityName, List<VariableElement> variableElements) {
        Writer writer;
        try {
            String simpleActivityName = variableElements.get(0).getEnclosingElement().getSimpleName().toString() + "$" + getInnerClassName();
            JavaFileObject sourceFile = filer.createSourceFile(newActivityName, variableElements.get(0).getEnclosingElement());
            writer = sourceFile.openWriter();
            writer.write("package " + packageName + " ;");
            writer.write("\n");
            writer.write("import com.liu.simple_butterknife_annotation." + getInnerClassName() + " ;");
            writer.write("\n");
            writer.write("public class " + simpleActivityName + " implements " + getInnerClassName() + "<" + activityName + ">{");
            writer.write("\n");
            writer.write("public void bind(" + activityName + " target){");
            writer.write("\n");
            //初始化控件
            for (VariableElement variableElement : variableElements) {
                SimpleBindView simpleBindView = variableElement.getAnnotation(SimpleBindView.class);
                //获取控件的名字
                String fileName = variableElement.getSimpleName().toString();
                //获取控件的类型(Button,TextView)
                TypeMirror typeMirror = variableElement.asType();
                writer.write("target." + fileName + " = (" + typeMirror + ")target.findViewById(" + simpleBindView.value() + ");");
                writer.write("\n");
            }


            writer.write("}");
            writer.write("\n");
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 内部类的名字
     */
    private String getInnerClassName() {
        return "SimpleViewBinder";
    }

    /**
     * 把所有的Element存放到map里
     */
    private Map<String, List<VariableElement>> handleElementIntoMap(Set<? extends Element> elementSet) {
        Map<String, List<VariableElement>> elementMap = new HashMap<>();
        for (Element element : elementSet) {
            VariableElement variableElement = (VariableElement) element;
            //获取activity名字
            String activityName = getActivityName(variableElement);
            //通过activity名字，获取集合
            List<VariableElement> elementList = elementMap.get(activityName);
            if (elementList == null) {
                elementList = new ArrayList<>();
                elementMap.put(activityName, elementList);
            }
            elementList.add(variableElement);
        }
        return elementMap;
    }

    /**
     * 通过VariableElement获取包名
     */
    private String getPackageName(VariableElement element) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        return processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
    }

    /**
     * 通过VariableElement获取所在的activity名字
     */
    private String getActivityName(VariableElement element) {
        String packageName = getPackageName(element);
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        return packageName + "." + typeElement.getSimpleName().toString();
    }
}
