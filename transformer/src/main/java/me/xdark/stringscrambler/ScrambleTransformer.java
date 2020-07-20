package me.xdark.stringscrambler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.concurrent.ThreadLocalRandom;

public final class ScrambleTransformer {
    private static final String INTRINSICS = "me/xdark/stringscrambler/StringIntrinsics";
    private static final String SCRAMBLE = "scramble";
    private static final String SCRAMBLE_DESC = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String UNSAFE = "me/xdark/stringscrambler/StringUnsafe";
    private static final String FREE = "free";
    private static final String FREE_DESC = "(Ljava/lang/String;)V";
    private static final String SET_VALUE = "setValue";
    private static final String SET_VALUE_DESC = "(Ljava/lang/String;[C)V";

    private ScrambleTransformer() { }

    public static void scramble(MethodNode method) {
        InsnList list = method.instructions;
        for (AbstractInsnNode node : list) {
            if (node instanceof LdcInsnNode) {
                Object cst = ((LdcInsnNode) node).cst;
                if (cst instanceof String) {
                    String str = (String) cst;
                    AbstractInsnNode next = node.getNext();
                    if (next instanceof MethodInsnNode) {
                        // Check caller
                        MethodInsnNode caller = (MethodInsnNode) next;
                        if (INTRINSICS.equals(caller.owner) && SCRAMBLE.equals(caller.name) && SCRAMBLE_DESC.equals(caller.desc)) {
                            // Patch
                            AbstractInsnNode releaseNode = caller.getNext();
                            list.remove(next);
                            char[] chars = str.toCharArray();
                            InsnList before = new InsnList();
                            // STACK?
                            before.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String")); // STACK? STRING
                            before.add(new InsnNode(Opcodes.DUP));  // STACK? STRING STRING
                            before.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "()V", false)); // STACK? STRING
                            before.add(AsmUtil.createCharArray(chars)); // STACK? STRING CHARS
                            before.add(new InsnNode(Opcodes.SWAP)); // STACK? CHARS STRING
                            before.add(new InsnNode(Opcodes.DUP_X1)); // STACK? STRING CHARS STRING
                            before.add(new InsnNode(Opcodes.SWAP)); // STACK? STRING STRING CHARS
                            before.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UNSAFE, SET_VALUE, SET_VALUE_DESC, false)); // STACK? STRING
                            before.add(new InsnNode(Opcodes.DUP)); // STACK? STRING STRING
                            int j = ++method.maxLocals;
                            before.add(new VarInsnNode(Opcodes.ASTORE, j));// STACK? STRING
                            list.insertBefore(node, before);
                            // After we're done, we must zero value array
                            InsnList after = new InsnList();
                            // STACK?
                            after.add(new VarInsnNode(Opcodes.ALOAD, j)); // STACK? STRING
                            after.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UNSAFE, FREE, FREE_DESC, false)); // STACK?
                            list.insert(releaseNode, after);
                            method.maxStack += 3;
                            list.remove(node);
                        }
                    }
                }
            }
        }
    }
}
