import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Ensures items configured for auto-delete are not picked up again after being dropped. */
public final class PatchItemAutoDeletePickup {
    private PatchItemAutoDeletePickup() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchItemAutoDeletePickup <input> <output>");
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode classNode = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(classNode, 0);

        int returnsPatched = 0;
        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("a") || !method.desc.equals("(LItemTemplate;)Z")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.IRETURN) {
                    // The original boolean is already on the stack. Add the
                    // template and let the helper veto IDs in Item Xoa.
                    InsnList added = new InsnList();
                    added.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ItemAutoDeleteSettings",
                            "allowPickup", "(ZLItemTemplate;)Z", false));
                    method.instructions.insertBefore(insn, added);
                    ++returnsPatched;
                }
            }
        }
        if (returnsPatched < 2) {
            throw new IllegalStateException("NSOT_MOB pickup policy patch site missing; returns=" + returnsPatched);
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        Files.write(output, writer.toByteArray());
        System.out.println("Patched NSOT_MOB Item Xoa pickup veto at " + returnsPatched + " returns");
    }
}
