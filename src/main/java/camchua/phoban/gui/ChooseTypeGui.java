package camchua.phoban.gui;

import camchua.phoban.PhoBan;
import camchua.phoban.game.Game;
import camchua.phoban.manager.FileManager;
import camchua.phoban.nbtapi.NBTItem;
import camchua.phoban.utils.ItemBuilder;
import camchua.phoban.utils.Messages;
import camchua.phoban.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChooseTypeGui implements Listener {
    public static HashMap<Player, ChooseTypeGui> viewers = new HashMap();
    public ArrayList<Inventory> pages = new ArrayList();
    public int curpage = 0;

    public ChooseTypeGui(Player p, int pg) {
        if (p != null && pg >= 0) {
            this.curpage = pg;
            Inventory page = this.gui(p);
            int firstempty = Utils.firstEmpty(page.getSize() / 9);
            List<String> type = new ArrayList();
            Iterator var6 = Game.listGame().iterator();

            String t;
            while (var6.hasNext()) {
                t = (String) var6.next();
                Game game = Game.getGame(t);
                if (!type.contains(game.getType())) {
                    type.add(game.getType());
                }
            }

            var6 = type.iterator();

            while (var6.hasNext()) {
                t = (String) var6.next();
                String status = !Game.hasTurn(p, t) ? FileManager.getFileConfig(FileManager.Files.GUI).getString("ChooseTypeGui.Format.NoTurn").replace("&", "\u00a7") : FileManager.getFileConfig(FileManager.Files.GUI).getString("ChooseTypeGui.Format.Join").replace("&", "\u00a7");
                HashMap<String, List<String>> replace = new HashMap();
                replace.put("<type>", Arrays.asList(t));
                replace.put("<status>", Arrays.asList(status));
                // BEGIN EDIT
                replace.put("<soluot_" + t.toLowerCase(Locale.ROOT) + ">", Collections.singletonList(String.valueOf(Game.getTurn(p, t))));
//                replace.put("<soluot_ngay_" + t.toLowerCase(Locale.ROOT) + ">", Collections.singletonList(String.valueOf(Game.getGame().maxTurnsPerDay - FileManager.getFileConfig(FileManager.Files.TURNS).getInt("Plays." +  + '.' + p.getName()))));
                // END EDIT
                ItemStack item = null;
                String key = this.parseType(t);
                if (key.equals("deo-co-con-cac-gi-o-day-het")) {
                    item = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.TypeFormat", replace);
                } else {
                    item = ItemBuilder.build(FileManager.Files.PHOBAN, key, replace);
                }

                NBTItem nbt = new NBTItem(item.clone());
                nbt.setString("ChooseTypeGui_ClickType", "ChooseType");
                nbt.setString("ChooseTypeGui_Type", t);
                if (page.firstEmpty() == firstempty) {
                    page.addItem(new ItemStack[]{nbt.getItem().clone()});
                    this.pages.add(page);
                    page = this.gui(p);
                } else {
                    page.addItem(new ItemStack[]{nbt.getItem().clone()});
                }
            }

            this.pages.add(page);
            if (viewers.containsKey(p)) {
                viewers.remove(p);
                p.openInventory((Inventory) this.pages.get(this.curpage));
                viewers.put(p, this);
            } else {
                p.openInventory((Inventory) this.pages.get(this.curpage));
                viewers.put(p, this);
            }

        }
    }

    private Inventory gui(Player p) {
        FileConfiguration gui = FileManager.getFileConfig(FileManager.Files.GUI);
        int rows = gui.getInt("ChooseTypeGui.Rows");
        if (rows < 3) {
            rows = 3;
        }

        Inventory inv = Bukkit.createInventory((InventoryHolder) null, rows * 9, ChatColor.translateAlternateColorCodes('&', gui.getString("PhoBanGui.Title")));
        ItemStack blank = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.Blank", new HashMap());
        Iterator var5 = gui.getIntegerList("ChooseTypeGui.Blank.Slot").iterator();

        label90:
        while (var5.hasNext()) {
            int slot = (Integer) var5.next();
            if (slot < gui.getInt("ChooseTypeGui.Rows") * 9) {
                if (slot <= -1) {
                    int i = 0;

                    while (true) {
                        if (i >= gui.getInt("ChooseTypeGui.Rows") * 9) {
                            break label90;
                        }

                        inv.setItem(i, blank.clone());
                        ++i;
                    }
                }

                inv.setItem(slot, blank.clone());
            }
        }

        ItemStack nextpage = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.NextPage", new HashMap());
        NBTItem nbt = new NBTItem(nextpage.clone());
        nbt.setString("ChooseTypeGui_ClickType", "NextPage");
        Iterator var13 = gui.getIntegerList("ChooseTypeGui.NextPage.Slot").iterator();

        int i;
        label75:
        while (var13.hasNext()) {
            int slot = (Integer) var13.next();
            if (slot < gui.getInt("ChooseTypeGui.Rows") * 9) {
                if (slot <= -1) {
                    i = 0;

                    while (true) {
                        if (i >= gui.getInt("ChooseTypeGui.Rows") * 9) {
                            break label75;
                        }

                        inv.setItem(i, nbt.getItem().clone());
                        ++i;
                    }
                }

                inv.setItem(slot, nbt.getItem().clone());
            }
        }

        ItemStack previouspage = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.PreviousPage", new HashMap());
        nbt = new NBTItem(previouspage.clone());
        nbt.setString("ChooseTypeGui_ClickType", "PreviousPage");
        Iterator var15 = gui.getIntegerList("ChooseTypeGui.PreviousPage.Slot").iterator();

        label60:
        while (var15.hasNext()) {
            i = (Integer) var15.next();
            if (i < gui.getInt("ChooseTypeGui.Rows") * 9) {
                if (i <= -1) {
                    int i2 = 0;

                    while (true) {
                        if (i2 >= gui.getInt("ChooseTypeGui.Rows") * 9) {
                            break label60;
                        }

                        inv.setItem(i2, nbt.getItem().clone());
                        ++i2;
                    }
                }

                inv.setItem(i, nbt.getItem().clone());
            }
        }

        // BEGIN EDIT
        List<String> types = gui.getStringList("ChooseTypeGui.TopTypes");
        if (types != null) {
            for (String type : types) {
                int slot = gui.getInt("ChooseTypeGui.TopBoard" + type + ".Slot");
                ItemStack topItem = getTopItem(type);
                inv.setItem(slot, topItem);
            }
        }

        ItemStack itemStack = this.getStatsPlayerItem(p);
        int slot = gui.getInt("ChooseTypeGui.StatsPlayer.Slot");
        inv.setItem(slot, itemStack);
        // END

        var15 = gui.getIntegerList("ChooseTypeGui.TypeSlot").iterator();

        while (var15.hasNext()) {
            i = (Integer) var15.next();
            inv.setItem(i, new ItemStack(Material.AIR));
        }

        return inv;
    }

    // BEGIN EDIT
    public ItemStack getTopItem(String gameType) {
        if (gameType == null) {
            gameType = "Overall";
        }

        FileConfiguration top = FileManager.getFileConfig(FileManager.Files.TOP);

        HashMap<String, List<String>> replace = new HashMap<>();
        int i = 1;
        if (top.getConfigurationSection(gameType) != null) {
            Set<String> topBoard = top.getConfigurationSection("TimePlay." + gameType).getKeys(false);

            for (String player : topBoard) {
                int timePlay = top.getInt("TimePlay." + gameType + '.' + player);
                if (timePlay == -1)
                    continue;

                replace.put("<player" + i + ">", Collections.singletonList(player));
                replace.put("<kills" + i + ">", Collections.singletonList("" + top.getInt(gameType + '.' + player)));
                replace.put("<time" + i + ">", Collections.singletonList(PhoBanGui.timeFormat(timePlay)));
                replace.put("<plays" + i + ">", Collections.singletonList("" + top.getInt("Plays." + gameType + '.' + player)));
                ++i;
            }
        }
        for (; i <= FileManager.getFileConfig(FileManager.Files.GUI).getInt("ChooseTypeGui.TopBoard" + gameType + ".Top"); ++i) {
            replace.put("<player" + i + ">", Collections.singletonList(""));
            replace.put("<kills" + i + ">", Collections.singletonList(""));
            replace.put("<time" + i + ">", Collections.singletonList(""));
            replace.put("<plays" + i + ">", Collections.singletonList(""));
        }

        ItemStack item = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.TopBoard" + gameType, replace);
        return item;
    }

    public ItemStack getStatsPlayerItem(Player p) {
        FileConfiguration top = FileManager.getFileConfig(FileManager.Files.TOP);

        HashMap<String, List<String>> replace = new HashMap<>();
        for (String game : Game.listGame()) {
            replace.put("<kills_" + game + ">", Collections.singletonList("" + top.getInt(game + '.' + p.getName())));

            int timePlay = top.getInt("TimePlay." + game + "." + p.getName(), -1);
            if (timePlay == -1)
                replace.put("<time_" + game + ">", Collections.singletonList(""));
            else
                replace.put("<time_" + game + ">", Collections.singletonList(PhoBanGui.timeFormat(timePlay)));

            if (top.contains("Plays." + game + '.' + p.getName())) {
                replace.put("<plays_" + game + ">", Collections.singletonList("" + top.getInt("Plays." + game + '.' + p.getName())));
            } else {
                replace.put("<plays_" + game + ">", Collections.singletonList(""));
            }
        }

        ItemStack item = ItemBuilder.build(FileManager.Files.GUI, "ChooseTypeGui.StatsPlayer", replace);
        return item;
    }
    // END

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        if (viewers.containsKey(p)) {
            e.setCancelled(true);
            ItemStack click = e.getCurrentItem();
            if (click == null) {
                return;
            }

            if (click.getType().equals(Material.AIR)) {
                return;
            }

            NBTItem nbt = new NBTItem(click);
            if (!nbt.hasKey("ChooseTypeGui_ClickType")) {
                return;
            }

            String clicktype = nbt.getString("ChooseTypeGui_ClickType");
            ChooseTypeGui inv;
            switch (clicktype.toLowerCase()) {
                case "nextpage":
                    inv = (ChooseTypeGui) viewers.get(p);
                    if (inv.curpage >= inv.pages.size() - 1) {
                        return;
                    }

                    new ChooseTypeGui(p, inv.curpage + 1);
                    return;
                case "previouspage":
                    inv = (ChooseTypeGui) viewers.get(p);
                    if (inv.curpage > 0) {
                        new ChooseTypeGui(p, inv.curpage - 1);
                    }

                    return;
                case "choosetype":
                    final String type = nbt.getString("ChooseTypeGui_Type");
                    if (!Game.hasTurn(p, type)) {
                        p.sendMessage(Messages.get("NoTurn"));
                        return;
                    }

                    p.closeInventory();
                    (new BukkitRunnable() {
                        public void run() {
                            new PhoBanGui(p, 0, type);
                        }
                    }).runTaskLater(PhoBan.inst(), 1L);
                    return;
            }
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (viewers.containsKey(p)) {
            viewers.remove(p);
        }

    }

    public String parseType(String type) {
        FileConfiguration phoban = FileManager.getFileConfig(FileManager.Files.PHOBAN);
        Iterator var3 = phoban.getKeys(false).iterator();

        String str;
        do {
            if (!var3.hasNext()) {
                return "deo-co-con-cac-gi-o-day-het";
            }

            str = (String) var3.next();
        } while (!phoban.contains(str + ".Type") || !phoban.getString(str + ".Type").equalsIgnoreCase(type));

        return str;
    }
}
