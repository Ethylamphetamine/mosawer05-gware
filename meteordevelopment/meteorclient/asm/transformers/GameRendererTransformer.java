/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.AbstractInsnNode
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.FieldInsnNode
 *  org.objectweb.asm.tree.InsnList
 *  org.objectweb.asm.tree.InsnNode
 *  org.objectweb.asm.tree.LdcInsnNode
 *  org.objectweb.asm.tree.MethodInsnNode
 *  org.objectweb.asm.tree.MethodNode
 *  org.objectweb.asm.tree.TypeInsnNode
 *  org.objectweb.asm.tree.VarInsnNode
 */
package meteordevelopment.meteorclient.asm.transformers;

import meteordevelopment.meteorclient.asm.AsmTransformer;
import meteordevelopment.meteorclient.asm.Descriptor;
import meteordevelopment.meteorclient.asm.MethodInfo;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class GameRendererTransformer
extends AsmTransformer {
    private final MethodInfo getFovMethod = new MethodInfo("net/minecraft/class_4184", null, new Descriptor("Lnet/minecraft/class_4184;", "F", "Z", "D"), true);

    public GameRendererTransformer() {
        super(GameRendererTransformer.mapClassName("net/minecraft/class_757"));
    }

    @Override
    public void transform(ClassNode klass) {
        MethodNode method = this.getMethod(klass, this.getFovMethod);
        if (method == null) {
            GameRendererTransformer.error("[Meteor Client] Could not find method GameRenderer.getFov()");
        }
        int injectionCount = 0;
        for (AbstractInsnNode insn : method.instructions) {
            AbstractInsnNode insns;
            block9: {
                block8: {
                    InsnNode _in;
                    if (insn instanceof LdcInsnNode) {
                        LdcInsnNode in = (LdcInsnNode)insn;
                        if (in.cst instanceof Double && (Double)in.cst == 90.0) {
                            insns = new InsnList();
                            this.generateEventCall((InsnList)insns, (AbstractInsnNode)new LdcInsnNode(in.cst));
                            method.instructions.insert(insn, (InsnList)insns);
                            method.instructions.remove(insn);
                            ++injectionCount;
                            continue;
                        }
                    }
                    if (!(insn instanceof MethodInsnNode)) break block8;
                    MethodInsnNode in1 = (MethodInsnNode)insn;
                    if (in1.name.equals("intValue") && (insns = insn.getNext()) instanceof InsnNode && (_in = (InsnNode)insns).getOpcode() == 135) break block9;
                }
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode in2 = (MethodInsnNode)insn;
                if (!in2.owner.equals(klass.name) || !in2.name.startsWith("redirect") || !in2.name.endsWith("getFov")) continue;
            }
            insns = new InsnList();
            insns.add((AbstractInsnNode)new VarInsnNode(57, method.maxLocals));
            this.generateEventCall((InsnList)insns, (AbstractInsnNode)new VarInsnNode(24, method.maxLocals));
            method.instructions.insert(insn.getNext(), (InsnList)insns);
            ++injectionCount;
        }
        if (injectionCount < 2) {
            GameRendererTransformer.error("[Meteor Client] Failed to modify GameRenderer.getFov()");
        }
    }

    private void generateEventCall(InsnList insns, AbstractInsnNode loadPreviousFov) {
        insns.add((AbstractInsnNode)new FieldInsnNode(178, "meteordevelopment/meteorclient/MeteorClient", "EVENT_BUS", "Lmeteordevelopment/orbit/IEventBus;"));
        insns.add(loadPreviousFov);
        insns.add((AbstractInsnNode)new MethodInsnNode(184, "meteordevelopment/meteorclient/events/render/GetFovEvent", "get", "(D)Lmeteordevelopment/meteorclient/events/render/GetFovEvent;"));
        insns.add((AbstractInsnNode)new MethodInsnNode(185, "meteordevelopment/orbit/IEventBus", "post", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        insns.add((AbstractInsnNode)new TypeInsnNode(192, "meteordevelopment/meteorclient/events/render/GetFovEvent"));
        insns.add((AbstractInsnNode)new FieldInsnNode(180, "meteordevelopment/meteorclient/events/render/GetFovEvent", "fov", "D"));
    }
}

