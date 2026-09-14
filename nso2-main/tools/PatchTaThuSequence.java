import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Prevents Daily Coordinator from skipping Ta Thu before its TaskOrder arrives. */
public final class PatchTaThuSequence {
    private PatchTaThuSequence() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: patch <input-dir> <output-dir>");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        Files.createDirectories(output);
        patchCoordinator(input.resolve("AutoDailyCoordinator.class"), output.resolve("AutoDailyCoordinator.class"));
        patchTask(input.resolve("TaskTaThuAuto.class"), output.resolve("TaskTaThuAuto.class"));
    }

    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);
        return node;
    }

    private static void write(ClassNode node, Path path) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) { return "java/lang/Object"; }
        };
        node.accept(writer);
        Files.write(path, writer.toByteArray());
    }

    private static void disableImmediateDoneCheck(ClassNode node, String methodName) {
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!methodName.equals(method.name)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)insn;
                if (call.getOpcode() == Opcodes.INVOKESTATIC
                        && "DailyCharacterProgress".equals(call.owner)
                        && "taThuDone".equals(call.name) && "()Z".equals(call.desc)) {
                    method.instructions.set(insn, new InsnNode(Opcodes.ICONST_0));
                    patched = true;
                    break;
                }
            }
            if (patched) break;
        }
        if (!patched) throw new IllegalStateException(node.name + ": immediate taThuDone check not found");
    }

    private static void patchCoordinator(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        disableImmediateDoneCheck(node, "update");
        boolean mandatory = false;
        for (MethodNode method : node.methods) {
            if (!"update".equals(method.name)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode)insn;
                if (field.getOpcode() == Opcodes.GETSTATIC && "AutoDailyPanel".equals(field.owner)
                        && "runTaThu".equals(field.name) && "Z".equals(field.desc)) {
                    method.instructions.set(insn, new InsnNode(Opcodes.ICONST_1));
                    mandatory = true;
                    break;
                }
            }
        }
        if (!mandatory) throw new IllegalStateException("AutoDailyCoordinator: runTaThu gate not found");
        write(node, output);
    }

    private static void patchTask(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        disableImmediateDoneCheck(node, "updateTaskLifecycle");
        boolean guardedFinish = false;
        for (MethodNode method : node.methods) {
            if (!"updateTaskLifecycle".equals(method.name) || !"()Z".equals(method.desc)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof LdcInsnNode)
                        || !"Khong nhan duoc NV Ta Thu; co the da het luot hom nay".equals(((LdcInsnNode)insn).cst)) continue;
                AbstractInsnNode popup = insn.getNext();
                while (popup != null && !(popup instanceof MethodInsnNode)) popup = popup.getNext();
                if (!(popup instanceof MethodInsnNode)) continue;
                LabelNode trulyDone = new LabelNode();
                InsnList guard = new InsnList();
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                        "refreshIfNeeded", "()V", false));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                        "taThuRemaining", "()I", false));
                guard.add(new JumpInsnNode(Opcodes.IFLE, trulyDone));
                guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
                guard.add(new InsnNode(Opcodes.ICONST_0));
                guard.add(new FieldInsnNode(Opcodes.PUTFIELD, "TaskTaThuAuto", "receiveAttempts", "I"));
                guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
                guard.add(new InsnNode(Opcodes.LCONST_0));
                guard.add(new FieldInsnNode(Opcodes.PUTFIELD, "TaskTaThuAuto", "lastTaskAction", "J"));
                guard.add(new LdcInsnNode("Ta Thu van con luot, tiep tuc thu nhan nhiem vu"));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "GameScr",
                        "addChatPopup", "(Ljava/lang/String;)V", false));
                guard.add(new InsnNode(Opcodes.ICONST_1));
                guard.add(new InsnNode(Opcodes.IRETURN));
                guard.add(trulyDone);
                method.instructions.insertBefore(insn, guard);
                ((LdcInsnNode)insn).cst = "Ta Thu da het luot hom nay";
                guardedFinish = true;
                break;
            }
        }
        if (!guardedFinish) throw new IllegalStateException("TaskTaThuAuto: receive-attempt finish not found");
        write(node, output);
    }
}
