package mc.adambor.ArenaPlunger;

import org.bukkit.entity.Player;

import mc.alk.arena.BattleArena;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.MCCommand;

public class AP_commandHandler extends CustomCommandExecutor {
	@MCCommand(cmds={"setPlunger"}, admin=true)
	public static boolean addPlunger(Player sender, ArenaPlunger arena) {
		arena.setPlunger(sender.getLocation());
		BattleArena.saveArenas(Main.plugin);
		return sendMessage(sender,"&2Plunger set!");
	}
	@MCCommand(cmds={"setMaterial"}, admin=true)
	public static boolean setMaterial(Player sender){
	    String material = sender.getItemInHand().getType().name();
		Main.plugin.getConfig().set("ArenaPlunger.plunger.material", material);
		Main.plugin.saveConfig();
	    return sendMessage(sender, "&2Plunger item set to: "+material);
	}
	@MCCommand(cmds={"setEffect"}, admin=true)
	public static boolean setEffect(Player sender, String effect){
		Main.plugin.getConfig().set("ArenaPlunger.plunger.effect", effect);
		Main.plugin.saveConfig();
		return sendMessage(sender, "&2Plunger effect set to: "+effect);
	}
}
