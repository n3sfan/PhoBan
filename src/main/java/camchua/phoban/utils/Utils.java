package camchua.phoban.utils;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Utils {
   private static boolean LEGACY = true;

   public static void checkVersion() {
      switch (Bukkit.getBukkitVersion().split("-")[0]) {
         case "1.13":
         case "1.13.1":
         case "1.13.2":
         case "1.14":
         case "1.14.1":
         case "1.14.2":
         case "1.14.3":
         case "1.14.4":
         case "1.15":
         case "1.15.1":
         case "1.15.2":
         case "1.16":
         case "1.16.1":
         case "1.16.2":
         case "1.16.3":
         case "1.16.4":
         case "1.16.5":
         case "1.17":
         case "1.17.1":
         case "1.18":
         case "1.18.1":
         case "1.18.2":
         case "1.19":
         case "1.19.1":
            LEGACY = false;
         default:
      }
   }

   public static boolean isLegacy() {
      return LEGACY;
   }

   public static Material matchMaterial(String mat) {
      Material res = LEGACY ? Material.matchMaterial(mat) : Material.matchMaterial(mat, LEGACY);
      if (res == null)
         return Material.STONE;
      return res;
   }

   public static int firstEmpty(int rows) {
      if (rows < 3) {
         rows = 3;
      }

      switch (rows) {
         case 3:
            return 16;
         case 4:
            return 25;
         case 5:
            return 34;
         case 6:
            return 43;
         default:
            return 43;
      }
   }

   public static boolean checkStage(HashMap<String, Integer> require, HashMap<String, Integer> current) {
      Iterator var2 = require.keySet().iterator();

      String key;
      do {
         if (!var2.hasNext()) {
            return true;
         }

         key = (String)var2.next();
         if (!current.containsKey(key)) {
            return false;
         }
      } while((Integer)current.get(key) >= (Integer)require.get(key));

      return false;
   }

   public static boolean isSuckBlock(Location loc) {
      Location loc1 = loc.clone();
      Location loc2 = loc.clone().add(0.0, 1.0, 0.0);
      return !loc1.getBlock().getType().equals(Material.AIR) && !loc2.getBlock().getType().equals(Material.AIR);
   }

   public static void scanSection(FileConfiguration configScan, FileConfiguration newConfig, String key, String arenaName) {
      if (configScan.contains(key)) {
         Iterator var4 = configScan.getConfigurationSection(key).getKeys(false).iterator();

         while(var4.hasNext()) {
            String k = (String)var4.next();
            if (configScan.isConfigurationSection(key + "." + k)) {
               scanSection(configScan, newConfig, key + "." + k, arenaName);
            } else {
               newConfig.set((key + "." + k).replaceFirst(arenaName + ".", ""), configScan.get(key + "." + k));
            }
         }

      }
   }

}
