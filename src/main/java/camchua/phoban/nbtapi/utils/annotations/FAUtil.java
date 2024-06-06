package camchua.phoban.nbtapi.utils.annotations;

import camchua.phoban.nbtapi.utils.annotations.ref.MethodRefrence;
import camchua.phoban.nbtapi.utils.annotations.ref.MethodRefrence1;
import camchua.phoban.nbtapi.utils.annotations.ref.MethodRefrence2;

import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.function.Function;

public class FAUtil {
   private static HashSet<String> cache = new HashSet();

   public static <T extends Annotation> T getAnnotation(MethodRefrence method, Class<T> annotation) {
      return getInternalMethod(method).getAnnotation(annotation);
   }

   public static <T extends Annotation, Z> T getAnnotation(MethodRefrence1<Z> method, Class<T> annotation) {
      return getInternalMethod(method).getAnnotation(annotation);
   }

   public static <T extends Annotation, Z, X> T getAnnotation(MethodRefrence2<Z, X> method, Class<T> annotation) {
      return getInternalMethod(method).getAnnotation(annotation);
   }

   public static Method getMethod(MethodRefrence method) {
      return getInternalMethod(method);
   }

   public static <Z> Method getMethod(MethodRefrence1<Z> method) {
      return getInternalMethod(method);
   }

   public static <T, Z> Method getMethod(MethodRefrence2<T, Z> method) {
      return getInternalMethod(method);
   }

   public static void check(MethodRefrence method, Function<Method, Boolean> checker) {
      checkLambda(method, checker);
   }

   public static <T> T check(MethodRefrence1<T> method, Function<Method, Boolean> checker) {
      checkLambda(method, checker);
      return null;
   }

   public static <T, Z> T check(MethodRefrence2<T, Z> method, Function<Method, Boolean> checker) {
      checkLambda(method, checker);
      return null;
   }

   private static void checkLambda(Object obj, Function<Method, Boolean> callable) {
      if (!cache.contains(obj.toString().split("/")[0])) {
         Method method = getInternalMethod(obj);
         if (method != null) {
            Boolean noRechecking = (Boolean)callable.apply(method);
            if (noRechecking) {
               cache.add(obj.toString().split("/")[0]);
            }
         }

         cache.add(obj.toString().split("/")[0]);
      }
   }

   private static Method getInternalMethod(Object obj) {
      for(Class<?> cl = obj.getClass(); cl != null; cl = cl.getSuperclass()) {
         try {
            Method m = cl.getDeclaredMethod("writeReplace");
            m.setAccessible(true);
            Object replacement = m.invoke(obj);
            if (!(replacement instanceof SerializedLambda)) {
               break;
            }

            SerializedLambda l = (SerializedLambda)replacement;
            Method[] var5 = Class.forName(l.getImplClass().replace('/', '.')).getDeclaredMethods();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Method method = var5[var7];
               if (method.getName().equals(l.getImplMethodName())) {
                  return method;
               }
            }
         } catch (InvocationTargetException | IllegalAccessException var9) {
            break;
         } catch (Exception var10) {
         }
      }

      return null;
   }
}
