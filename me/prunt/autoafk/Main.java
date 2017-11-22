package me.prunt.autoafk;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Prunt
 *
 */
public class Main extends JavaPlugin implements Listener {
    // Stores how many minutes the player has been inactive
    HashMap<Player, Integer> afkMinutes = new HashMap<>();
    // Stores AFK players
    HashMap<Player, ParticleTask> afkList = new HashMap<>();
    // Stores CountdownTask list
    HashMap<Player, CountdownTask> countList = new HashMap<>();
    // Stores old playerlist names that have been replaced with [AFK] tag +
    // playerlist name
    HashMap<Player, String> oldPlayerListNames = new HashMap<>();
    // Anti /afk spam
    HashMap<Player, Long> lastUsed = new HashMap<>();

    // Debug switch
    boolean debug = false;

    @Override
    public void onEnable() {
	// In case it's a reload
	afkMinutes.clear();
	afkList.clear();
	oldPlayerListNames.clear();
	for (Player p : getServer().getOnlinePlayers()) {
	    afkMinutes.put(p, 0);
	}

	// Copys config.yml file, if it doesn't exist
	saveDefaultConfig();

	// Registers events
	getServer().getPluginManager().registerEvents(this, this);

	// AutoAFK loop, every 1 minute
	getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	    @Override
	    public void run() {
		// Loops through all players
		for (Player p : getServer().getOnlinePlayers()) {
		    // Adds a minute to player's count
		    addMinute(p);

		    if (debug)
			System.out.println("0 " + p.getName());

		    // If player is already in AFK mode
		    if (afkList.containsKey(p)) {

			if (debug)
			    System.out.println("1 " + p.getName());

			// if kick mode is enabled and player can be kicked
			if (!(p.hasPermission("autoafk.kickexempt")) && getConfig().getBoolean("kick.enabled")) {

			    if (debug)
				System.out.println("7 " + p.getName());

			    // if it's time to kick
			    if (getTime(p) == getKickTime(p)) {
				// Puts player into AFK mode
				kick(p);
			    }
			}
		    } else { // if not in afk

			if (debug)
			    System.out.println("2 " + p.getName());

			// if auto mode is enabled and player can be
			// automatically put into afk
			if (getConfig().getBoolean("auto.enabled") && !(p.hasPermission("autoafk.exempt"))) {

			    if (debug)
				System.out.println("5 " + p.getName());

			    // if it's time to put info afk
			    if (getTime(p) == getAutoTime(p)) {

				if (debug)
				    System.out.println("6 " + p.getName());

				// Puts player into AFK mode
				autoAFK(p);
			    }
			}
		    }
		}
	    }
	}, 1200, 1200);
    }

    private void kick(Player p) {
	// If the kick.onlywhenfull option is true
	if (getConfig().getBoolean("kick.onlywhenfull")) {
	    // If the server isn't full
	    if (getServer().getOnlinePlayers().size() != getServer().getMaxPlayers()) {
		// Abort kicking
		return;
	    }
	}

	if (debug)
	    System.out.println("3 " + p.getName());

	// Gets the command sender
	CommandSender sender = getServer().getConsoleSender();
	if (!getConfig().getBoolean("kick.command-as-console")) {
	    sender = p;
	}

	// Loop through all commands and execute them
	for (String cmd : getConfig().getStringList("kick.commands")) {
	    getServer().dispatchCommand(sender,
		    ChatColor.translateAlternateColorCodes('&', cmd).replaceAll("%player%", p.getName()));

	    if (debug)
		System.out.println("4 " + p.getName());
	}
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
	Player p = e.getPlayer();

	if (e.getMessage().toLowerCase().startsWith("/afk")) {
	    if (getConfig().getBoolean("anti-spam.enabled")) {
		if (lastUsed.containsKey(p)) {
		    long between = System.currentTimeMillis() - lastUsed.get(p);
		    long cooldown = (1000 * getConfig().getInt("anti-spam.cooldown"));
		    String timeLeft = String.valueOf((int) ((cooldown - between) / 1000));

		    if (between < cooldown) {
			p.sendMessage(getMessage("messages.anti-spam").replaceAll("%timeLeft%", timeLeft));
			e.setCancelled(true);
		    } else {
			lastUsed.remove(p);
		    }
		} else {
		    lastUsed.put(p, System.currentTimeMillis());
		}
	    }
	} else {
	    // If this listener is enabled
	    if (getConfig().getBoolean("listeners.commands"))
		// Removes player from AFK
		delAFK(e.getPlayer());
	}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (cmd.getName().equalsIgnoreCase("afk")) {
	    // If the sender isn't a player
	    if (!(sender instanceof Player)) {
		// Sens warning
		sender.sendMessage(getMessage("messages.no-console"));
		return true;
	    }

	    Player p = (Player) sender;

	    // If the player is in AFK mode
	    if (afkList.containsKey(p)) {
		// Then removes the AFK mode
		delAFK(p);

		// But if the player is not in AFK mode
	    } else {
		// Then adds enough AFK minutes to the player
		afkMinutes.put(p, getAutoTime(p));

		// And activates the AFK mode
		addAFK(p);
	    }
	} else if (cmd.getName().equalsIgnoreCase("autoafk")) {
	    // Generates new config if it doesn't exist
	    saveDefaultConfig();

	    // Reloads config
	    reloadConfig();

	    // Sends confirmation message
	    sender.sendMessage(getMessage("messages.reload"));
	}

	return true;
    }

    String getMessage(String msg) {
	return ChatColor.translateAlternateColorCodes('&', getConfig().getString(msg));
    }

    // Gets the player's minutes
    int getTime(Player p) {
	if (afkMinutes.containsKey(p)) {
	    return afkMinutes.get(p);
	} else {
	    return 0;
	}
    }

    // Adds a minute to player's count
    void addMinute(Player p) {
	if (afkMinutes.containsKey(p)) {
	    afkMinutes.put(p, afkMinutes.get(p) + 1);
	} else {
	    afkMinutes.put(p, 1);
	}
    }

    // Starts AFK countdown
    void autoAFK(Player p) {
	// if countdown is enabled and server version is above 1.8
	// TODO should be working with 1.8 as well (TitleAPI)
	if (getConfig().getBoolean("countdown.enabled") && !getServer().getVersion().contains("1.8")) {
	    // initiate countdown
	    CountdownTask task = new CountdownTask(this, p);
	    task.setId(getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 0, 20));

	    countList.put(p, task);
	} else {
	    addAFK(p);
	}
    }

    // Puts player into AFK mode
    void addAFK(Player p) {
	// When tablist name changing is enabled
	if (getConfig().getBoolean("tablist.enabled")) {
	    // Saves old tablist name into a map
	    oldPlayerListNames.put(p, p.getPlayerListName());

	    // Adds prefix in front of the tablist name
	    p.setPlayerListName(getMessage("tablist.prefix") + p.getName());
	}

	// Broadcasts the message
	broadcast(p, "messages.afk-on");

	// initiate particles when server version is above 1.8
	// TODO should be working with 1.8 as well (ParticleAPI)
	ParticleTask task = null;
	if (getConfig().getBoolean("particles.enabled") && !getServer().getVersion().contains("1.8")) {
	    task = new ParticleTask(this, p);
	    task.setId(getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 0, 20));
	}

	// add to list
	afkList.put(p, task);
    }

    // Removes player from AFK mode
    void delAFK(Player p) {
	// If player is in AFK mode
	if (afkList.containsKey(p)) {
	    // Resets the playerlist name
	    p.setPlayerListName(oldPlayerListNames.get(p));

	    // Broadcasts the message
	    broadcast(p, "messages.afk-off");
	}

	// Abort player's tasks
	abortTasks(p);

	// Resets the counter
	resetTime(p);
    }

    void abortTasks(Player p) {
	// If player is in AFK
	if (afkList.containsKey(p)) {
	    // If particle task is running
	    if (afkList.get(p) != null) {
		// Cancels particle task
		getServer().getScheduler().cancelTask(afkList.get(p).getId());
	    }

	    // Removes from AFK list
	    afkList.remove(p);
	}

	// If player is in countdown list
	if (countList.containsKey(p)) {
	    // If countdown is running
	    if (countList.get(p) != null) {
		// Abort counting
		getServer().getScheduler().cancelTask(countList.get(p).getId());
	    }

	    // Removes from countdown list
	    countList.remove(p);
	}
    }

    // Returns player specific auto time
    int getAutoTime(Player p) {
	// Gets default auto time
	int time = getConfig().getInt("auto.time");

	// Gets list of custom permissions
	Set<String> list = getConfig().getConfigurationSection("permissions").getKeys(false);

	// Loops through the list
	for (String perm : list) {
	    if (p.hasPermission("autoafk.custom." + perm))
		return getConfig().getInt("permissions." + perm + ".auto");
	}

	return time;
    }

    // Returns player specific kick time
    int getKickTime(Player p) {
	// Gets default auto time
	int time = getConfig().getInt("kick.time");

	// Gets list of custom permissions
	Set<String> list = getConfig().getConfigurationSection("permissions").getKeys(false);

	// Loops through the list
	for (String perm : list) {
	    if (p.hasPermission("autoafk.custom." + perm))
		time = getConfig().getInt("permissions." + perm + ".kick");
	    break;
	}

	return time + getAutoTime(p);
    }

    // Broadcasts specified message
    void broadcast(Player p, String type) {
	// Doesn't broadcast anything when player is vanished
	if (isVanished(p)) {

	}

	// Prepares the message
	String msg = getMessage(type).replaceAll("%player%", p.getName());

	// If messages.broadcast option is true
	if (getConfig().getString("messages.broadcast").equalsIgnoreCase("true")) {
	    // Broadcasts afk message to everyone
	    getServer().broadcastMessage(msg);

	    // If the broadcast option is set to private
	} else if (getConfig().getString("messages.broadcast").equalsIgnoreCase("private")) {
	    // Sends the message only to the player
	    p.sendMessage(msg);
	}
    }

    void resetTime(Player p) {
	// Reset player's minutes
	afkMinutes.put(p, 0);
    }

    // https://www.spigotmc.org/resources/supervanish-be-invisible.1331/
    // This code is supported by SuperVanish, PremiumVanish, VanishNoPacket and a
    // few more vanish plugins.
    private boolean isVanished(Player player) {
	for (MetadataValue meta : player.getMetadata("vanished")) {
	    if (meta.asBoolean())
		return true;
	}
	return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
	// Reset player's minutes
	resetTime(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
	// Remove player from minutes list
	afkMinutes.remove(e.getPlayer());

	// Abort player's tasks
	abortTasks(e.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.chat"))
	    // Removes player from AFK
	    getServer().getScheduler().runTask(this, new Runnable() {
		@Override
		public void run() {
		    delAFK(e.getPlayer());
		}
	    });
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.interact.entity"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.interact.anything"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.bed-enter"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.world-change"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.book-edit"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.item.drop"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.item.pickup"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.item.break"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.shear"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.toggle.flight"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.toggle.sprint"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.toggle.sneak"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.unleash"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.bucket.fill"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.bucket.empty"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.move"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent e) {
	// If this listener is enabled
	if (getConfig().getBoolean("listeners.xp"))
	    // Removes player from AFK
	    delAFK(e.getPlayer());
    }
}
