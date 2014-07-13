package se.zephyyrr.minecraft.tod.cinnahorse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CinnaHorse {
	
	static FileConfiguration config;
	
	public static Horse getRandomHorse(Player p) {
		Horse h = spawnHorse(p.getLocation());
		h.setOwner(p);
		h.setPassenger(p);
		h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2));
		return h;
	}
	
	public static Horse getHorse(Player p) {
		Horse h = spawnHorse(p.getLocation(), getVariant(p), getStyle(p), getColor(p));
		h.setJumpStrength(getJumpStrength(p));
		h.setMaxHealth(getMaxHealth(p));
		h.setHealth(getMaxHealth(p));

		h.setOwner(p);
		h.setCustomName(getName(p));
		h.setPassenger(p);
		h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2));

		return h;
	}
	
	private static Horse spawnHorse(Location l) {
		Horse h = (Horse) l.getWorld().spawnEntity(l,
				EntityType.HORSE);
		h.setAdult();
		h.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		h.setRemoveWhenFarAway(true);
		return h;
	}
	
	private static Horse spawnHorse(Location l, Horse.Variant variant, Horse.Style style, Horse.Color color) {
		Horse h = spawnHorse(l);
		h.setVariant(variant);
		h.setColor(color);
		h.setStyle(style);
		return h;
	}
	
	public static String getName(Player p) {
		return config.getString("Players." + p.getName() + ".Name",
				config.getString("Defaults.Name", "Horse"));
	}
	
	public static double getMaxHealth(Player p) {
		return config.getDouble("Players." + p.getName() + ".Health",
				config.getDouble("Defaults.Health", 20));
	}

	public static Horse.Style getStyle(Player p) {
		Horse.Style style;
		String sstyle = config.getString(
				"Players." + p.getName() + ".Style",
				config.getString("Defaults.Style", "NONE"));
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

	public static Horse.Color getColor(Player p) {
		Horse.Color color;
		String scolor = config.getString(
				"Players." + p.getName() + ".Color",
				config.getString("Defaults.Color", "CHESTNUT"));
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

	public static Variant getVariant(Player p) {
		Variant var;
		String svar = config.getString(
				"Players." + p.getName() + ".Variant",
				config.getString("Defaults.Variant", "HORSE"));
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
	
	public static double getJumpStrength(Player p) {
		return config.getDouble("Players." + p.getName() + ".JumpStrength",
				config.getDouble("Defaults.JumpStrength", 1));
	}

	// TODO Write handler for when player dismounts horse

}
