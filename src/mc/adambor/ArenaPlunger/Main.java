package mc.adambor.ArenaPlunger;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	static Main plugin;
	@Override
	public void onEnable(){
		plugin = this;
		BattleArena.registerCompetition(this, "ArenaPlunger", "ap", ArenaPlunger.class, new AP_commandHandler());
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " enabled!");
	}

	@Override
	public void onDisable(){
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " stopping!");
	}

	@Override
	public void reloadConfig(){
		super.reloadConfig();
	}
}
