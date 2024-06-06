package camchua.phoban.utils;

import camchua.phoban.manager.FileManager;

public class Messages {
   public static String get(String message) {
      return FileManager.getFileConfig(FileManager.Files.MESSAGE).getString(message, message).replace("&", "\u00a7");
   }

   public static boolean has(String message) {
      return FileManager.getFileConfig(FileManager.Files.MESSAGE).contains(message);
   }
}
