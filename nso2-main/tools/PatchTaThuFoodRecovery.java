import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Keeps the newest runtime Ta Thu class aligned with the source combat flow. */
public final class PatchTaThuFoodRecovery {
    private PatchTaThuFoodRecovery() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PatchTaThuFoodRecovery <TaskTaThuAuto.class>");
        }
        Path path = Paths.get(args[0]);
        ClassNode owner = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(owner, 0);
        int foodPatched = 0;
        int assistPatched = 0;
        for (MethodNode method : owner.methods) {
            if ("maintainFood".equals(method.name) && "()Z".equals(method.desc)) {
                clearMethod(method);
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "TaskTaThuAuto", "getSchoolMap", "()I", false));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "AutoFoodRecovery", "maintain", "(LAuto;I)Z", false));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
                ++foodPatched;
            } else if ("shouldWaitForGroupAssist".equals(method.name) && "()Z".equals(method.desc)) {
                clearMethod(method);
                method.instructions.add(new InsnNode(Opcodes.ICONST_0));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
                ++assistPatched;
            }
        }
        if (foodPatched != 1 || assistPatched != 1) {
            throw new IllegalStateException("Expected one food and one assist method, patched food="
                    + foodPatched + " assist=" + assistPatched);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        owner.accept(writer);
        Files.write(path, writer.toByteArray());
        System.out.println("Patched Ta Thu food recovery and uninterrupted combat");
    }

    private static void clearMethod(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
    }
}
