package camchua.phoban.nbtapi;

import camchua.phoban.nbtapi.utils.MinecraftVersion;
import camchua.phoban.nbtapi.utils.annotations.AvailableSince;
import camchua.phoban.nbtapi.utils.annotations.CheckUtil;
import camchua.phoban.nbtapi.utils.annotations.FAUtil;
import java.lang.invoke.SerializedLambda;
import org.bukkit.Chunk;

public class NBTChunk {
   private final Chunk chunk;

   public NBTChunk(Chunk chunk) {
      this.chunk = chunk;
   }

   @AvailableSince(
      version = MinecraftVersion.MC1_16_R3
   )
   public NBTCompound getPersistentDataContainer() {
      FAUtil.check(this::getPersistentDataContainer, CheckUtil::isAvaliable);
      return new NBTPersistentDataContainer(this.chunk.getPersistentDataContainer());
   }

   // $FF: synthetic method
   /*private static Object $deserializeLambda$(SerializedLambda lambda) {
      switch (lambda.getImplMethodName()) {
         case "getPersistentDataContainer":
            if (lambda.getImplMethodKind() == 5 && lambda.getFunctionalInterfaceClass().equals("camchua/phoban/nbtapi/utils/annotations/ref/MethodRefrence") && lambda.getFunctionalInterfaceMethodName().equals("callable") && lambda.getFunctionalInterfaceMethodSignature().equals("()V") && lambda.getImplClass().equals("camchua/phoban/nbtapi/NBTChunk") && lambda.getImplMethodSignature().equals("()Lcamchua/phoban/nbtapi/NBTCompound;")) {
               return (NBTChunk)lambda.getCapturedArg(0)::getPersistentDataContainer;
            }
         default:
            throw new IllegalArgumentException("Invalid lambda deserialization");
      }
   }*/
}
