package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CinnaHorse extends JavaPlugin {
	
	Map<Player, Horse> horses;
	
	@Override
	public void onEnable() {
		getLogger().info("Initializing CinnaHorse");
		horses = new HashMap<Player, Horse>();
	}
	
	@Override
	public void onDisable() {
		getLogger().info("Shutting down CinnaHorse");
		Iterator<Entry<Player, Horse>> iter = horses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Player, Horse> entry = iter.next();
			entry.getValue().setHealth(0); //Kill the beasts!
			iter.remove();
		}
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p;
		if (sender instanceof Player) {
			p = (Player) sender;
		} else if (args.length > 0) {
			p = this.getServer().getPlayer(args[0]);
			if (p == null) {
				sender.sendMessage("Player not found.");
				return false;
			}
		} else {
			return false;
		}
		
		if (sender.hasPermission("cinnahorse.summon")) {
			if (!horses.containsKey(sender)) {
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
		Horse h = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
		h.setAdult();
		h.setJumpStrength(2);
		h.setVariant(Variant.SKELETON_HORSE);
		h.setMaxHealth(100);
		h.setHealth(100);
		
		h.setOwner(p);
		h.setCustomName("Shi");
		h.getEquipment().setChestplate(new ItemStack(Material.SADDLE));
		h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2000, 2));
		h.setRemoveWhenFarAway(true);
		
		return h;
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		if (horses != null && horses.containsKey(p)) {
			horses.get(p).setHealth(0);
			horses.remove(p);
		}
	}
	
}
