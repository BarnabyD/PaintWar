package com.darkblade12.paintwar.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Modern particle effect handler using Bukkit's Particle API (1.13+).
 * This replaces the deprecated custom packet-based particle system.
 */
public enum ParticleEffect {

	HUGE_EXPLOSION(Particle.EXPLOSION),
	LARGE_EXPLODE(Particle.EXPLOSION),
	FIREWORKS_SPARK(Particle.FIREWORK),
	BUBBLE(Particle.BUBBLE),
	SUSPEND(Particle.UNDERWATER),
	DEPTH_SUSPEND(Particle.UNDERWATER),
	TOWN_AURA(Particle.ITEM_SLIME),
	CRIT(Particle.CRIT),
	MAGIC_CRIT(Particle.CRIT),
	MOB_SPELL(Particle.EFFECT),
	MOB_SPELL_AMBIENT(Particle.EFFECT),
	SPELL(Particle.EFFECT),
	INSTANT_SPELL(Particle.INSTANT_EFFECT),
	WITCH_MAGIC(Particle.EFFECT),
	NOTE(Particle.NOTE),
	PORTAL(Particle.PORTAL),
	ENCHANTMENT_TABLE(Particle.ENCHANT),
	EXPLODE(Particle.EXPLOSION),
	FLAME(Particle.FLAME),
	LAVA(Particle.LAVA),
	FOOTSTEP(Particle.FALLING_DUST),
	SPLASH(Particle.SPLASH),
	LARGE_SMOKE(Particle.LARGE_SMOKE),
	CLOUD(Particle.CLOUD),
	RED_DUST(Particle.DUST),
	SNOWBALL_POOF(Particle.POOF),
	DRIP_WATER(Particle.DRIPPING_WATER),
	DRIP_LAVA(Particle.DRIPPING_LAVA),
	SNOW_SHOVEL(Particle.FALLING_DUST),
	SLIME(Particle.ITEM_SLIME),
	HEART(Particle.HEART),
	ANGRY_VILLAGER(Particle.ANGRY_VILLAGER),
	HAPPY_VILLAGER(Particle.HAPPY_VILLAGER);

	private final Particle particle;

	ParticleEffect(Particle particle) {
		this.particle = particle;
	}

	public Particle getParticle() {
		return particle;
	}

	public void play(Player p, Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		if (p != null && loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, speed, null, true);
		}
	}

	public void play(Location loc, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		if (loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, speed, null, false);
		}
	}

	public void play(Location loc, double range, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
		if (loc != null && loc.getWorld() != null) {
			for (Player p : loc.getWorld().getPlayers()) {
				if (p.getLocation().distance(loc) <= range) {
					loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, speed, null, true);
				}
			}
		}
	}

	public static void playTileCrack(Player p, Location loc, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		// Modern replacement: use FALLING_DUST particle
		if (p != null && loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, true);
		}
	}

	public static void playTileCrack(Location loc, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		if (loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, false);
		}
	}

	public static void playTileCrack(Location loc, double range, int id, byte data, float offsetX, float offsetY, float offsetZ, int amount) {
		if (loc != null && loc.getWorld() != null) {
			for (Player p : loc.getWorld().getPlayers()) {
				if (p.getLocation().distance(loc) <= range) {
					loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, true);
				}
			}
		}
	}

	public static void playIconCrack(Player p, Location loc, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		// Modern replacement: use ITEM particle
		if (p != null && loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(Particle.ITEM, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, true);
		}
	}

	public static void playIconCrack(Location loc, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		if (loc != null && loc.getWorld() != null) {
			loc.getWorld().spawnParticle(Particle.ITEM, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, false);
		}
	}

	public static void playIconCrack(Location loc, double range, int id, float offsetX, float offsetY, float offsetZ, int amount) {
		if (loc != null && loc.getWorld() != null) {
			for (Player p : loc.getWorld().getPlayers()) {
				if (p.getLocation().distance(loc) <= range) {
					loc.getWorld().spawnParticle(Particle.ITEM, loc, amount, offsetX, offsetY, offsetZ, 0.1F, null, true);
				}
			}
		}
	}
}
