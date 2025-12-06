package com.darkblade12.paintwar;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import com.darkblade12.paintwar.arena.ArenaManager;
import com.darkblade12.paintwar.command.PaintWarCE;
import com.darkblade12.paintwar.command.PaintWarTabCompleter;
import com.darkblade12.paintwar.data.DataManager;
import com.darkblade12.paintwar.help.HelpPageManager;
import com.darkblade12.paintwar.message.MessageManager;
import com.darkblade12.paintwar.sign.SignManager;
import com.darkblade12.paintwar.stats.StatsManager;
import com.darkblade12.paintwar.util.VaultUtil;

public class PaintWar extends JavaPlugin {
	public static final String PREFIX = "&8&l[&a&oPaint&4&oWar&8&l] &r";
	public Logger l;
	public Configuration config;
	public VaultUtil vault;
	public DataManager data;
	public SettingManager setting;
	public HelpPageManager help;
	public ArenaManager arena;
	public MessageManager message;
	public StatsManager stats;
	public SignManager sign;

	public void onEnable() {
		l = getLogger();
		loadConfig();
		data = new DataManager(this);
		setting = new SettingManager(this);
		arena = new ArenaManager(this);
		message = new MessageManager(this);
		stats = new StatsManager(this);
		sign = new SignManager(this);
		if (!message.initialize() || !stats.initialize() || !sign.initialize()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		help = new HelpPageManager(this, message.help_page_header(getPluginMeta().getVersion()) + "\n", "&8[&bPage <current_page> &7of &6&l<page_amount>&8]",
				"<random_color>\u2756 &6&o/<command> &r\u268A &2<description>\n&r  &d\u2756 &5&oExecutable as Console: <console_check>\n&r  &7\u2756 &8&oPermission: &7<permission>", 10);
		checkForVault();
		new PaintWarCE(this);
		new PaintWarTabCompleter(this);
		l.info("Game system has been successfully enabled!");
	}

	public void onDisable() {
		stats.disable();
		arena.disable();
		sign.disable();
		l.info("Game system has been disabled!");
	}

	public void loadConfig() {
		if (new File("plugins/" + getName() + "/config.yml").exists())
			l.info("config.yml successfully loaded.");
		else
			saveDefaultConfig();
		config = getConfig();
	}

	private void checkForVault() {
		if (!VaultUtil.isVaultEconomyInstalled())
			return;
		l.info("Vault has been hooked, money distribution is active.");
		vault = new VaultUtil();

	}

	public void reload() {
		loadConfig();
		setting.initialize();
		arena.disable();
		arena.initialize();
		sign.disable();
		if (!message.initialize() || !stats.initialize() || !sign.initialize()) {
			l.warning("Failed to reload, plugin will disable!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		checkForVault();
		l.info("Plugin has been reloaded!");
	}

	public static ItemStack getWand() {
		ItemStack wand = new ItemStack(Material.STICK);
		ItemMeta im = wand.getItemMeta();
		if (im != null) {
			im.displayName(Component.text("Selection Wand", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
			im.lore(Arrays.asList(
				Component.text("Left click: ").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)
					.append(Component.text("select", NamedTextColor.RED))
					.append(Component.text(" position 1", NamedTextColor.GOLD)),
				Component.text("Right click: ").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)
					.append(Component.text("select", NamedTextColor.RED))
					.append(Component.text(" position 2", NamedTextColor.GOLD))
			));
			wand.setItemMeta(im);
		}
		return wand;
	}

}
