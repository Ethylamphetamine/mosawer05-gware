/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  org.objectweb.asm.ClassReader
 *  org.objectweb.asm.ClassVisitor
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.MixinEnvironment
 *  org.spongepowered.asm.mixin.transformer.IMixinTransformer
 *  org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry
 *  org.spongepowered.asm.transformers.MixinClassWriter
 */
package meteordevelopment.meteorclient.asm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.asm.AsmTransformer;
import meteordevelopment.meteorclient.asm.transformers.GameRendererTransformer;
import meteordevelopment.meteorclient.asm.transformers.PacketInflaterTransformer;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry;
import org.spongepowered.asm.transformers.MixinClassWriter;

public class Asm {
    public static Asm INSTANCE;
    private final Map<String, AsmTransformer> transformers = new HashMap<String, AsmTransformer>();
    private final boolean export;

    public Asm(boolean export) {
        this.export = export;
    }

    public static void init() {
        if (INSTANCE != null) {
            return;
        }
        INSTANCE = new Asm(System.getProperty("meteor.asm.export") != null);
        INSTANCE.add(new GameRendererTransformer());
        INSTANCE.add(new PacketInflaterTransformer());
    }

    private void add(AsmTransformer transformer) {
        this.transformers.put(transformer.targetName, transformer);
    }

    public byte[] transform(String name, byte[] bytes) {
        AsmTransformer transformer = this.transformers.get(name);
        if (transformer != null) {
            ClassNode klass = new ClassNode();
            ClassReader reader = new ClassReader(bytes);
            reader.accept((ClassVisitor)klass, 8);
            transformer.transform(klass);
            MixinClassWriter writer = new MixinClassWriter(reader, 2);
            klass.accept((ClassVisitor)writer);
            bytes = writer.toByteArray();
            this.export(name, bytes);
        }
        return bytes;
    }

    private void export(String name, byte[] bytes) {
        if (this.export) {
            try {
                Path path = Path.of(FabricLoader.getInstance().getGameDir().toString(), ".meteor.asm.out", name.replace('.', '/') + ".class");
                new File(path.toUri()).getParentFile().mkdirs();
                Files.write(path, bytes, new OpenOption[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Transformer
    implements IMixinTransformer {
        public IMixinTransformer delegate;

        public void audit(MixinEnvironment environment) {
            this.delegate.audit(environment);
        }

        public List<String> reload(String mixinClass, ClassNode classNode) {
            return this.delegate.reload(mixinClass, classNode);
        }

        public boolean computeFramesForClass(MixinEnvironment environment, String name, ClassNode classNode) {
            return this.delegate.computeFramesForClass(environment, name, classNode);
        }

        public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
            basicClass = this.delegate.transformClassBytes(name, transformedName, basicClass);
            return INSTANCE.transform(name, basicClass);
        }

        public byte[] transformClass(MixinEnvironment environment, String name, byte[] classBytes) {
            return this.delegate.transformClass(environment, name, classBytes);
        }

        public boolean transformClass(MixinEnvironment environment, String name, ClassNode classNode) {
            return this.delegate.transformClass(environment, name, classNode);
        }

        public byte[] generateClass(MixinEnvironment environment, String name) {
            return this.delegate.generateClass(environment, name);
        }

        public boolean generateClass(MixinEnvironment environment, String name, ClassNode classNode) {
            return this.delegate.generateClass(environment, name, classNode);
        }

        public IExtensionRegistry getExtensions() {
            return this.delegate.getExtensions();
        }
    }
}

