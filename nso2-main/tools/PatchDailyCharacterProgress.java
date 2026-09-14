import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Patches the newest runtime classes without replacing them with stale source. */
public final class PatchDailyCharacterProgress {
    private PatchDailyCharacterProgress() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: patch <input-dir> <output-dir>");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        Files.createDirectories(output);
        patchTaskAuto(input.resolve("TaskAuto.class"), output.resolve("TaskAuto.class"));
        patchTaskTaThu(input.resolve("TaskTaThuAuto.class"), output.resolve("TaskTaThuAuto.class"));
        patchCoordinator(input.resolve("AutoDailyCoordinator.class"), output.resolve("AutoDailyCoordinator.class"));
        patchController(input.resolve("Controller.class"), output.resolve("Controller.class"));
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

    private static void replaceHud(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                "hudText", "()Ljava/lang/String;", false));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
    }

    private static void patchHud(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean found = false;
        for (MethodNode method : node.methods) {
            if ("toString".equals(method.name) && "()Ljava/lang/String;".equals(method.desc)) {
                replaceHud(method);
                found = true;
            }
        }
        if (!found) throw new IllegalStateException(input + ": toString not found");
        write(node, output);
    }

    private static void patchTaskAuto(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean hud = false;
        boolean update = false;
        for (MethodNode method : node.methods) {
            if ("toString".equals(method.name) && "()Ljava/lang/String;".equals(method.desc)) {
                replaceHud(method);
                hud = true;
            } else if ("update".equals(method.name) && "()V".equals(method.desc)) {
                InsnList sync = new InsnList();
                sync.add(new VarInsnNode(Opcodes.ALOAD, 0));
                sync.add(new VarInsnNode(Opcodes.ALOAD, 0));
                sync.add(new FieldInsnNode(Opcodes.GETFIELD, "TaskAuto", "completedTasks", "I"));
                sync.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                        "mergeDailyCompleted", "(I)I", false));
                sync.add(new FieldInsnNode(Opcodes.PUTFIELD, "TaskAuto", "completedTasks", "I"));
                method.instructions.insert(sync);
                update = true;
            }
        }
        if (!hud || !update) throw new IllegalStateException("TaskAuto patch target not found");
        write(node, output);
    }

    private static void patchTaskTaThu(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean hud = false;
        boolean lifecycle = false;
        for (MethodNode method : node.methods) {
            if ("toString".equals(method.name) && "()Ljava/lang/String;".equals(method.desc)) {
                replaceHud(method);
                hud = true;
            } else if ("updateTaskLifecycle".equals(method.name) && "()Z".equals(method.desc)) {
                LabelNode continueTask = new LabelNode();
                InsnList finish = new InsnList();
                finish.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                        "taThuDone", "()Z", false));
                finish.add(new JumpInsnNode(Opcodes.IFEQ, continueTask));
                finish.add(new LdcInsnNode("Tà Thú đã hết lượt hôm nay"));
                finish.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "GameScr",
                        "addChatPopup", "(Ljava/lang/String;)V", false));
                finish.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "NSOT_MOB", "d", "()V", false));
                finish.add(new InsnNode(Opcodes.ICONST_1));
                finish.add(new InsnNode(Opcodes.IRETURN));
                finish.add(continueTask);
                method.instructions.insert(finish);
                lifecycle = true;
            }
        }
        if (!hud || !lifecycle) throw new IllegalStateException("TaskTaThuAuto patch target not found");
        write(node, output);
    }

    private static void patchCoordinator(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean hud = false;
        boolean gate = false;
        boolean daily = false;
        boolean taThu = false;
        boolean startRefresh = false;
        for (MethodNode method : node.methods) {
            if ("toString".equals(method.name) && "()Ljava/lang/String;".equals(method.desc)) {
                replaceHud(method);
                hud = true;
            } else if ("g".equals(method.name) && "()V".equals(method.desc)) {
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "DailyCharacterProgress", "forceRefresh", "()V", false));
                startRefresh = true;
            } else if ("update".equals(method.name) && "()V".equals(method.desc)) {
                LabelNode ready = new LabelNode();
                InsnList prefix = new InsnList();
                prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                        "ensureSnapshot", "()Z", false));
                prefix.add(new JumpInsnNode(Opcodes.IFNE, ready));
                prefix.add(new InsnNode(Opcodes.RETURN));
                prefix.add(ready);
                method.instructions.insert(prefix);
                gate = true;

                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (!(insn instanceof FieldInsnNode)) continue;
                    FieldInsnNode field = (FieldInsnNode) insn;
                    if (field.getOpcode() != Opcodes.GETSTATIC || !"AutoDailyPanel".equals(field.owner)) continue;
                    if ("runDailyQuest".equals(field.name)) {
                        LabelNode notDone = new LabelNode();
                        InsnList skip = new InsnList();
                        skip.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                                "dailyDone", "()Z", false));
                        skip.add(new JumpInsnNode(Opcodes.IFEQ, notDone));
                        skip.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        skip.add(new InsnNode(Opcodes.ICONST_4));
                        skip.add(new LdcInsnNode("daily-already-20-from-character-info"));
                        skip.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "AutoDailyCoordinator",
                                "setState", "(ILjava/lang/String;)V", false));
                        skip.add(new InsnNode(Opcodes.RETURN));
                        skip.add(notDone);
                        method.instructions.insertBefore(insn, skip);
                        daily = true;
                    } else if ("runTaThu".equals(field.name)) {
                        LabelNode notDone = new LabelNode();
                        InsnList skip = new InsnList();
                        skip.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyCharacterProgress",
                                "taThuDone", "()Z", false));
                        skip.add(new JumpInsnNode(Opcodes.IFEQ, notDone));
                        skip.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        skip.add(new IntInsnNodeCompat(Opcodes.BIPUSH, 6));
                        skip.add(new LdcInsnNode("tathu-already-0-from-character-info"));
                        skip.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "AutoDailyCoordinator",
                                "setState", "(ILjava/lang/String;)V", false));
                        skip.add(new InsnNode(Opcodes.RETURN));
                        skip.add(notDone);
                        method.instructions.insertBefore(insn, skip);
                        taThu = true;
                    }
                }
            }
        }
        if (!hud || !gate || !daily || !taThu || !startRefresh)
            throw new IllegalStateException("AutoDailyCoordinator patch target not found");
        write(node, output);
    }

    private static void patchController(Path input, Path output) throws Exception {
        ClassNode node = read(input);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.getOpcode() == Opcodes.PUTFIELD && "Char".equals(field.owner)
                        && "countLoopBoos".equals(field.name) && "I".equals(field.desc)) {
                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "DailyCharacterProgress", "onServerSnapshot", "()V", false));
                    patched = true;
                    break;
                }
            }
            if (patched) break;
        }
        if (!patched) throw new IllegalStateException("Controller countLoopBoos assignment not found");
        write(node, output);
    }

    /** Avoids another public import solely for one BIPUSH instruction. */
    private static final class IntInsnNodeCompat extends org.objectweb.asm.tree.IntInsnNode {
        IntInsnNodeCompat(int opcode, int operand) { super(opcode, operand); }
    }
}
