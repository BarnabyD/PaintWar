package com.darkblade12.paintwar.arena.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.darkblade12.paintwar.PaintWar;
import com.darkblade12.paintwar.arena.powerup.Powerup;
import com.darkblade12.paintwar.arena.util.PaintColor;
import com.darkblade12.paintwar.util.FireworkUtil;

public class Floor extends Cuboid {
	private PaintWar plugin;
	private static final Random RANDOM = new Random();
	private List<Material> ignoredMaterials;
	private List<PaintColor> immortalColors;
	private Map<Location, BlockState> backup;

	public Floor(Location l1, Location l2, PaintWar plugin, List<Material> ignoredMaterials) throws Exception {
		super(l1, l2);
		this.plugin = plugin;
		this.ignoredMaterials = ignoredMaterials;
		immortalColors = new ArrayList<PaintColor>();
	}

	public void createBackup() {
		backup = new HashMap<Location, BlockState>();
		for (Block b : this)
			backup.put(b.getLocation(), b.getState());
	}

	public void restoreBackup() {
		if (backup == null || backup.size() == 0)
			throw new UnsupportedOperationException("Cannot restore an empty backup");
		for (Entry<Location, BlockState> e : backup.entrySet()) {
			BlockState state = e.getValue();
			state.update(true, false);
		}
		backup = null;
		removeItems();
	}

	private void removeItems() {
		List<Chunk> removed = new ArrayList<Chunk>();
		for (Block b : this) {
			Location loc = b.getLocation();
			Chunk c = loc.add(0.0D, 1.0D, 0.0D).getChunk();
			if (removed.contains(c))
				continue;
			for (Entity e : c.getEntities())
				if (e instanceof Item)
					e.remove();
			removed.add(c);
		}
	}

	public void dropPowerup(Powerup pow) {
		World world = getWorld();
		Location randomLoc = new Location(world, RANDOM.nextInt(x2 - x1 + 1) + x1, y2 + 2, RANDOM.nextInt(z2 - z1 + 1) + z1);
		for (int e = 1; e <= 6; e++)
			world.spawnParticle(Particle.GLOW, randomLoc, 1);
		FireworkUtil.generateFirework(randomLoc);
		world.dropItem(randomLoc, pow.getItem());
	}

	/**
	 * Check if a material is any wool type (WHITE_WOOL, ORANGE_WOOL, etc.)
	 */
	private static boolean isWoolMaterial(Material mat) {
		return mat.name().endsWith("_WOOL");
	}

	/**
	 * Get the DyeColor from a wool block
	 */
	private static DyeColor getDyeColorFromWool(Material woolMaterial) {
		String name = woolMaterial.name();
		if (!name.endsWith("_WOOL")) {
			return DyeColor.WHITE; // default
		}
		String colorName = name.substring(0, name.length() - 5); // remove "_WOOL"
		try {
			return DyeColor.valueOf(colorName);
		} catch (IllegalArgumentException e) {
			return DyeColor.WHITE;
		}
	}

	/**
	 * Get the wool material for a given DyeColor
	 */
	private static Material getWoolMaterial(DyeColor color) {
		String materialName = color.name() + "_WOOL";
		return Material.valueOf(materialName);
	}

	private void colorBlock(Block b, PaintColor color) {
		if (!isIgnored(b)) {
			DyeColor dyeColor = DyeColor.getByWoolData(color.getCorrespondingData());
			Material woolMaterial = getWoolMaterial(dyeColor);
			b.setType(woolMaterial);
		}
	}

	private void eraseColor(Block b) {
		if (!isIgnored(b)) {
			BlockState state = backup.get(b.getLocation());
			if (state != null)
				state.update(true, false);
		}
	}

	public void colorTrace(Player p) {
		if (plugin.data.hasEmptyPaint(p))
			return;
		Location loc = p.getLocation();
		int brushSize = plugin.data.getBrushSize(p);
		int centerX = loc.getBlockX();
		int centerY = loc.getBlockY();
		int centerZ = loc.getBlockZ();
		
		// Paint blocks in a 3D sphere around the player (head to feet level)
		// This creates a more natural painting effect instead of just ground level
		paintBlocksSphere(p, centerX, centerY, centerZ, brushSize);
	}
	
