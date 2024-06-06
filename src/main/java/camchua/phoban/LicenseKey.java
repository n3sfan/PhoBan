package camchua.phoban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LicenseKey {
   public static final String plugins = "PhoBan";
   private static final String API_URL = "dihoastore-mc.tk";

   public static boolean KeyStatus(String key) {
      try {
         if (getIpaddress() == null) {
            return false;
         } else {
            String query = key + "!K04!" + getIpaddress();
            HttpURLConnection connection = (HttpURLConnection)(new URL("https://dihoastore-mc.tk/api/api.php?query=" + getMd5(query) + "&plugin=" + "PhoBan" + "&license=" + key + "&ip=" + getIpaddress())).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(2000);
            connection.connect();
            BufferedReader stream = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();

            String inputLine;
            while((inputLine = stream.readLine()) != null) {
               sb.append(inputLine);
            }

            stream.close();
            connection.disconnect();
            String json = sb.toString().replace(" ", "");
            if (json != null) {
               JSONParser parser = new JSONParser();
               JSONObject object = (JSONObject)parser.parse(json);
               if (object.get("error").toString().equals("yes")) {
                  return false;
               } else if (object.get("error").toString().equals("no")) {
                  return object.get("status").toString() != "online";
               } else {
                  return false;
               }
            } else {
               Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aJson can't be parse, please contact Di Hoa Store");
               return false;
            }
         }
      } catch (ParseException | IOException var9) {
         Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aJson can't be parse, please contact Di Hoa Store");
         return false;
      }
   }

   public static boolean action(String key, String action) {
      try {
         if (getIpaddress() == null) {
            return false;
         } else {
            String query = key + "!K04!" + getIpaddress();
            URL url = new URL("https://dihoastore-mc.tk/api/api.php?query=" + getMd5(query) + "&plugin=" + "PhoBan" + "&license=" + key + "&action=" + action + "&ip=" + getIpaddress());
            BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();

            String inputLine;
            while((inputLine = stream.readLine()) != null) {
               sb.append(inputLine);
            }

            stream.close();
            String json = sb.toString().replace(" ", "");
            if (json != null) {
               JSONParser parser = new JSONParser();
               JSONObject object = (JSONObject)parser.parse(json);
               if (object.get("error").toString().equals("yes")) {
                  Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aKey have an error, please contact Di Hoa Store");
                  return false;
               } else if (action.equals("enable")) {
                  return object.get("status").toString().equals("offline");
               } else {
                  return object.get("status").toString().equals("online");
               }
            } else {
               return false;
            }
         }
      } catch (ParseException | IOException var10) {
         Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aJson can't be parse, please contact Di Hoa Store");
         return false;
      }
   }

   public static String getVersion() {
      try {
         URL url = new URL("https://dihoastore-mc.tk/api/update.php?plugin=PhoBan");
         BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
         StringBuilder sb = new StringBuilder();

         String inputLine;
         while((inputLine = stream.readLine()) != null) {
            sb.append(inputLine);
         }

         stream.close();
         String json = sb.toString();
         if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject)parser.parse(json);
            return object.get("error") == "yes" ? "error string" : object.get("version").toString();
         } else {
            return "Json is null";
         }
      } catch (ParseException | IOException var7) {
         return "parser error";
      }
   }

   public static String getMd5(String input) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] messageDigest = md.digest(input.getBytes());
         BigInteger no = new BigInteger(1, messageDigest);

         String hashtext;
         for(hashtext = no.toString(16); hashtext.length() < 32; hashtext = "0" + hashtext) {
         }

         return hashtext;
      } catch (NoSuchAlgorithmException var5) {
         NoSuchAlgorithmException e = var5;
         throw new RuntimeException(e);
      }
   }

   private static String getIpaddress() {
      String ip = null;

      try {
         URL whatismyip = new URL("http://checkip.amazonaws.com");
         BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
         ip = in.readLine();
      } catch (MalformedURLException var3) {
         Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aCan't get your ip address");
      } catch (IOException var4) {
         Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan: \u00a7aCan't get your ip address");
      }

      return ip;
   }
}
