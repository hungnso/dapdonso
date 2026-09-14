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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds the two-stage no-EXP recovery to the newest AutoNhiemVuChinh runtime. */
public final class PatchCombatStallRecovery {
    private PatchCombatStallRecovery() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: patch <input-class> <output-class>");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);
        patch(node);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String left, String right) { return "java/lang/Object"; }
        };
        node.accept(writer);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    private static void patch(ClassNode node) {
        MethodNode method = null;
        for (MethodNode candidate : node.methods) {
            if ("monitorCombatStall".equals(candidate.name) && "(LChar;)Z".equals(candidate.desc)) {
                method = candidate;
                break;
            }
        }
        if (method == null) throw new IllegalStateException("monitorCombatStall not found");

        boolean farmHook = false;
        for (MethodNode farm : node.methods) {
            if (!"doLevelFarm".equals(farm.name)
                    || !"(LChar;IILjava/lang/String;Z)V".equals(farm.desc)) continue;
            for (AbstractInsnNode insn = farm.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)insn;
                if (call.getOpcode() != Opcodes.INVOKESTATIC
                        || !"AutoNhiemVuChinh".equals(call.owner)
                        || !"countLiveMobs".equals(call.name) || !"()I".equals(call.desc)) continue;
                LabelNode continueFarm = new LabelNode();
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "AutoNhiemVuChinh",
                        "monitorCombatStall", "(LChar;)Z", false));
                hook.add(new JumpInsnNode(Opcodes.IFEQ, continueFarm));
                hook.add(new InsnNode(Opcodes.RETURN));
                hook.add(continueFarm);
                farm.instructions.insertBefore(insn, hook);
                farmHook = true;
                break;
            }
            break;
        }

        // HP damage resets combatAttempts in the original watchdog. Do not let that
        // suppress the EXP timer while attacks are still being sent.
        boolean attackGate = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof FieldInsnNode)) continue;
            FieldInsnNode field = (FieldInsnNode)insn;
            if (field.getOpcode() != Opcodes.GETFIELD || !"AutoNhiemVuChinh".equals(field.owner)
                    || !"combatAttempts".equals(field.name)) continue;
            AbstractInsnNode loadThis = previousReal(insn);
            AbstractInsnNode four = nextReal(insn);
            AbstractInsnNode jump = nextReal(four);
            if (loadThis == null || loadThis.getOpcode() != Opcodes.ALOAD
                    || four == null || four.getOpcode() != Opcodes.ICONST_4
                    || !(jump instanceof JumpInsnNode) || jump.getOpcode() != Opcodes.IF_ICMPGE) continue;
            method.instructions.remove(loadThis);
            method.instructions.remove(insn);
            method.instructions.remove(four);
            ((JumpInsnNode)jump).setOpcode(Opcodes.GOTO);
            attackGate = true;
            break;
        }

        // Observe EXP independently from HP immediately after the method stores 'now' in local 4.
        boolean observed = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == Opcodes.LSTORE && insn instanceof VarInsnNode
                    && ((VarInsnNode)insn).var == 4) {
                InsnList observe = new InsnList();
                observe.add(new VarInsnNode(Opcodes.ALOAD, 1));
                observe.add(new VarInsnNode(Opcodes.ALOAD, 0));
                observe.add(new FieldInsnNode(Opcodes.GETFIELD, "AutoNhiemVuChinh", "combatLastAttempt", "J"));
                observe.add(new VarInsnNode(Opcodes.ALOAD, 0));
                observe.add(new FieldInsnNode(Opcodes.GETFIELD, "AutoNhiemVuChinh", "combatAttempts", "I"));
                observe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "CombatStallRecovery",
                        "observe", "(LChar;JI)V", false));
                method.instructions.insert(insn, observe);
                observed = true;
                break;
            }
        }

        // Replace now-combatLastProgress>=15000 (HP-or-EXP) with the EXP-only timer.
        boolean expTimer = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof FieldInsnNode)) continue;
            FieldInsnNode field = (FieldInsnNode)insn;
            if (field.getOpcode() != Opcodes.GETFIELD || !"AutoNhiemVuChinh".equals(field.owner)
                    || !"combatLastProgress".equals(field.name)) continue;
            AbstractInsnNode start = insn.getPrevious();
            while (start != null && start.getOpcode() != Opcodes.LLOAD) start = start.getPrevious();
            AbstractInsnNode end = insn;
            while (end.getNext() != null && end.getNext().getOpcode() != Opcodes.DUP) {
                end = end.getNext();
            }
            AbstractInsnNode insertBefore = end.getNext();
            if (start == null || insertBefore == null) continue;
            AbstractInsnNode cursor = start;
            while (cursor != insertBefore) {
                AbstractInsnNode next = cursor.getNext();
                method.instructions.remove(cursor);
                cursor = next;
            }
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(Opcodes.ALOAD, 1));
            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "CombatStallRecovery",
                    "isNoExpTimedOut", "(LChar;)Z", false));
            method.instructions.insertBefore(insertBefore, replacement);
            expTimer = true;
            break;
        }

        // Before reconnecting, move/focus another mob and restart the watchdog once.
        boolean retry = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode)insn;
            if (!"AutoNhiemVuChinh".equals(call.owner) || !"forceCombatReconnect".equals(call.name)) continue;
            AbstractInsnNode receiver = insn.getPrevious();
            while (receiver != null && receiver.getOpcode() != Opcodes.ALOAD) receiver = receiver.getPrevious();
            if (receiver == null) continue;
            LabelNode reconnect = new LabelNode();
            InsnList attempt = new InsnList();
            attempt.add(new VarInsnNode(Opcodes.ALOAD, 1));
            attempt.add(new VarInsnNode(Opcodes.ALOAD, 0));
            attempt.add(new FieldInsnNode(Opcodes.GETFIELD, "AutoNhiemVuChinh", "combatMobId", "I"));
            attempt.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "CombatStallRecovery",
                    "tryNearbyMob", "(LChar;I)Z", false));
            attempt.add(new JumpInsnNode(Opcodes.IFEQ, reconnect));
            attempt.add(new VarInsnNode(Opcodes.ALOAD, 0));
            attempt.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "AutoNhiemVuChinh",
                    "resetCombatWatchdog", "()V", false));
            attempt.add(new InsnNode(Opcodes.ICONST_1));
            attempt.add(new InsnNode(Opcodes.IRETURN));
            attempt.add(reconnect);
            method.instructions.insertBefore(receiver, attempt);
            retry = true;
            break;
        }

        boolean reason = false;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LdcInsnNode && "danh-khong-mat-mau".equals(((LdcInsnNode)insn).cst)) {
                ((LdcInsnNode)insn).cst = "danh-khong-co-exp-sau-khi-doi-quai";
                reason = true;
            }
        }
        if (!farmHook || !attackGate || !observed || !expTimer || !retry || !reason)
            throw new IllegalStateException("Incomplete patch farm=" + farmHook + " gate=" + attackGate + " observed=" + observed + " exp=" + expTimer
                    + " retry=" + retry + " reason=" + reason);
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode node) {
        AbstractInsnNode cursor = node == null ? null : node.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode node) {
        AbstractInsnNode cursor = node == null ? null : node.getNext();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getNext();
        return cursor;
    }
}
