import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/** Patches only the a.GameCanvas invocation, preserving its PAINT override. */
public final class PatchX5InvocationReflective {
    private PatchX5InvocationReflective() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: input.class output.class");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        byte[] source = Files.readAllBytes(input);
        ClassLoader loader = PatchX5InvocationReflective.class.getClassLoader();
        Class<?> readerType = Class.forName("org.objectweb.asm.ClassReader", true, loader);
        Class<?> nodeType = Class.forName("org.objectweb.asm.tree.ClassNode", true, loader);
        Class<?> writerType = Class.forName("org.objectweb.asm.ClassWriter", true, loader);
        Object reader = readerType.getConstructor(byte[].class).newInstance((Object) source);
        Object node = nodeType.getConstructor(int.class).newInstance(589824);
        readerType.getMethod("accept", Class.forName("org.objectweb.asm.ClassVisitor"), int.class)
                .invoke(reader, node, 0);
        Field methodsField = nodeType.getField("methods");
        List<?> methods = (List<?>) methodsField.get(node);
        boolean changed = false;
        for (Object methodNode : methods) {
            Field name = methodNode.getClass().getField("name");
            Field desc = methodNode.getClass().getField("desc");
            if (!"PAINT".equals(name.get(methodNode))
                    || !"(Ljavax/microedition/lcdui/Graphics;)V".equals(desc.get(methodNode))) continue;
            Field instructions = methodNode.getClass().getField("instructions");
            Object list = instructions.get(methodNode);
            Method toArray = list.getClass().getMethod("toArray");
            Object[] insns = (Object[]) toArray.invoke(list);
            for (Object insn : insns) {
                if (!insn.getClass().getName().equals("org.objectweb.asm.tree.MethodInsnNode")) continue;
                Field owner = insn.getClass().getField("owner");
                Field calledName = insn.getClass().getField("name");
                Field calledDesc = insn.getClass().getField("desc");
                if ("a/mScreen".equals(owner.get(insn))
                        && "PAINT".equals(calledName.get(insn))
                        && "(La/mGraphics;)V".equals(calledDesc.get(insn))) {
                    calledName.set(insn, "paint");
                    changed = true;
                }
            }
        }
        if (!changed) throw new IllegalStateException("target invocation not found");
        Object writer = writerType.getConstructor(readerType, int.class).newInstance(reader, 0);
        nodeType.getMethod("accept", Class.forName("org.objectweb.asm.ClassVisitor"))
                .invoke(node, writer);
        byte[] patched = (byte[]) writerType.getMethod("toByteArray").invoke(writer);
        Files.write(output, patched);
    }
}
