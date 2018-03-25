package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.psliusar.layers.binder.processor.save.SaveFieldProcessor;
import com.psliusar.layers.binder.processor.view.ViewFieldProcessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class LayersAnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Map<String, BinderClassHolder> classHolders = new HashMap<>();
    private final List<Processor> processors = new ArrayList<>();
    private final Set<Class<? extends Annotation>> annotations = new HashSet<>();

    public LayersAnnotationProcessor() {
        processors.add(new ViewFieldProcessor(this));
        processors.add(new SaveFieldProcessor(this));
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Processor processor : processors) {
            final Class<? extends Annotation> ann = processor.getAnnotation();
            annotations.add(ann);
            types.add(ann.getCanonicalName());
        }
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        try {
            for (Processor processor : processors) {
                processor.process(env);
            }

            for (Map.Entry<String, BinderClassHolder> entry : classHolders.entrySet()) {
                final BinderClassHolder holder = entry.getValue();

                if (holder.isFileWritten()) {
                    continue;
                }

                holder.setFileWritten();

                final String filename = entry.getKey();
                try {
                    JavaFileObject jfo = filer.createSourceFile(filename);
                    Writer writer = jfo.openWriter();
                    writer.write(holder.getJavaClassFile());
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    logError("Exception occurred while attempting to write view binder %s.")
                            .arguments(filename)
                            .throwable(e)
                            .print();
                }
            }
        } catch (Throwable t) {
            logError("Exception occurred while processing project.")
                    .throwable(t)
                    .print();
            return false;
        }
        return true;
    }

    @NonNull
    public Elements getElementUtils() {
        return elementUtils;
    }

    @NonNull
    public Types getTypeUtils() {
        return typeUtils;
    }

    @NonNull
    public Map<String, BinderClassHolder> getClassHolders() {
        return classHolders;
    }

    @NonNull
    public String resolveParentClassName(@NonNull TypeElement source) {
        if (source.getKind() == ElementKind.FIELD) {
            source = (TypeElement) source.getEnclosingElement().asType();
        }
        if (source.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException("Only CLASS and FIELD elements are supported");
        }
        TypeMirror superclass = source.getSuperclass();
        // TODO cache classes graph
        while (superclass.getKind() != TypeKind.NONE) {
            final TypeElement superclassElement = (TypeElement) typeUtils.asElement(superclass);
            final String superclassPackageName = elementUtils.getPackageOf(superclassElement)
                    .getQualifiedName()
                    .toString();
            final String superclassClassName = Processor.getSimpleClassName(superclassElement, superclassPackageName);
            final String className = Processor.getBinderClassName(superclassPackageName, superclassClassName);
            if (elementUtils.getTypeElement(className) != null) {
                return className;
            }

            superclass = superclassElement.getSuperclass();
        }

        return "com.psliusar.layers.binder.ObjectBinder";
    }

    private boolean isAnnotated(@NonNull Element element) {
        for (Class<? extends Annotation> ann : annotations) {
            if (element.getAnnotation(ann) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnnotated(@NonNull List<? extends Element> elements) {
        for (Element element : elements) {
            if (isAnnotated(element)) {
                return true;
            }
        }
        return false;
    }

    public Logg logError(String message) {
        return new Logg(processingEnv).kind(Kind.ERROR).message(message);
    }

    public Logg logWarning(String message) {
        return new Logg(processingEnv).kind(Kind.WARNING).message(message);
    }

    public static class Logg {

        private final ProcessingEnvironment env;
        private String message;
        private Object[] args;
        private Element element;
        private Kind kind;
        private Throwable throwable;

        public Logg(@NonNull ProcessingEnvironment env) {
            this.env = env;
        }

        public Logg message(@Nullable String message) {
            this.message = message;
            return this;
        }

        public Logg arguments(Object... args) {
            this.args = args;
            return this;
        }

        public Logg element(@Nullable Element element) {
            this.element = element;
            return this;
        }

        public Logg kind(@NonNull Kind kind) {
            this.kind = kind;
            return this;
        }

        public Logg throwable(@Nullable Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public void print() {
            if (throwable != null) {
                final StringWriter stackTrace = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stackTrace));
                message = (message == null ? "" : message + " ") + "Stack trace: %s";

                if (args == null) {
                    args = new Object[1];
                } else {
                    final Object[] oldArgs = args;
                    args = new Object[oldArgs.length + 1];
                    System.arraycopy(oldArgs, 0, args, 0, oldArgs.length);
                }
                args[args.length - 1] = stackTrace.toString();
            }

            final Messager messager = env.getMessager();
            if (element == null) {
                messager.printMessage(kind, String.format(message, args));
            } else {
                messager.printMessage(kind, String.format(message, args), element);
            }
        }
    }
}
