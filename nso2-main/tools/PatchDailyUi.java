import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Applies the two small Daily HUD fixes without replacing newer runtime classes with stale source. */
public final class PatchDailyUi {
    private PatchDailyUi() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchDailyUi <input-class-dir> <output-class-dir>");
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        Files.createDirectories(output);
        patchTaskAuto(input.resolve("TaskAuto.class"), output.resolve("TaskAuto.class"));
        patchGameScr(input.resolve("GameScr.class"), output.resolve("GameScr.class"));
    }

    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);
        return node;
    }

    private static void write(ClassNode node, Path path) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) {
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        Files.write(path, writer.toByteArray());
    }

    private static void patchTaskAuto(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"toString".equals(method.name) || !"()Ljava/lang/String;".equals(method.desc)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode)insn;
                if (field.getOpcode() == Opcodes.GETSTATIC && "TaskAuto".equals(field.owner)
                        && "q".equals(field.name) && "I".equals(field.desc)) {
                    method.instructions.insertBefore(field, new VarInsnNode(Opcodes.ALOAD, 0));
                    field.setOpcode(Opcodes.GETFIELD);
                    field.name = "completedTasks";
                    patched = true;
                    break;
                }
            }
        }
        if (!patched) throw new IllegalStateException("TaskAuto.toString counter pattern not found");
        write(node, output);
    }

    private static void patchGameScr(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"g".equals(method.name) || !"(LmGraphics;)V".equals(method.desc)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)insn;
                if (!"Auto".equals(call.owner) || !"toString".equals(call.name)) continue;
                for (AbstractInsnNode cursor = insn.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof VarInsnNode && cursor.getOpcode() == Opcodes.ILOAD
                            && ((VarInsnNode)cursor).var == 3) {
                        method.instructions.insert(cursor,
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "GameScr", "autoStatusTextX", "(I)I", false));
                        patched = true;
                        break;
                    }
                    if (cursor instanceof MethodInsnNode) break;
                }
                if (patched) break;
            }
        }
        if (!patched) throw new IllegalStateException("GameScr auto status X pattern not found");

        MethodNode helper = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                "autoStatusTextX", "(I)I", null, null);
        LabelNode wide = new LabelNode();
        helper.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "GameCanvas", "z", "I"));
        helper.instructions.add(new org.objectweb.asm.tree.IntInsnNode(Opcodes.SIPUSH, 240));
        helper.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, wide));
        helper.instructions.add(new InsnNode(Opcodes.ICONST_3));
        helper.instructions.add(new InsnNode(Opcodes.IRETURN));
        helper.instructions.add(wide);
        helper.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        helper.instructions.add(new InsnNode(Opcodes.IRETURN));
        node.methods.add(helper);
        write(node, output);
    }
}
