package dev.ghostlov3r;

import dev.ghostlov3r.beengine.Server;
import dev.ghostlov3r.beengine.block.Blocks;
import dev.ghostlov3r.beengine.scheduler.AsyncTask;
import dev.ghostlov3r.beengine.world.World;
import dev.ghostlov3r.beengine.world.format.BlockArray;
import dev.ghostlov3r.beengine.world.format.ChunkSection;
import dev.ghostlov3r.beengine.world.format.io.WorldProvider;
import dev.ghostlov3r.minigame.MGGamer;
import dev.ghostlov3r.minigame.Wizard;
import dev.ghostlov3r.minigame.data.ArenaType;
import dev.ghostlov3r.minigame.data.GameMap;
import dev.ghostlov3r.minigame.data.MapTeam;

public class SW_Wizard extends Wizard<GameMap, MapTeam> {

	public SW_Wizard(MGGamer creator, ArenaType type) {
		super(creator, type);
	}

	// Замена сундуков на губки
	@Override
	protected void afterCreationEnd() {
		Server.asyncPool().execute(new AsyncTask() {
			@Override
			public void run() {
				WorldProvider provider = WorldProvider.open(World.pathByName(map.worldName));
				provider.forEachChunk(ctx -> {
					for (ChunkSection section : ctx.chunk().sections()) {
						for (BlockArray layer : section.blockLayers()) {
							layer.replaceAll(Blocks.CHEST().fullId(), Blocks.SPONGE().fullId());
						}
					}
					if (ctx.chunk().isDirty()) {
						provider.writeChunk(ctx);
					}
				});
				WorldProvider.close(World.pathByName(map.worldName));
			}

			@Override
			public String name() {
				return "Chest replacing (World "+map.worldName+")";
			}
		});
	}
}
