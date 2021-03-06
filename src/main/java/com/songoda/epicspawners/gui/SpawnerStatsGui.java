package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.HeadUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpawnerStatsGui extends CustomizableGui {

    private final EpicSpawners plugin;
    private final Player player;
    private final Map<SpawnerData, Integer> entities = new HashMap<>();

    public SpawnerStatsGui(EpicSpawners plugin, Player player) {
        super(plugin, "stats");
        setRows(6);
        this.plugin = plugin;
        this.player = player;

        for (Map.Entry<EntityType, Integer> entry : plugin.getPlayerDataManager().getPlayerData(player).getEntityKills().entrySet()) {
            SpawnerData data = plugin.getSpawnerManager().getSpawnerData(entry.getKey());
            if (data.isActive())
                entities.put(data, entry.getValue());
        }

        setTitle(plugin.getLocale().getMessage("interface.spawnerstats.title").getMessage());
        showPage();
    }

    public void showPage() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5", 0, 1, true, true, glass2);

        pages = (int) Math.max(1, Math.ceil(entities.size() / ((double) 28)));

        // enable page event
        setNextPage(5, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        setButton("exit", 8, GuiUtils.createButtonItem(CompatibleMaterial.valueOf(plugin.getConfig().getString("Interfaces.Exit Icon")),
                plugin.getLocale().getMessage("general.nametag.exit").getMessage()), (event) -> player.closeInventory());

        Set<Map.Entry<SpawnerData, Integer>> entries = entities.entrySet().stream().skip((page - 1) * 28).limit(28)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toCollection(LinkedHashSet::new));

        int num = 11;
        for (Map.Entry<SpawnerData, Integer> entry : entries) {
            if (num == 16 || num == 36)
                num = num + 2;

            int goal = Settings.KILL_DROP_GOAL.getInt();

            SpawnerData spawnerData = entry.getKey();

            int customGoal = spawnerData.getKillDropGoal();
            if (customGoal != 0) goal = customGoal;

            setItem(num, GuiUtils.createButtonItem(HeadUtils.getTexturedSkull(spawnerData),
                    TextUtils.formatText("&6" + spawnerData.getIdentifyingName() + "&7: &e" + entry.getValue() + "&7/&e" + goal)));
            num++;
        }
    }
}
