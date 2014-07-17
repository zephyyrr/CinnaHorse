package se.zephyyrr.minecraft.tod.cinnahorse;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InsufficientResourcesException;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RentalAgency {
	private Map<Player, Horse> rentals;
	private Map<Player, Calendar> timelimits;
	private Map<Player, Double> deposits;

	private Economy econ;
	private boolean free; // If no economy provider is available, set everything
							// free.
	private ConfigurationSection config;

	public RentalAgency(Plugin p) {
		config = p.getConfig().getConfigurationSection("Rental");
		rentals = new HashMap<Player, Horse>();
		timelimits = new HashMap<Player, Calendar>();
		deposits = new HashMap<Player, Double>();
		if (config.getBoolean("Economy", false)) {
			RegisteredServiceProvider<Economy> rsp = p.getServer()
					.getServicesManager().getRegistration(Economy.class);
			if (rsp == null) {
				p.getLogger().info("No Economy provider found.");
				free = true;
			} else {
				econ = rsp.getProvider();
				if (econ == null) {
					p.getLogger()
							.info("The economy provider failed to provide a valid economy handler.");
					free = true;
				}
				p.getLogger().info(
						"Found economy handler " + econ.getName() + ".");
			}
		} else {
			p.getLogger().info("Disabling economics of rentals.");
			free = true;
		}

		if (free) {
			p.getLogger().warning("Disabling economics of rentals.");
		}

		p.getServer().getScheduler().runTaskTimer(p, () -> {
			// Loop over all rentals
				for (Map.Entry<Player, Calendar> e : timelimits.entrySet()) {
					// If timelimit expired
				if (getRemainingTime(e.getKey()) < 1) {
					// "Return" horse.
					revoke(e.getKey());
				}
			}
		}, 10, 10);
	}

	public Horse rent(Player rider, int ticks)
			throws InsufficientResourcesException {
		return rent(rider, rider, ticks);
	}

	public Horse rent(Player rider, CommandSender payer, int minutes)
			throws InsufficientResourcesException {
		//Check for all requirements
		
		if (!free && payer instanceof Player) {
			// Only players are supported payers
			Player ppayer = (Player) payer;
			boolean payDeposit = rider.equals(payer);
			
			if (!econ.has(ppayer, getRentCost(minutes) + (payDeposit ? getDeposit() : 0))) {
				payer.sendMessage("Not enough money to pay the rent.");
				payer.sendMessage("The cost is " + getRentCost(minutes)
						+ " and " + getDeposit() + " in deposit.");
				throw new InsufficientResourcesException();
			}

			if (getRentCost(minutes) > 0) {
				EconomyResponse resp = econ.withdrawPlayer(ppayer,
						getRentCost(minutes));
				if (!resp.transactionSuccess()) {
					throw new RuntimeException("Unsuccessfull payment transaction.");
				}
			}

			if (getDeposit() > 0 && payDeposit) {
				EconomyResponse resp = econ
						.withdrawPlayer(ppayer, getDeposit());
				if (!resp.transactionSuccess()) {
					throw new RuntimeException("Unsuccessfull deposit transaction.");
				}
				deposits.put(rider, getDeposit());
			}
		}

		Horse h = null;
		if (isRenting(rider)) {
			h = rentals.get(rider);
		} else {
			// TODO check for and grab a saddle from player if config says so.
			if (rider.getInventory().contains(Material.SADDLE)
					&& !config.getBoolean("FreeSaddles", false)) {
				
				int pos = rider.getInventory().first(Material.SADDLE);
				ItemStack saddles = rider.getInventory().getItem(pos);
				if (saddles.getAmount() == 1) {
					rider.getInventory().clear(pos);
				} else {
					saddles.setAmount(saddles.getAmount() - 1);
				}
			} else {
				payer.sendMessage("You need to bring you own saddle.");
				refund(rider, payer, minutes);
				throw new InsufficientResourcesException("saddle");
			}
			h = CinnaHorse.getRandomHorse(rider);
			rentals.put(rider, h);
		}

		Calendar limit = Calendar.getInstance();
		limit.add(Calendar.MINUTE, minutes);
		timelimits.put(rider, limit);

		return h;
	}
	
	public void increaseTimelimit(Player rider, Player payer, int minutes) throws InsufficientResourcesException {
		if (!timelimits.containsKey(rider)) {
			return;
		}
		if (!free) {			
			if (!econ.has(payer, getRentCost(minutes))) {
				payer.sendMessage("Not enough money to pay the rent.");
				payer.sendMessage("The cost is " + getRentCost(minutes) + " " + econ.currencyNamePlural() + ".");
				throw new InsufficientResourcesException();
			}

			if (getRentCost(minutes) > 0) {
				EconomyResponse resp = econ.withdrawPlayer(payer,
						getRentCost(minutes));
				if (!resp.transactionSuccess()) {
					throw new RuntimeException("Unsuccessfull payment transaction.");
				}
			}
		}
		
		timelimits.get(rider).add(Calendar.MINUTE, minutes);
	}

	private void refund(Player rider, CommandSender payer, int minutes) {
		if (!free && payer instanceof Player) {
			econ.depositPlayer((Player) payer, getRentCost(minutes) + 
				(deposits.containsKey(rider) ? deposits.get(rider) : 0));
		}
	}

	public double getRentCost(int minutes) {
		return config.getDouble("Cost", 1)*minutes;
	}

	public double getDeposit() {
		return config.getDouble("Deposit", 0);
	}

	public boolean isRenting(Player p) {
		if (rentals.containsKey(p)) {
			return true;
		}
		return false;
	}

	public void revoke() {
		Set<Player> renting = new HashSet<Player>(rentals.keySet());
		for (Player p : renting) {
			revoke(p);
		}
	}

	public void revoke(Player p) {
		p.sendMessage("Your horse rental has expired.");
		if (deposits.containsKey(p) && !rentals.get(p).isDead()) {
			econ.depositPlayer(p, deposits.get(p));
		}
		if (rentals.get(p).getInventory().getSaddle().getType() == Material.SADDLE) {
			int pos = p.getInventory().firstEmpty();
			if (pos != -1) {
				p.getInventory().setItem(pos, new ItemStack(Material.SADDLE));
			}
		}
		deposits.remove(p);
		rentals.get(p).setHealth(0);
		rentals.remove(p);
		timelimits.remove(p);
	}

	public long getRemainingTime(Player p) {
		if (timelimits.containsKey(p)) {
			return timelimits.get(p).getTimeInMillis()
					- Calendar.getInstance().getTimeInMillis();
		} else {
			return 0;
		}
	}
}
