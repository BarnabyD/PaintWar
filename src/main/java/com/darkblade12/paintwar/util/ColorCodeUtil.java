package com.darkblade12.paintwar.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/**
 * Utility class to convert legacy Minecraft color codes to Adventure Components.
 * Replaces the old § character system with the modern Paper API.
 */
public class ColorCodeUtil {

	/**
	 * Converts a legacy color code string (with &) to an Adventure Component.
	 * Example: "&cRed &aGreen" -> Component with red and green text
	 */
	public static Component fromLegacyString(String legacy) {
		if (legacy == null) {
			return Component.empty();
		}

		Component result = Component.empty();
		String[] parts = legacy.split("&");

		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				// First part has no color code
				if (!parts[i].isEmpty()) {
					result = result.append(Component.text(parts[i]));
				}
			} else {
				// Each part starts with a color code
				if (parts[i].isEmpty()) {
					continue;
				}

				char colorCode = parts[i].charAt(0);
				String text = parts[i].substring(1);

				NamedTextColor color = getColorFromCode(colorCode);
				TextDecoration decoration = getDecorationFromCode(colorCode);

				Component component;
				if (text.isEmpty()) {
					component = Component.empty();
				} else if (color != null && decoration != null) {
					component = Component.text(text, color, decoration);
				} else if (color != null) {
					component = Component.text(text, color);
				} else if (decoration != null) {
					component = Component.text(text).decorate(decoration);
				} else {
					component = Component.text(text);
				}

				result = result.append(component);
			}
		}

		return result;
	}

	private static NamedTextColor getColorFromCode(char code) {
		return switch (code) {
			case '0' -> NamedTextColor.BLACK;
			case '1' -> NamedTextColor.DARK_BLUE;
			case '2' -> NamedTextColor.DARK_GREEN;
			case '3' -> NamedTextColor.DARK_AQUA;
			case '4' -> NamedTextColor.DARK_RED;
			case '5' -> NamedTextColor.DARK_PURPLE;
			case '6' -> NamedTextColor.GOLD;
			case '7' -> NamedTextColor.GRAY;
			case '8' -> NamedTextColor.DARK_GRAY;
			case '9' -> NamedTextColor.BLUE;
			case 'a' -> NamedTextColor.GREEN;
			case 'b' -> NamedTextColor.AQUA;
			case 'c' -> NamedTextColor.RED;
			case 'd' -> NamedTextColor.LIGHT_PURPLE;
			case 'e' -> NamedTextColor.YELLOW;
			case 'f' -> NamedTextColor.WHITE;
			default -> null;
		};
	}

	private static TextDecoration getDecorationFromCode(char code) {
		return switch (code) {
			case 'k' -> TextDecoration.OBFUSCATED;
			case 'l' -> TextDecoration.BOLD;
			case 'm' -> TextDecoration.STRIKETHROUGH;
			case 'n' -> TextDecoration.UNDERLINED;
			case 'o' -> TextDecoration.ITALIC;
			case 'r' -> null; // reset, handled differently
			default -> null;
		};
	}

	/**
	 * Sends a message with color codes to a CommandSender.
	 * Automatically converts & codes to Adventure Components.
	 */
	public static void sendColoredMessage(org.bukkit.command.CommandSender sender, String message) {
		sender.sendMessage(fromLegacyString(message));
	}
}
