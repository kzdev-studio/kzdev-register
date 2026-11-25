package pl.kzdev.register;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MovementFreezer {
    private final JavaPlugin plugin;
    private final AuthManager authManager;
    private final Map<UUID, Player> frozenPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Location> spawns = new ConcurrentHashMap<>();

    public MovementFreezer(JavaPlugin plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    public void freeze(Player player) {
        frozenPlayers.put(player.getUniqueId(), player);
        spawns.put(player.getUniqueId(), player.getLocation().clone());
    }

    public void unfreeze(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        spawns.remove(player.getUniqueId());
    }

    public void start() {
        schedule();
    }

    private void schedule() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : new ArrayList<>(frozenPlayers.keySet())) {
                Player p = frozenPlayers.get(uuid);

                if (p == null || !p.isOnline()) {
                    frozenPlayers.remove(uuid);
                    spawns.remove(uuid);
                    continue;
                }

                if (authManager.isAuthenticated(p)) {
                    frozenPlayers.remove(uuid);
                    spawns.remove(uuid);
                    continue;
                }

                Location spawn = spawns.get(uuid);
                if (spawn == null) continue;

                Location current = p.getLocation();
                if (Math.abs(current.getX() - spawn.getX()) > 0.3 ||
                        Math.abs(current.getZ() - spawn.getZ()) > 0.3 ||
                        Math.abs(current.getY() - spawn.getY()) > 0.3) {
                    p.teleport(spawn);
                }
            }
            schedule();
        }, 3L);
    }
}
