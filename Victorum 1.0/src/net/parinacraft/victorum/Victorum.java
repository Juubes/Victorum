package net.parinacraft.victorum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.parinacraft.victorum.commands.ClaimCommand;
import net.parinacraft.victorum.data.ClaimHandler;
import net.parinacraft.victorum.data.FactionHandler;
import net.parinacraft.victorum.data.PlayerDataHandler;
import net.parinacraft.victorum.data.RelationHandler;
import net.parinacraft.victorum.data.SQLManager;
import net.parinacraft.victorum.events.ChatListener;
import net.parinacraft.victorum.events.ChestOpenListener;
import net.parinacraft.victorum.events.ClaimBuildProtection;
import net.parinacraft.victorum.events.ClaimInvClickCanceller;
import net.parinacraft.victorum.events.ConnectionListener;
import net.parinacraft.victorum.events.MovementListener;
import net.parinacraft.victorum.http.MapServer;

public class Victorum extends JavaPlugin {
	private FactionHandler factionHandler;
	private PlayerDataHandler playerDataHandler;
	private RelationHandler relationHandler;
	private ClaimHandler claimHandler;
	private SQLManager sqlManager;
	private MapServer mapServer;
	public Economy economy;

	@Override
	public void onEnable() {
		// Reload?
		saveDefaultConfig();

		setupEconomy();

		getCommand("claim").setExecutor(new ClaimCommand(this));

		Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ChestOpenListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ClaimInvClickCanceller(this), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
		Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ClaimBuildProtection(this), this);

		// Prepare global options
		Opt.load(this);

		// Create databases
		sqlManager = new SQLManager(this);
		int defaultFaction = sqlManager.createDatabases();

		// Load all data from database to memory
		getLogger().info("Importing data from MySQL...");
		long start = System.nanoTime();
		this.factionHandler = new FactionHandler(this, defaultFaction);
		this.relationHandler = new RelationHandler(this);
		this.playerDataHandler = new PlayerDataHandler(this);
		this.claimHandler = new ClaimHandler(this);
		int timeMS = (int) ((System.nanoTime() - start) / 1E6);
		getLogger().info("Done (" + timeMS + "ms)");

		mapServer = new MapServer(this);
		mapServer.startAsync();
	}

	@Override
	public void onDisable() {
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
				net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public static Victorum getPlugin() {
		return (Victorum) Bukkit.getPluginManager().getPlugin("Victorum");
	}

	public FactionHandler getFactionHandler() {
		return this.factionHandler;
	}

	public RelationHandler getRelationHandler() {
		return relationHandler;
	}

	public ClaimHandler getClaimHandler() {
		return claimHandler;
	}

	public PlayerDataHandler getPlayerDataHandler() {
		return playerDataHandler;
	}

	public SQLManager getSqlManager() {
		return sqlManager;
	}

	public MapServer getMapServer() {
		return mapServer;
	}

	public static Victorum get() {
		return (Victorum) Bukkit.getPluginManager().getPlugin("Victorum");
	}
}
