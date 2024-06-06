package camchua.phoban.nbtapi.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class ApiMetricsLite {
   private static final String PLUGINNAME = "ItemNBTAPI";
   public static final int B_STATS_VERSION = 1;
   public static final int NBT_BSTATS_VERSION = 1;
   private static final String URL = "https://bStats.org/submitData/bukkit";
   private boolean enabled;
   private static boolean logFailedRequests;
   private static boolean logSentData;
   private static boolean logResponseStatusText;
   private static String serverUUID;
   private Plugin plugin;

   public ApiMetricsLite() {
      Plugin[] var1 = Bukkit.getPluginManager().getPlugins();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Plugin plug = var1[var3];
         this.plugin = plug;
         if (this.plugin != null) {
            break;
         }
      }

      if (this.plugin != null) {
         File bStatsFolder = new File(this.plugin.getDataFolder().getParentFile(), "bStats");
         File configFile = new File(bStatsFolder, "config.yml");
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            config.options().header("bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)").copyDefaults(true);

            try {
               config.save(configFile);
            } catch (IOException var8) {
            }
         }

         serverUUID = config.getString("serverUuid");
         logFailedRequests = config.getBoolean("logFailedRequests", false);
         this.enabled = config.getBoolean("enabled", true);
         logSentData = config.getBoolean("logSentData", false);
         logResponseStatusText = config.getBoolean("logResponseStatusText", false);
         if (this.enabled) {
            boolean found = false;
            Iterator var5 = Bukkit.getServicesManager().getKnownServices().iterator();

            while(var5.hasNext()) {
               Class<?> service = (Class)var5.next();

               try {
                  service.getField("NBT_BSTATS_VERSION");
                  return;
               } catch (NoSuchFieldException var9) {
               }

               try {
                  service.getField("B_STATS_VERSION");
                  found = true;
                  break;
               } catch (NoSuchFieldException var10) {
               }
            }

            if (Bukkit.isPrimaryThread()) {
               Bukkit.getServicesManager().register(ApiMetricsLite.class, this, this.plugin, ServicePriority.Normal);
               if (!found) {
                  MinecraftVersion.getLogger().info("[NBTAPI] Using the plugin '" + this.plugin.getName() + "' to create a bStats instance!");
                  this.startSubmitting();
               }
            } else {
               boolean finalFound = found;
               Bukkit.getScheduler().runTask(this.plugin, () -> {
                  Bukkit.getServicesManager().register(ApiMetricsLite.class, this, this.plugin, ServicePriority.Normal);
                  if (!finalFound) {
                     MinecraftVersion.getLogger().info("[NBTAPI] Using the plugin '" + this.plugin.getName() + "' to create a bStats instance!");
                     this.startSubmitting();
                  }

               });
            }
         }

      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   private void startSubmitting() {
      final Timer timer = new Timer(true);
      timer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            if (!ApiMetricsLite.this.plugin.isEnabled()) {
               timer.cancel();
            } else {
               Bukkit.getScheduler().runTask(ApiMetricsLite.this.plugin, () -> {
                  ApiMetricsLite.this.submitData();
               });
            }
         }
      }, 300000L, 1800000L);
   }

   public JsonObject getPluginData() {
      JsonObject data = new JsonObject();
      data.addProperty("pluginName", "ItemNBTAPI");
      data.addProperty("pluginVersion", "2.11.0-SNAPSHOT");
      data.add("customCharts", new JsonArray());
      return data;
   }

   private JsonObject getServerData() {
      int playerAmount;
      try {
         Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
         playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection)onlinePlayersMethod.invoke(Bukkit.getServer())).size() : ((Player[])onlinePlayersMethod.invoke(Bukkit.getServer())).length;
      } catch (Exception var11) {
         playerAmount = Bukkit.getOnlinePlayers().size();
      }

      int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
      String bukkitVersion = Bukkit.getVersion();
      String bukkitName = Bukkit.getName();
      String javaVersion = System.getProperty("java.version");
      String osName = System.getProperty("os.name");
      String osArch = System.getProperty("os.arch");
      String osVersion = System.getProperty("os.version");
      int coreCount = Runtime.getRuntime().availableProcessors();
      JsonObject data = new JsonObject();
      data.addProperty("serverUUID", serverUUID);
      data.addProperty("playerAmount", playerAmount);
      data.addProperty("onlineMode", onlineMode);
      data.addProperty("bukkitVersion", bukkitVersion);
      data.addProperty("bukkitName", bukkitName);
      data.addProperty("javaVersion", javaVersion);
      data.addProperty("osName", osName);
      data.addProperty("osArch", osArch);
      data.addProperty("osVersion", osVersion);
      data.addProperty("coreCount", coreCount);
      return data;
   }

   private void submitData() {
      final JsonObject data = this.getServerData();
      JsonArray pluginData = new JsonArray();
      Iterator var3 = Bukkit.getServicesManager().getKnownServices().iterator();

      while(var3.hasNext()) {
         Class<?> service = (Class)var3.next();

         try {
            service.getField("B_STATS_VERSION");
            Iterator var5 = Bukkit.getServicesManager().getRegistrations(service).iterator();

            while(var5.hasNext()) {
               RegisteredServiceProvider<?> provider = (RegisteredServiceProvider)var5.next();

               try {
                  Object plugin = provider.getService().getMethod("getPluginData").invoke(provider.getProvider());
                  if (plugin instanceof JsonObject) {
                     pluginData.add((JsonObject)plugin);
                  } else {
                     try {
                        Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
                        if (plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) {
                           Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString");
                           jsonStringGetter.setAccessible(true);
                           String jsonString = (String)jsonStringGetter.invoke(plugin);
                           JsonObject object = (new JsonParser()).parse(jsonString).getAsJsonObject();
                           pluginData.add(object);
                        }
                     } catch (ClassNotFoundException var12) {
                        ClassNotFoundException e = var12;
                        if (logFailedRequests) {
                           MinecraftVersion.getLogger().log(Level.WARNING, (String)"[NBTAPI][BSTATS] Encountered exception while posting request!", (Throwable)e);
                        }
                     }
                  }
               } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException var13) {
               }
            }
         } catch (NoSuchFieldException var14) {
         }
      }

      data.add("plugins", pluginData);
      (new Thread(new Runnable() {
         public void run() {
            try {
               ApiMetricsLite.sendData(ApiMetricsLite.this.plugin, data);
            } catch (Exception var2) {
               Exception e = var2;
               if (ApiMetricsLite.logFailedRequests) {
                  MinecraftVersion.getLogger().log(Level.WARNING, (String)("[NBTAPI][BSTATS] Could not submit plugin stats of " + ApiMetricsLite.this.plugin.getName()), (Throwable)e);
               }
            }

         }
      })).start();
   }

   private static void sendData(Plugin plugin, JsonObject data) throws Exception {
      if (data == null) {
         throw new IllegalArgumentException("Data cannot be null!");
      } else if (Bukkit.isPrimaryThread()) {
         throw new IllegalAccessException("This method must not be called from the main thread!");
      } else {
         if (logSentData) {
            MinecraftVersion.getLogger().info("[NBTAPI][BSTATS] Sending data to bStats: " + data.toString());
         }

         HttpsURLConnection connection = (HttpsURLConnection)(new URL("https://bStats.org/submitData/bukkit")).openConnection();
         byte[] compressedData = compress(data.toString());
         connection.setRequestMethod("POST");
         connection.addRequestProperty("Accept", "application/json");
         connection.addRequestProperty("Connection", "close");
         connection.addRequestProperty("Content-Encoding", "gzip");
         connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("User-Agent", "MC-Server/1");
         connection.setDoOutput(true);
         DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
         outputStream.write(compressedData);
         outputStream.flush();
         outputStream.close();
         InputStream inputStream = connection.getInputStream();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         StringBuilder builder = new StringBuilder();

         String line;
         while((line = bufferedReader.readLine()) != null) {
            builder.append(line);
         }

         bufferedReader.close();
         if (logResponseStatusText) {
            MinecraftVersion.getLogger().info("[NBTAPI][BSTATS] Sent data to bStats and received response: " + builder.toString());
         }

      }
   }

   private static byte[] compress(String str) throws IOException {
      if (str == null) {
         return new byte[0];
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
         gzip.write(str.getBytes(StandardCharsets.UTF_8));
         gzip.close();
         return outputStream.toByteArray();
      }
   }
}
