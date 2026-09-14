import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

/** Applies the MP recovery patch without depending on constant-pool indexes. */
public final class PatchControllerMpRecovery {
    private static final String MP_WARNING = "Kh\u00f4ng \u0111\u1ee7 MP \u0111\u1ec3 s\u1eed d\u1ee5ng";

    private PatchControllerMpRecovery() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchControllerMpRecovery <input.class> <output.class>");
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode owner = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(owner, 0);

        int patched = 0;
        int alreadyPatched = 0;
        boolean activityProtocolPatched = false;
        for (MethodNode method : owner.methods) {
            if (method.name.equals("onMessage") && method.desc.equals("(LMessage;)V")) {
                InsnList added = new InsnList();
                added.add(new VarInsnNode(Opcodes.ALOAD, 1));
                added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ActivityQuickClaim",
                        "normalizeIncomingMessage", "(LMessage;)V", false));
                method.instructions.insert(added);
                activityProtocolPatched = true;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof LdcInsnNode) || !MP_WARNING.equals(((LdcInsnNode) insn).cst)) {
                    continue;
                }

                AbstractInsnNode cursor = insn.getNext();
                for (int remaining = 12; cursor != null && remaining-- > 0; cursor = cursor.getNext()) {
                    if (cursor instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) cursor;
                        if (call.getOpcode() == Opcodes.INVOKESTATIC && "NSOT_MOB".equals(call.owner)
                                && "o".equals(call.name) && "()V".equals(call.desc)) {
                            ++alreadyPatched;
                            break;
                        }
                    }
                    if (cursor instanceof FieldInsnNode) {
                        FieldInsnNode field = (FieldInsnNode) cursor;
                        if (field.getOpcode() == Opcodes.PUTSTATIC && "Auto".equals(field.owner)
                                && "m".equals(field.name) && "Z".equals(field.desc)) {
                            AbstractInsnNode value = previousRealInsn(cursor);
                            if (value == null || value.getOpcode() != Opcodes.ICONST_0) {
                                throw new IllegalStateException("Unexpected value before Auto.m MP recovery assignment");
                            }
                            method.instructions.set(value,
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, "NSOT_MOB", "o", "()V", false));
                            method.instructions.remove(cursor);
                            ++patched;
                            break;
                        }
                    }
                }
            }
        }

        if (patched != 1 && !(patched == 0 && alreadyPatched == 1)) {
            throw new IllegalStateException("Expected exactly one MP recovery site, patched " + patched
                    + ", already patched " + alreadyPatched);
        }
        if (!activityProtocolPatched) {
            throw new IllegalStateException("Controller onMessage activity protocol patch site missing");
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        owner.accept(writer);
        Files.write(output, writer.toByteArray());
        System.out.println("Patched Controller MP recovery and activity protocol");
    }

    private static AbstractInsnNode previousRealInsn(AbstractInsnNode insn) {
        AbstractInsnNode result = insn.getPrevious();
        while (result != null && result.getOpcode() < 0) {
            result = result.getPrevious();
        }
        return result;
    }
}
