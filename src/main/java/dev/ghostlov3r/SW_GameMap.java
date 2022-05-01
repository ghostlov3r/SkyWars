package dev.ghostlov3r;

import beengine.block.Block;
import beengine.block.Blocks;
import beengine.util.DiskMap;
import beengine.util.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ghostlov3r.minigame.data.GameMap;
import dev.ghostlov3r.minigame.data.MapTeam;

public class SW_GameMap extends GameMap {

	@JsonProperty("cage-block")
	public String cageBlockName = "GRAY_STAINED_GLASS";

	@JsonIgnore
	public Block cageBlock;

	public SW_GameMap(DiskMap<String, ?> map, String key) {
		super(map, key);
	}

	@Override
	public void init(Class<? extends MapTeam> teamKlass) {
		super.init(teamKlass);
		try {
			cageBlock = (Block) Utils.invoke(Blocks.class, cageBlockName.replace(' ', '_').toUpperCase());
		}
		catch (Exception e) {
			cageBlock = Blocks.GRAY_STAINED_GLASS();
		}
	}
}
