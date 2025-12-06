package com.darkblade12.paintwar.arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.util.PaintColor;

public class ScoreboardManager {
	private PaintWar plugin;
	private Arena arena;
	private Scoreboard scoreboard;
	private Objective objective;

	public ScoreboardManager(PaintWar plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		
		// Create a new scoreboard
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		// Create the objective
		@SuppressWarnings("deprecation")
		Objective obj = scoreboard.registerNewObjective(
			"paintwar_scores",
			"dummy",
			net.kyori.adventure.text.Component.text("§6§l" + arena.getName())
		);
		this.objective = obj;
		
		// Set display slot to the right side
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void updateScoreboard() {
		// Safety checks - objective might be unregistered
		if (scoreboard == null || objective == null) {
			return;
		}
		
		try {
			// Get all players and their paint percentages
			Map<String, Integer> scores = calculatePlayerScores();
			
			// Clear old scores
			for (String entry : new ArrayList<>(scoreboard.getEntries())) {
				scoreboard.resetScores(entry);
			}
			
			// Sort players by score (descending)
			List<Entry<String, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
			sortedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
			
			// Display top 5 players
			int maxPlayers = Math.min(5, sortedScores.size());
			int score = 100;
			
			for (int i = 0; i < maxPlayers; i++) {
				Entry<String, Integer> entry = sortedScores.get(i);
				String playerName = entry.getKey();
				int percentage = entry.getValue();
				
				// Create display text with percentage
				String displayText = String.format("§e%d. §f%s §7%d%%", i + 1, playerName, percentage);
				try {
					objective.getScore(displayText).setScore(score - i);
				} catch (IllegalStateException e) {
					// Objective was unregistered, stop updating
					return;
				}
			}
		} catch (Exception e) {
			// If any error occurs during scoreboard update, silently fail
			// This prevents the task from spamming errors
		}
	}

	private Map<String, Integer> calculatePlayerScores() {
		Map<String, Integer> scores = new HashMap<>();
		Map<PaintColor, String> colorMap = new HashMap<>();
		
		// Build color to player name map
		for (Player p : arena.getPlayers()) {
			colorMap.put(plugin.data.getPaintColor(p), p.getName());
		}
		
		// Get floor data and calculate percentages
		int totalBlocks = arena.getFloor().getVolume();
		for (Entry<PaintColor, Integer> colorEntry : arena.getFloor().getColorMap().entrySet()) {
			String playerName = colorMap.get(colorEntry.getKey());
			if (playerName != null) {
				int blocks = colorEntry.getValue();
				int percentage = totalBlocks > 0 ? (int) (((double) blocks / totalBlocks) * 100) : 0;
				scores.put(playerName, percentage);
			}
		}
		
		return scores;
	}

	public void showToPlayer(Player p) {
		p.setScoreboard(scoreboard);
	}

	public void showToAll() {
		for (Player p : arena.getPlayers()) {
			showToPlayer(p);
		}
	}

	public void cleanup() {
		try {
			// Clear the scoreboard entries
			for (String entry : new ArrayList<>(scoreboard.getEntries())) {
				scoreboard.resetScores(entry);
			}
			// Only unregister if the objective is still registered
			if (objective != null) {
				try {
					objective.unregister();
				} catch (IllegalStateException e) {
					// Objective was already unregistered, ignore
				}
			}
		} catch (Exception e) {
			// If any error occurs during cleanup, silently fail
			// This prevents shutdown errors
		}
		// Players will default back to the main scoreboard
	}
}
