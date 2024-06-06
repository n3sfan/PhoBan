package camchua.phoban.nbtapi;

import camchua.phoban.nbtapi.utils.nmsmappings.ClassWrapper;
import camchua.phoban.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class NBTFloatList extends NBTList<Float> {
   protected NBTFloatList(NBTCompound owner, String name, NBTType type, Object list) {
      super(owner, name, type, list);
   }

   protected Object asTag(Float object) {
      try {
         Constructor<?> con = ClassWrapper.NMS_NBTTAGFLOAT.getClazz().getDeclaredConstructor(Float.TYPE);
         con.setAccessible(true);
         return con.newInstance(object);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
         Exception e = var3;
         throw new NbtApiException("Error while wrapping the Object " + object + " to it's NMS object!", e);
      }
   }

   public Float get(int index) {
      try {
         Object obj = ReflectionMethod.LIST_GET.run(this.listObject, index);
         return Float.valueOf(obj.toString());
      } catch (NumberFormatException var3) {
         return 0.0F;
      } catch (Exception var4) {
         Exception ex = var4;
         throw new NbtApiException(ex);
      }
   }
}
