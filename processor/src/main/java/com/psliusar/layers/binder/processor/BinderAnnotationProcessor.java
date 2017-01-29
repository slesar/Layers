package com.psliusar.layers.binder.processor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BinderAnnotationProcessor extends AbstractProcessor {

    private static final BinderProcessor[] PROCESSORS = {
            new FieldProcessor()
    };

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    private Map<String, BinderClassHolder> classHolders = new HashMap<>();

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
        for (BinderProcessor processor : PROCESSORS) {
            types.add(processor.getAnnotation().getCanonicalName());
        }
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        try {
            for (BinderProcessor processor : PROCESSORS) {
                processor.process(this, env, elementUtils, typeUtils, classHolders);
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

    protected Logg logError(String message) {
        return new Logg(processingEnv).kind(Kind.ERROR).message(message);
    }

    protected Logg logWarning(String message) {
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
