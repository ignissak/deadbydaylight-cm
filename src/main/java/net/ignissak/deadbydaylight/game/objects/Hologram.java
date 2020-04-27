package net.ignissak.deadbydaylight.game.objects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Arrays;

public class Hologram {

    private List<ArmorStand> armorStands;

    @NotNull
    private List<String> lines;

    @NotNull
    private Location location;

    public Hologram(@NotNull Location location, @NotNull String... lines) {
        this.lines = Arrays.asList(lines);
        this.location = location;
    }

    /**
     * Spawns hologram on desired location.
     */
    public void spawn() {
        int i = 0;
        for (String line : lines) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));

            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setInvulnerable(true);

            location.add(0, -1 * i, 0);
            armorStand.teleport(location);

            armorStands.add(armorStand);
            i += 0.2D;
        }
    }

    /**
     * Removes hologram.
     */
    public void despawn() {
        for (ArmorStand armorStand : armorStands) {
            armorStand.remove();
        }
    }

    /**
     * Updates all armor stands.
     */
    private void update() {
        for (ArmorStand armorStand : armorStands) {
            for (String line : lines) {
                armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));

                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setInvulnerable(true);
            }
        }
    }

    public void setLines(String... lines) {
        this.lines = Arrays.asList(lines);
        update();
    }

    public List<ArmorStand> getArmorStands() {
        return armorStands;
    }

    @NotNull
    public List<String> getLines() {
        return lines;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }
}
