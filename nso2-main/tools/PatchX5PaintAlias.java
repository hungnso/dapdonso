import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Adds the legacy uppercase paint bridge expected by some GHEP_X5 classes. */
public final class PatchX5PaintAlias {
    private PatchX5PaintAlias() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: input.class output.class");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        byte[] original = Files.readAllBytes(input);
        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, 0);
        final boolean[] found = new boolean[]{false};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("PAINT".equals(name)
                        && "(La/mGraphics;)V".equals(descriptor)) found[0] = true;
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        }, 0);
        if (!found[0]) {
            MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, "PAINT",
                    "(La/mGraphics;)V", null, null);
            method.visitCode();
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitVarInsn(Opcodes.ALOAD, 1);
            method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "a/mScreen", "paint",
                    "(La/mGraphics;)V", false);
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.write(output, writer.toByteArray());
    }
}
