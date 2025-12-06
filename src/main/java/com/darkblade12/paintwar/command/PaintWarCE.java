package com.darkblade12.paintwar.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.Arena;
import com.darkblade12.paintwar.arena.State;
import com.darkblade12.paintwar.arena.event.PlayerStartCountdownEvent;
import com.darkblade12.paintwar.arena.event.PlayerStopGameEvent;
import com.darkblade12.paintwar.arena.region.Cuboid;
import com.darkblade12.paintwar.data.DataManager;
import com.darkblade12.paintwar.help.CommandDetails;
import com.darkblade12.paintwar.sign.ArenaSign;
import com.darkblade12.paintwar.stats.Stat;
import com.darkblade12.paintwar.util.ColorCodeUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

public class PaintWarCE implements CommandExecutor {
	private PaintWar plugin;
	private CommandDetails help;

	public PaintWarCE(PaintWar plugin) {
		this.plugin = plugin;
		plugin.getCommand("pw").setExecutor(this);
		help = plugin.help.getCommand("help");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			ColorCodeUtil.sendColoredMessage(sender, help.getInvalidUsageString());
			return true;
		}
		Player p = sender instanceof Player ? (Player) sender : null;
		String sub = args[0].toLowerCase();
		CommandDetails cd = plugin.help.getCommand(sub + (sub.equals("top") ? " <won/lost/wl>" : ""));
		if (cd == null) {
			ColorCodeUtil.sendColoredMessage(sender, help.getInvalidUsageString());
			return true;
		} else if (!cd.checkUsage(plugin, sender, args))
			return true;
		if (sub.equals("create")) {
			String name = args[1];
			if (plugin.arena.getArena(name) != null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(true));
				return true;
			}
			Arena.create(plugin, name);
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(true, name));
		} else if (sub.equals("remove")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			}
			a.remove();
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false, a.getName()));
		} else if (sub.equals("select")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			}
			plugin.data.setSelectedArena(p, a);
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_selected(a.getName()));
		} else if (sub.equals("wand")) {
			if (!DataManager.hasEnoughSpace(p, PaintWar.getWand())) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_not_enough_space());
				return true;
			}
			p.getInventory().addItem(PaintWar.getWand());
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_got_wand());
		} else if (sub.equals("set")) {
			Arena a;
			if (args.length == 2) {
				a = plugin.data.getSelectedArena(p);
				if (a == null) {
					ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[2]);
			}
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (!a.isInEditMode()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_not_in_edit_mode());
				return true;
			} else if (!plugin.data.isSelectionComplete(p)) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_too_few_positions());
				return true;
			}
			Cuboid region;
			try {
				region = plugin.data.getSelection(p);
			} catch (Exception e) {
				// this catch is useless since it's checked before if both positions aren't null
				return true;
			}
			if (args[1].equalsIgnoreCase("floor")) {
				a.setFloor(region);
			} else if (args[1].equalsIgnoreCase("protection")) {
				a.setProtection(region);
			} else {
				ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_set(args[1].equalsIgnoreCase("floor"), a.getName()));
		} else if (sub.equalsIgnoreCase("spawn")) {
			Arena a;
			if (args.length == 3) {
				a = plugin.data.getSelectedArena(p);
				if (a == null) {
					ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[3]);
			}
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			}
			String spawn = args[2];
			if (!a.isInEditMode()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_not_in_edit_mode());
				return true;
			}
			String operation = args[1].toLowerCase();
			if (operation.equals("add")) {
				if (a.getSpawnAmount() == 16) {
					ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_too_x_spawns(true));
					return true;
				} else if (a.hasSpawn(spawn)) {
					ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_spawn_x(true));
					return true;
				}
				a.addSpawn(spawn, p.getLocation());
			} else if (operation.equals("del")) {
				if (!a.hasSpawn(spawn)) {
					ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_spawn_x(false));
					return true;
				}
				a.deleteSpawn(spawn);
			} else {
				ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_spawn_x(operation.equals("add"), spawn, a.getName()));
		} else if (sub.equals("check")) {
			Arena a;
			if (args.length == 1 && p != null) {
				a = plugin.data.getSelectedArena(p);
				if (a == null) {
					ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[1]);
			}
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (!a.isInEditMode()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_not_in_edit_mode());
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_checklist(a));
		} else if (sub.equals("edit")) {
			Arena a;
			if (args.length == 1 && p != null) {
				a = plugin.data.getSelectedArena(p);
				if (a == null) {
					ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
					return true;
				}
			} else {
				a = plugin.arena.getArena(args[1]);
			}
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (a.isInEditMode() && !a.isReadyForUse()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_for_use(false));
				return true;
			}
			a.switchEditMode();
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_toggle_edit_mode(a.getName(), a.isInEditMode()));
		} else if (sub.equals("arenas")) {
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_list());
		} else if (sub.equals("players")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_list(a));
		} else if (sub.equals("join")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (!a.isSetup()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_setup(false));
				return true;
			} else if (plugin.arena.hasJoinedArena(p)) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_joined(true));
				return true;
			} else if (a.requiresEmptyInventory() && !DataManager.hasEmptyInventory(p)) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_inventory_not_empty());
				return true;
			} else if (a.isFull()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_full());
				return true;
			} else if (a.getState() == State.NOT_JOINABLE) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_game_x_started(true));
				return true;
			}
			a.handleJoin(p);
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_joined(a));
		} else if (sub.equals("leave")) {
			Arena a = plugin.arena.getJoinedArena(p);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_joined(false));
				return true;
			}
			a.handleLeave(p);
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_left(a));
		} else if (sub.equals("start")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (!a.isSetup()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_setup(false));
				return true;
			} else if (a.getState() != State.JOINABLE) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_game_x_started(true));
				return true;
			} else if (a.getPlayers().size() < 1) { // TODO change to 2
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_can_not_start());
				return true;
			}
			if (p != null) {
				PlayerStartCountdownEvent e = new PlayerStartCountdownEvent(p, a);
				e.call();
				if (e.isCancelled())
					return true;
			}
			a.broadcastMessage(plugin.message.arena_game_started_other(sender.getName()));
			a.startCountdown();
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_game_started(a.getName()));
		} else if (sub.equals("stop")) {
			Arena a = plugin.arena.getArena(args[1]);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x(false));
				return true;
			} else if (a.getState() != State.NOT_JOINABLE) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_game_x_started(false));
				return true;
			}
			if (p != null) {
				PlayerStopGameEvent e = new PlayerStopGameEvent(p, a);
				e.call();
				if (e.isCancelled())
					return true;
			}
			a.broadcastMessage(plugin.message.arena_game_stopped_other(sender.getName()));
			a.stopGame(true);
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_game_stopped(a.getName()));
		} else if (sub.equals("kick")) {
			Player k = Bukkit.getPlayer(args[1]);
			if (k == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_not_existent());
				return true;
			}
			String playerName = k.getName();
			Arena a = plugin.arena.getJoinedArena(k);
			if (a == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.arena_x_joined(false));
				return true;
			}
			String name = a.getName();
			a.handleLeave(k);
			a.broadcastMessage(plugin.message.player_kicked_other(name, playerName));
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.player_kicked(playerName, name));
		} else if (sub.equals("stats")) {
			boolean own = args.length == 1;
			if (p == null && own) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.command_no_console_executor());
				return true;
			}
			String name = own ? sender.getName() : plugin.stats.getPlayerName(args[1]);
			if (!plugin.stats.hasStats(name)) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.stats_not_found(name));
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.stats(name));
		} else if (sub.equals("top")) {
			if (!plugin.stats.hasStats()) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.no_top_ten());
				return true;
			}
			Stat s = Stat.fromName(args[1]);
			if (s == null) {
				ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
				return true;
			}
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.top_ten(s));
		} else if (sub.equals("powerups")) {
			if (args.length == 1) {
				// Show all powerups with hover tooltips
				if (sender instanceof Player) {
					Player player = (Player) sender;
					Component message = Component.empty();
					message = message.append(ColorCodeUtil.fromLegacyString("&6&l=== PaintWar Powerups ===\n"));
					message = message.append(ColorCodeUtil.fromLegacyString("&7Hover over powerups for details\n\n"));
					
					int count = 0;
					for (com.darkblade12.paintwar.arena.powerup.Powerup pow : com.darkblade12.paintwar.arena.powerup.Powerup.values()) {
						if (count > 0) {
							message = message.append(Component.newline());
						}
						
						String displayText = "&e• &f" + pow.getName();
						String description = getPowerupDescription(pow);
						Component hover = ColorCodeUtil.fromLegacyString(description);
						
						Component powerupComponent = ColorCodeUtil.fromLegacyString(displayText)
							.hoverEvent(HoverEvent.showText(hover));
						
						message = message.append(powerupComponent);
						count++;
					}
					player.sendMessage(message);
				} else {
					// Console doesn't support hover events
					StringBuilder sb = new StringBuilder();
					sb.append("&6&l=== PaintWar Powerups ===\n");
					for (com.darkblade12.paintwar.arena.powerup.Powerup pow : com.darkblade12.paintwar.arena.powerup.Powerup.values()) {
						sb.append("&e• &f").append(pow.getName()).append("\n");
					}
					ColorCodeUtil.sendColoredMessage(sender, sb.toString());
				}
			} else {
				// Show specific powerup info
				com.darkblade12.paintwar.arena.powerup.Powerup pow = com.darkblade12.paintwar.arena.powerup.Powerup.fromName(args[1]);
				if (pow == null) {
					ColorCodeUtil.sendColoredMessage(sender, "&cPowerup not found!");
					return true;
				}
				String description = getPowerupDescription(pow);
				StringBuilder sb = new StringBuilder();
				sb.append("&6&l=== ").append(pow.getName()).append(" ===\n");
				sb.append(description);
				ColorCodeUtil.sendColoredMessage(sender, sb.toString());
			}
		} else if (sub.equals("signs")) {
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.sign_list());
		} else if (sub.equals("tp")) {
			ArenaSign as = plugin.sign.getSign(args[1]);
			if (as == null) {
				ColorCodeUtil.sendColoredMessage(sender, plugin.message.sign_not_existent());
				return true;
			}
			p.teleport(as.getLocation());
			ColorCodeUtil.sendColoredMessage(sender, plugin.message.sign_teleport(as.getId()));
		} else if (sub.equals("reload")) {
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("config")) {
					plugin.loadConfig();
					plugin.setting.initialize();
				} else {
					ColorCodeUtil.sendColoredMessage(sender, cd.getInvalidUsageString());
					return true;
				}
			} else {
				plugin.reload();
			}
			ColorCodeUtil.sendColoredMessage(sender, args.length == 2 ? plugin.message.reload_config() : plugin.message.reload_plugin());
		} else if (sub.equals("help")) {
			int page = 1;
			if (args.length == 2)
				try {
					page = Integer.parseInt(args[1]);
					if (!plugin.help.hasHelpPage(sender, page)) {
						ColorCodeUtil.sendColoredMessage(sender, plugin.message.help_page_not_existent());
						return true;
					}
				} catch (Exception e) {
					ColorCodeUtil.sendColoredMessage(sender, plugin.message.help_page_invalid_number());
					return true;
				}
			plugin.help.displayHelpPage(sender, page);
		}
		return true;
	}

	private String getPowerupDescription(com.darkblade12.paintwar.arena.powerup.Powerup pow) {
		switch (pow.getName()) {
			case "Big_Brush":
				return "&7Temporarily increases your brush size to paint larger areas.";
			case "Tiny_Brush":
				return "&7Shrinks all opponents' brush sizes, making it harder for them to paint.";
			case "Empty_Paint":
				return "&7Makes all opponents' paint disappear when they place it, wasting their effort.";
			case "Speed":
				return "&7Grants you a temporary speed boost to move faster around the arena.";
			case "Freeze":
				return "&7Freezes all opponents in place, blocking their movement.";
			case "Big_Blob":
				return "&7Paints a large circular area around you instantly.";
			case "Tiny_Blobs":
				return "&7Paints multiple small circles scattered around the map.";
			case "Advanced_Darkness":
				return "&7Blinds opponents while giving them night vision - disorienting effect.";
			case "Drunken":
				return "&7Makes opponents nauseous and disoriented for a period of time.";
			case "Slowness":
				return "&7Slows down all opponents, making them move at a snail's pace.";
			case "Jumping":
				return "&7Makes opponents jump uncontrollably, disrupting their gameplay.";
			case "Color_Bombs":
				return "&7Gives you snowballs that paint areas when thrown at the floor.";
			case "Immortal_Color":
				return "&7Your paint color becomes immune to being painted over by opponents.";
			case "Eraser":
				return "&7Temporarily turn into an eraser to remove opponent paint from the floor.";
			case "Powerup_Magnet":
				return "&7Automatically pulls nearby powerups toward you for easy collection.";
			case "Dash":
				return "&7Gives you multiple dash charges to move quickly around the arena.";
			case "No_Borders":
				return "&7Allows you to paint outside the normal arena boundaries temporarily.";
			default:
				return "&7Unknown powerup.";
		}
	}
}

