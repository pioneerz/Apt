package com.example.compiler;

import com.example.annomation.DIActivity;
import com.example.annomation.DIView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author Administrator
 *         2018/4/9 0009.
 */
@AutoService(Processor.class)
public class DIProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(DIActivity.class.getCanonicalName());
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (TypeElement element: set) {
            Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(element);
            TypeElement typeElement;
            for (Element e: elementsAnnotatedWith) {
                mMessager.printMessage(Diagnostic.Kind.NOTE, "name = " + e.getSimpleName());
                typeElement = (TypeElement) e;
                List<? extends Element> allMembers = mElementUtils.getAllMembers(typeElement);
                MethodSpec.Builder bindViewMethodSpecBuilder = MethodSpec.methodBuilder("bindView")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeName.VOID)
                        .addParameter(ClassName.get(typeElement.asType()), "activity");
                for (Element item: allMembers) {
                    DIView diView = item.getAnnotation(DIView.class);
                    if (diView == null) {
                        continue;
                    }
                    bindViewMethodSpecBuilder.addStatement(
                            String.format("activity.%s = (%s) activity.findViewById(%s)",
                                    item.getSimpleName(),
                                    ClassName.get(item.asType()).toString(),
                                    diView.value()));
                }

                TypeSpec typeSpec = TypeSpec.classBuilder("DI"+e.getSimpleName())
                        /*.superclass(TypeName.get(typeElement.asType()))*/
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(bindViewMethodSpecBuilder.build())
                        .build();

                JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec).build();
                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return false;
    }
}
