package camchua.phoban.game;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerData {
   private static HashMap<Player, PlayerData> data = new HashMap();
   private Player player;
   private Location location;
   private Game game;

   public static HashMap<Player, PlayerData> data() {
      return data;
   }

   public PlayerData(Player p, Game g, Location loc) {
      this.player = p;
      this.location = loc;
      this.game = g;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Game getGame() {
      return this.game;
   }

   public Location getLocation() {
      return this.location;
   }
}
