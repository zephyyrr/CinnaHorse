package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RentalAgency {
	private Map<Player, Horse> rentals;
	private Map<Player, Calendar> timelimits;
	
	public RentalAgency(Plugin p) {
		rentals = new HashMap<Player, Horse>();
		timelimits = new HashMap<Player, Calendar>();
		p.getServer().getScheduler().runTaskTimer(p, () -> {
			Date now = new Date();
			// Loop over all rentals
			for (Map.Entry<Player, Calendar> e : timelimits.entrySet()) {
				//If timelimit expired
				if (e.getValue().after(now)) {
					//"Return" horse.
					rentals.get(e.getKey()).setHealth(0);
					rentals.remove(e.getKey());
					timelimits.remove(e.getKey());
				}
			}
		}, 10, 10);
	}
	
	public Horse rent(Player p, int ticks) {
		Horse h = null;
		if (rentals.containsKey(p)) {
			h = rentals.get(p);
			rentals.put(p, h);
		} else {
			h = CinnaHorse.getRandomHorse(p);
		}
		
		Calendar limit = Calendar.getInstance();
		limit.add(Calendar.SECOND, ticks*20);
		timelimits.put(p, limit);
		
		return h;
	}
	
	public boolean isRenting(Player p) {
		if (rentals.containsKey(p)) {
			return true;
		}
		return false;
	}
}
