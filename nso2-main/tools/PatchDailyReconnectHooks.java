import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class PatchDailyReconnectHooks {
    private static final String HELPER = "DailyReconnectRecovery";

    private PatchDailyReconnectHooks() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchDailyReconnectHooks <Controller.class> <GameScr.class>");
        }
        patchController(Paths.get(args[0]));
        patchGameScr(Paths.get(args[1]));
        System.out.println("Patched daily reconnect capture/restore hooks");
    }

    private static void patchController(Path path) throws Exception {
        ClassNode node = read(path);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("onDisconnected") && method.desc.equals("()V")) {
                method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER,
                        "captureBeforeDisconnect", "()V", false));
                patched = true;
                break;
            }
        }
        if (!patched) {
            throw new IllegalStateException("Controller.onDisconnected hook site not found");
        }
        write(path, node);
    }

    private static void patchGameScr(Path path) throws Exception {
        ClassNode node = read(path);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("switchToMe") || !method.desc.equals("()V")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && call.owner.equals("NSOT_MOB")
                            && call.name.equals("b") && call.desc.equals("()V")) {
                        AbstractInsnNode previous = previousReal(insn);
                        if (!(previous instanceof FieldInsnNode)
                                || previous.getOpcode() != Opcodes.GETSTATIC
                                || !((FieldInsnNode) previous).owner.equals("NSOT_MOB")) {
                            throw new IllegalStateException("Unexpected NSOT_MOB.b call context in switchToMe");
                        }
                        method.instructions.insertBefore(previous, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER,
                                "restoreBeforeAutoThreadStarts", "()V", false));
                        patched = true;
                        break;
                    }
                }
            }
        }
        if (!patched) {
            throw new IllegalStateException("GameScr.switchToMe restore hook site not found");
        }
        write(path, node);
    }

    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, 0);
        return node;
    }

    private static void write(Path path, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Files.write(path, writer.toByteArray());
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode insn) {
        AbstractInsnNode current = insn.getPrevious();
        while (current != null && current.getOpcode() < 0) {
            current = current.getPrevious();
        }
        return current;
    }
}
