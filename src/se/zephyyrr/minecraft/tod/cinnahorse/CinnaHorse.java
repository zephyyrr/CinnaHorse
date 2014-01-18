package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CinnaHorse extends JavaPlugin implements Listener {

	Map<Player, Horse> horses;

	@Override
	public void onEnable() {
		getLogger().info("Initializing CinnaHorse");
		horses = new HashMap<Player, Horse>();
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
			return summon(sender, args);
		case "reload":
			return reload();
		}

		return false;
	}

	private boolean reload() {
		reloadConfig();
		return true;
	}

	private boolean summon(CommandSender sender, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (args.length > 1) {
			p = this.getServer().getPlayer(args[1]);
			if (p == null) {
				sender.sendMessage("Player not found.");
				return false;
			}
		}

		if (p == null) {
			return false;
		}

		if (sender.hasPermission("cinnahorse.summon")) {
			if (!horses.containsKey(p) || horses.get(p).isDead()) {
				Horse h = spawnHorse(p);
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

	private Horse spawnHorse(Player p) {
		Horse h = (Horse) p.getWorld().spawnEntity(p.getLocation(),
				EntityType.HORSE);
		h.setAdult();
		h.setJumpStrength(getConfig().getDouble("Players." + p.getName() + ".JumpStrength",
				getConfig().getDouble("Defaults.JumpStrength", 1)));

		h.setVariant(getVariant(p));
		h.setColor(getColor(p));
		h.setStyle(getStyle(p));

		h.setMaxHealth(getConfig().getDouble("Players." + p.getName() + ".Health",
				getConfig().getDouble("Defaults.Health", 10)));
		h.setHealth(getConfig().getDouble(p.getName() + ".Health",
				getConfig().getDouble("Defaults.Health", 10)));

		h.setOwner(p);
		h.setCustomName(getConfig().getString("Players." + p.getName() + ".Name",
				getConfig().getString("Defaults.Name", "Apple Jack")));
		h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		h.setPassenger(p);
		h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2000, 2));
		h.setRemoveWhenFarAway(true);

		return h;
	}

	private Horse.Style getStyle(Player p) {
		Horse.Style style;
		String sstyle = getConfig().getString(
				"Players." + p.getName() + ".Style",
				getConfig().getString("Defaults.Style", "NONE"));
		if (sstyle == null)
			return Style.NONE;
		switch (sstyle.toUpperCase()) {
		case "NONE":
			style = Style.NONE;
			break;
		case "BLACK_DOTS":
			style = Style.BLACK_DOTS;
			break;
		case "WHITE_DOTS":
			style = Style.WHITE_DOTS;
			break;
		case "WHITEFIELD":
			style = Style.WHITEFIELD;
			break;
		case "WHITE":
			style = Style.WHITE;
			break;
		default:
			style = Style.NONE;
			break;
		}
		return style;
	}

	private Horse.Color getColor(Player p) {
		Horse.Color color;
		String scolor = getConfig().getString(
				"Players." + p.getName() + ".Color",
				getConfig().getString("Defaults.Color", "CHESTNUT"));
		if (scolor == null)
			return Color.CHESTNUT;
		switch (scolor.toUpperCase()) {
		case "BROWN":
			color = Color.BROWN;
			break;
		case "CHESTNUT":
			color = Color.CHESTNUT;
			break;
		case "BLACK":
			color = Color.BLACK;
			break;
		case "CREAMY":
			color = Color.CREAMY;
			break;
		case "DARK_BROWN":
			color = Color.DARK_BROWN;
			break;
		case "WHITE":
			color = Color.WHITE;
			break;
		case "GRAY":
		case "GREY":
			color = Color.GRAY;
			break;

		default:
			color = Color.CHESTNUT;
			break;
		}
		return color;
	}

	private Variant getVariant(Player p) {
		Variant var;
		String svar = getConfig().getString(
				"Players." + p.getName() + ".Variant",
				getConfig().getString("Defaults.Variant", "HORSE"));
		switch (svar.toUpperCase()) {
		case "SKELETON_HORSE":
		case "SKELETON":
			var = Variant.SKELETON_HORSE;
			break;
		case "UNDEAD_HORSE":
		case "UNDEAD":
			var = Variant.UNDEAD_HORSE;
			break;
		case "DONKEY":
			var = Variant.DONKEY;
			break;
		case "MULE":
			var = Variant.MULE;
			break;
		case "HORSE":
			var = Variant.HORSE;
			break;
		default:
			var = Variant.HORSE;
			break;
		}
		return var;
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

	// TODO Write handler for when player dismounts horse

}
