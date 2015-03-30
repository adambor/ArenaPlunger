package mc.adambor.ArenaPlunger;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.messaging.MatchMessageHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.Persist;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

public class ArenaPlunger extends Arena {
	@Persist
    Location plungerloc;
	Item plunger;
	MatchMessageHandler mmh;
	ItemStack plungeritem;
	BukkitTask timerid;
	SScoreboard scoreb;
    SObjective objective;
    ArenaPlayer plungerholding;
    HashMap<ArenaTeam,SEntry> entry = new HashMap<ArenaTeam,SEntry>();
    SEntry holdingentry;
    Random rand = new Random();
    
	public void onOpen(){
		mmh = match.getMessageHandler();
	}
	public void onStart(){
		scoreb = match.getScoreboard();
		objective = scoreb.registerNewObjective("ap", "ArenaPlunger", ChatColor.GOLD+"Plunger", SAPIDisplaySlot.SIDEBAR);
		objective.setDisplayTeams(false);
		objective.setDisplayPlayers(false);
		for(ArenaTeam team : match.getTeams()){
			entry.put(team, objective.addEntry(team.getDisplayName(), 0));
		}
		objective.addEntry("Holding plunger:", -1);
		holdingentry = objective.addEntry("-", -2);
		
		plungeritem = new ItemStack(Material.TORCH);
		ItemMeta im = plungeritem.getItemMeta();
		im.setDisplayName(ChatColor.GOLD+"Plunger");
		plungeritem.setItemMeta(im);
		plunger = plungerloc.getWorld().dropItem(plungerloc, plungeritem);
		plunger.setCustomName(ChatColor.GOLD+"Plunger");
		plunger.setCustomNameVisible(true);
		timerid = Bukkit.getScheduler().runTaskTimer(Main.plugin, new Runnable(){
			@Override
			public void run() {
				if(plungerholding != null){
					objective.setPoints(entry.get(plungerholding.getTeam()),objective.getPoints(entry.get(plungerholding.getTeam()))+1);
					flames(plungerholding.getLocation());
				} else {
					flames(plunger.getLocation());
				}
			}
		}, 0, 20L);
	}
	public void flames(Location location) {
		for(int x=0;x<11;x++){
			location = location.add(rand.nextDouble()-0.5, rand.nextDouble()-0.5, rand.nextDouble()-0.5);
			location.getWorld().playEffect(location, Effect.NOTE, 10);
		}
	}
	public void onCancel(){
		timerid.cancel();
		plunger.remove();
	}
	public void onVictory(){
		timerid.cancel();
		plunger.remove();
	}
	@ArenaEventHandler
	public void onPlace(BlockPlaceEvent e){
		if(e.getBlock().getType().equals(Material.TORCH)) e.setCancelled(true);
	}
	@ArenaEventHandler
	public void onPickup(PlayerPickupItemEvent e){
		if(e.getItem().equals(plunger)){
			plungerholding = BattleArena.toArenaPlayer(e.getPlayer());
			scoreb.setEntryDisplayName(holdingentry, BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getTeamChatColor()+e.getPlayer().getName());
			plunger = null;
			match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_picked").replace("%p", e.getPlayer().getName()).replace("%t", BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getDisplayName()));
		}
	}
	@ArenaEventHandler
	public void onDrop(PlayerDropItemEvent e){
		if(e.getItemDrop().getItemStack().equals(plungeritem)){
		    plungerholding = null;
		    scoreb.setEntryDisplayName(holdingentry,"-");
		    e.getItemDrop().setCustomName(ChatColor.GOLD+"Plunger");
		    e.getItemDrop().setCustomNameVisible(true);
		    plunger = e.getItemDrop();
		    match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_dropped").replace("%p", e.getPlayer().getName()).replace("%t", BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getDisplayName()));
		}
	}
	@ArenaEventHandler
	public void onDeath(final PlayerDeathEvent e){
		if(e.getEntity().getInventory().contains(plungeritem)){
			plunger = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), plungeritem);
		    plunger.setCustomName(ChatColor.GOLD+"Plunger");
		    plunger.setCustomNameVisible(true);
		    plungerholding = null;
		    scoreb.setEntryDisplayName(holdingentry,"-");
		    match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_dropped").replace("%p", e.getEntity().getName()).replace("%t", BattleArena.toArenaPlayer(e.getEntity()).getTeam().getDisplayName()));
			e.setKeepInventory(true);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
				public void run(){
				e.getEntity().getInventory().remove(plungeritem);
				}
			}, 2L);
		}
	}
	@ArenaEventHandler(needsPlayer = false)
	public void itemDeath(EntityDeathEvent e){
		if(e.getEntity().equals(plunger)){
			plunger = plungerloc.getWorld().dropItem(plungerloc, plungeritem);
			plunger.setCustomName(ChatColor.GOLD+"Plunger");
			plunger.setCustomNameVisible(true);
			match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_broken"));
		}
	}
	@ArenaEventHandler(needsPlayer = false)
	public void onDespawn(ItemDespawnEvent e){
		if(e.getEntity().equals(plunger)){
			e.setCancelled(true);
		}
	}
	public void setPlunger(Location location) {
		plungerloc = location;
	}
}