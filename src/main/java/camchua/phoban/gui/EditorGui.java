package camchua.phoban.gui;

import camchua.phoban.PhoBan;
import camchua.phoban.game.Game;
import camchua.phoban.manager.FileManager;
import camchua.phoban.nbtapi.NBTItem;
import camchua.phoban.utils.ItemBuilder;
import camchua.phoban.utils.Messages;
import camchua.phoban.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class EditorGui implements Listener {
   private static HashMap<Player, String> viewers = new HashMap();
   private static HashMap<Player, String> editor = new HashMap();

   public static void open(Player p, String name) {
      FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
      Game game = Game.getGame(name);
      File configFile = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
      if (!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (Exception var17) {
         }
      }

      FileConfiguration room = game == null ? YamlConfiguration.loadConfiguration(configFile) : game.getConfig();
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, gui.getInt("EditorGui.Rows") * 9, gui.getString("EditorGui.Title").replace("&", "\u00a7").replace("<name>", name));
      HashMap<String, List<String>> replace = new HashMap();
      replace.put("<max_players>", Arrays.asList(String.valueOf(((FileConfiguration)room).getInt("Player", 0))));
      replace.put("<prefix>", Arrays.asList(((FileConfiguration)room).getString("Prefix", "").replace("&", "\u00a7")));
      replace.put("<time>", Arrays.asList(String.valueOf(((FileConfiguration)room).getInt("Time", 0))));
      List<String> lores = new ArrayList();
      Iterator var9;
      String s;
      String content;
      int amount;
      String clicktype;
      if (((FileConfiguration)room).contains("Mob1")) {
         var9 = ((FileConfiguration)room).getConfigurationSection("Mob1").getKeys(false).iterator();

         while(var9.hasNext()) {
            s = (String)var9.next();
            content = ((FileConfiguration)room).getString("Mob1." + s + ".Type", "null");
            amount = ((FileConfiguration)room).getInt("Mob1." + s + ".Amount", 0);
            clicktype = gui.getString("EditorGui.MobLoreFormat").replace("&", "\u00a7").replace("<mobs>", content).replace("<amount>", amount + "");
            lores.add(clicktype);
         }
      }

      replace.put("<mob1>", lores);
      lores = new ArrayList();
      if (((FileConfiguration)room).contains("Mob2")) {
         var9 = ((FileConfiguration)room).getConfigurationSection("Mob2").getKeys(false).iterator();

         while(var9.hasNext()) {
            s = (String)var9.next();
            content = ((FileConfiguration)room).getString("Mob2." + s + ".Type", "null");
            amount = ((FileConfiguration)room).getInt("Mob2." + s + ".Amount", 0);
            clicktype = gui.getString("EditorGui.MobLoreFormat").replace("&", "\u00a7").replace("<mobs>", content).replace("<amount>", amount + "");
            lores.add(clicktype);
         }
      }

      replace.put("<mob2>", lores);
      lores = new ArrayList();
      if (((FileConfiguration)room).contains("Mob3")) {
         var9 = ((FileConfiguration)room).getConfigurationSection("Mob3").getKeys(false).iterator();

         while(var9.hasNext()) {
            s = (String)var9.next();
            content = ((FileConfiguration)room).getString("Mob3." + s + ".Type", "null");
            amount = ((FileConfiguration)room).getInt("Mob3." + s + ".Amount", 0);
            clicktype = gui.getString("EditorGui.MobLoreFormat").replace("&", "\u00a7").replace("<mobs>", content).replace("<amount>", amount + "");
            lores.add(clicktype);
         }
      }

      replace.put("<mob3>", lores);
      replace.put("<boss_type>", Arrays.asList(((FileConfiguration)room).getString("Boss.Type", "null")));
      replace.put("<boss_amount>", Arrays.asList(String.valueOf(((FileConfiguration)room).getInt("Boss.Amount", 0))));
      Location spawn = (Location)((FileConfiguration)room).get("Spawn");
      replace.put("<spawn>", Arrays.asList(spawn == null ? "" : spawn.getBlockX() + "," + spawn.getBlockY() + "," + spawn.getBlockZ() + "," + spawn.getWorld().getName()));
      replace.put("<type>", Arrays.asList(((FileConfiguration)room).getString("Type"), ""));
      Iterator var19 = gui.getConfigurationSection("EditorGui.Content").getKeys(false).iterator();

      while(true) {
         while(var19.hasNext()) {
            content = (String)var19.next();
            ItemStack item = ItemBuilder.build(FileManager.Files.GUI, "EditorGui.Content." + content, replace);
            if (gui.contains("EditorGui.Content." + content + ".ClickType")) {
               clicktype = gui.getString("EditorGui.Content." + content + ".ClickType");
               if (clicktype.contains("EditMob")) {
                  String mob = clicktype.replace("Edit", "");
                  if (isEdited(name, mob)) {
                     item = ItemBuilder.build(FileManager.Files.GUI, "EditorGui.Content." + content + ".Edited", replace);
                  }
               }
            }

            NBTItem nbt = new NBTItem(item);
            if (gui.contains("EditorGui.Content." + content + ".ClickType")) {
               nbt.setString("EditorGui_ClickType", gui.getString("EditorGui.Content." + content + ".ClickType"));
            }

            Iterator var22 = gui.getIntegerList("EditorGui.Content." + content + ".Slot").iterator();

            while(var22.hasNext()) {
               int slot = (Integer)var22.next();
               if (slot < gui.getInt("EditorGui.Rows") * 9) {
                  if (slot <= -1) {
                     for(int i = 0; i < gui.getInt("EditorGui.Rows") * 9; ++i) {
                        inv.setItem(i, nbt.getItem().clone());
                     }
                     break;
                  }

                  inv.setItem(slot, nbt.getItem().clone());
               }
            }
         }

         p.openInventory(inv);
         if (viewers.containsKey(p)) {
            viewers.remove(p);
         }

         viewers.put(p, name);
         return;
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent e) {
      final Player p = (Player)e.getWhoClicked();
      if (viewers.containsKey(p)) {
         e.setCancelled(true);
         final String room = (String)viewers.get(p);
         ItemStack click = e.getCurrentItem();
         if (click != null) {
            if (!click.getType().equals(Material.AIR)) {
               NBTItem nbt = new NBTItem(click);
               if (nbt.hasKey("EditorGui_ClickType")) {
                  String clicktype = nbt.getString("EditorGui_ClickType");
                  File file;
                  switch (clicktype.toLowerCase()) {
                     case "editplayer":
                        if (editor.containsKey(p)) {
                           editor.remove(p);
                        }

                        editor.put(p, room + ":editplayer");
                        p.closeInventory();
                        p.sendMessage(Messages.get("EditPlayer"));
                        return;
                     case "editprefix":
                        if (editor.containsKey(p)) {
                           editor.remove(p);
                        }

                        editor.put(p, room + ":editprefix");
                        p.closeInventory();
                        p.sendMessage(Messages.get("EditPrefix"));
                        return;
                     case "edittime":
                        if (editor.containsKey(p)) {
                           editor.remove(p);
                        }

                        editor.put(p, room + ":edittime");
                        p.closeInventory();
                        p.sendMessage(Messages.get("EditTime"));
                        return;
                     case "editreward":
                        p.closeInventory();
                        (new BukkitRunnable() {
                           public void run() {
                              RewardGui.open(p, room);
                           }
                        }).runTaskLater(PhoBan.inst(), 1L);
                        return;
                     case "editmob1":
                        p.closeInventory();
                        p.getInventory().addItem(new ItemStack[]{this.superultrablazerod(room, "Mob1")});
                        p.sendMessage(Messages.get("EditMob_Step1"));
                        return;
                     case "editmob2":
                        p.closeInventory();
                        p.getInventory().addItem(new ItemStack[]{this.superultrablazerod(room, "Mob2")});
                        p.sendMessage(Messages.get("EditMob_Step1"));
                        return;
                     case "editmob3":
                        p.closeInventory();
                        p.getInventory().addItem(new ItemStack[]{this.superultrablazerod(room, "Mob3")});
                        p.sendMessage(Messages.get("EditMob_Step1"));
                        return;
                     case "editboss":
                        p.closeInventory();
                        p.getInventory().addItem(new ItemStack[]{this.superultrablazerod(room, "Boss")});
                        p.sendMessage(Messages.get("EditBoss_Step1"));
                        return;
                     case "editspawn":
                        p.closeInventory();
                        Location loc = p.getLocation();
                        File file2 = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + room + ".yml");
                        FileConfiguration rooms = YamlConfiguration.loadConfiguration(file2);
                        rooms.set("Spawn", loc);
                        FileManager.saveFileConfig(rooms, (File)file2);

                        try {
                           Game game = Game.getGame(room);
                           game.setSpawn(loc);
                        } catch (Exception var15) {
                        }

                        open(p, room);
                        return;
                     case "edittype":
                        if (editor.containsKey(p)) {
                           editor.remove(p);
                        }

                        editor.put(p, room + ":edittype");
                        p.closeInventory();
                        p.sendMessage(Messages.get("EditType"));
                        return;
                     case "deleteroom":
                        file2 = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + room + ".yml");
                        file2.delete();
                        Game.deleteRoom(room);
                        p.closeInventory();
                        p.sendMessage(Messages.get("DeleteRoom"));
                        return;
                     case "confirm":
                        file2 = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + room + ".yml");
                        rooms = YamlConfiguration.loadConfiguration(file2);
                        if (!Game.canJoin(rooms)) {
                           p.sendMessage(Messages.get("RoomNotConfig"));
                           return;
                        }

                        Game.load(room, rooms, file2);
                        p.closeInventory();
                        p.sendMessage(Messages.get("ConfigDone"));
                        String type = rooms.getString("Type");
                        String result = containsPhobanType(type);
                        if (result.equals("deo-co-con-cac-gi-o-day-het")) {
                           FileConfiguration phoban = FileManager.getFileConfig(FileManager.Files.PHOBAN);
                           FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
                           phoban.set(type + "Room.Type", type);
                           phoban.set(type + "Room.ID", gui.getString("ChooseTypeGui.TypeFormat.ID"));
                           phoban.set(type + "Room.Name", gui.getString("ChooseTypeGui.TypeFormat.Name"));
                           phoban.set(type + "Room.Lore", gui.getStringList("ChooseTypeGui.TypeFormat.Lore"));
                           FileManager.saveFileConfig(phoban, FileManager.Files.PHOBAN);
                        }

                        return;
                     default:
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent e) {
      Player p = (Player)e.getPlayer();
      if (viewers.containsKey(p)) {
         viewers.remove(p);
      }

   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent e) {
      final Player p = e.getPlayer();
      if (editor.containsKey(p)) {
         e.setCancelled(true);
         String type = ((String)editor.get(p)).split(":")[1];
         final String name = ((String)editor.get(p)).split(":")[0];
         String mess = ChatColor.stripColor(e.getMessage());
         if (mess.equalsIgnoreCase("cancel") && !type.toLowerCase().contains("editmob") && !type.toLowerCase().contains("editboss")) {
            editor.remove(p);
            (new BukkitRunnable() {
               public void run() {
                  EditorGui.open(p, name);
               }
            }).runTaskLater(PhoBan.inst(), 0L);
            return;
         }

         Game game = Game.getGame(name);
         File file;
         YamlConfiguration room;
         Exception ex;
         int time;
         String id;
         switch (type.toLowerCase()) {
            case "editplayer":
               try {
                  time = (int)Long.parseLong(mess);
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Player", time);
                  FileManager.saveFileConfig(room, (File)file);

                  try {
                     game.setMaxPlayer(time);
                  } catch (Exception var15) {
                  }

                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var20) {
                  ex = var20;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editprefix":
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Prefix", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               (new BukkitRunnable() {
                  public void run() {
                     EditorGui.open(p, name);
                  }
               }).runTaskLater(PhoBan.inst(), 0L);
               break;
            case "edittime":
               try {
                  time = Integer.parseInt(mess);
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Time", time);
                  FileManager.saveFileConfig(room, (File)file);

                  try {
                     game.setMaxTime(time);
                  } catch (Exception var14) {
                  }

                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var21) {
                  ex = var21;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob1_control":
               id = ((String)editor.get(p)).split(":")[2];
               switch (mess.toLowerCase()) {
                  case "add":
                     editor.remove(p);
                     editor.put(p, name + ":editmob1_type:" + id);
                     p.sendMessage(Messages.get("EditMob_Step3"));
                     return;
                  case "remove":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob1", (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     p.sendMessage(Messages.get("RemoveMob"));
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  case "exit":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob1." + id, (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  default:
                     p.sendMessage(Messages.get("EditMob_Step2"));
                     return;
               }
            case "editmob1_type":
               id = ((String)editor.get(p)).split(":")[2];
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Mob1." + id + ".Type", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               editor.put(p, name + ":editmob1_amount:" + id);
               p.sendMessage(Messages.get("EditMob_Step4"));
               break;
            case "editmob1_amount":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob1." + id + ".Amount", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  editor.put(p, name + ":editmob1_time:" + id);
                  p.sendMessage(Messages.get("EditMob_Step5"));
               } catch (Exception var16) {
                  ex = var16;
                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob1_time":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob1." + id + ".Time", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var19) {
                  ex = var19;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob2_control":
               id = ((String)editor.get(p)).split(":")[2];
               switch (mess.toLowerCase()) {
                  case "add":
                     editor.remove(p);
                     editor.put(p, name + ":editmob2_type:" + id);
                     p.sendMessage(Messages.get("EditMob_Step3"));
                     return;
                  case "remove":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob2", (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     p.sendMessage("RemoveMob");
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  case "exit":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob2." + id, (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  default:
                     p.sendMessage(Messages.get("EditMob_Step2"));
                     return;
               }
            case "editmob2_type":
               id = ((String)editor.get(p)).split(":")[2];
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Mob2." + id + ".Type", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               editor.put(p, name + ":editmob2_amount:" + id);
               p.sendMessage(Messages.get("EditMob_Step4"));
               break;
            case "editmob2_amount":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob2." + id + ".Amount", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  editor.put(p, name + ":editmob2_time:" + id);
                  p.sendMessage(Messages.get("EditMob_Step5"));
               } catch (Exception var17) {
                  ex = var17;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob2_time":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob2." + id + ".Time", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var18) {
                  ex = var18;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob3_control":
               id = ((String)editor.get(p)).split(":")[2];
               switch (mess.toLowerCase()) {
                  case "add":
                     editor.remove(p);
                     editor.put(p, name + ":editmob3_type:" + id);
                     p.sendMessage(Messages.get("EditMob_Step3"));
                     return;
                  case "remove":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob3", (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     p.sendMessage("RemoveMob");
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  case "exit":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Mob3." + id, (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  default:
                     p.sendMessage(Messages.get("EditMob_Step2"));
                     return;
               }
            case "editmob3_type":
               id = ((String)editor.get(p)).split(":")[2];
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Mob3." + id + ".Type", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               editor.put(p, name + ":editmob3_amount:" + id);
               p.sendMessage(Messages.get("EditMob_Step4"));
               break;
            case "editmob3_amount":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob3." + id + ".Amount", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  editor.put(p, name + ":editmob3_time:" + id);
                  p.sendMessage(Messages.get("EditMob_Step5"));
               } catch (Exception var25) {
                  ex = var25;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editmob3_time":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Mob3." + id + ".Time", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var23) {
                  ex = var23;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editboss_control":
               id = ((String)editor.get(p)).split(":")[2];
               switch (mess.toLowerCase()) {
                  case "add":
                     editor.remove(p);
                     editor.put(p, name + ":editboss_type:" + id);
                     p.sendMessage(Messages.get("EditBoss_Step3"));
                     return;
                  case "remove":
                     file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                     room = YamlConfiguration.loadConfiguration(file);
                     room.set("Boss", (Object)null);
                     FileManager.saveFileConfig(room, (File)file);
                     p.sendMessage("RemoveMob");
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  case "exit":
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  default:
                     p.sendMessage(Messages.get("EditBoss_Step2"));
                     return;
               }
            case "editboss_type":
               id = ((String)editor.get(p)).split(":")[2];
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Boss.Type", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               editor.put(p, name + ":editboss_amount:" + id);
               p.sendMessage(Messages.get("EditBoss_Step4"));
               break;
            case "editboss_amount":
               try {
                  id = ((String)editor.get(p)).split(":")[2];
                  int amount = Integer.parseInt(mess);
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Boss.Amount", amount);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  editor.put(p, name + ":editboss_time:" + id);
                  p.sendMessage(Messages.get("EditMob_Step5"));
               } catch (Exception var22) {
                  ex = var22;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "editboss_time":
               try {
                  time = Integer.parseInt(mess);
                  id = ((String)editor.get(p)).split(":")[2];
                  file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
                  room = YamlConfiguration.loadConfiguration(file);
                  room.set("Boss.Time", time);
                  FileManager.saveFileConfig(room, (File)file);
                  editor.remove(p);
                  (new BukkitRunnable() {
                     public void run() {
                        EditorGui.open(p, name);
                     }
                  }).runTaskLater(PhoBan.inst(), 0L);
               } catch (Exception var24) {
                  ex = var24;
                  if (ex.getMessage() == null) {
                     (new BukkitRunnable() {
                        public void run() {
                           EditorGui.open(p, name);
                        }
                     }).runTaskLater(PhoBan.inst(), 0L);
                     return;
                  }

                  if (ex.getMessage().contains("For input string:")) {
                     p.sendMessage(Messages.get("NotInt"));
                  } else {
                     p.sendMessage(Messages.get("Error").replace("<error>", ex.getMessage()));
                     ex.printStackTrace();
                  }
               }
               break;
            case "edittype":
               file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + name + ".yml");
               room = YamlConfiguration.loadConfiguration(file);
               room.set("Type", mess);
               FileManager.saveFileConfig(room, (File)file);
               editor.remove(p);
               (new BukkitRunnable() {
                  public void run() {
                     EditorGui.open(p, name);
                  }
               }).runTaskLater(PhoBan.inst(), 0L);
         }
      }

   }

   private ItemStack superultrablazerod(String room, String mob) {
      ItemStack item = new ItemStack(Utils.matchMaterial("blaze_rod"), 1);
      NBTItem nbt = new NBTItem(item);
      nbt.setString("Room", room);
      nbt.setString("Mob", mob);
      return nbt.getItem().clone();
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent e) {
      Player p = e.getPlayer();
      if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
         ItemStack item = e.getItem();
         if (item != null) {
            if (!item.getType().equals(Material.AIR)) {
               NBTItem nbt = new NBTItem(item);
               if (nbt.hasKey("Room")) {
                  if (nbt.hasKey("Mob")) {
                     e.setCancelled(true);
                     String room = nbt.getString("Room");
                     String mob = nbt.getString("Mob");
                     Location loc = e.getClickedBlock().getLocation();
                     File file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + room + ".yml");
                     FileConfiguration rooms = YamlConfiguration.loadConfiguration(file);
                     String id = UUID.randomUUID().toString();
                     if (mob.equalsIgnoreCase("boss")) {
                        rooms.set("Boss.Location", loc);
                     } else {
                        rooms.set(mob + "." + id + ".Location", loc);
                     }

                     FileManager.saveFileConfig(rooms, (File)file);
                     if (editor.containsKey(p)) {
                        editor.remove(p);
                     }

                     editor.put(p, room + ":edit" + mob + "_control:" + id);
                     p.getInventory().setItemInMainHand((ItemStack)null);
                     if (mob.equalsIgnoreCase("boss")) {
                        p.sendMessage(Messages.get("EditBoss_Step2"));
                     } else {
                        p.sendMessage(Messages.get("EditMob_Step2"));
                     }

                  }
               }
            }
         }
      }
   }

   private static boolean isEdited(String room, String mob) {
      File file = new File(PhoBan.inst().getDataFolder(), "room" + File.separator + room + ".yml");
      FileConfiguration rooms = YamlConfiguration.loadConfiguration(file);
      if (rooms.contains(room + "." + mob)) {
         Iterator var4 = rooms.getConfigurationSection(room + "." + mob).getKeys(false).iterator();

         while(var4.hasNext()) {
            String s = (String)var4.next();
            if (rooms.contains(room + "." + mob + "." + s + ".Type") && rooms.contains(room + "." + mob + "." + s + ".Amount") && rooms.contains(room + "." + mob + "." + s + ".Location")) {
               return true;
            }
         }
      }

      return false;
   }

   private static String containsPhobanType(String type) {
      FileConfiguration phoban = FileManager.getFileConfig(FileManager.Files.PHOBAN);
      Iterator var2 = phoban.getKeys(false).iterator();

      String key;
      do {
         if (!var2.hasNext()) {
            return "deo-co-con-cac-gi-o-day-het";
         }

         key = (String)var2.next();
      } while(!phoban.contains(key + ".Type") || !phoban.getString(key + ".Type").equalsIgnoreCase(type));

      return phoban.getString(key + ".Type");
   }
}
