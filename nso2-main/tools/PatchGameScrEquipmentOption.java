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
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchGameScrEquipmentOption {
    private static final String HELPER = "EquipmentPickupSettings";

    private PatchGameScrEquipmentOption() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PatchGameScrEquipmentOption <input> <output>");
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        ClassNode classNode = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(classNode, 0);

        boolean labelPatched = false;
        boolean touchPatched = false;
        boolean keyPatched = false;
        boolean deleteMenuPatched = false;
        boolean deleteCommandPatched = false;
        boolean rewardRootMenuPatched = false;
        boolean rewardCommandPatched = false;
        boolean deleteTickPatched = false;
        int deleteListReadsPatched = 0;
        int deleteListActionsPatched = 0;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("c") && method.desc.equals("(LmGraphics;)V")) {
                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof LdcInsnNode) {
                        Object value = ((LdcInsnNode) insn).cst;
                        if (value instanceof String && ((String) value).contains("nsotiensv4.com")) {
                            ((LdcInsnNode) insn).cst = "Chỉ nhặt vũ khí";
                            AbstractInsnNode booleanInsn = nextReal(insn);
                            if (booleanInsn == null || booleanInsn.getOpcode() != Opcodes.ICONST_1) {
                                throw new IllegalStateException("Unexpected checkbox bytecode after footer label");
                            }
                            method.instructions.set(booleanInsn,
                                    new FieldInsnNode(Opcodes.GETSTATIC, "AutoDailyPanel", "weaponOnlyPickup", "Z"));
                            labelPatched = true;
                            break;
                        }
                    }
                }
            } else if (method.name.equals("ab") && method.desc.equals("()V")) {
                prependCall(method, "handleAutoSettingsTouch");
                touchPatched = true;
            } else if (method.name.equals("cz") && method.desc.equals("()V")) {
                prependCall(method, "handleAutoSettingsKey");
                keyPatched = true;
            } else if (method.name.equals("bg") && method.desc.equals("()V")) {
                deleteMenuPatched = insertItemDeleteMenu(method);
            } else if (method.name.equals("bi") && method.desc.equals("()V")) {
                rewardRootMenuPatched = insertRewardRootMenu(method);
            } else if (method.name.equals("b") && method.desc.equals("(ILjava/lang/Object;)V")) {
                prependItemDeleteCommand(method);
                deleteCommandPatched = true;
                prependRewardCommand(method);
                rewardCommandPatched = true;
            } else if (method.name.equals("update") && method.desc.equals("()V")) {
                InsnList added = new InsnList();
                added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ItemAutoDeleteSettings", "tick", "()V", false));
                method.instructions.insert(added);
                deleteTickPatched = true;
            }

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                AbstractInsnNode next = insn.getNext();
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) insn;
                    if (field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals("NSOT_MOB")
                            && field.name.equals("f") && field.desc.equals("[S")) {
                        method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "ItemAutoDeleteSettings", "getActiveList", "()[S", false));
                        ++deleteListReadsPatched;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if (call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.equals("NSOT_MOB")) {
                        if (call.name.equals("a") && call.desc.equals("(S)V")) {
                            method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "ItemAutoDeleteSettings", "addActive", "(S)V", false));
                            ++deleteListActionsPatched;
                        } else if (call.name.equals("b") && call.desc.equals("(S)V")) {
                            method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "ItemAutoDeleteSettings", "removeActive", "(S)V", false));
                            ++deleteListActionsPatched;
                        } else if (call.name.equals("n") && call.desc.equals("()V")) {
                            method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "ItemAutoDeleteSettings", "compactActive", "()V", false));
                            ++deleteListActionsPatched;
                        }
                    }
                }
                insn = next;
            }
        }

        if (!labelPatched || !touchPatched || !keyPatched || !deleteMenuPatched
                || !deleteCommandPatched || !rewardRootMenuPatched || !rewardCommandPatched
                || !deleteTickPatched || deleteListReadsPatched < 3
                || deleteListActionsPatched < 3) {
            throw new IllegalStateException("GameScr patch sites missing: label=" + labelPatched
                    + " touch=" + touchPatched + " key=" + keyPatched
                    + " deleteMenu=" + deleteMenuPatched + " deleteCommand=" + deleteCommandPatched
                    + " rewardRootMenu=" + rewardRootMenuPatched + " rewardCommand=" + rewardCommandPatched
                    + " deleteTick=" + deleteTickPatched + " listReads=" + deleteListReadsPatched
                    + " listActions=" + deleteListActionsPatched);
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        Files.write(output, writer.toByteArray());
        System.out.println("Patched GameScr equipment option and Item Xoa menu");
    }

    private static boolean insertItemDeleteMenu(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof LdcInsnNode)) {
                continue;
            }
            Object value = ((LdcInsnNode) insn).cst;
            if (!(value instanceof String) || !((String) value).startsWith("Item Nh")) {
                continue;
            }
            AbstractInsnNode addCall = insn;
            while (addCall != null && (!(addCall instanceof MethodInsnNode)
                    || !((MethodInsnNode) addCall).owner.equals("MyVector")
                    || !((MethodInsnNode) addCall).name.equals("addElement"))) {
                addCall = addCall.getNext();
            }
            if (addCall == null) {
                return false;
            }
            InsnList added = new InsnList();
            added.add(new VarInsnNode(Opcodes.ALOAD, 0));
            added.add(new TypeInsnNode(Opcodes.NEW, "Command1"));
            added.add(new InsnNode(Opcodes.DUP));
            added.add(new LdcInsnNode("Item X\u00f3a"));
            added.add(new LdcInsnNode(Integer.valueOf(11990076)));
            added.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "Command1", "<init>",
                    "(Ljava/lang/String;I)V", false));
            added.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "MyVector", "addElement",
                    "(Ljava/lang/Object;)V", false));
            method.instructions.insert(addCall, added);
            return true;
        }
        return false;
    }

    private static boolean insertRewardRootMenu(MethodNode method) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof LdcInsnNode)
                    || !"AUTO NST".equals(((LdcInsnNode) insn).cst)) {
                continue;
            }
            AbstractInsnNode addCall = insn;
            while (addCall != null && (!(addCall instanceof MethodInsnNode)
                    || !((MethodInsnNode) addCall).owner.equals("MyVector")
                    || !((MethodInsnNode) addCall).name.equals("addElement"))) {
                addCall = addCall.getNext();
            }
            if (addCall == null) {
                return false;
            }
            InsnList added = new InsnList();
            addMenuCommand(added, "Nh\u1eadn th\u01b0\u1edfng nhanh", 11990079);
            method.instructions.insert(addCall, added);
            return true;
        }
        return false;
    }

    private static void addMenuCommand(InsnList added, String title, int id) {
        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
        added.add(new TypeInsnNode(Opcodes.NEW, "Command1"));
        added.add(new InsnNode(Opcodes.DUP));
        added.add(new LdcInsnNode(title));
        added.add(new LdcInsnNode(Integer.valueOf(id)));
        added.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "Command1", "<init>",
                "(Ljava/lang/String;I)V", false));
        added.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "MyVector", "addElement",
                "(Ljava/lang/Object;)V", false));
    }

    private static void prependItemDeleteCommand(MethodNode method) {
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ILOAD, 1));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ItemAutoDeleteSettings",
                "normalizeMenuCommand", "(I)I", false));
        added.add(new VarInsnNode(Opcodes.ISTORE, 1));
        method.instructions.insert(added);
    }

    private static void prependRewardCommand(MethodNode method) {
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ILOAD, 1));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "ActivityQuickClaim",
                "normalizeMenuCommand", "(I)I", false));
        added.add(new VarInsnNode(Opcodes.ISTORE, 1));
        method.instructions.insert(added);
    }

    private static void prependCall(MethodNode method, String name) {
        InsnList added = new InsnList();
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, name, "()V", false));
        method.instructions.insert(added);
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode current = insn.getNext();
        while (current != null && current.getOpcode() < 0) {
            current = current.getNext();
        }
        return current;
    }
}
