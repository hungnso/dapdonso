import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Replaces unstable GameScr.fi based Ta Thu actions with caption navigation. */
public final class PatchTaThuMenuNavigation {
    private PatchTaThuMenuNavigation() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: patch <input-class> <output-class>");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if (!"updateTaskLifecycle".equals(method.name) || !"()Z".equals(method.desc)) continue;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null;) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode)insn;
                    if (call.getOpcode() == Opcodes.INVOKESTATIC && "GameScr".equals(call.owner)
                            && "b".equals(call.name) && "(III)V".equals(call.desc)) {
                        int option = getPreviousIntConstant(insn);
                        if (option == 0 || option == 2) {
                            AbstractInsnNode start = insn;
                            for (int i = 0; i < 5; ++i) start = previousReal(start);
                            if (start == null || getIntConstant(start) != 25)
                                throw new IllegalStateException("Unexpected GameScr.b argument layout");
                            AbstractInsnNode cursor = start;
                            AbstractInsnNode after = insn.getNext();
                            while (cursor != after) {
                                AbstractInsnNode removeNext = cursor.getNext();
                                method.instructions.remove(cursor);
                                cursor = removeNext;
                            }
                            LabelNode selected = new LabelNode();
                            InsnList replacement = new InsnList();
                            replacement.add(pushInt(option));
                            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "TaThuMenuNavigator",
                                    "tick", "(I)Z", false));
                            replacement.add(new JumpInsnNode(Opcodes.IFNE, selected));
                            replacement.add(new InsnNode(Opcodes.ICONST_1));
                            replacement.add(new InsnNode(Opcodes.IRETURN));
                            replacement.add(selected);
                            method.instructions.insertBefore(after, replacement);
                            ++patched;
                        }
                    }
                }
                insn = next;
            }
        }
        if (patched != 2) throw new IllegalStateException("Expected 2 Ta Thu menu calls, patched=" + patched);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) { return "java/lang/Object"; }
        };
        node.accept(writer);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode node) {
        AbstractInsnNode cursor = node == null ? null : node.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }

    private static int getPreviousIntConstant(AbstractInsnNode node) {
        return getIntConstant(previousReal(node));
    }

    private static int getIntConstant(AbstractInsnNode node) {
        if (node == null) return Integer.MIN_VALUE;
        int opcode = node.getOpcode();
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) return opcode - Opcodes.ICONST_0;
        if (node instanceof org.objectweb.asm.tree.IntInsnNode)
            return ((org.objectweb.asm.tree.IntInsnNode)node).operand;
        return Integer.MIN_VALUE;
    }

    private static AbstractInsnNode pushInt(int value) {
        return new InsnNode(Opcodes.ICONST_0 + value);
    }
}
