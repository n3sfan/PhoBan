package camchua.phoban.game;

import camchua.phoban.PhoBan;
import camchua.phoban.manager.FileManager;
import camchua.phoban.utils.Messages;
import camchua.phoban.utils.Random;
import camchua.phoban.utils.Utils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.BukkitAPIHelper;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Game {
    private static LinkedHashMap<String, Game> game = new LinkedHashMap();
    private String name;
    private GameStatus status;
    private GameTask task;
    private int maxtime;
    private Location spawn;
    private int max_players;
    private int time;
    private int stageTime;
    private List<Player> players;
    private HashMap<String, HashMap<String, Integer>> stage1;
    private HashMap<String, HashMap<String, Integer>> stage2;
    private HashMap<String, HashMap<String, Integer>> stage3;
    private HashMap<String, HashMap<String, Integer>> timeStage1;
    private HashMap<String, HashMap<String, Integer>> timeStage2;
    private HashMap<String, HashMap<String, Integer>> timeStage3;
    private HashMap<String, Integer> boss;
    private HashMap<String, Integer> timeBoss;
    private List<HashMap<String, HashMap<String, Integer>>> stage = new ArrayList();
    private List<HashMap<String, HashMap<String, Integer>>> timeStage = new ArrayList();
    private int current_stage;
    private int stage_count;
    public HashMap<String, Integer> current_progress;
    // BEGIN EDIT
    private Map<String, Integer> kills;
    public int maxTurnsPerDay;
    // END EDIT
    private FileConfiguration room;
    private File configFile;
    private String type;
    public boolean stage_countdown = false;
    public boolean quit_countdown = false;

    public static LinkedHashMap<String, Game> game() {
        return game;
    }

    public static Game getGame(String name) {
        return !game.containsKey(name) ? null : (Game) game.get(name);
    }

    public static List<String> listGame() {
        return new ArrayList(game.keySet());
    }

    public static List<String> listGameWithoutCompleteSetup() {
        File folder = new File(PhoBan.inst().getDataFolder(), "room" + File.separator);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<String> listGame = new ArrayList();
        File[] var2 = folder.listFiles();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            File file = var2[var4];
            if (file.getName().contains(".yml")) {
                String name = file.getName().replace(".yml", "");
                listGame.add(name);
            }
        }

        return listGame;
    }

    public static void convertData() {
        File oldFile = new File(PhoBan.inst().getDataFolder(), "room.yml");
        if (oldFile.exists()) {
            FileConfiguration room = YamlConfiguration.loadConfiguration(oldFile);
            Iterator var2 = room.getKeys(false).iterator();

            while (var2.hasNext()) {
                String key = (String) var2.next();
                File newFile = new File(PhoBan.inst().getDataFolder(), File.separator + "room" + File.separator + key + ".yml");
                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                    } catch (Exception var7) {
                    }
                }

                FileConfiguration config = new YamlConfiguration();
                Utils.scanSection(room, config, key, key);

                try {
                    config.save(newFile);
                } catch (Exception var8) {
                }
            }

            oldFile.delete();
        }
    }

    public static void load() {
        File folder = new File(PhoBan.inst().getDataFolder(), "room" + File.separator);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] var1 = folder.listFiles();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            File file = var1[var3];
            if (file.getName().contains(".yml")) {
                String name = file.getName().replace(".yml", "");
                FileConfiguration room = YamlConfiguration.loadConfiguration(file);
                if (canJoin(room)) {
                    load(name, room, file);
                }
            }
        }

    }

    public static void load(String name, FileConfiguration room, File configFile) {
        if (game.containsKey(name)) {
            game.remove(name);
        }

        int time = room.getInt("Time");
        Location spawn = (Location) room.get("Spawn");
        int max_players = room.getInt("Player");
        HashMap<String, HashMap<String, Integer>> stage1 = new HashMap();
        HashMap<String, HashMap<String, Integer>> timeStage1 = new HashMap();
        Iterator var8 = room.getConfigurationSection("Mob1").getKeys(false).iterator();

        while (var8.hasNext()) {
            String key = (String) var8.next();
            String type = room.getString("Mob1." + key + ".Type");
            int amount = room.getInt("Mob1." + key + ".Amount");
            int timeStage = room.getInt("Mob1." + key + ".Time");
            if (timeStage == 0) {
                timeStage = -1;
            }

            HashMap<String, Integer> a = new HashMap();
            a.put(type, amount);
            HashMap<String, Integer> b = new HashMap();
            b.put(type, timeStage);
            stage1.put(key, a);
            timeStage1.put(key, b);
        }

        HashMap<String, HashMap<String, Integer>> stage2 = new HashMap();
        HashMap<String, HashMap<String, Integer>> timeStage2 = new HashMap();
        Iterator var21 = room.getConfigurationSection("Mob2").getKeys(false).iterator();

        HashMap boss;
        HashMap timeBoss;
        String boss_type;
        int boss_amount;
        int timeStage;
        while (var21.hasNext()) {
            String key = (String) var21.next();
            boss_type = room.getString("Mob2." + key + ".Type");
            boss_amount = room.getInt("Mob2." + key + ".Amount");
            timeStage = room.getInt("Mob2." + key + ".Time");
            if (timeStage == 0) {
                timeStage = -1;
            }

            boss = new HashMap();
            boss.put(boss_type, boss_amount);
            timeBoss = new HashMap();
            timeBoss.put(boss_type, timeStage);
            stage2.put(key, boss);
            timeStage2.put(key, timeBoss);
        }

        HashMap<String, HashMap<String, Integer>> stage3 = new HashMap();
        HashMap<String, HashMap<String, Integer>> timeStage3 = new HashMap();
        Iterator var26 = room.getConfigurationSection("Mob3").getKeys(false).iterator();

        while (var26.hasNext()) {
            String key = (String) var26.next();
            String type = room.getString("Mob3." + key + ".Type");
            int amount = room.getInt("Mob3." + key + ".Amount");
            int timeStage6 = room.getInt("Mob3." + key + ".Time");
            if (timeStage6 == 0) {
                timeStage6 = -1;
            }

            HashMap<String, Integer> a = new HashMap();
            a.put(type, amount);
            HashMap<String, Integer> b = new HashMap();
            b.put(type, timeStage6);
            stage3.put(key, a);
            timeStage3.put(key, b);
        }

        boss_type = room.getString("Boss.Type");
        boss_amount = room.getInt("Boss.Amount");
        timeStage = room.getInt("Boss.Time");
        boss = new HashMap();
        boss.put(boss_type, boss_amount);
        timeBoss = new HashMap();
        timeBoss.put(boss_type, timeStage);
        String type = room.getString("Type");
        // BEGIN EDIT
        int maxTurnsPerDay = room.getInt("MaxTurnPerDay");
        Game g = new Game(name, time, spawn, max_players, stage1, stage2, stage3, boss, timeStage1, timeStage2, timeStage3, timeBoss, room, type, configFile, maxTurnsPerDay);
        // END
        game.put(name, g);
    }

    public static void deleteRoom(String room) {
        if (game.containsKey(room)) {
            game.remove(room);
        }

    }

    public static boolean canJoin(FileConfiguration room) {
        return room.contains("Prefix") && room.contains("Player") && room.contains("Time") && room.contains("Boss") && room.contains("Reward") && room.contains("Mob1") && room.contains("Mob2") && room.contains("Mob3") && room.contains("RewardAmount") && room.contains("Spawn") && room.contains("Type");
    }

    public static int getTurn(OfflinePlayer p, String type) {
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
        return !data.contains(p.getName() + ".Turn." + type) ? 1 : data.getInt(p.getName() + ".Turn." + type);
    }

    public static void giveTurn(OfflinePlayer p, String type, int amount) {
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
        int turn = getTurn(p, type) + amount;
        data.set(p.getName() + ".Turn." + type, turn);
        FileManager.saveFileConfig(data, FileManager.Files.DATA);
    }

    public static void takeTurn(OfflinePlayer p, String type, int amount) {
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
        int turn = getTurn(p, type) - amount;
        data.set(p.getName() + ".Turn." + type, turn);
        FileManager.saveFileConfig(data, FileManager.Files.DATA);
    }

    public static void complete(OfflinePlayer p, String name) {
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
        List<String> complete = data.contains(p.getName() + ".Complete") ? data.getStringList(p.getName() + ".Complete") : new ArrayList();
        if (!((List) complete).contains(name)) {
            ((List) complete).add(name);
            data.set(p.getName() + ".Complete", complete);
            FileManager.saveFileConfig(data, FileManager.Files.DATA);
        }

    }

    public static boolean isComplete(OfflinePlayer p, String name) {
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.DATA);
        List<String> complete = data.contains(p.getName() + ".Complete") ? data.getStringList(p.getName() + ".Complete") : new ArrayList();
        return ((List) complete).contains(name);
    }

    public static boolean hasTurn(OfflinePlayer p, String type) {
        return getTurn(p, type) > 0;
    }

    // BEGIN EDIT

    /**
     * Check if "MaxPlayersInRooms" players is playing.
     */
    public static boolean isAnotherRoomStart() {
        Game g;
        int count = 0;
        int max = FileManager.getFileConfig(FileManager.Files.CONFIG).getInt("Settings.MaxPlayersInRooms");

        for (String key : game.keySet()) {
            g = game.get(key);
            if (g.getStatus() == GameStatus.PLAYING || g.getStatus() == GameStatus.STARTING) {
                ++count;
            }

            if (count == max) {
                return true;
            }
        }

        return false;
    }
    // END EDIT

    public Game(String name, int time, Location spawn, int max_players, HashMap<String, HashMap<String, Integer>> stage1, HashMap<String, HashMap<String, Integer>> stage2, HashMap<String, HashMap<String, Integer>> stage3, HashMap<String, Integer> boss, HashMap<String, HashMap<String, Integer>> timeStage1, HashMap<String, HashMap<String, Integer>> timeStage2, HashMap<String, HashMap<String, Integer>> timeStage3, HashMap<String, Integer> timeBoss, FileConfiguration room, String type, File configFile, int maxTurnsPerDay) {
        this.name = name;
        this.status = GameStatus.WAITING;
        this.maxtime = time;
        this.spawn = spawn;
        this.max_players = max_players;
        this.time = time;
        this.stageTime = 0;
        this.players = new ArrayList();
        this.current_stage = 0;
        this.stage1 = stage1;
        this.stage2 = stage2;
        this.stage3 = stage3;
        this.timeStage1 = timeStage1;
        this.timeStage2 = timeStage2;
        this.timeStage3 = timeStage3;
        this.boss = boss;
        this.timeBoss = timeBoss;
        this.stage = new ArrayList();
        this.current_stage = 1;
        this.stage_count = -1;
        this.current_progress = new HashMap();
        // BEGIN EDIT
        this.kills = new HashMap<>();
        this.maxTurnsPerDay = maxTurnsPerDay;
        // END
        this.room = room;
        this.configFile = configFile;
        this.type = type;
        this.initStage();
        this.task = new GameTask(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PhoBan.inst(), this.task, 20L, 20L);
    }

    public String getName() {
        return this.name;
    }

    public GameStatus getStatus() {
        return this.status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public int getTimeLeft() {
        return this.time;
    }

    public void time() {
        --this.time;
        if (this.stageTime > 0) {
            --this.stageTime;
        }

    }

    public void resetTime() {
        this.time = this.maxtime;
    }

    public int getStageTime() {
        return this.stageTime;
    }

    public void clearCurrentStage() {
        this.clearMobs();
    }

    public void initStage() {
        Iterator var1 = this.stage1.keySet().iterator();

        String key;
        HashMap h;
        while (var1.hasNext()) {
            key = (String) var1.next();
            h = new HashMap();
            h.put(key, (HashMap) this.stage1.get(key));
            this.stage.add(h);
        }

        var1 = this.stage2.keySet().iterator();

        while (var1.hasNext()) {
            key = (String) var1.next();
            h = new HashMap();
            h.put(key, (HashMap) this.stage2.get(key));
            this.stage.add(h);
        }

        var1 = this.stage3.keySet().iterator();

        while (var1.hasNext()) {
            key = (String) var1.next();
            h = new HashMap();
            h.put(key, (HashMap) this.stage3.get(key));
            this.stage.add(h);
        }

        HashMap<String, HashMap<String, Integer>> h2 = new HashMap();
        h2.put("Boss", this.boss);
        this.stage.add(h2);
        var1 = this.timeStage1.keySet().iterator();

        while (var1.hasNext()) {
            key = (String) var1.next();
            h2 = new HashMap();
            h2.put(key, (HashMap) this.timeStage1.get(key));
            this.timeStage.add(h2);
        }

        var1 = this.timeStage2.keySet().iterator();

        while (var1.hasNext()) {
            key = (String) var1.next();
            h2 = new HashMap();
            h2.put(key, (HashMap) this.timeStage2.get(key));
            this.timeStage.add(h2);
        }

        var1 = this.timeStage3.keySet().iterator();

        while (var1.hasNext()) {
            key = (String) var1.next();
            h2 = new HashMap();
            h2.put(key, (HashMap) this.timeStage3.get(key));
            this.timeStage.add(h2);
        }

        h2 = new HashMap();
        h2.put("Boss", this.timeBoss);
        this.timeStage.add(h2);
    }

    public void addProgress(String key, int value) {
        if (!this.current_progress.containsKey(key)) {
            this.current_progress.put(key, value);
        } else {
            this.current_progress.replace(key, (Integer) this.current_progress.get(key) + value);
        }

    }

    // BEGIN EDIT
    public Map<String, Integer> getKills() {
        return kills;
    }

    public int getKill(String player) {
        return this.kills.getOrDefault(player, 0);
    }

    public void addKill(String key, int val) {
        if (!this.kills.containsKey(key))
            this.kills.put(key, val);
        else
            this.kills.replace(key, this.kills.get(key) + val);
    }
    // END

    public HashMap<String, Integer> getStage(int s) {
        Iterator var2 = ((HashMap) this.stage.get(s)).keySet().iterator();
        if (var2.hasNext()) {
            String key = (String) var2.next();
            return (HashMap) ((HashMap) this.stage.get(s)).get(key);
        } else {
            return new HashMap();
        }
    }

    public HashMap<String, Integer> getTimeStage(int s) {
        Iterator var2 = ((HashMap) this.timeStage.get(s)).keySet().iterator();
        if (var2.hasNext()) {
            String key = (String) var2.next();
            return (HashMap) ((HashMap) this.timeStage.get(s)).get(key);
        } else {
            return new HashMap();
        }
    }

    public String getStageKey(int s) {
        Iterator var2 = ((HashMap) this.stage.get(s)).keySet().iterator();
        if (var2.hasNext()) {
            String key = (String) var2.next();
            return key;
        } else {
            return "";
        }
    }

    public int keyToStage(String key) {
        Iterator var2 = this.stage1.keySet().iterator();

        String k;
        do {
            if (!var2.hasNext()) {
                var2 = this.stage2.keySet().iterator();

                do {
                    if (!var2.hasNext()) {
                        var2 = this.stage3.keySet().iterator();

                        do {
                            if (!var2.hasNext()) {
                                return 4;
                            }

                            k = (String) var2.next();
                        } while (!k.equals(key));

                        return 3;
                    }

                    k = (String) var2.next();
                } while (!k.equals(key));

                return 2;
            }

            k = (String) var2.next();
        } while (!k.equals(key));

        return 1;
    }

    public int keyToTurn(String key) {
        int stage = this.keyToStage(key);
        int turn = 0;
        Iterator var4;
        String k;
        if (stage == 1) {
            for (var4 = this.stage1.keySet().iterator(); var4.hasNext(); ++turn) {
                k = (String) var4.next();
                if (k.equals(key)) {
                    return turn + 1;
                }
            }
        }

        turn = 0;
        if (stage == 2) {
            for (var4 = this.stage2.keySet().iterator(); var4.hasNext(); ++turn) {
                k = (String) var4.next();
                if (k.equals(key)) {
                    return turn + 1;
                }
            }
        }

        turn = 0;
        if (stage == 3) {
            for (var4 = this.stage3.keySet().iterator(); var4.hasNext(); ++turn) {
                k = (String) var4.next();
                if (k.equals(key)) {
                    return turn + 1;
                }
            }
        }

        return turn;
    }

    public void checkStage() {
        if (Utils.checkStage(this.getStage(this.stage_count), this.current_progress)) {
            this.newStage();
        }

    }

    public int getProgressLeft() {
        Iterator var1 = this.current_progress.keySet().iterator();
        if (var1.hasNext()) {
            String key = (String) var1.next();
            return !this.current_progress.containsKey(key) ? 0 : this.getProgressMax() - (Integer) this.current_progress.get(key);
        } else {
            return 0;
        }
    }

    public int getProgressCurrent() {
        Iterator var1 = this.current_progress.keySet().iterator();
        if (var1.hasNext()) {
            String key = (String) var1.next();
            return !this.current_progress.containsKey(key) ? 0 : (Integer) this.current_progress.get(key);
        } else {
            return 0;
        }
    }

    public int getProgressMax() {
        Iterator var1 = this.getStage(this.stage_count).keySet().iterator();
        if (var1.hasNext()) {
            String key = (String) var1.next();
            return !this.getStage(this.stage_count).containsKey(key) ? 0 : (Integer) this.getStage(this.stage_count).get(key);
        } else {
            return 0;
        }
    }

    public boolean newStage() {
        if (this.stage_count + 1 >= this.stage.size()) {
            this.complete();
            return false;
        } else if (this.keyToStage(this.getStageKey(this.stage_count + 1)) > this.current_stage) {
            this.stage_countdown = true;
            return true;
        } else {
            this.stageTime = 0;
            this.stage_countdown = false;
            ++this.stage_count;
            this.current_progress = new HashMap();
            int radius = FileManager.getFileConfig(FileManager.Files.CONFIG).getInt("Settings.SpawnRadius");
            Iterator var2 = this.getStage(this.stage_count).keySet().iterator();

            while (var2.hasNext()) {
                String key = (String) var2.next();
                int amount = (Integer) this.getStage(this.stage_count).get(key);
                int timeStage = (Integer) this.getTimeStage(this.stage_count).get(key);
                if (timeStage > 0) {
                    this.stageTime = timeStage;
                } else {
                    this.stageTime = -1;
                }

                String mob = this.current_stage == 1 ? "Mob1" : (this.current_stage == 2 ? "Mob2" : (this.current_stage == 3 ? "Mob3" : "Boss"));
                String keyy = mob.equals("Boss") ? "" : "." + this.getStageKey(this.stage_count);
                Location loc = (Location) this.room.get(mob + keyy + ".Location");
                loc.add(0.0, 1.0, 0.0);
                if (FileManager.getFileConfig(FileManager.Files.CONFIG).getBoolean("Settings.TeleportNewStage")) {
                    Iterator var9 = this.players.iterator();

                    while (var9.hasNext()) {
                        Player player = (Player) var9.next();
                        player.teleport(loc);
                    }
                }

                loc.subtract(0.0, 1.0, 0.0);
                // BEGIN EDIT
//                final BukkitAPIHelper mm = PhoBan.inst().getBukkitAPIHelper();
                BukkitAPIHelper mm = MythicMobs.inst().getAPIHelper();
                // END
                Iterator var19 = this.players.iterator();

                while (var19.hasNext()) {
                    Player player = (Player) var19.next();
                    String displayName = mm.getMythicMob(key).getDisplayName().get();
                    String title = Messages.get("StageInfo." + (mob.equals("Boss") ? "Boss" : "Mob") + ".Title").replace("&", "\u00a7").replace("<stage>", this.current_stage + "").replace("<turn>", this.keyToTurn(this.getStageKey(this.stage_count)) + "").replace("<mob>", key).replace("<amount>", amount + "").replace("<name>", displayName);
                    String subtitle = Messages.get("StageInfo." + (mob.equals("Boss") ? "Boss" : "Mob") + ".Subtitle").replace("&", "\u00a7").replace("<stage>", this.current_stage + "").replace("<turn>", this.keyToTurn(this.getStageKey(this.stage_count)) + "").replace("<mob>", key).replace("<amount>", amount + "").replace("<name>", displayName);
                    player.sendTitle(title, subtitle);
                }

                try {
                    for (int i = 1; i <= amount; ++i) {
                        double origin = (double) (-radius);
                        double bound = (double) radius + 0.1;

                        Location spawn;
                        for (spawn = loc.clone().add(ThreadLocalRandom.current().nextDouble(origin, bound), 1.0, ThreadLocalRandom.current().nextDouble(origin, bound)); Utils.isSuckBlock(spawn); spawn = loc.clone().add(ThreadLocalRandom.current().nextDouble(origin, bound), 1.0, ThreadLocalRandom.current().nextDouble(origin, bound))) {
                        }

                        if (!spawn.getChunk().isLoaded()) {
                            spawn.getChunk().load();
                        }

                        Entity entity = mm.spawnMythicMob(key, spawn);
                        // BEGIN EDIT
//                        final String internalName = mm.getMythicMobInternalName(entity);
//                        final String name = mm.getMythicMobDisplayNameGet((Entity) entity);
                        MythicMob type = mm.getMythicMobInstance(entity).getType();
                        final String internalName = type.getInternalName();
                        final String name = type.getDisplayName().get();
//                        try {
//                            final String internalName = mm.getMythicMobInternalName(entity);
//                            final String name = mm.getMythicMobDisplayNameGet((Entity) entity);
//                            System.out.println(internalName + " " + name);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println(name + " added");
                        EntityData.data().put(entity.getUniqueId(), new EntityData(entity, this, name, internalName));
                        // END
                    }
                } catch (Exception var17) {
                    var17.printStackTrace();
                }
            }

            return true;
        }
    }

    public void nextStage() {
        ++this.current_stage;
    }

    public void start() {
        this.status = GameStatus.PLAYING;
        this.current_stage = 1;
        this.stage_count = -1;
        this.stage_countdown = false;
        this.quit_countdown = false;
        Iterator var1 = this.players.iterator();

        while (var1.hasNext()) {
            Player p = (Player) var1.next();
            p.teleport(this.spawn);
        }

        this.newStage();
    }

    public void forceStop() {
        this.leaveAllAfterComplete();
    }

    public void join(Player p) {
        FileConfiguration config = FileManager.getFileConfig(FileManager.Files.CONFIG);
        // BEGIN EDIT
        FileConfiguration topConfig = FileManager.getFileConfig(FileManager.Files.TOP);
        if (topConfig.getInt("Plays." + type + '.' + p.getName()) >= config.getInt("Settings.MaxTurn")) {
            String msg = Messages.get("MaxTurnReached").replace("<max_turn>", "" + config.getInt("Settings.MaxTurn"));
            p.sendMessage(msg);
            return;
        }
        FileConfiguration turnConfig = FileManager.getFileConfig(FileManager.Files.TURNS);
        if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000 - turnConfig.getLong("Start." + type) >= TimeUnit.DAYS.toMillis(1L)) {
            turnConfig.set("Start." + type, LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000L);
            turnConfig.set("Plays." + type, null);
            FileManager.saveFileConfig(turnConfig, FileManager.Files.TURNS);
        }
        if (turnConfig.getInt("Plays." + type + '.' + p.getName()) >= this.maxTurnsPerDay) {
            String msg = Messages.get("MaxTurnPerDayReached").replace("<max_turn>", "" + this.maxTurnsPerDay);
            p.sendMessage(msg);
            return;
        }

        String roomRequire = config.getString("Settings.RoomRequire." + this.type, "");
        if (!roomRequire.isEmpty() && !isComplete(p, roomRequire)) {
            String msg = Messages.get("RoomRequire").replace("<room>", roomRequire);
            p.sendMessage(msg);
        } else {
            int levelRequire = config.getInt("Settings.LevelRequire." + this.type, 0);
            // END
            if (levelRequire != 0 && levelRequire > p.getLevel()) {
                String msg = Messages.get("LevelRequire").replace("<level>", String.valueOf(levelRequire));
                p.sendMessage(msg);
            } else {
                if (PlayerData.data().containsKey(p)) {
                    PlayerData.data().remove(p);
                }

                FileConfiguration conf = FileManager.getFileConfig(FileManager.Files.LOBBY);
                Location loc = conf.contains("lobby") ? ((Location) conf.get("lobby")).clone() : p.getLocation().clone();
                p.teleport(conf.contains("lobby") ? loc : this.spawn);
                p.setGameMode(GameMode.SURVIVAL);
                PlayerData.data().put(p, new PlayerData(p, this, loc));
                this.players.add(p);
                p.sendMessage(Messages.get("LeaveOnJoin"));
                String msg = Messages.get("PlayerJoin").replace("<player>", p.getName()).replace("<joined>", String.valueOf(this.players.size())).replace("<max>", String.valueOf(this.max_players));
                this.players.forEach((player) -> {
                    player.sendMessage(msg);
                });
            }
        }
    }

    // BEGIN EDIT
    private static void sortSectionValues(ConfigurationSection section, Comparator<Map.Entry<String, Object>> compare) {
        Map<String, Object> values = section.getValues(false);
        section.getParent().set(section.getName(), null);
        final ConfigurationSection newSection = section.getParent().createSection(section.getName());

        values.entrySet().stream().sorted(compare).forEach(e -> {
            newSection.set(e.getKey(), e.getValue());
        });
    }
    // END

    public void leave(final Player p, boolean message) {
        if (PlayerData.data().containsKey(p)) {
            this.players.remove(p);
            final Location loc = ((PlayerData) PlayerData.data().get(p)).getLocation();
            PlayerData.data().remove(p);
            (new BukkitRunnable() {
                public void run() {
                    p.teleport(loc);
                }
            }).runTaskLater(PhoBan.inst(), 1L);
            p.setGameMode(GameMode.SURVIVAL);
            String type = this.room.getString("Type");
            takeTurn(p, type, 1);
            if (message) {
                String msg = Messages.get("PlayerQuit").replace("<player>", p.getName()).replace("<joined>", String.valueOf(this.players.size())).replace("<max>", String.valueOf(this.max_players));
                this.players.forEach((player) -> {
                    player.sendMessage(msg);
                });
            }

        }
    }

    private void clearMobs() {
        List<EntityData> edata = new ArrayList(EntityData.data().values());
//        System.out.println("Cleared: " + edata.size());
        Iterator var2 = edata.iterator();

        while (var2.hasNext()) {
            EntityData e = (EntityData) var2.next();
            if (e.getGame().equals(this)) {
//                System.out.println("Removed " + e.getEntity().getCustomName());
                EntityData.data().remove(e.getEntity().getUniqueId());
                // BEGIN EDIT
//                ActiveMob activeMob = MythicMobs.inst().getAPIHelper().getMythicMobInstance(e.getEntity());
//                if (activeMob != null) {
////                    System.out.println("not null");
//                    activeMob.setDespawned();
//                    activeMob.getEntity().remove();
//                    MythicMobs.inst().getMobManager().unregisterActiveMob(activeMob);
//                    activeMob.getEntity().getBukkitEntity().remove();
//                }
                e.getEntity().remove();
                // END
            }
        }

    }

    public void restore() {
        this.players = new ArrayList();
        this.current_stage = 1;
        this.stage_count = -1;
        this.clearMobs();
        this.status = GameStatus.WAITING;
        this.time = this.maxtime;
        // BEGIN EDIT
        this.kills.clear();
        // END EDIT
    }

    public void complete() {
        List<Player> playerss = new ArrayList(this.players);
        Bukkit.broadcastMessage(Messages.get("BroadcastComplete").replace("&", "\u00a7").replace("<player>", ((Player) this.players.get(0)).getName()).replace("<prefix>", this.room.getString("Prefix").replace("&", "\u00a7")));
        Iterator var2 = playerss.iterator();

        while (var2.hasNext()) {
            Player p = (Player) var2.next();
            p.sendMessage(Messages.get("Complete"));
            this.reward(p);
            // BEGIN EDIT
            this.recordToTop(p);
            // END EDIT
        }

        this.quit_countdown = true;
        this.task.setCountdown(FileManager.getFileConfig(FileManager.Files.CONFIG).getInt("Settings.QuitCountdown"));
    }

    public void leaveAllAfterComplete() {
        List<Player> playerss = new ArrayList(this.players);
        Iterator var2 = playerss.iterator();

        while (var2.hasNext()) {
            Player p = (Player) var2.next();
            this.leave(p, false);
        }

        this.restore();
    }

    // BEGIN EDIT
    public static void resetTopData(String gameType, String player) {
        if (!Game.listGame().contains(gameType))
            return;

        FileConfiguration topConfig = FileManager.getFileConfig(FileManager.Files.TOP);
        // Has never played.
        if (!topConfig.contains(gameType + '.' + player))
            return;

        topConfig.set("Overall." + player, topConfig.getInt("Overall." + player) - topConfig.getInt(gameType + '.' + player));
        if (topConfig.getInt("Overall." + player) <= 0) {
            topConfig.set("Overall." + player, null);
        }

        for (String type : new String[]{gameType}) {
            topConfig.set(type + '.' + player, null);
            sortSectionValues(topConfig.getConfigurationSection(type), Comparator.<Map.Entry<String, Object>>comparingInt(e -> (int) e.getValue()).reversed());

            topConfig.set("TimePlay." + type + '.' + player, null);
            topConfig.set("Plays." + type + '.' + player, null);

            sortTopPlayer(topConfig, type);
        }

        FileManager.saveFileConfig(topConfig, FileManager.Files.TOP);
    }

    /**
     * After completing game i.e. player must pass.
     */
    private void recordToTop(Player p) {
        FileConfiguration topConfig = FileManager.getFileConfig(FileManager.Files.TOP);
//        System.out.println(kills);

        topConfig.set("Overall." + p.getName(), topConfig.getInt("Overall." + p.getName()) + kills.getOrDefault(p.getName(), 0));
        sortSectionValues(topConfig.getConfigurationSection("Overall"), Comparator.<Map.Entry<String, Object>>comparingInt(e -> (int) e.getValue()).reversed());

        for (String type : new String[]{getType()}) {
            topConfig.set(getType() + '.' + p.getName(), topConfig.getInt(getType() + '.' + p.getName()) + kills.getOrDefault(p.getName(), 0));
            sortSectionValues(topConfig.getConfigurationSection(getType()), Comparator.<Map.Entry<String, Object>>comparingInt(e -> (int) e.getValue()).reversed());

            topConfig.set("TimePlay." + type + '.' + p.getName(), topConfig.getInt("TimePlay." + type + '.' + p.getName()) + (maxtime - getTimeLeft()));
            topConfig.set("Plays." + type + '.' + p.getName(), topConfig.getInt("Plays." + type + '.' + p.getName()) + 1);

            sortTopPlayer(topConfig, type);
        }
        FileManager.saveFileConfig(topConfig, FileManager.Files.TOP);

        FileConfiguration turnConfig = FileManager.getFileConfig(FileManager.Files.TURNS);
        turnConfig.set("Plays." + type + '.' + p.getName(), turnConfig.getInt("Plays." + type + '.' + p.getName()) + 1);
        FileManager.saveFileConfig(turnConfig, FileManager.Files.TURNS);
    }

    private static void sortTopPlayer(FileConfiguration topConfig, String game) {
        // Sort for TimePlay.'type'
        sortSectionValues(topConfig.getConfigurationSection("TimePlay." + game), (e1, e2) -> {
            int kills1 = topConfig.getInt(game + '.' + e1.getKey());
            int kills2 = topConfig.getInt(game + '.' + e2.getKey());
            int plays1 = topConfig.getInt("Plays." + game + "." + e1.getKey());
            int plays2 = topConfig.getInt("Plays." + game + "." + e2.getKey());

            if (kills1 == kills2) {
                if (plays1 == plays2) {
                    return Integer.compare((int) (e1.getValue()), (int) (e2.getValue()));
                }
                return Integer.compare(plays1, plays2);
            }
            return Integer.compare(kills2, kills1);
        });
    }
    // END

    public void reward(Player p) {
        // BEGIN EDIT
        complete(p, this.type);
        // END
        Random random = new Random();
        Iterator var3 = this.room.getConfigurationSection("Reward").getKeys(false).iterator();

        ItemStack item;
        while (var3.hasNext()) {
            String key = (String) var3.next();
            item = this.room.getItemStack("Reward." + key + ".Item");
            int chance = this.room.getInt("Reward." + key + ".Chance");
            random.addChance(item, (double) chance);
        }

        List<ItemStack> a = new ArrayList();

        for (int i = 1; i <= this.room.getInt("RewardAmount"); ++i) {
            for (item = (ItemStack) random.getRandomElement(); a.contains(item); item = (ItemStack) random.getRandomElement()) {
            }

            a.add(item);
            p.getInventory().addItem(new ItemStack[]{item});
        }

        Iterator var9 = FileManager.getFileConfig(FileManager.Files.CONFIG).getStringList("Settings.RewardCommand." + this.name).iterator();

        while (var9.hasNext()) {
            String cmd = (String) var9.next();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", p.getName()));
        }

    }

    public void starting() {
        this.status = GameStatus.STARTING;
    }

    public boolean isFull() {
        return this.players.size() >= this.max_players;
    }

    public void setMaxPlayer(int max) {
        this.max_players = max;
    }

    public void setMaxTime(int time) {
        this.maxtime = time;
    }

    public int getMaxTime() {
        return maxtime;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public void spectator(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
    }

    public boolean isLeader(Player p) {
        return this.players.size() <= 0 ? false : ((Player) this.players.get(0)).getName().equals(p.getName());
    }

    public Location mobLocation() {
        String mob = this.current_stage == 1 ? "Mob1" : (this.current_stage == 2 ? "Mob2" : (this.current_stage == 3 ? "Mob3" : "Boss"));
        String keyy = mob.equals("Boss") ? "" : "." + this.getStageKey(this.stage_count);
        Location loc = (Location) this.room.get(mob + keyy + ".Location");
        return loc;
    }

    public FileConfiguration getConfig() {
        return this.room;
    }

    public File getConfigFile() {
        return this.configFile;
    }

    public String getType() {
        return this.type;
    }
}
