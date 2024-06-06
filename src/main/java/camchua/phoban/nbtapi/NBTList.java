package camchua.phoban.nbtapi;

import camchua.phoban.nbtapi.utils.MinecraftVersion;
import camchua.phoban.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class NBTList<T> implements List<T> {
   private String listName;
   private NBTCompound parent;
   private NBTType type;
   protected Object listObject;

   protected NBTList(NBTCompound owner, String name, NBTType type, Object list) {
      this.parent = owner;
      this.listName = name;
      this.type = type;
      this.listObject = list;
   }

   public String getName() {
      return this.listName;
   }

   public NBTCompound getParent() {
      return this.parent;
   }

   protected void save() {
      this.parent.set(this.listName, this.listObject);
   }

   protected abstract Object asTag(T var1);

   public boolean add(T element) {
      boolean var8;
      try {
         this.parent.getWriteLock().lock();
         if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
            ReflectionMethod.LIST_ADD.run(this.listObject, this.size(), this.asTag(element));
         } else {
            ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
         }

         this.save();
         var8 = true;
      } catch (Exception var6) {
         Exception ex = var6;
         throw new NbtApiException(ex);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return var8;
   }

   public void add(int index, T element) {
      try {
         this.parent.getWriteLock().lock();
         if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
            ReflectionMethod.LIST_ADD.run(this.listObject, index, this.asTag(element));
         } else {
            ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
         }

         this.save();
      } catch (Exception var7) {
         Exception ex = var7;
         throw new NbtApiException(ex);
      } finally {
         this.parent.getWriteLock().unlock();
      }

   }

   public T set(int index, T element) {
      Object var4;
      try {
         this.parent.getWriteLock().lock();
         T prev = this.get(index);
         ReflectionMethod.LIST_SET.run(this.listObject, index, this.asTag(element));
         this.save();
         var4 = prev;
      } catch (Exception var8) {
         Exception ex = var8;
         throw new NbtApiException(ex);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return (T) var4;
   }

   public T remove(int i) {
      Object var3;
      try {
         this.parent.getWriteLock().lock();
         T old = this.get(i);
         ReflectionMethod.LIST_REMOVE_KEY.run(this.listObject, i);
         this.save();
         var3 = old;
      } catch (Exception var7) {
         Exception ex = var7;
         throw new NbtApiException(ex);
      } finally {
         this.parent.getWriteLock().unlock();
      }

      return (T) var3;
   }

   public int size() {
      int var7;
      try {
         this.parent.getReadLock().lock();
         var7 = (Integer)ReflectionMethod.LIST_SIZE.run(this.listObject);
      } catch (Exception var5) {
         Exception ex = var5;
         throw new NbtApiException(ex);
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var7;
   }

   public NBTType getType() {
      return this.type;
   }

   public boolean isEmpty() {
      return this.size() == 0;
   }

   public void clear() {
      while(!this.isEmpty()) {
         this.remove(0);
      }

   }

   public boolean contains(Object o) {
      try {
         this.parent.getReadLock().lock();

         for(int i = 0; i < this.size(); ++i) {
            if (o.equals(this.get(i))) {
               boolean var3 = true;
               return var3;
            }
         }

         boolean var7 = false;
         return var7;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   public int indexOf(Object o) {
      byte var7;
      try {
         this.parent.getReadLock().lock();

         for(int i = 0; i < this.size(); ++i) {
            if (o.equals(this.get(i))) {
               int var3 = i;
               return var3;
            }
         }

         var7 = -1;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var7;
   }

   public boolean addAll(Collection<? extends T> c) {
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();
         Iterator var3 = c.iterator();

         while(var3.hasNext()) {
            T ele = (T) var3.next();
            this.add(ele);
         }

         boolean var8 = size != this.size();
         return var8;
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   public boolean addAll(int index, Collection<? extends T> c) {
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();
         Iterator var4 = c.iterator();

         while(var4.hasNext()) {
            T ele = (T) var4.next();
            this.add(index++, ele);
         }

         boolean var9 = size != this.size();
         return var9;
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   public boolean containsAll(Collection<?> c) {
      boolean var4;
      try {
         this.parent.getReadLock().lock();
         Iterator var2 = c.iterator();

         Object ele;
         do {
            if (!var2.hasNext()) {
               boolean var8 = true;
               return var8;
            }

            ele = var2.next();
         } while(this.contains(ele));

         var4 = false;
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var4;
   }

   public int lastIndexOf(Object o) {
      try {
         this.parent.getReadLock().lock();
         int index = -1;

         int i;
         for(i = 0; i < this.size(); ++i) {
            if (o.equals(this.get(i))) {
               index = i;
            }
         }

         i = index;
         return i;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   public boolean removeAll(Collection<?> c) {
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();
         Iterator var3 = c.iterator();

         while(var3.hasNext()) {
            Object obj = var3.next();
            this.remove(obj);
         }

         boolean var8 = size != this.size();
         return var8;
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   public boolean retainAll(Collection<?> c) {
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();
         Iterator var3 = c.iterator();

         label65:
         while(true) {
            if (var3.hasNext()) {
               Object obj = var3.next();
               int i = 0;

               while(true) {
                  if (i >= this.size()) {
                     continue label65;
                  }

                  if (!obj.equals(this.get(i))) {
                     this.remove(i--);
                  }

                  ++i;
               }
            }

            boolean var9 = size != this.size();
            return var9;
         }
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   public boolean remove(Object o) {
      try {
         this.parent.getWriteLock().lock();
         int size = this.size();

         int id;
         while((id = this.indexOf(o)) != -1) {
            this.remove(id);
         }

         boolean var4 = size != this.size();
         return var4;
      } finally {
         this.parent.getWriteLock().unlock();
      }
   }

   public Iterator<T> iterator() {
      return new Iterator<T>() {
         private int index = -1;

         public boolean hasNext() {
            return NBTList.this.size() > this.index + 1;
         }

         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return NBTList.this.get(++this.index);
            }
         }

         public void remove() {
            NBTList.this.remove(this.index);
            --this.index;
         }
      };
   }

   public ListIterator<T> listIterator() {
      return this.listIterator(0);
   }

   public ListIterator<T> listIterator(final int startIndex) {
      final NBTList<T> list = this;
      return new ListIterator<T>() {
         int index = startIndex - 1;

         public void add(T e) {
            list.add(this.index, e);
         }

         public boolean hasNext() {
            return NBTList.this.size() > this.index + 1;
         }

         public boolean hasPrevious() {
            return this.index >= 0 && this.index <= NBTList.this.size();
         }

         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return NBTList.this.get(++this.index);
            }
         }

         public int nextIndex() {
            return this.index + 1;
         }

         public T previous() {
            if (!this.hasPrevious()) {
               throw new NoSuchElementException("Id: " + (this.index - 1));
            } else {
               return NBTList.this.get(this.index--);
            }
         }

         public int previousIndex() {
            return this.index - 1;
         }

         public void remove() {
            list.remove(this.index);
            --this.index;
         }

         public void set(T e) {
            list.set(this.index, e);
         }
      };
   }

   public Object[] toArray() {
      try {
         this.parent.getReadLock().lock();
         Object[] ar = new Object[this.size()];

         for(int i = 0; i < this.size(); ++i) {
            ar[i] = this.get(i);
         }

         Object[] var6 = ar;
         return var6;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   public <E> E[] toArray(E[] a) {
      try {
         this.parent.getReadLock().lock();
         E[] ar = Arrays.copyOf(a, this.size());
         Arrays.fill(ar, (Object)null);
         Class<?> arrayclass = a.getClass().getComponentType();

         for(int i = 0; i < this.size(); ++i) {
            T obj = this.get(i);
            if (!arrayclass.isInstance(obj)) {
               throw new ArrayStoreException("The array does not match the objects stored in the List.");
            }

            ar[i] = (E) this.get(i);
         }

         Object[] var9 = ar;
         return (E[]) var9;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   public List<T> subList(int fromIndex, int toIndex) {
      try {
         this.parent.getReadLock().lock();
         ArrayList<T> list = new ArrayList();

         for(int i = fromIndex; i < toIndex; ++i) {
            list.add(this.get(i));
         }

         ArrayList var8 = list;
         return var8;
      } finally {
         this.parent.getReadLock().unlock();
      }
   }

   public String toString() {
      String var1;
      try {
         this.parent.getReadLock().lock();
         var1 = this.listObject.toString();
      } finally {
         this.parent.getReadLock().unlock();
      }

      return var1;
   }
}
