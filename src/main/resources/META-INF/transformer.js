var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

function initializeCoreMod() {
    return {
        "ClassInstanceMultiMap_<init>": {
            "target": {
                "type": "METHOD",
                "class": "net/minecraft/util/ClassInstanceMultiMap",
                "methodName": "<init>",
                "methodDesc": "(Ljava/lang/Class;)V"
            },
            "transformer": function (mn) {
                var insnList = mn.instructions.toArray();
                for (var i = 0; i < insnList.length; i++) {
                    var node = insnList[i];
                    if (node.getOpcode() === Opcodes.PUTFIELD && node.owner.equals("net/minecraft/util/ClassInstanceMultiMap") && node.name.equals(ASMAPI.mapField("f_13529_")) && node.desc.equals("Ljava/util/List;")) {
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                        mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "io/github/zekerzhayard/cme_persistententitysectionmanager/CopyOnWriteArrayListWithMutableIterator", "create", "()Ljava/util/List;", false));
                    }
                }
                return mn;
            }
        }
    }
}
