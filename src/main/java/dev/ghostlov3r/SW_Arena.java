package dev.ghostlov3r;

import dev.ghostlov3r.minigame.MiniGame;
import dev.ghostlov3r.minigame.arena.Arena;
import dev.ghostlov3r.minigame.arena.Team;
import dev.ghostlov3r.minigame.arena.WinData;
import dev.ghostlov3r.minigame.data.ArenaType;

public class SW_Arena extends Arena<Team<SW_Arena, SW_Gamer>, SW_Gamer> {

	public SW_Arena(MiniGame manager, ArenaType type, int id) {
		super(manager, type, id);
	}

	@Override
	public WinData forceWinDataOnEnd() {
		Team<SW_Arena, SW_Gamer> winner = null;
		int maxTeamKills = 0;
		for (Team<SW_Arena, SW_Gamer> team : teams()) {
			int teamKills = 0;
			for (SW_Gamer gamer : team.gamers().toList()) {
				teamKills += gamer.gameCtx().kills;
			}
			if (teamKills > maxTeamKills) {
				maxTeamKills = teamKills;
				winner = team;
			}
		}
		if (winner == null) {
			return super.forceWinDataOnEnd();
		} else {
			return new WinData(this, winner);
		}
	}
}
