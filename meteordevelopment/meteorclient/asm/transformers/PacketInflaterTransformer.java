/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Label
 *  org.objectweb.asm.Type
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.JumpInsnNode
 *  org.objectweb.asm.tree.LabelNode
 *  org.objectweb.asm.tree.LdcInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TypeInsnNode
 */
package meteordevelopment.meteorclient.asm.transformers;

import meteordevelopment.meteorclient.asm.AsmTransformer;
import meteordevelopment.meteorclient.asm.Descriptor;
import meteordevelopment.meteorclient.asm.MethodInfo;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class PacketInflaterTransformer
extends AsmTransformer {
    private final MethodInfo decodeMethod = new MethodInfo("net/minecraft/class_2532", "decode", new Descriptor("Lio/netty/channel/ChannelHandlerContext;", "Lio/netty/buffer/ByteBuf;", "Ljava/util/List;", "V"), true);

    public PacketInflaterTransformer() {
        super(PacketInflaterTransformer.mapClassName("net/minecraft/class_2532"));
    }

    @Override
    public void transform(ClassNode klass) {
        MethodNode method = this.getMethod(klass, this.decodeMethod);
        if (method == null) {
            PacketInflaterTransformer.error("[Meteor Client] Could not find method PacketInflater.decode()");
        }
        int newCount = 0;
        LabelNode label = new LabelNode(new Label());
        for (AbstractInsnNode insn : method.instructions) {
            TypeInsnNode typeInsn;
            if (insn instanceof TypeInsnNode && (typeInsn = (TypeInsnNode)insn).getOpcode() == 187 && typeInsn.desc.equals("io/netty/handler/codec/DecoderException")) {
                if (++newCount != 2) continue;
                InsnList list = new InsnList();
                list.add((AbstractInsnNode)new MethodInsnNode(184, "meteordevelopment/meteorclient/systems/modules/Modules", "get", "()Lmeteordevelopment/meteorclient/systems/modules/Modules;", false));
                list.add((AbstractInsnNode)new LdcInsnNode((Object)Type.getType(AntiPacketKick.class)));
                list.add((AbstractInsnNode)new MethodInsnNode(182, "meteordevelopment/meteorclient/systems/modules/Modules", "isActive", "(Ljava/lang/Class;)Z", false));
                list.add((AbstractInsnNode)new JumpInsnNode(154, label));
                method.instructions.insertBefore(insn, list);
                continue;
            }
            if (newCount != 2 || insn.getOpcode() != 191) continue;
            method.instructions.insert(insn, (AbstractInsnNode)label);
            return;
        }
        PacketInflaterTransformer.error("[Meteor Client] Failed to modify PacketInflater.decode()");
    }
}

