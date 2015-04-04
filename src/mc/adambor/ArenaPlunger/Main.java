package mc.adambor.ArenaPlunger;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class Main extends JavaPlugin {
	static Main plugin;
	@Override
	public void onEnable(){
		plugin = this;
		BattleArena.registerCompetition(this, "ArenaPlunger", "ap", ArenaPlunger.class, new AP_commandHandler());
		loadConfig();
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " enabled!");
	}

	@Override
	public void onDisable(){
		Log.info("[" + getName()+ "] v" + getDescription().getVersion()+ " stopping!");
	}
    @Override
	public void loadConfig(){
        FileConfiguration conf = getConfig();
		ArenaPlunger.material = Material.valueOf(conf.getString("ArenaPlunger.plunger.material", "TORCH").toUpperCase());
		ArenaPlunger.effect = Effect.valueOf(conf.getString("ArenaPlunger.plunger.effect", "NOTE"));
	}
	@Override
	public void reloadConfig(){
	    loadConfig();
	.	super.reloadConfig();
	}
}
