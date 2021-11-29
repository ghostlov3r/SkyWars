package dev.ghostlov3r;

import dev.ghostlov3r.beengine.block.Blocks;
import dev.ghostlov3r.beengine.event.entity.EntityDamageEvent;
import dev.ghostlov3r.beengine.scheduler.Scheduler;
import dev.ghostlov3r.beengine.scheduler.Task;
import dev.ghostlov3r.beengine.world.SimpleChunkManager;
import dev.ghostlov3r.beengine.world.Sound;
import dev.ghostlov3r.beengine.world.World;
import dev.ghostlov3r.math.FMath;
import dev.ghostlov3r.minecraft.protocol.v113.packet.WorldSoundEvent;
import dev.ghostlov3r.minigame.MiniGame;
import dev.ghostlov3r.minigame.arena.Arena;
import dev.ghostlov3r.minigame.arena.ArenaState;
import dev.ghostlov3r.minigame.arena.Team;
import dev.ghostlov3r.minigame.arena.WinData;
import dev.ghostlov3r.minigame.data.ArenaType;

public class SW_Arena extends Arena<Team<SW_Arena, SW_Gamer>, SW_Gamer> {

	CageMaker cageMaker;
	int fallDamageProtection;

	public SW_Arena(MiniGame manager, ArenaType type, int id) {
		super(manager, type, id);
	}

	@Override
	protected void onWaitEnd() {
		cageMaker = new CageMaker();
		cageMaker.yOffset = FMath.floor(map().teams.get(0).locations().get(0).y) - 1;
		teams().forEach(team -> {
			if (team.gamersCount != 0) {
				cageMaker.positions.add(map().teams.get(team.id()).locations().get(0).asVector());
			}
		});
	}

	@Override
	protected World.Factory worldFactory() {
		return (l, s, worldProvider, asyncPool) -> {
			World world = super.worldFactory().instantiateWorld(l, s, worldProvider, asyncPool);
			SimpleChunkManager manager = new SimpleChunkManager();
			cageMaker.positions.forEach(pos -> {
				for (int x = -2; x <= 2; x++) {
					for (int z = -2; z <= 2; z++) {
						manager.setChunk(world.loadChunk((pos.floorX() + x) >> 4, (pos.floorZ() + z) >> 4));
					}
				}
			});
			cageMaker.world = manager;
			cageMaker.block = ((SW_GameMap)map()).cageBlock;
			cageMaker.doFull();
			return world;
		};
	}

	// Убираем клетки слоями, чтобы не заспамить бедные клиенты
	@Override
	protected void onGameStart() {
		fallDamageProtection = 10;
		cageMaker.world = gameWorld();
		cageMaker.block = Blocks.AIR();
		Scheduler.delayedRepeat(1, 2, new Task() {
			int stage = 0;
			@Override
			public void run() {
				if (state() != ArenaState.GAME) {
					cageMaker = null;
					cancel();
					return;
				}
				switch (stage) {
					case 0 -> {
						cageMaker.doHorizontal();
						gamers().forEach(gamer -> gamer.broadcastSound(Sound.of(WorldSoundEvent.SoundId.PISTON_IN), gamer.asList()));
					}
					case 1, 2, 3 -> {
						cageMaker.doWallLayer();
					}
					case 4 -> {
						cageMaker.doHorizontal();
						cageMaker = null;
						cancel();
					}
				}
				++stage;
				if (cageMaker != null) {
					++cageMaker.yOffset;
				}
			}
		});
	}

	@Override
	protected void onGameTick(int second) {
		--fallDamageProtection;
	}

	@Override
	protected void onDamage0(EntityDamageEvent event) {
		if (event.entity() instanceof SW_Gamer
				&& event.cause() == EntityDamageEvent.Cause.FALL
				&& fallDamageProtection >= 0) {
			event.cancel();
			return;
		}
		super.onDamage0(event);
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
