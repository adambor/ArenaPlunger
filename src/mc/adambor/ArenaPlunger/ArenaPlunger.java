package mc.adambor.ArenaPlunger;

import mc.adambor.ArenaPlunger.Victory;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.messaging.MatchMessageHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.serializers.Persist;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;

public class ArenaPlunger extends Arena {
	@Persist
    Location plungerloc;
	Item plunger;
	static Material material;
	static Effect effect;
	static Integer count;
	MatchMessageHandler mmh;
	ItemStack plungeritem;
	BukkitTask timerid1;
	BukkitTask timerid2;
	SScoreboard scoreb;
    SObjective objective;
    ArenaPlayer plungerholding;
    Random rand = new Random();
    static Integer maxdroppedtime;
    Integer pltimer = maxdroppedtime;
    BukkitTask plungertimer;
    SEntry holdingentry;
    Victory scores;
    static Integer pointsToWin = 100;
    
	public void onOpen(){
		mmh = match.getMessageHandler();
        VictoryCondition vc = getMatch().getVictoryCondition(Victory.class);
        scores = (Victory) (vc != null ? vc : new Victory(getMatch()));
        getMatch().addVictoryCondition(scores);
    }
	public void onStart(){
		scoreb = match.getScoreboard();
		objective = scores.scores;
		objective.setDisplayName(ChatColor.GOLD+"Plunger");
		objective.setDisplayPlayers(false);
		objective.addEntry(ChatColor.GOLD+"Plunger:", -1);
		holdingentry = objective.addEntry("-", -2);
		
		plungeritem = new ItemStack(material);
		ItemMeta im = plungeritem.getItemMeta();
		im.setDisplayName(ChatColor.GOLD+"Plunger");
		plungeritem.setItemMeta(im);
		plunger = plungerloc.getWorld().dropItem(plungerloc, plungeritem);
		plunger.setVelocity(new Vector(0,0,0));
		plunger.setCustomName(ChatColor.GOLD+"Plunger");
		plunger.setCustomNameVisible(true);
		timerid1 = new BukkitRunnable(){
			@Override
			public void run() {
				if(plungerholding != null){
					if(plungerholding.isOnline() == true){
					if(scores.addScore(plungerholding.getTeam(), plungerholding) >= pointsToWin){
						setWinner(plungerholding.getTeam());
						onCancel();
					}
					flames(plungerholding.getPlayer().getEyeLocation().subtract(0, 0.5, 0));
					}
				} else {
					flames(plunger.getLocation().add(0,0.5D,0));
				}
			}
		}.runTaskTimer(Main.plugin, 0, 20L);
		timerid2 = new BukkitRunnable(){
			@Override
			public void run() {
                updateCompassLocation();
			}
		}.runTaskTimer(Main.plugin, 0, 20L);
	}
	public void flames(Location location) {
		for(int x=0;x<count;x++){
			Location loc = location.clone();
			loc = loc.add(rand.nextDouble()-0.5, rand.nextDouble()-0.5, rand.nextDouble()-0.5);
			location.getWorld().playEffect(loc, effect, 10);
		}
	}
	@Override
	public void onCancel(){
		timerid1.cancel();
		timerid2.cancel();
		removePlunger();
		stopPlungerTimer();
	}
    @Override
    public void onComplete() {
        super.onComplete();
		timerid1.cancel();
		timerid2.cancel();
		removePlunger();
		stopPlungerTimer();
    }
	public void removePlunger(){
		if(plunger != null){
			plunger.remove();
		}
	}
	public void updateCompassLocation(){
		Location loc = null;
		if(plunger != null){
			loc = plunger.getLocation();
		} else {
			if(plungerholding != null){
				loc = plungerholding.getLocation();
			}
		}
		for(ArenaTeam team : match.getTeams()){
			for(ArenaPlayer ap : team.getLivingPlayers()){
			        ap.getPlayer().setCompassTarget(loc);
			}
		}
	}
	@ArenaEventHandler(needsPlayer=false)
	public void onPlace(BlockPlaceEvent e){
		if(e.getBlockPlaced().getType().equals(material)){
			e.setCancelled(true);
		}
	}
	@ArenaEventHandler
	public void onPickup(PlayerPickupItemEvent e){
		if(e.getItem().equals(plunger)){
			plungerholding = BattleArena.toArenaPlayer(e.getPlayer());
			scoreb.setEntryDisplayName(holdingentry, BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getTeamChatColor()+e.getPlayer().getName());
			plunger = null;
			match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_picked").replace("%p", e.getPlayer().getName()).replace("%t", BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getDisplayName()));
			stopPlungerTimer();
		}
	}
	@ArenaEventHandler
	public void onDrop(PlayerDropItemEvent e){
		ItemStack item = e.getItemDrop().getItemStack().clone();
		item.setAmount(1);
		if(item.equals(plungeritem)){
		    plungerholding = null;
		    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"dropped");
		    startPlungerTimer();
		    e.getItemDrop().setCustomName(ChatColor.GOLD+"Plunger");
		    e.getItemDrop().setCustomNameVisible(true);
		    plunger = e.getItemDrop();
		    match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_dropped").replace("%p", e.getPlayer().getName()).replace("%t", BattleArena.toArenaPlayer(e.getPlayer()).getTeam().getDisplayName()));
		}
	}
    public void startPlungerTimer(){
	    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"dropped - "+maxdroppedtime);
    	plungertimer = new BukkitRunnable(){
    		@Override
    		public void run(){
    			pltimer--;
    		    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"dropped - "+pltimer);
    			if(pltimer == 0){
    				if(plunger!=null) plunger.remove();
    				plunger = plungerloc.getWorld().dropItem(plungerloc, plungeritem);
    				plunger.setCustomName(ChatColor.GOLD+"Plunger");
    				plunger.setCustomNameVisible(true);
    				match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_timer"));
        		    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"at spawn");
    				stopPlungerTimer();
    			}
    		}
    	}.runTaskTimer(Main.plugin, 20L, 20L);
    }
    public void stopPlungerTimer(){
    	if(plungertimer != null){
    	    plungertimer.cancel();
    	}
    	pltimer = maxdroppedtime;
    }
	@ArenaEventHandler
	public void onDeath(final PlayerDeathEvent e){
		if(e.getEntity().getInventory().contains(plungeritem)){
			plunger = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), plungeritem);
		    plunger.setCustomName(ChatColor.GOLD+"Plunger");
		    plunger.setCustomNameVisible(true);
		    plungerholding = null;
		    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"dropped");
		    startPlungerTimer();
		    match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_dropped").replace("%p", e.getEntity().getName()).replace("%t", BattleArena.toArenaPlayer(e.getEntity()).getTeam().getDisplayName()));
		}
	}
	@ArenaEventHandler(needsPlayer = false)
	public void itemDeath(EntityDamageByBlockEvent e){
		if(e.getEntity().equals(plunger)){
			stopPlungerTimer();
			plunger.remove();
			plunger = plungerloc.getWorld().dropItem(plungerloc, plungeritem);
			plunger.setCustomName(ChatColor.GOLD+"Plunger");
			plunger.setCustomNameVisible(true);
		    scoreb.setEntryDisplayName(holdingentry,ChatColor.ITALIC+""+ChatColor.GRAY+"at spawn");
			match.sendMessage(mmh.getMessage("ArenaPlunger.plunger_broken"));
		}
	}
	@ArenaEventHandler(needsPlayer = false)
	public void onDespawn(ItemDespawnEvent e){
        match.sendMessage("Item despawn");
		if(e.getEntity().equals(plunger)){
			e.setCancelled(true);
		}
	}
	public void setPlunger(Location location) {
		plungerloc = location;
	}
}
