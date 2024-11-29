package camchua.phoban.game;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Entity;

public class EntityData {
   private static HashMap<UUID, EntityData> data = new HashMap<>();
   private Entity entity;
   private Game game;
   // BEGIN EDIT
   public UUID uuid;
   public String name, internalName;
   // END

   public static HashMap<UUID, EntityData> data() {
      return data;
   }

   public EntityData(Entity e, Game g, String name, String internalName) {
      this.entity = e;
      this.game = g;
      // BEGIN EDIT
      this.uuid = e.getUniqueId();
      this.name = name;
      this.internalName = internalName;
      // END
   }

   public Entity getEntity() {
      return this.entity;
   }

   public Game getGame() {
      return this.game;
   }
}
