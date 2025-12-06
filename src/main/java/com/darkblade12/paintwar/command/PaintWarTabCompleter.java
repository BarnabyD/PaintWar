package com.darkblade12.paintwar.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;

public class PaintWarTabCompleter implements TabCompleter {
	private PaintWar plugin;

	public PaintWarTabCompleter(PaintWar plugin) {
		this.plugin = plugin;
		plugin.getCommand("pw").setTabCompleter(this);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 0)
			return completions;

		if (args.length == 1) {
			// First argument: subcommand names
			String input = args[0].toLowerCase();
			String[] subcommands = { "create", "remove", "select", "wand", "set", "spawn", "check", "edit", 
									  "arenas", "players", "join", "leave", "start", "stop", "kick", "stats", 
									  "top", "signs", "tp", "reload", "help" };
			
			for (String sub : subcommands) {
				if (sub.startsWith(input) && hasPermission(sender, sub)) {
					completions.add(sub);
				}
			}
			return completions;
		}

		// Get the subcommand
		String sub = args[0].toLowerCase();
		
		// Second argument completions based on subcommand
		if (args.length == 2) {
			String input = args[1].toLowerCase();
			
			if (sub.equals("create")) {
				// No completion for create (free text)
				return completions;
			} else if (sub.equals("remove") || sub.equals("select") || sub.equals("check") || 
					   sub.equals("edit") || sub.equals("players") || sub.equals("join") || 
					   sub.equals("start") || sub.equals("stop")) {
				// Arena name completion
				return getArenaCompletions(input);
			} else if (sub.equals("set")) {
				// set <floor/protection>
				if ("floor".startsWith(input))
					completions.add("floor");
				if ("protection".startsWith(input))
					completions.add("protection");
				return completions;
			} else if (sub.equals("spawn")) {
				// spawn <add/del>
				if ("add".startsWith(input))
					completions.add("add");
				if ("delete".startsWith(input))
					completions.add("delete");
				if ("del".startsWith(input))
					completions.add("del");
				return completions;
			} else if (sub.equals("kick")) {
				// kick <player>
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().toLowerCase().startsWith(input)) {
						completions.add(p.getName());
					}
				}
				return completions;
			} else if (sub.equals("stats")) {
				// stats [player] - optional
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().toLowerCase().startsWith(input)) {
						completions.add(p.getName());
					}
				}
				return completions;
			} else if (sub.equals("top")) {
				// top <won/lost/wl>
				if ("won".startsWith(input))
					completions.add("won");
				if ("lost".startsWith(input))
					completions.add("lost");
				if ("wl".startsWith(input))
					completions.add("wl");
				return completions;
			} else if (sub.equals("tp")) {
				// tp <id> - arena sign IDs
				return getSignIdCompletions(input);
			} else if (sub.equals("reload")) {
				// reload [config] - optional
				if ("config".startsWith(input))
					completions.add("config");
				return completions;
			}
		}

		// Third argument completions
		if (args.length == 3) {
			String input = args[2].toLowerCase();
			
			if (sub.equals("spawn")) {
				// spawn <add/del> <arena>
				return getArenaCompletions(input);
			} else if (sub.equals("set")) {
				// set <floor/protection> [arena]
				return getArenaCompletions(input);
			}
		}

		return completions;
	}

	private List<String> getArenaCompletions(String input) {
		List<String> completions = new ArrayList<>();
		for (Arena arena : plugin.arena.getArenas()) {
			if (arena.getName().toLowerCase().startsWith(input)) {
				completions.add(arena.getName());
			}
		}
		return completions;
	}

	private List<String> getSignIdCompletions(String input) {
		List<String> completions = new ArrayList<>();
		for (Object signObj : plugin.sign.getSigns()) {
			// Get sign ID - signs have getId() method
			if (signObj instanceof com.darkblade12.paintwar.sign.ArenaSign) {
				com.darkblade12.paintwar.sign.ArenaSign sign = (com.darkblade12.paintwar.sign.ArenaSign) signObj;
				if (sign.getId().toLowerCase().startsWith(input)) {
					completions.add(sign.getId());
				}
			}
		}
		return completions;
	}

	private boolean hasPermission(CommandSender sender, String subcommand) {
		// Check if sender has permission for the subcommand
		String perm = "PaintWar." + subcommand;
		return sender.hasPermission(perm) || sender.hasPermission("PaintWar.*");
	}
}