	private void paintBlocksSphere(Player p, int centerX, int centerY, int centerZ, int radius) {
		if (radius < 0)
			return;
		
		PaintColor color = plugin.data.getPaintColor(p);
		boolean eraser = plugin.data.isEraser(p);
		World world = getWorld();
		
		int sqr = radius * radius;
		
		// Paint in 3D sphere around player (from centerY - radius to centerY + radius)
		for (int x = centerX - radius; x <= centerX + radius; x++) {
			for (int z = centerZ - radius; z <= centerZ + radius; z++) {
				for (int y = centerY - radius; y <= centerY + radius; y++) {
					// Check if block is within sphere radius
					if ((centerX - x) * (centerX - x) + (centerZ - z) * (centerZ - z) + (centerY - y) * (centerY - y) <= sqr) {
						Block b = world.getBlockAt(x, y, z);
						if (eraser)
							eraseColor(b);
						else
							colorBlock(b, color);
					}
				}
			}
		}
	}

	public void colorCircle(Player p, int cX, int cY, int cZ, int r) {
		if (r < 0)
			return;
		PaintColor color = plugin.data.getPaintColor(p);
		boolean eraser = plugin.data.isEraser(p);
		World world = getWorld();
		if (r == 0) {
			Block b = world.getBlockAt(cX, cY, cZ);
			if (eraser)
				eraseColor(b);
			else
				colorBlock(b, color);
			return;
		}
		int sqr = r * r;
		for (int x = cX - r; x <= cX + r; x++)
			for (int z = cZ - r; z <= cZ + r; z++)
				if ((cX - x) * (cX - x) + (cZ - z) * (cZ - z) <= sqr) {
					Block b = world.getBlockAt(x, cY, z);
					if (eraser)
						eraseColor(b);
					else
						colorBlock(b, color);
				}
	}

	public void colorMultipleCircles(Player p, int amount, int radius) {
		for (int i = 1; i <= amount; i++)
			colorCircle(p, RANDOM.nextInt(x2 - x1 + 1) + x1, y2, RANDOM.nextInt(z2 - z1 + 1) + z1, radius);
	}

	public void setImmortal(PaintColor color, boolean immortal) {
		if (immortal)
			immortalColors.add(color);
		else
			immortalColors.remove(color);
	}

	/**
	 * Check if a block is decorative/non-solid and should not be painted
	 */
	private static boolean isDecorativeBlock(Material mat) {
		String name = mat.name();
		// Ignore stairs, slabs, buttons, pressure plates, switches, doors, trapdoors, fences
		return name.endsWith("_STAIRS") || 
		       name.endsWith("_SLAB") || 
		       name.endsWith("_BUTTON") || 
		       name.endsWith("_PRESSURE_PLATE") || 
		       name.contains("BUTTON") ||
		       name.contains("SWITCH") ||
		       name.endsWith("_DOOR") || 
		       name.endsWith("_TRAPDOOR") ||
		       name.endsWith("_FENCE") ||
		       name.endsWith("_FENCE_GATE") ||
		       mat == Material.REDSTONE_WIRE ||
		       mat == Material.REPEATER ||
		       mat == Material.COMPARATOR ||
		       mat == Material.LEVER ||
		       mat == Material.TRIPWIRE ||
		       mat == Material.TRIPWIRE_HOOK;
	}

	private boolean isIgnored(Block b) {
		if (ignoredMaterials.contains(b.getType())) {
			return true;
		}
		if (!isInside(b.getLocation())) {
			return true;
		}
		if (isDecorativeBlock(b.getType())) {
			return true;
		}
		if (isWoolMaterial(b.getType())) {
			DyeColor color = getDyeColorFromWool(b.getType());
			PaintColor paintColor = PaintColor.fromCorrespondingData((byte) color.getWoolData());
			return immortalColors.contains(paintColor);
		}
		return false;
	}

	public Map<PaintColor, Integer> getColorMap() {
		Map<PaintColor, Integer> map = new HashMap<PaintColor, Integer>();
		for (Block b : this)
			if (isWoolMaterial(b.getType())) {
				DyeColor color = getDyeColorFromWool(b.getType());
				PaintColor paintColor = PaintColor.fromCorrespondingData((byte) color.getWoolData());
				map.put(paintColor, (map.containsKey(paintColor) ? map.get(paintColor) : 0) + 1);
			}
		return map;
	}

	@Override
	public int getVolume() {
		int blocks = 0;
		for (Block b : this)
			if (!isIgnored(b))
				blocks++;
		return blocks;
	}
}
