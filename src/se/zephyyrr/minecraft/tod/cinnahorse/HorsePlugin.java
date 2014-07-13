package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HorsePlugin extends JavaPlugin implements Listener {
	public Map<Player, Horse> horses;
	public RentalAgency rentals;

	@Override
	public void onEnable() {
		getLogger().info("Initializing CinnaHorse");
		horses = new HashMap<Player, Horse>();
		rentals = new RentalAgency(this);
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		getLogger().info("Shutting down CinnaHorse");
		Iterator<Entry<Player, Horse>> iter = horses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Player, Horse> entry = iter.next();
			entry.getValue().setHealth(0); // Kill the beasts!
			iter.remove();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (args.length < 1) {
			return false;
		}

		switch (args[0].toLowerCase()) {
		case "summon":
			return summonHorse(sender, args);
		case "reload":
			return reloadConfiguration();
		case "set":
			return setHorse(sender, args);
		case "list":
			return listHorse(sender, args);
		case "rent":
			return rentHorse(sender, args);
		}

		return false;
	}

	private boolean rentHorse(CommandSender sender, String[] args) {
		Player p = getTarget(sender, args);
		if (p == null) {
			return false;
		}
		rentals.rent(p, 8000); //TODO read limit from args and adjust cost.
		return false;
	}

	private Player getTarget(CommandSender sender, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (args.length > 1) {
			p = this.getServer().getPlayer(args[1]);
			if (p == null) {
				sender.sendMessage("Player not found.");
				return null;
			}
		}

		return p;
	}

	private boolean listHorse(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean setHorse(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean summonHorse(CommandSender sender, String[] args) {
		Player p = getTarget(sender, args);
		if (p == null) {
			return false;
		}

		if (sender.hasPermission("cinnahorse.summon")) {
			if (!horses.containsKey(p) || horses.get(p).isDead()) {
				Horse h = CinnaHorse.getHorse(p);
				horses.put(p, h);
			} else {
				horses.get(p).setHealth(0);
				horses.remove(p);
			}
			return true;
		} else {
			sender.sendMessage("Sorry, not enough permissions.");
		}
		return false;
	}

	private boolean reloadConfiguration() {
		reloadConfiguration();
		return true;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		getLogger().info(
				"Removing " + p.getName() + "'s horse due to quitting.");
		if (horses != null && horses.containsKey(p)) {
			horses.get(p).setHealth(0);
			horses.remove(p);
		}
	}
}