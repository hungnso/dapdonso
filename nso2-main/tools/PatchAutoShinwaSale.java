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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Hooks AutoShinwaSale into the preserved runtime GameScr/Controller classes. */
public final class PatchAutoShinwaSale {
    private static final String HELPER = "AutoShinwaSale";

    private PatchAutoShinwaSale() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchAutoShinwaSale <GameScr.class> <Controller.class>");
        }
        patchGameScr(Paths.get(args[0]));
        patchController(Paths.get(args[1]));
        System.out.println("Patched Auto Shinwa sale hooks");
    }

    private static void patchGameScr(Path path) throws Exception {
        ClassNode node = read(path);
        boolean commandPatched = false;
        boolean tickPatched = false;
        boolean nstMenuPatched = false;

        for (MethodNode method : node.methods) {
            if (method.name.equals("b") && method.desc.equals("(ILjava/lang/Object;)V")) {
                InsnList added = new InsnList();
                added.add(new VarInsnNode(Opcodes.ILOAD, 1));
                added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER,
                        "normalizeMenuCommand", "(I)I", false));
                added.add(new VarInsnNode(Opcodes.ISTORE, 1));
                method.instructions.insert(added);
                commandPatched = true;
            } else if (method.name.equals("bg") && method.desc.equals("()V")) {
                nstMenuPatched = injectNstMenuEntry(method);
            } else if (method.name.equals("update") && method.desc.equals("()V")) {
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER,
                        "tick", "()V", false));
                tickPatched = true;
            }
        }

        if (!commandPatched || !tickPatched || !nstMenuPatched) {
            throw new IllegalStateException("GameScr Shinwa hooks missing: command="
                    + commandPatched + " tick=" + tickPatched + " nstMenu=" + nstMenuPatched);
        }
        write(path, node);
    }

    /** Adds a visible "Bán Shinwa" entry right after Auto Hàng Ngày in AUTO NST. */
    private static boolean injectNstMenuEntry(MethodNode method) {
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
                        added.add(new org.objectweb.asm.tree.TypeInsnNode(Opcodes.NEW, "Command1"));
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

    private static void patchController(Path path) throws Exception {
        ClassNode node = read(path);
        int ackHooks = 0;

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode)) {
                    continue;
                }
                FieldInsnNode field = (FieldInsnNode) insn;
                if (field.getOpcode() != Opcodes.PUTSTATIC
                        || !"GameScr".equals(field.owner)
                        || !"itemSell".equals(field.name)
                        || !"LItem;".equals(field.desc)) {
                    continue;
                }
                AbstractInsnNode previous = previousReal(insn);
                if (previous != null && previous.getOpcode() == Opcodes.ACONST_NULL) {
                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER,
                            "onSaleAck", "()V", false));
                    ++ackHooks;
                }
            }
        }

        if (ackHooks != 1) {
            throw new IllegalStateException("Expected exactly one Controller sale ACK hook, found " + ackHooks);
        }
        write(path, node);
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode current = insn.getPrevious();
        while (current != null && current.getOpcode() < 0) {
            current = current.getPrevious();
        }
        return current;
    }

    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);
        return node;
    }

    private static void write(Path path, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Files.write(path, writer.toByteArray());
    }
}
