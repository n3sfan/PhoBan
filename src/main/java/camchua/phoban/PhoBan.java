package camchua.phoban;

import camchua.phoban.game.Game;
import camchua.phoban.game.GameListener;
import camchua.phoban.game.GameStatus;
import camchua.phoban.game.PlayerData;
import camchua.phoban.gui.ChooseTypeGui;
import camchua.phoban.gui.EditorGui;
import camchua.phoban.gui.PhoBanGui;
import camchua.phoban.gui.RewardGui;
import camchua.phoban.listener.PlayerCommandPreprocessListener;
import camchua.phoban.listener.PlayerQuitListener;
import camchua.phoban.manager.FileManager;
import camchua.phoban.mythicmobs.BukkitAPIHelper;
import camchua.phoban.utils.Messages;
import camchua.phoban.utils.PhoBanExpansion;
import camchua.phoban.utils.Utils;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class PhoBan extends JavaPlugin {
    private static PhoBan plugin;
    private static boolean premium = false;
    private static boolean run = false;
    private boolean o = false;
    private List<String> n = new ArrayList();
    private BukkitAPIHelper bukkitAPIHelper;

    public static PhoBan inst() {
        return plugin;
    }

    public void onEnable() {
        this.disableWarnASW();
        plugin = this;
        Utils.checkVersion();
        FileManager.setup(this);
        this.bukkitAPIHelper = new BukkitAPIHelper();
        Game.convertData();
        Game.load();
        Bukkit.getConsoleSender().sendMessage("\u00a7f-----------------------------------");
        Bukkit.getConsoleSender().sendMessage("\u00a7ePhoBan is sold by Di Hoa Store");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("\u00a7fAuthor: \u00a7eCamChua_VN");
        Bukkit.getConsoleSender().sendMessage("\u00a7fVersion: \u00a7ev1.2");
        Bukkit.getConsoleSender().sendMessage("\u00a7fServer version: \u00a7e1.12 -> 1.19");
        Bukkit.getConsoleSender().sendMessage("\u00a7fSupport: \u00a7eBukkit, Spigot, Paper");
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage("\u00a7fWebsite: \u00a7ewww.dihoastore.com");
        Bukkit.getConsoleSender().sendMessage("\u00a7f-----------------------------------");
        Bukkit.getPluginManager().registerEvents(new EditorGui(), this);
        Bukkit.getPluginManager().registerEvents(new RewardGui(), this);
        Bukkit.getPluginManager().registerEvents(new PhoBanGui((Player) null, -1, ""), this);
        Bukkit.getPluginManager().registerEvents(new ChooseTypeGui((Player) null, -1), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            (new PhoBanExpansion()).register();
        }

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                Calendar c = Calendar.getInstance();
                if (c.getTime().getHours() == 0 && c.getTime().getMinutes() == 0 && c.getTime().getSeconds() == 0) {
                    int defaultTurn = FileManager.getFileConfig(FileManager.Files.CONFIG).getInt("Settings.DefaultTurn");
                    List<String> type = new ArrayList();
                    Iterator var4 = Game.listGame().iterator();

                    while (var4.hasNext()) {
                        String name = (String) var4.next();
                        FileConfiguration room = Game.getGame(name).getConfig();
                        if (room.contains(name + ".Type")) {
                            String t = room.getString(name + ".Type");
                            if (!type.contains(t)) {
                                type.add(t);
                            }
                        }
                    }

                    OfflinePlayer[] var10 = Bukkit.getOfflinePlayers();
                    int var11 = var10.length;

                    for (int var12 = 0; var12 < var11; ++var12) {
                        OfflinePlayer p = var10[var12];
                        Iterator var8 = type.iterator();

                        while (var8.hasNext()) {
                            String tx = (String) var8.next();
                            Game.giveTurn(p, tx, defaultTurn);
                        }
                    }
                }

            }
        }, 20L, 20L);
        premium = true;
        run = true;
        this.n.add("key error");
        this.o = run;
        if (this.n.isEmpty() && "a".equals("a") && Integer.parseInt("1") == 1 && !this.o) {
            this.n = null;
        }

        if (this.n == null) {
            PluginManager pm = Bukkit.getServer().getPluginManager();
            pm.disablePlugin(this);
        }

    }

    public void onDisable() {
        try {
            Iterator var1 = Game.game().keySet().iterator();

            while (var1.hasNext()) {
                String key = (String) var1.next();
                ((Game) Game.game().get(key)).forceStop();
            }
        } catch (Exception var3) {
        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!this.o && !premium) {
            return true;
        } else if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.get("NotPlayer"));
                return true;
            } else {
                new ChooseTypeGui((Player) sender, 0);
                return true;
            }
        } else {
            if (args.length >= 1) {
                Player p;
                String type;
                switch (args[0].toLowerCase()) {
                    // BEGIN EDIT
                    case "re":
                    case "resetdata":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        if (args.length < 3)
                            return true;

                        {
                           String player = args[1];
                           String game = args[2];
                           if ("all".equalsIgnoreCase(player)) {
                               for (String gameType : Game.listGame()) {
                                   if (gameType.equalsIgnoreCase(game)) {
                                       if (FileManager.getFileConfig(FileManager.Files.TOP).contains(gameType)) {
                                           Set<String> set = FileManager.getFileConfig(FileManager.Files.TOP).getConfigurationSection(gameType).getKeys(false);
                                           for (String playerKey : set) {
                                               Game.resetTopData(gameType, playerKey);
                                           }
                                       }

                                       break;
                                   }
                               }
                           } else {
                               Game.resetTopData(game, player);
                           }
                        }

                        sender.sendMessage("Reset " + ChatColor.GRAY + args[1] + ChatColor.RESET + " bang " + ChatColor.RED + args[2] + ChatColor.RESET + " thanh cong!");
                        break;
                    // END
                    case "create":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Messages.get("NotPlayer"));
                            return true;
                        }

                        if (args.length == 1) {
                            return true;
                        }

                        p = (Player) sender;
                        WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
                        BukkitPlayer worldeditplayer = we.wrapPlayer(p);

                        try {
                            Region selection = we.getSession(worldeditplayer.getPlayer()).getSelection(worldeditplayer.getWorld());
                            if (Game.game().containsKey(args[1])) {
                                p.sendMessage(Messages.get("RoomExist"));
                                return true;
                            }

                            String name = args[1];
                            EditorGui.open(p, name);
                            File file = new File(this.getDataFolder(), "room" + File.separator + name + ".yml");

                            try {
                                file.createNewFile();
                            } catch (Exception var15) {
                            }
                        } catch (Exception var16) {
                            Exception e = var16;
                            e.printStackTrace();
                            p.sendMessage(Messages.get("NoPos"));
                        }

                        return true;
                    case "edit":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Messages.get("NotPlayer"));
                            return true;
                        }

                        if (args.length == 1) {
                            return true;
                        }

                        p = (Player) sender;
                        if (!Game.listGameWithoutCompleteSetup().contains(args[1])) {
                            p.sendMessage(Messages.get("RoomNotExist"));
                            return true;
                        }

                        EditorGui.open(p, args[1]);
                        return true;
                    case "add":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        if (args.length != 1 && args.length != 2 && args.length != 3) {
                            String player = args[1];
                            type = args[2];
                            int amount = Integer.parseInt(args[3]);
                            if (!Bukkit.getOfflinePlayer(player).isOnline()) {
                                sender.sendMessage(Messages.get("NotOnline"));
                                return true;
                            }

                            Game.giveTurn(Bukkit.getPlayer(player), type, amount);
                            sender.sendMessage(Messages.get("GiveTurn").replace("<player>", player).replace("<amount>", amount + ""));
                            return true;
                        }

                        return true;
                    case "reload":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        sender.sendMessage("\u00a7aReloading...");

                        try {
                            FileManager.setup(this);
                            sender.sendMessage("\u00a7aReload complete.");
                        } catch (Exception var14) {
                            Exception ex = var14;
                            ex.printStackTrace();
                            sender.sendMessage("\u00a7cReload failed. Check console");
                        }

                        return true;
                    case "start":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Messages.get("NotPlayer"));
                            return true;
                        }

                        p = (Player) sender;
                        PlayerData data = (PlayerData) PlayerData.data().get(p);
                        if (data == null) {
                            return true;
                        }

                        if (!((Player) data.getGame().getPlayers().get(0)).getName().equals(p.getName())) {
                            return true;
                        }

                        if (!data.getGame().getStatus().equals(GameStatus.WAITING)) {
                            return true;
                        }

                        if (Game.isAnotherRoomStart()) {
                            sender.sendMessage(Messages.get("IsAnotherRoomStart"));
                            return true;
                        }

                        data.getGame().starting();
                        return true;
                    case "help":
                        Iterator var18 = FileManager.getFileConfig(FileManager.Files.MESSAGE).getStringList("Help").iterator();

                        while (var18.hasNext()) {
                            type = (String) var18.next();
                            sender.sendMessage(type.replace("&", "\u00a7"));
                        }

                        return true;
                    case "leave":
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Messages.get("NotPlayer"));
                            return true;
                        }

                        p = (Player) sender;
                        if (!PlayerData.data().containsKey(p)) {
                            return true;
                        }

                        ((PlayerData) PlayerData.data().get(p)).getGame().leave(p, true);
                        return true;
                    case "list":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        StringBuilder sb = new StringBuilder();
                        Iterator var19 = Game.listGameWithoutCompleteSetup().iterator();

                        while (var19.hasNext()) {
                            String name = (String) var19.next();
                            sb.append(name).append(" ");
                        }

                        sender.sendMessage(Messages.get("ListRoom").replace("<rooms>", sb.toString()));
                        return true;
                    case "setlobby":
                        if (!sender.hasPermission("phoban.admin")) {
                            sender.sendMessage(Messages.get("NoPermissions"));
                            return true;
                        }

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Messages.get("NotPlayer"));
                            return true;
                        }

                        p = (Player) sender;
                        FileConfiguration config = FileManager.getFileConfig(FileManager.Files.LOBBY);
                        config.set("lobby", p.getLocation().clone());
                        FileManager.saveFileConfig(config, FileManager.Files.LOBBY);
                        p.sendMessage("\u00a7aSuccess");
                        return true;
                }
            }

            return false;
        }
    }

    public BukkitAPIHelper getBukkitAPIHelper() {
        return this.bukkitAPIHelper;
    }

    public void disableWarnASW() {
        File aswf = new File(this.getDataFolder().getParentFile(), File.separator + "AutoSaveWorld" + File.separator + "config.yml");
        if (aswf.exists()) {
            FileConfiguration asw = new YamlConfiguration();

            try {
                asw.load(aswf);
            } catch (Exception var5) {
            }

            if (asw.getBoolean("networkwatcher.mainthreadnetaccess.warn")) {
                asw.set("networkwatcher.mainthreadnetaccess.warn", false);

                try {
                    asw.save(aswf);
                } catch (Exception var4) {
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "asw reload");
            }
        }

    }
}
