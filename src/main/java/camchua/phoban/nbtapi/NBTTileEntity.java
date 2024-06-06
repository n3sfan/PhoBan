package camchua.phoban.nbtapi;

import camchua.phoban.nbtapi.utils.MinecraftVersion;
import camchua.phoban.nbtapi.utils.annotations.AvailableSince;
import camchua.phoban.nbtapi.utils.annotations.CheckUtil;
import camchua.phoban.nbtapi.utils.annotations.FAUtil;
import java.lang.invoke.SerializedLambda;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;

public class NBTTileEntity extends NBTCompound {
   private final BlockState tile;

   public NBTTileEntity(BlockState tile) {
      super((NBTCompound)null, (String)null);
      if (tile != null && (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3) || tile.isPlaced())) {
         this.tile = tile;
      } else {
         throw new NullPointerException("Tile can't be null/not placed!");
      }
   }

   public Object getCompound() {
      if (!Bukkit.isPrimaryThread()) {
         throw new NbtApiException("BlockEntity NBT needs to be accessed sync!");
      } else {
         return NBTReflectionUtil.getTileEntityNBTTagCompound(this.tile);
      }
   }

   protected void setCompound(Object compound) {
      if (!Bukkit.isPrimaryThread()) {
         throw new NbtApiException("BlockEntity NBT needs to be accessed sync!");
      } else {
         NBTReflectionUtil.setTileEntityNBTTagCompound(this.tile, compound);
      }
   }

   @AvailableSince(
      version = MinecraftVersion.MC1_14_R1
   )
   public NBTCompound getPersistentDataContainer() {
      FAUtil.check(this::getPersistentDataContainer, CheckUtil::isAvaliable);
      if (this.hasTag("PublicBukkitValues")) {
         return this.getCompound("PublicBukkitValues");
      } else {
         NBTContainer container = new NBTContainer();
         container.addCompound("PublicBukkitValues").setString("__nbtapi", "Marker to make the PersistentDataContainer have content");
         this.mergeCompound(container);
         return this.getCompound("PublicBukkitValues");
      }
   }

   // $FF: synthetic method
  /* private static Object $deserializeLambda$(SerializedLambda lambda) {
      switch (lambda.getImplMethodName()) {
         case "getPersistentDataContainer":
            if (lambda.getImplMethodKind() == 5 && lambda.getFunctionalInterfaceClass().equals("camchua/phoban/nbtapi/utils/annotations/ref/MethodRefrence") && lambda.getFunctionalInterfaceMethodName().equals("callable") && lambda.getFunctionalInterfaceMethodSignature().equals("()V") && lambda.getImplClass().equals("camchua/phoban/nbtapi/NBTTileEntity") && lambda.getImplMethodSignature().equals("()Lcamchua/phoban/nbtapi/NBTCompound;")) {
               return (NBTTileEntity)lambda.getCapturedArg(0)::getPersistentDataContainer;
            }
         default:
            throw new IllegalArgumentException("Invalid lambda deserialization");
      }
   }*/
}
