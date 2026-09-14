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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Injects the Daily/Ta Thu stall watchdog before the active Auto.update(). */
public final class PatchDailyStallRecovery {
    private PatchDailyStallRecovery() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PatchDailyStallRecovery <NSOT_MOB.class>");
        }

        Path path = Paths.get(args[0]);
        ClassNode owner = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(owner, 0);

        boolean patched = false;
        for (MethodNode method : owner.methods) {
            if (!"run".equals(method.name) || !"()V".equals(method.desc)) {
                continue;
            }

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !"Auto".equals(call.owner)
                        || !"update".equals(call.name)
                        || !"()V".equals(call.desc)) {
                    continue;
                }

                AbstractInsnNode loadAuto = previousReal(insn);
                if (!(loadAuto instanceof FieldInsnNode)) {
                    continue;
                }
                FieldInsnNode field = (FieldInsnNode) loadAuto;
                if (field.getOpcode() != Opcodes.GETSTATIC
                        || !"NSOT_MOB".equals(field.owner)
                        || !"b".equals(field.name)
                        || !"LAuto;".equals(field.desc)) {
                    continue;
                }

                LabelNode doUpdate = new LabelNode();
                LabelNode afterUpdate = new LabelNode();
                InsnList guard = new InsnList();
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "DailyStallRecovery",
                        "tick", "()Z", false));
                guard.add(new JumpInsnNode(Opcodes.IFEQ, doUpdate));
                guard.add(new JumpInsnNode(Opcodes.GOTO, afterUpdate));
                guard.add(doUpdate);
                method.instructions.insertBefore(loadAuto, guard);
                method.instructions.insert(insn, afterUpdate);
                patched = true;
                break;
            }
            break;
        }

        if (!patched) {
            throw new IllegalStateException("NSOT_MOB.run Auto.update hook site not found");
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) {
                return "java/lang/Object";
            }
        };
        owner.accept(writer);
        Files.write(path, writer.toByteArray());
        System.out.println("Patched Daily/Ta Thu stall watchdog");
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode current = insn.getPrevious();
        while (current != null && current.getOpcode() < 0) {
            current = current.getPrevious();
        }
        return current;
    }
}
