package com.songoda.epicspawners.listeners;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    private final EpicSpawners plugin;

    public SpawnerListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FIREWORK) return;
        if (entity.getVehicle() != null) {
            entity.getVehicle().remove();
            entity.remove();
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            if (entity.getPassengers().size() != 0) {
                for (Entity e : entity.getPassengers()) {
                    e.remove();
                }
                entity.remove();
            }
        }
        entity.remove();

        Location location = event.getSpawner().getLocation();

        if (!plugin.getSpawnerManager().isSpawner(location)) {
            PlacedSpawner spawner = new PlacedSpawner(location);
            plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getEntityType().name());
            if (spawnerData == null) return;
            spawner.addSpawnerStack(new SpawnerStack(spawner, spawnerData.getFirstTier(), 1));
            EpicSpawners.getInstance().getDataManager().createSpawner(spawner);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!Settings.HOSTILE_MOBS_ATTACK_SECOND.getBoolean()) return;
        if (event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK"))
            return;
        event.setCancelled(true);
    }
}
