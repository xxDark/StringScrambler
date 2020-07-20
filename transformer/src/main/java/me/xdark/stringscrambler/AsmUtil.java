package me.xdark.stringscrambler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class AsmUtil {
    private AsmUtil() { }

    public static InsnList createCharArray(char[] chars) {
        int j = chars.length;
        InsnList list = new InsnList();
        list.add(getNumberInsn(j));
        list.add(new IntInsnNode(Opcodes.NEWARRAY, 5));
        for (int i = 0; i < j; i++) {
            list.add(new InsnNode(Opcodes.DUP));
            list.add(getNumberInsn(i));
            list.add(getNumberInsn(chars[i]));
            list.add(new InsnNode(Opcodes.I2C));
            list.add(new InsnNode(Opcodes.CASTORE));
        }
        return list;
    }

    private static AbstractInsnNode getNumberInsn(int i) {
        if (i >= -1 && i <= 5) {
            return new InsnNode(i + 3);
        } else if (i >= -128 && i <= 127) {
            return new IntInsnNode(Opcodes.BIPUSH, i);
        } else if (i >= -32768 && i <= 32767) {
            return new IntInsnNode(Opcodes.SIPUSH, i);
        } else {
            return new LdcInsnNode(i);
        }
    }
}
