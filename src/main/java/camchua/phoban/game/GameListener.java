package camchua.phoban.game;

import camchua.phoban.PhoBan;
import camchua.phoban.manager.FileManager;
import camchua.phoban.utils.Messages;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class GameListener implements Listener {
   private HashMap<Player, Long> protect = new HashMap();

   @EventHandler
   public void onListener(EntityDeathEvent e) {
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         if (PlayerData.data().containsKey(p)) {
            if (FileManager.getFileConfig(FileManager.Files.CONFIG).getBoolean("Settings.DeathRespawn")) {
               try {
                  Bukkit.getScheduler().scheduleSyncDelayedTask(PhoBan.inst(), () -> {
                     p.spigot().respawn();
                     PlayerData data = (PlayerData)PlayerData.data().get(p);
                     Game game = data.getGame();
                     p.teleport(game.mobLocation());
                     if (this.protect.containsKey(p)) {
                        this.protect.remove(p);
                     }

                     this.protect.put(p, System.currentTimeMillis());
                  }, 1L);
               } catch (Exception var10) {
                  Exception ex = var10;
                  ex.printStackTrace();
                  System.out.println("\u00a7cError when respawn player " + p.getName());
               }
            } else {
               Bukkit.getScheduler().scheduleSyncDelayedTask(PhoBan.inst(), () -> {
                  p.spigot().respawn();
                  ((PlayerData)PlayerData.data().get(p)).getGame().leave(p, false);
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + p.getName());
                  Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + p.getName());
               }, 1L);
            }
         }
      } else {
//          System.out.println(EntityData.data().keySet().stream().anyMatch(key -> key.getUniqueId().equals(e.getEntity().getUniqueId())));
//          System.out.println(MythicMobs.inst().getAPIHelper().isMythicMob(e.getEntity()));
          final BukkitAPIHelper helper = MythicMobs.inst().getAPIHelper();

          if (e.getEntity().getKiller() != null) {
              HashMap<UUID, EntityData> dataMap = new HashMap<>();
              if (EntityData.data().containsKey(e.getEntity().getUniqueId())) {
//                      final String internalName = mm.getMythicMobInternalName(e.getEntity());
//                      final String name = mm.getMythicMobDisplayNameGet((Entity) e.getEntity());
                  final String killerName = e.getEntity().getKiller().getName();
                  final Entity entity = e.getEntity();
                  final EntityData data = (EntityData) EntityData.data().get(e.getEntity().getUniqueId());
//                  System.out.println(data.name + " killed2");
                  Bukkit.getScheduler().runTask(PhoBan.inst(), () -> {
                      Game game = data.getGame();
                      game.addProgress(data.internalName, 1);
                      // BEGIN EDIT
//               if (e.getEntity().getKiller() != null) {

                      game.addKill(ChatColor.stripColor(killerName), 1);
//               }
                      // END EDIT
                      int max = game.getProgressMax();
                      int current = game.getProgressCurrent();
                      Iterator var8 = game.getPlayers().iterator();

                      while (var8.hasNext()) {
                          Player player = (Player) var8.next();
                          player.sendMessage(Messages.get("MobsLeft").replace("<name>", data.name).replace("<max>", max + "").replace("<current>", current + ""));
                      }

                      EntityData.data().remove(entity.getUniqueId());
                  });
              }
          }
      }
   }

   // BEGIN EDIT
   /*@EventHandler
   public void onEntityRemove(EntityRemoveFromWorldEvent e) {
//      Bukkit.getScheduler().runTaskLater(PhoBan.inst(), () -> {
      BukkitAPIHelper mm = PhoBan.inst().getBukkitAPIHelper();
      if (mm.isMythicMob(e.getEntity())) {
         if (EntityData.data().containsKey(e.getEntity())) {
            EntityData data = (EntityData) EntityData.data().get(e.getEntity());
            Game game = data.getGame();
            game.addProgress(mm.getMythicMobInternalName(e.getEntity()), 1);
            // BEGIN EDIT
//               if (e.getEntity().getKiller() != null) {
            game.addKill(((LivingEntity) e.getEntity()).getKiller().getName(), 1);
//               }
            // END EDIT
            int max = game.getProgressMax();
            int current = game.getProgressCurrent();
            String name = mm.getMythicMobDisplayNameGet((Entity) e.getEntity());
            Iterator var8 = game.getPlayers().iterator();

            while (var8.hasNext()) {
               Player player = (Player) var8.next();
               player.sendMessage(Messages.get("MobsLeft").replace("<name>", name).replace("<max>", max + "").replace("<current>", current + ""));
            }

            EntityData.data().remove(e.getEntity());
         }
      }
//      }, 1L);
   }*/
   // END

   private boolean noProtect(Player p) {
      if (!this.protect.containsKey(p)) {
         return true;
      } else {
         long current = System.currentTimeMillis();
         int respawnProtect = FileManager.getFileConfig(FileManager.Files.CONFIG).getInt("Settings.RespawnProtect");
         long deathTime = (Long)this.protect.get(p);
         return (current - deathTime) / 1000L > (long)respawnProtect;
      }
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         if (!this.noProtect(p)) {
            e.setCancelled(true);
         }

      }
   }
}
