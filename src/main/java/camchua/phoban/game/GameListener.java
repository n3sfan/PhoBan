package camchua.phoban.game;

import camchua.phoban.PhoBan;
import camchua.phoban.manager.FileManager;
import camchua.phoban.mythicmobs.BukkitAPIHelper;
import camchua.phoban.utils.Messages;
import java.util.HashMap;
import java.util.Iterator;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
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
         BukkitAPIHelper mm = PhoBan.inst().getBukkitAPIHelper();
         if (mm.isMythicMob(e.getEntity())) {
            if (EntityData.data().containsKey(e.getEntity())) {
               EntityData data = (EntityData)EntityData.data().get(e.getEntity());
               Game game = data.getGame();
               game.addProgress(mm.getMythicMobInternalName(e.getEntity()), 1);
               // BEGIN EDIT
//               if (e.getEntity().getKiller() != null) {
                  game.addKill(e.getEntity().getKiller().getName(), 1);
//               }
               // END EDIT
               int max = game.getProgressMax();
               int current = game.getProgressCurrent();
               String name = mm.getMythicMobDisplayNameGet((Entity)e.getEntity());
               Iterator var8 = game.getPlayers().iterator();

               while(var8.hasNext()) {
                  Player player = (Player)var8.next();
                  player.sendMessage(Messages.get("MobsLeft").replace("<name>", name).replace("<max>", max + "").replace("<current>", current + ""));
               }

               EntityData.data().remove(e.getEntity());
            }
         }
      }
   }

   @EventHandler
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
   }

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
