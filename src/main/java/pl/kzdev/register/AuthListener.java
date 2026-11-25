package pl.kzdev.register;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthListener implements Listener {
    private final KZDevRegister plugin;
    private final AuthManager authManager;
    private final MovementFreezer freezer;
    private final Map<UUID, BossBar> authBars = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> musicBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> musicTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> particleTasks = new ConcurrentHashMap<>();

    public AuthListener(KZDevRegister plugin, AuthManager authManager, MovementFreezer freezer) {
        this.plugin = plugin;
        this.authManager = authManager;
        this.freezer = freezer;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authManager.removeAuthenticated(player);

        freezer.freeze(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, false, false));

        boolean hasAcc = authManager.hasAccount(player.getName());

        if (plugin.getConfig().getBoolean("music.enabled", true)) {
            long delayTicks = Math.max(1L, plugin.getConfig().getLong("music.delay-ticks", 40L));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !authManager.isAuthenticated(player)) {
                    startMusicLoop(player);
                }
            }, delayTicks);

            String trackName = plugin.getConfig().getString("music.display-name", "Muzyka powitalna");
            String barText = plugin.getConfig().getString("music.bossbar", "§d♪ §fAktualnie gra: §b{track} §d♪")
                    .replace("{track}", trackName);
            BossBar musicBar = Bukkit.createBossBar(barText, BarColor.PURPLE, BarStyle.SEGMENTED_20);
            if (musicBar != null) {
                musicBar.addPlayer(player);
                musicBars.put(player.getUniqueId(), musicBar);
            }
        }

        if (plugin.getConfig().getBoolean("auth.particles-enabled", true)) {
            startParticleLoop(player);
        }

        String title = hasAcc ? plugin.getConfig().getString("messages.title-login") : plugin.getConfig().getString("messages.title-register");
        String subtitle = hasAcc ? plugin.getConfig().getString("messages.subtitle-login") : plugin.getConfig().getString("messages.subtitle-register");
        String msg = hasAcc ? plugin.getConfig().getString("messages.login-required") : plugin.getConfig().getString("messages.register-required");

        player.sendTitle(title, subtitle, 10, 200, 20);
        player.sendMessage(msg);

        BossBar authBar = Bukkit.createBossBar(plugin.getConfig().getString("messages.bossbar"), BarColor.RED, BarStyle.SOLID);
        if (authBar != null) {
            authBar.addPlayer(player);
            authBars.put(player.getUniqueId(), authBar);
        }

        long kickSeconds = Math.max(10L, plugin.getConfig().getLong("auth.kick-seconds", 60L));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !authManager.isAuthenticated(player)) {
                player.kickPlayer(plugin.getConfig().getString("messages.kick-timeout", "§cNie zalogowałeś się w wyznaczonym czasie!"));
            }
        }, 20L * kickSeconds);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        authManager.removeAuthenticated(player);
        cleanup(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!authManager.isAuthenticated(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfig().getString("messages.chat-blocked"));
        }
    }

    public void removeBar(Player player) {
        cleanup(player);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.0f);
    }

    private void cleanup(Player player) {
        BossBar authBar = authBars.remove(player.getUniqueId());
        if (authBar != null) authBar.removeAll();
        BossBar musicBar = musicBars.remove(player.getUniqueId());
        if (musicBar != null) musicBar.removeAll();

        stopMusic(player);
        stopParticles(player);
        player.removePotionEffect(PotionEffectType.DARKNESS);
        freezer.unfreeze(player);
    }

    private void startMusicLoop(Player player) {
        if (!player.isOnline() || authManager.isAuthenticated(player)) return;
        if (!plugin.getConfig().getBoolean("music.enabled", true)) return;

        BukkitTask oldTask = musicTasks.remove(player.getUniqueId());
        if (oldTask != null) oldTask.cancel();

        String soundKey = plugin.getConfig().getString("music.sound", "MUSIC_DISC_CAT");
        float volume = (float) plugin.getConfig().getDouble("music.volume", 0.4);
        float pitch = (float) plugin.getConfig().getDouble("music.pitch", 1.0);
        long loopTicks = Math.max(40L, plugin.getConfig().getLong("music.loop-ticks", 365L));

        stopSoundKey(player, soundKey);
        playSoundKey(player, soundKey, volume, pitch);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> startMusicLoop(player), loopTicks);
        if (task != null) musicTasks.put(player.getUniqueId(), task);
    }

    private void playSoundKey(Player player, String soundKey, float volume, float pitch) {
        if (soundKey == null || soundKey.isBlank()) return;
        try {
            Sound bukkitSound = Sound.valueOf(soundKey.trim().toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), bukkitSound, volume, pitch);
        } catch (IllegalArgumentException custom) {
            player.playSound(player.getLocation(), soundKey.trim(), volume, pitch);
        }
    }

    private void stopSoundKey(Player player, String soundKey) {
        if (soundKey == null || soundKey.isBlank()) return;
        try {
            Sound bukkitSound = Sound.valueOf(soundKey.trim().toUpperCase(Locale.ROOT));
            player.stopSound(bukkitSound);
        } catch (IllegalArgumentException custom) {
            try {
                player.stopSound(soundKey.trim());
            } catch (Throwable ignored) {}
        }
    }

    private void startParticleLoop(Player player) {
        if (!player.isOnline() || authManager.isAuthenticated(player)) return;
        Location base = player.getLocation().add(0, 1, 0);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.spawnParticle(Particle.END_ROD, base, 15, 1.5, 2.0, 1.5, 0.02);
            online.spawnParticle(Particle.PORTAL, base, 10, 1.2, 1.5, 1.2, 0.05);
            online.spawnParticle(Particle.FLAME, base, 5, 1.0, 1.5, 1.0, 0.01);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> startParticleLoop(player), 4L);
        if (task != null) particleTasks.put(player.getUniqueId(), task);
    }

    public void stopMusic(Player player) {
        BukkitTask task = musicTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
        String soundKey = plugin.getConfig().getString("music.sound", "MUSIC_DISC_CAT");
        stopSoundKey(player, soundKey);
    }

    public void stopParticles(Player player) {
        BukkitTask task = particleTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }
}
