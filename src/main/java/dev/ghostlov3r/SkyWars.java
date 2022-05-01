package dev.ghostlov3r;

import beengine.plugin.AbstractPlugin;
import beengine.util.config.Config;
import dev.ghostlov3r.minigame.MiniGame;

public class SkyWars extends AbstractPlugin<Config> {

	@Override
	protected void onEnable() {
		MiniGame.builder()
				.arenaType(SW_Arena.class)
				.gamerType(SW_Gamer.class)
				.wizardType(SW_Wizard.class)
				.mapType(SW_GameMap.class)
				.build();
	}
}
