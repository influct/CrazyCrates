package com.badbones69.crazycrates;

import com.badbones69.crazycrates.api.CrazyManager;
import com.badbones69.crazycrates.api.FileManager.Files;
import com.badbones69.crazycrates.api.FileManager;
import com.badbones69.crazycrates.api.enums.settings.Messages;
import com.badbones69.crazycrates.api.managers.quadcrates.SessionManager;
import com.badbones69.crazycrates.commands.CCCommand;
import com.badbones69.crazycrates.commands.CCTab;
import com.badbones69.crazycrates.commands.subs.player.BaseKeyCommand;
import com.badbones69.crazycrates.cratetypes.CSGO;
import com.badbones69.crazycrates.cratetypes.Cosmic;
import com.badbones69.crazycrates.cratetypes.CrateOnTheGo;
import com.badbones69.crazycrates.cratetypes.QuadCrate;
import com.badbones69.crazycrates.cratetypes.QuickCrate;
import com.badbones69.crazycrates.cratetypes.Roulette;
import com.badbones69.crazycrates.cratetypes.War;
import com.badbones69.crazycrates.cratetypes.Wheel;
import com.badbones69.crazycrates.cratetypes.Wonder;
import com.badbones69.crazycrates.listeners.BrokeLocationsListener;
import com.badbones69.crazycrates.listeners.CrateControlListener;
import com.badbones69.crazycrates.listeners.FireworkDamageListener;
import com.badbones69.crazycrates.listeners.MenuListener;
import com.badbones69.crazycrates.listeners.MiscListener;
import com.badbones69.crazycrates.listeners.PreviewListener;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.message.MessageKey;
import io.papermc.lib.PaperLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class CrazyCrates extends JavaPlugin implements Listener {

    private final FileManager fileManager = FileManager.getInstance();
    private final CrazyManager crazyManager = CrazyManager.getInstance();

    private boolean isEnabled = false;

    BukkitCommandManager<CommandSender> manager = BukkitCommandManager.create(this);

    @Override
    public void onEnable() {

        if (!PaperLib.isPaper()) {
            getLogger().warning("====================================================");
            getLogger().warning(" " + this.getName() + " works better if you use Paper ");
            getLogger().warning(" as your server software.");
            getLogger().warning(" ");
            getLogger().warning(" Paper offers significant performance improvements,");
            getLogger().warning(" bug fixes, security enhancements and optional");
            getLogger().warning(" features for server owners to enhance their server.");
            getLogger().warning(" ");
            getLogger().warning(" All of your plugins should still work, and the");
            getLogger().warning(" Paper community will gladly help you fix any issues.");
            getLogger().warning("");
            getLogger().warning(" Join the Paper Community @ https://papermc.io");
            getLogger().warning("====================================================");

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {

            crazyManager.loadPlugin(this);

            fileManager.logInfo(true)
                    .registerDefaultGenerateFiles("CrateExample.yml", "/crates", "/crates")
                    .registerDefaultGenerateFiles("QuadCrateExample.yml", "/crates", "/crates")
                    .registerDefaultGenerateFiles("CosmicCrateExample.yml", "/crates", "/crates")
                    .registerDefaultGenerateFiles("QuickCrateExample.yml", "/crates", "/crates")
                    .registerDefaultGenerateFiles("classic.nbt", "/schematics", "/schematics")
                    .registerDefaultGenerateFiles("nether.nbt", "/schematics", "/schematics")
                    .registerDefaultGenerateFiles("outdoors.nbt", "/schematics", "/schematics")
                    .registerDefaultGenerateFiles("sea.nbt", "/schematics", "/schematics")
                    .registerDefaultGenerateFiles("soul.nbt", "/schematics", "/schematics")
                    .registerDefaultGenerateFiles("wooden.nbt", "/schematics", "/schematics")
                    .registerCustomFilesFolder("/crates")
                    .registerCustomFilesFolder("/schematics")
                    .setup(this);

            // Clean files if we have to.
            cleanFiles();

            // Add extra messages.
            Messages.addMissingMessages();

            String metricsPath = FileManager.Files.CONFIG.getFile().getString("Settings.Toggle-Metrics");
            boolean metricsEnabled = Files.CONFIG.getFile().getBoolean("Settings.Toggle-Metrics");

            if (metricsPath != null) {
                if (metricsEnabled) new Metrics(this, 4514);
            } else {
                getLogger().warning("Metrics was automatically enabled.");
                getLogger().warning("Please add Toggle-Metrics: false to the top of your config.yml.");
                getLogger().warning("https://github.com/Crazy-Crew/Crazy-Crates/blob/main/src/main/resources/config.yml");
                getLogger().warning("An example if confused is linked above.");

                new Metrics(this, 4514);
            }
        } catch (Exception e) {

            getLogger().severe(e.getMessage());

            for (StackTraceElement stack : e.getStackTrace()) {
                getLogger().severe(String.valueOf(stack));
            }

            isEnabled = false;

            return;
        }

        enable();

        isEnabled = true;
    }

    @Override
    public void onDisable() {
        if (!isEnabled) return;

        SessionManager.endCrates();

        QuickCrate.removeAllRewards();

        if (crazyManager.getHologramController() != null) crazyManager.getHologramController().removeAllHolograms();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        crazyManager.setNewPlayerKeys(e.getPlayer());
        crazyManager.loadOfflinePlayersKeys(e.getPlayer());
    }

    public void cleanFiles() {
        if (!Files.LOCATIONS.getFile().contains("Locations")) {
            Files.LOCATIONS.getFile().set("Locations.Clear", null);
            Files.LOCATIONS.saveFile();
        }

        if (!Files.DATA.getFile().contains("Players")) {
            Files.DATA.getFile().set("Players.Clear", null);
            Files.DATA.saveFile();
        }
    }

    private void enable() {

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new MenuListener(), this);
        pluginManager.registerEvents(new PreviewListener(), this);
        pluginManager.registerEvents(new FireworkDamageListener(), this);
        pluginManager.registerEvents(new CrateControlListener(), this);
        pluginManager.registerEvents(new MiscListener(), this);

        pluginManager.registerEvents(new War(), this);
        pluginManager.registerEvents(new CSGO(), this);
        pluginManager.registerEvents(new Wheel(), this);
        pluginManager.registerEvents(new Wonder(), this);
        pluginManager.registerEvents(new Cosmic(), this);
        pluginManager.registerEvents(new Roulette(), this);
        pluginManager.registerEvents(new QuickCrate(), this);
        pluginManager.registerEvents(new CrateOnTheGo(), this);
        pluginManager.registerEvents(new QuadCrate(), this);

        pluginManager.registerEvents(this, this);

        crazyManager.loadCrates();

        if (!crazyManager.getBrokeCrateLocations().isEmpty()) pluginManager.registerEvents(new BrokeLocationsListener(), this);

        manager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> {
            sender.sendMessage(Messages.UNKNOWN_COMMAND.getMessage());
        });

        manager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, context) -> {
            sender.sendMessage(Messages.TOO_MANY_ARGS.getMessage());
        });

        manager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            sender.sendMessage(Messages.NOT_ENOUGH_ARGS.getMessage());
        });

        manager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, context) -> {
            sender.sendMessage(Messages.NOT_ONLINE.getMessage().replace("%player%", context.getTypedArgument()));
        });

        manager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, context) -> {
            sender.sendMessage(Messages.NO_PERMISSION.getMessage());
        });

        manager.registerMessage(BukkitMessageKey.PLAYER_ONLY, (sender, context) -> {
            sender.sendMessage(Messages.MUST_BE_A_PLAYER.getMessage());
        });

        manager.registerMessage(BukkitMessageKey.CONSOLE_ONLY, (sender, context) -> {
            sender.sendMessage(Messages.MUST_BE_A_CONSOLE_SENDER.getMessage());
        });

        manager.registerCommand(new BaseKeyCommand());

        getCommand("crazycrates").setExecutor(new CCCommand());
        getCommand("crazycrates").setTabCompleter(new CCTab());
    }
}