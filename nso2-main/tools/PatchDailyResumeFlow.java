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
import org.objectweb.asm.tree.MethodNode;

/** Keeps the interrupted auto under AutoDailyCoordinator instead of discarding it. */
public final class PatchDailyResumeFlow {
    private PatchDailyResumeFlow() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PatchDailyResumeFlow <NSOT_MOB.class>");
        }
        Path path = Paths.get(args[0]);
        ClassNode owner = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(owner, 0);

        int removed = 0;
        for (MethodNode method : owner.methods) {
            if (!"startAutoDaily".equals(method.name) || !"(Z)V".equals(method.desc)) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode)) {
                    continue;
                }
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.getOpcode() != Opcodes.PUTSTATIC || !"NSOT_MOB".equals(field.owner)
                        || !"b".equals(field.name) || !"LAuto;".equals(field.desc)) {
                    continue;
                }
                AbstractInsnNode value = previousRealInsn(insn);
                if (value != null && value.getOpcode() == Opcodes.ACONST_NULL) {
                    method.instructions.remove(value);
                    method.instructions.remove(insn);
                    ++removed;
                }
                break;
            }
        }

        if (removed != 1) {
            throw new IllegalStateException("Expected one startAutoDaily auto-clear, removed " + removed);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        owner.accept(writer);
        Files.write(path, writer.toByteArray());
        System.out.println("Patched Daily resume flow: preserved interrupted auto");
    }

    private static AbstractInsnNode previousRealInsn(AbstractInsnNode insn) {
        AbstractInsnNode result = insn.getPrevious();
        while (result != null && result.getOpcode() < 0) {
            result = result.getPrevious();
        }
        return result;
    }
}
