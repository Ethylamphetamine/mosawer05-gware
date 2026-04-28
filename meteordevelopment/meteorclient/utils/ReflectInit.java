/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class ReflectInit {
    private static final List<Reflections> reflections = new ArrayList<Reflections>();

    private ReflectInit() {
    }

    public static void registerPackages() {
        for (MeteorAddon addon : AddonManager.ADDONS) {
            try {
                ReflectInit.add(addon);
            }
            catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        }
    }

    private static void add(MeteorAddon addon) {
        String pkg = addon.getPackage();
        if (pkg == null || pkg.isBlank()) {
            return;
        }
        reflections.add(new Reflections(pkg, Scanners.MethodsAnnotated));
    }

    public static void init(Class<? extends Annotation> annotation) {
        for (Reflections reflection : reflections) {
            Method m;
            Set<Method> initTasks = reflection.getMethodsAnnotatedWith(annotation);
            if (initTasks == null) {
                return;
            }
            Map<Class<?>, List<Method>> byClass = initTasks.stream().collect(Collectors.groupingBy(Method::getDeclaringClass));
            HashSet<Method> left = new HashSet<Method>(initTasks);
            while ((m = (Method)left.stream().findAny().orElse(null)) != null) {
                ReflectInit.reflectInit(m, annotation, left, byClass);
            }
        }
    }

    private static <T extends Annotation> void reflectInit(Method task, Class<T> annotation, Set<Method> left, Map<Class<?>, List<Method>> byClass) {
        left.remove(task);
        for (Class<?> clazz : ReflectInit.getDependencies(task, annotation)) {
            for (Method m : byClass.getOrDefault(clazz, Collections.emptyList())) {
                if (!left.contains(m)) continue;
                ReflectInit.reflectInit(m, annotation, left, byClass);
            }
        }
        try {
            task.invoke(null, new Object[0]);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error running @%s task '%s.%s'".formatted(annotation.getSimpleName(), task.getDeclaringClass().getSimpleName(), task.getName()), e);
        }
        catch (NullPointerException e) {
            throw new RuntimeException("Method \"%s\" using Init annotations from non-static context".formatted(task.getName()), e);
        }
    }

    private static <T extends Annotation> Class<?>[] getDependencies(Method task, Class<T> annotation) {
        T init;
        T t = init = task.getAnnotation(annotation);
        Objects.requireNonNull(t);
        T t2 = t;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PreInit.class, PostInit.class}, t2, n)) {
            case 0 -> {
                PreInit pre = (PreInit)t2;
                yield pre.dependencies();
            }
            case 1 -> {
                PostInit post = (PostInit)t2;
                yield post.dependencies();
            }
            default -> new Class[]{};
        };
    }
}

