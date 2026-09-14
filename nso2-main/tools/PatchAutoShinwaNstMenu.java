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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Incremental patch for v24: expose Shinwa sale directly in AUTO NST. */
public final class PatchAutoShinwaNstMenu {
    private PatchAutoShinwaNstMenu() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: PatchAutoShinwaNstMenu <GameScr.class>");
        }
        Path path = Paths.get(args[0]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("bg") && method.desc.equals("()V")) {
                patched = inject(method);
                break;
            }
        }
        if (!patched) {
            throw new IllegalStateException("Khong tim thay diem chen Ban Shinwa trong AUTO NST");
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Files.write(path, writer.toByteArray());
        System.out.println("Patched AUTO NST -> Ban Shinwa");
    }

    private static boolean inject(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof LdcInsnNode)) {
                continue;
            }
            Object cst = ((LdcInsnNode) insn).cst;
            if (!(cst instanceof Integer) || ((Integer) cst).intValue() != 1100097) {
                continue;
            }
            AbstractInsnNode cursor = insn;
            while (cursor != null) {
                if (cursor instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) cursor;
                    if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && "MyVector".equals(call.owner)
                            && "addElement".equals(call.name)
                            && "(Ljava/lang/Object;)V".equals(call.desc)) {
                        InsnList added = new InsnList();
                        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        added.add(new TypeInsnNode(Opcodes.NEW, "Command1"));
                        added.add(new InsnNode(Opcodes.DUP));
                        added.add(new LdcInsnNode("Bán Shinwa"));
                        added.add(new LdcInsnNode(Integer.valueOf(11000601)));
                        added.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "Command1", "<init>",
                                "(Ljava/lang/String;I)V", false));
                        added.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "MyVector", "addElement",
                                "(Ljava/lang/Object;)V", false));
                        method.instructions.insert(cursor, added);
                        return true;
                    }
                }
                cursor = cursor.getNext();
            }
        }
        return false;
    }
}
