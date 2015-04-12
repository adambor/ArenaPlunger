package mc.adambor.ArenaPlunger;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.events.matches.messages.MatchTimeExpiredMessageEvent;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.messaging.MatchMessageHandler;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.scoreboard.ArenaDisplaySlot;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.VictoryCondition;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesLeaderRanking;

import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class Victory extends VictoryCondition implements DefinesLeaderRanking{

	final ArenaObjective scores;
	Integer capturesToWin;
	MatchMessageHandler mmh;

	public Victory(Match match) {
		super(match);
		this.scores = new ArenaObjective("Victory", "VictoryCondition");
		scores.setDisplayName(ChatColor.GOLD+"Starting up...");
		ArenaScoreboard scoreboard = match.getScoreboard();
		scores.setDisplaySlot(ArenaDisplaySlot.SIDEBAR);
		scoreboard.addObjective(scores);

		/// set all points to 0 so they display in Scoreboard
		this.resetScores();
	}
	public void resetScores(){
		scores.setAllPoints(match, 1);
		scores.setAllPoints(match, 0);
	}
	
	@ArenaEventHandler(priority=EventPriority.HIGHEST)
	public void onMatchFindCurrentLeaderEvent(MatchFindCurrentLeaderEvent event){
		event.setResult(scores.getMatchResult(match));
	}
	
	@ArenaEventHandler(priority=EventPriority.HIGHEST)
	public void matchResult(MatchResultEvent e){
		e.setMatchResult(scores.getMatchResult(match));
	}
	
	@ArenaEventHandler(priority=EventPriority.HIGHEST)
	public void onMatchTimeExpiredMessage(MatchTimeExpiredMessageEvent event){
		match.setMatchResult(scores.getMatchResult(match));
	}
	
	public Integer addScore(ArenaTeam team, ArenaPlayer ap) {
		scores.addPoints(ap, 1);
		return scores.addPoints(team, 1);
	}

	@Override
	public List<ArenaTeam> getLeaders() {
		return scores.getLeaders();
	}

	@Override
	public TreeMap<?, Collection<ArenaTeam>> getRanks() {
		return scores.getRanks();
	}
}