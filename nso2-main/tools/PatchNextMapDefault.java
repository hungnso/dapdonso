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
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Normalizes persisted next-map speed while preserving the newer runtime Char class. */
public final class PatchNextMapDefault {
    private PatchNextMapDefault() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: PatchNextMapDefault <input> <output>");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)insn;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL || !"java/io/DataInputStream".equals(call.owner)
                        || !"readInt".equals(call.name) || !"()I".equals(call.desc)) continue;
                AbstractInsnNode next = nextReal(insn.getNext());
                if (!(next instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode)next;
                if (field.getOpcode() == Opcodes.PUTSTATIC && "NSOT_MOB".equals(field.owner)
                        && "u".equals(field.name) && "I".equals(field.desc)) {
                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "Char", "normalizeNextMapSpeed", "(I)I", false));
                    patched = true;
                    break;
                }
            }
            if (patched) break;
        }
        if (!patched) throw new IllegalStateException("Persisted NSOT_MOB.u load pattern not found");

        MethodNode helper = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                "normalizeNextMapSpeed", "(I)I", null, null);
        LabelNode reset = new LabelNode();
        LabelNode valid = new LabelNode();
        helper.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        helper.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 20));
        helper.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, reset));
        helper.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        helper.instructions.add(new JumpInsnNode(Opcodes.IFLE, reset));
        helper.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        helper.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 100));
        helper.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, valid));
        helper.instructions.add(reset);
        helper.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 100));
        helper.instructions.add(new InsnNode(Opcodes.IRETURN));
        helper.instructions.add(valid);
        helper.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
        helper.instructions.add(new InsnNode(Opcodes.IRETURN));
        node.methods.add(helper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) { return "java/lang/Object"; }
        };
        node.accept(writer);
        Files.write(output, writer.toByteArray());
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode node) {
        while (node != null && node.getOpcode() < 0) node = node.getNext();
        return node;
    }
}
