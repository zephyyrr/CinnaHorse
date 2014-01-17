package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CinnaHorse extends JavaPlugin {
	
	Map<Player, Horse> horses;
	
	@Override
	public void onEnable() {
		horses = new HashMap<Player, Horse>();
	}
	
	@Override
	public void onDisable() {
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
			if (!horses.containsKey(sender))
				spawnHorse(p);
			else {
				horses.get(p).setHealth(0);
				horses.remove(p);
			}
		}
		return false;
	}

	private void spawnHorse(Player p) {
		Horse h = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
		h.setAdult();
		h.setOwner(p);
		h.setVariant(Variant.SKELETON_HORSE);
		h.setJumpStrength(2);
		h.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2000, 2));
		h.setMaxHealth(100);
		h.setHealth(100);
		h.setRemoveWhenFarAway(true);
		h.setCustomName("Shi");
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
