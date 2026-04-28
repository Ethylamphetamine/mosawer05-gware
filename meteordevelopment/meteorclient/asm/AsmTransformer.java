/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.MethodNode
 */
package meteordevelopment.meteorclient.asm;

import meteordevelopment.meteorclient.asm.MethodInfo;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AsmTransformer {
    public final String targetName;

    protected AsmTransformer(String targetName) {
        this.targetName = targetName;
    }

    public abstract void transform(ClassNode var1);

    protected MethodNode getMethod(ClassNode klass, MethodInfo methodInfo) {
        for (MethodNode method : klass.methods) {
            if (!methodInfo.equals(method)) continue;
            return method;
        }
        return null;
    }

    protected static void error(String message) {
        System.err.println(message);
        throw new RuntimeException(message);
    }

    protected static String mapClassName(String name) {
        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", name.replace('/', '.'));
    }
}

