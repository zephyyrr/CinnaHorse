package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.InsufficientResourcesException;

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
		CinnaHorse.config = getConfig();
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
		rentals.revoke();
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
		case "time":
			return rentalTimelimit(sender, args);
		}

		return false;
	}

	private boolean rentalTimelimit(CommandSender sender, String[] args) {
		if (!sender.hasPermission("cinnahorse.time")) {
			sender.sendMessage("Not enough permissions to check timelimits.");
			return true;
		}
		Player p = getTarget(sender, args);
		if (p == null) {
			return false;
		}

		if (!p.equals(sender) && !p.hasPermission("cinnahorse.time.others")) {
			sender.sendMessage("Not enough permissions to check timelimits of others.");
			return true;
		}
		if (rentals.isRenting(p)) {
			sender.sendMessage(p.getName() + " has "
					+ rentals.getRemainingTime(p) / 1000
					+ " seconds remaining.");
		} else {
			sender.sendMessage(p.getName() + " is currently not renting anything.");
		}
		return true;
	}

	private boolean rentHorse(CommandSender sender, String[] args) {
		Player p = null;
		int minutes = 0;
		if (args.length > 2) {
			p = getTarget(sender, args);
			minutes = Integer.parseInt(args[2]);
		} else if (sender instanceof Player && args.length > 1) {
			p = (Player) sender;
			minutes = Integer.parseInt(args[1]);
		} else {
			return false;
		}
		if (p == null) {
			return false;
		}
		if (p != sender && !sender.hasPermission("cinnahorse.rent.others")) {
			sender.sendMessage("Not enough permissions to do rent horses for others.");
			return true;
		}

		if (sender.hasPermission("cinnahorse.rent")) {
			if (!rentals.isRenting(p)) {
				try {
					rentals.rent(p, p, minutes);
					return true;
				} catch (InsufficientResourcesException e) {
					return true;
				} catch (RuntimeException e) {
					sender.sendMessage("Unable to process payment. Please check your funds.");
					return true;
				}
			} else {
				try {
					rentals.increaseTimelimit(p, p, minutes);
				} catch (InsufficientResourcesException e) {
					return true;
				}
			}
		} else {
			sender.sendMessage("Insufficient permissions to rent horses.");
		}
		return true;
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
		if (!sender.hasPermission("cinnahorse.list")) {
			sender.sendMessage("Not enough permissions to list horses.");
			return true;
		}

		Player p = getTarget(sender, args);
		if (p == null) {
			return false;
		}

		if (p != sender && !sender.hasPermission("cinnahorse.list.others")) {
			sender.sendMessage("Not enough permissions to list others horses.");
			return true;
		}

		sender.sendMessage("| Horse of " + p.getName());
		sender.sendMessage("| Name: " + CinnaHorse.getName(p));
		sender.sendMessage("| Health: " + CinnaHorse.getMaxHealth(p));
		sender.sendMessage("| Jump Strength: " + CinnaHorse.getJumpStrength(p));

		Horse.Variant variant = CinnaHorse.getVariant(p);
		sender.sendMessage("| Variant: " + variant);
		if (variant == Horse.Variant.HORSE) {
			sender.sendMessage("| Style: " + CinnaHorse.getStyle(p));
			sender.sendMessage("| Color: " + CinnaHorse.getColor(p));
		}
		return true;
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
		reloadConfig();
		CinnaHorse.config = getConfig();
		rentals.setConfig(getConfig().getConfigurationSection("Rental"));
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
		if (rentals.isRenting(p)) {
			rentals.revoke(p);
		}
	}
}