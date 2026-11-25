package pl.kzdev.register;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration data;
    private final Set<UUID> authenticated = ConcurrentHashMap.newKeySet();
    private final Set<String> authenticatedNames = ConcurrentHashMap.newKeySet();

    public AuthManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "passwords.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            file.getParentFile().mkdirs();
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać haseł: " + e.getMessage());
        }
    }

    public boolean hasAccount(String name) {
        return data.contains("passwords." + name.toLowerCase());
    }

    public boolean register(String name, String password) {
        if (hasAccount(name)) return false;
        data.set("passwords." + name.toLowerCase(), PasswordUtil.hash(password));
        save();
        return true;
    }

    public boolean login(String name, String password) {
        String stored = data.getString("passwords." + name.toLowerCase());
        return stored != null && PasswordUtil.verify(password, stored);
    }

    public boolean isAuthenticated(Player player) {
        return authenticated.contains(player.getUniqueId()) || authenticatedNames.contains(player.getName().toLowerCase());
    }

    public void setAuthenticated(Player player) {
        authenticated.add(player.getUniqueId());
        authenticatedNames.add(player.getName().toLowerCase());
    }

    public void removeAuthenticated(Player player) {
        authenticated.remove(player.getUniqueId());
        authenticatedNames.remove(player.getName().toLowerCase());
    }
}
