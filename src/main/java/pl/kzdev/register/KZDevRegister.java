package pl.kzdev.register;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class KZDevRegister extends JavaPlugin {

    private AuthManager authManager;
    private MovementFreezer freezer;
    private AuthListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.authManager = new AuthManager(this);
        this.freezer = new MovementFreezer(this, authManager);
        this.listener = new AuthListener(this, authManager, freezer);

        getServer().getPluginManager().registerEvents(listener, this);
        freezer.start();

        getLogger().info("KZDevRegister 1.0.0 - Bezpieczne logowanie i muzyka powitalna aktywne!");
    }

    @Override
    public void onDisable() {
        if (authManager != null) authManager.save();
        getLogger().info("KZDevRegister wyłączony.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getConfig().getString("messages.only-players", "Ta komenda jest tylko dla graczy!"));
            return true;
        }

        String cmd = command.getName().toLowerCase();
        if (cmd.equals("register")) {
            handleRegister(player, args);
            return true;
        }
        if (cmd.equals("login")) {
            handleLogin(player, args);
            return true;
        }
        return false;
    }

    private void handleRegister(Player player, String[] args) {
        if (authManager.isAuthenticated(player)) {
            freezer.unfreeze(player);
            listener.removeBar(player);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.sendMessage(getConfig().getString("messages.already-logged-in", "§aJesteś już zalogowany!"));
            return;
        }
        if (authManager.hasAccount(player.getName())) {
            player.sendMessage(getConfig().getString("messages.already-registered"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUżycie: §e/register <hasło> <hasło>");
            return;
        }
        if (!args[0].equals(args[1])) {
            player.sendMessage(getConfig().getString("messages.passwords-dont-match"));
            return;
        }
        if (args[0].length() < 4) {
            player.sendMessage(getConfig().getString("messages.password-too-short"));
            return;
        }

        if (authManager.register(player.getName(), args[0])) {
            authManager.setAuthenticated(player);
            freezer.unfreeze(player);
            listener.removeBar(player);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.sendTitle(
                    getConfig().getString("messages.title-success-register", "§a§lZAREJESTROWANO!"),
                    getConfig().getString("messages.subtitle-success", "§aMiłej gry na serwerze!"),
                    10, 60, 20
            );
            player.sendMessage(getConfig().getString("messages.registered"));
        } else {
            player.sendMessage("§cBłąd rejestracji. Zgłoś to administratorowi.");
        }
    }

    private void handleLogin(Player player, String[] args) {
        if (authManager.isAuthenticated(player)) {
            freezer.unfreeze(player);
            listener.removeBar(player);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.sendMessage(getConfig().getString("messages.already-logged-in", "§aJesteś już zalogowany!"));
            return;
        }
        if (!authManager.hasAccount(player.getName())) {
            player.sendMessage(getConfig().getString("messages.not-registered"));
            return;
        }
        if (args.length < 1) {
            player.sendMessage("§cUżycie: §e/login <hasło>");
            return;
        }

        if (authManager.login(player.getName(), args[0])) {
            authManager.setAuthenticated(player);
            freezer.unfreeze(player);
            listener.removeBar(player);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.sendTitle(
                    getConfig().getString("messages.title-success-login", "§a§lZALOGOWANO!"),
                    getConfig().getString("messages.subtitle-success", "§aMiłej gry na serwerze!"),
                    10, 60, 20
            );
            player.sendMessage(getConfig().getString("messages.logged-in"));
        } else {
            player.sendMessage(getConfig().getString("messages.wrong-password"));
        }
    }
}
