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
}
