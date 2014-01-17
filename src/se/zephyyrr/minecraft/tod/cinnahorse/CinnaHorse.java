package se.zephyyrr.minecraft.tod.cinnahorse;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.plugin.java.JavaPlugin;

public class CinnaHorse extends JavaPlugin {

	@Override
	public void onEnable() {
		getCommand("horse").setExecutor(this);
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
			Horse h = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
			h.setAdult();
			h.setOwner(p);
			h.setVariant(Variant.SKELETON_HORSE);
			h.setJumpStrength(2);
			h.setMaxHealth(100);
			h.setHealth(100);
			h.setRemoveWhenFarAway(true);
			h.setCustomName("Shi");
		}
		return false;
	}
	
}
