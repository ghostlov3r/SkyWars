package dev.ghostlov3r;

import dev.ghostlov3r.beengine.block.Block;
import dev.ghostlov3r.beengine.world.ChunkManager;
import dev.ghostlov3r.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class CageMaker {

	public ChunkManager world;
	public Block block;
	public List<Vector3> positions = new ArrayList<>();
	public int yOffset;

	public void doFull () {
		int wasY = yOffset;
		doHorizontal();
		for (int i = 0; i < 3; i++) {
			++yOffset;
			doWallLayer();
		}
		++yOffset;
		doHorizontal();
		yOffset = wasY;
	}

	public void doHorizontal () {
		positions.forEach(pos -> doHorizontal(pos.floorX(), yOffset, pos.floorZ()));
	}

	public void doWallLayer () {
		positions.forEach(pos -> doWallLayer(pos.floorX(), yOffset, pos.floorZ()));
	}

	private void doWallLayer (int x, int y, int z) {
		for (int xx = -2; xx <= 2; xx++) {
			for (int zz = -2; zz <= 2; zz++) {
				if (Math.abs(xx) != Math.abs(zz) && ((Math.abs(xx) == 2 && Math.abs(zz) != 2) || (Math.abs(xx) != 2 && Math.abs(zz) == 2))) {
					world.setBlock(x + xx, y, z + zz, block);
				}
			}
		}
	}

	private void doHorizontal (int x, int y, int z) {
		for (int xx = -1; xx <= 1; xx++) {
			for (int zz = -1; zz <= 1; zz++) {
				world.setBlock(x + xx, y, z + zz, block);
			}
		}
	}
}
