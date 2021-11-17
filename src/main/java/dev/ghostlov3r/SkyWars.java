package dev.ghostlov3r;

import dev.ghostlov3r.beengine.event.EventListener;
import dev.ghostlov3r.beengine.plugin.AbstractPlugin;
import dev.ghostlov3r.beengine.utils.config.Config;
import dev.ghostlov3r.beengine.world.World;
import dev.ghostlov3r.math.Vector3;
import dev.ghostlov3r.minigame.MiniGame;

public class SkyWars extends AbstractPlugin<Config> implements EventListener<SW_Gamer> {

	@Override
	protected void onEnable() {
		MiniGame.builder()
				.arenaType(SW_Arena.class)
				.gamerType(SW_Gamer.class)
				.wizardType(SW_Wizard.class)
				.build();

		if (World.defaultWorld().uniqueName().equals("tesla_sw_lobby")) {
			// Hardcoded spawn pos
			World.defaultWorld().setSpawnLocation(new Vector3(-1276, 74, 67));
		}

		// wait -49999 172 -49999
	}
}
