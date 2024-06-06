package camchua.phoban.utils;

import camchua.phoban.game.Game;
import camchua.phoban.game.GameStatus;
import camchua.phoban.game.PlayerData;
import camchua.phoban.gui.PhoBanGui;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.stream.IntStream;

public class PhoBanExpansion extends PlaceholderExpansion {
   public String getIdentifier() {
      return "phoban";
   }

   public String getPlugin() {
      return "PhoBan";
   }

   public String getAuthor() {
      return "CamChua_VN";
   }

   public String getVersion() {
      return "1.0";
   }

   public String onRequest(OfflinePlayer p, String identifier) {
      // BEGIN EDIT
      if (identifier.matches("tongtimechoi_\\w+")) {
         String room = identifier.substring("tongtimechoi_".length());
         Game game = Game.getGame(room);
         if (game == null || game.getStatus() != GameStatus.PLAYING) {
            return "0";
         }

         return PhoBanGui.timeFormat(game.getMaxTime());
      } else if (identifier.matches("luotketiep_\\w+")) {
         String room = identifier.substring("luotketiep_".length());
         Game game = Game.getGame(room);
         if (game == null || game.getStatus() != GameStatus.PLAYING) {
            return "0";
         }
         return PhoBanGui.timeFormat(game.getStageTime());
      } else if (identifier.matches("soluongquai_\\w+")) {
         String room = identifier.substring("soluongquai_".length());
         Game game = Game.getGame(room);
         if (game == null || game.getStatus() != GameStatus.PLAYING) {
            return "0";
         }

         return "" + (game.getProgressMax() - game.getProgressCurrent());
      } else if (identifier.matches("soquaigietduoc_\\w+")) {
         String room = identifier.substring("soquaigietduoc_".length());
         Game game = Game.getGame(room);
         if (game == null || game.getStatus() != GameStatus.PLAYING) {
            return "0";
         }
         return "" + (game.getKills().values().stream().mapToInt(i -> i).sum());
      }
      // END
      try {
         String[] args = identifier.split("_");
         Player player = p.getPlayer();
         if (!PlayerData.data().containsKey(player)) {
            return "";
         } else {
            PlayerData data = (PlayerData)PlayerData.data().get(player);
            Game game = data.getGame();
            FileConfiguration room = game.getConfig();
            switch (args[0].toLowerCase()) {
               case "time":
                  return PhoBanGui.timeFormat(data.getGame().getTimeLeft());
               case "prefix":
                  return room.getString("Prefix", "").replace("&", "\u00a7");
               case "maxplayers":
                  return String.valueOf(room.getInt("Player"));
               case "minplayers":
                  return String.valueOf(data.getGame().getPlayers().size());
               default:
                  return "";
            }
         }
      } catch (Exception var10) {
         Exception ex = var10;
         return "PhoBan placeholder error: " + ex.getMessage() + " | identifier: " + identifier;
      }
   }
}
