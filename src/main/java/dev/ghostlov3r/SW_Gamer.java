package dev.ghostlov3r;

import beengine.block.BlockIds;
import beengine.block.Blocks;
import beengine.block.blocks.BlockSponge;
import beengine.block.blocks.BlockTNT;
import beengine.entity.effect.EffectInstance;
import beengine.entity.effect.Effects;
import beengine.event.block.BlockBreakEvent;
import beengine.event.block.BlockPlaceEvent;
import beengine.item.*;
import beengine.minecraft.MinecraftSession;
import beengine.nbt.NbtMap;
import beengine.player.PlayerInfo;
import beengine.scheduler.Scheduler;
import beengine.util.math.FRand;
import beengine.world.Particle;
import beengine.world.Sound;
import dev.ghostlov3r.minigame.MGGamer;
import dev.ghostlov3r.minigame.arena.ArenaState;
import dev.ghostlov3r.minigame.arena.Team;

import java.util.List;

public class SW_Gamer extends MGGamer<SW_Arena, Team<SW_Arena, SW_Gamer>> {

	public SW_Gamer(MinecraftSession session, PlayerInfo info, boolean a, NbtMap data) {
		super(session, info, a, data);
	}

	@Override
	protected String additionalGameInfo() {
		return """
				Когда матч начнется, ты окажешься на острове. На карте будет еще несколько островов, и на них тоже будут игроки.

				На игровой карте раскиданы специальные блоки с лутом. Сломай его, чтобы получить случайные предметы или другие бонусы.
				Но осторожно! Бонус может сыграть против тебя.

				Чтобы победить в матче, скидывай противников в небытие. Если скинут или поразят тебя, ты проиграешь.
				Победит игрок или команда, которая последней окажется в живых.

				Совет 1: преимущество имеет тот, кто выше. Но помни, что у противника может быть лук.
				Совет 2: чтобы подобраться к противнику, построй мост между островами, но не забудь про страховку.
				Совет 3: если противнику досталась мощная экипировка, а тебя ничего нет, попробуй применить хитрость.""";
	}

	private static final List<Item> randomLoot = List.of(
			Items.APPLE(),
			Items.GOLDEN_APPLE(),

			Items.WOODEN_SWORD(),
			Items.STONE_SWORD(),
			Items.IRON_SWORD(),
			Items.GOLDEN_SWORD(),
			Items.DIAMOND_SWORD(),

			Items.GOLDEN_AXE(),
			Items.DIAMOND_AXE(),
			Items.IRON_PICKAXE(),
			Items.DIAMOND_PICKAXE(),

			Items.BOW(),
			Items.ARROW(),

			Items.FLINT_AND_STEEL(),
			Blocks.TNT().asItem(),

			Items.SNOWBALL(),
			Items.ENDER_PEARL(),

			ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.WITHER.ordinal()),
			ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.HARMING.ordinal()),
			ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.SLOWNESS.ordinal()),
			// Items.SPLASH_POTION()

			Items.LEATHER_CAP(),
			Items.LEATHER_TUNIC(),
			Items.LEATHER_PANTS(),
			Items.LEATHER_BOOTS(),

			Items.CHAINMAIL_HELMET(),
			Items.CHAINMAIL_CHESTPLATE(),
			Items.CHAINMAIL_LEGGINGS(),
			Items.CHAINMAIL_BOOTS(),

			Items.DIAMOND_CHESTPLATE(),

			Blocks.SPRUCE_PLANKS().asItem(),
			Blocks.STONE().asItem(),
			Blocks.COBBLESTONE().asItem(),
			Blocks.BRICKS().asItem(),
			Blocks.GRASS().asItem()
	);

	@Override
	public void onBlockBreak(BlockBreakEvent<MGGamer> event) {
		if (event.block() instanceof BlockSponge) {
			event.setDrops(Item.EMPTY_ARRAY);

			boolean lucky = false;
			if (FRand.nextInt(0, 2) == 0) {
				lucky = true;
				effects().add(new EffectInstance(
					switch (FRand.nextInt(0, 5)) {
						case 0 -> Effects.HEALTH_BOOST;
						case 1 -> Effects.SPEED;
						case 2 -> Effects.JUMP_BOOST;
						case 3 -> Effects.REGENERATION;
						case 4 -> Effects.WITHER;
						default -> Effects.FATAL_POISON;
					}
				));
			}
			for (int i = 0, c = FRand.nextInt(0, 4); i < c; i++) {
				lucky = true;
				Item item = randomLoot.get(FRand.random().nextInt(randomLoot.size()));
				if (item.id() == ItemIds.TNT || item.id() == ItemIds.ENDER_PEARL) {
					item.setCount(1);
				} else {
					item.setCount(FRand.random().nextInt(item.maxStackSize() + 1));
				}
				world.dropItem(event.block(), item.clone());
			}
			if (lucky) {
				world.addSound(event.block(), Sound.XP_COLLECT);
				world.addParticle(event.block().add(0.5f, 0.7f, 0.5f), Particle.FLAME);
				world.addSound(event.block(), Sound.XP_COLLECT);
			}
			if (FRand.random().nextInt(200) == 1) {
				world.setBlock(event.block(), Blocks.TNT());
				if (world.getBlock(event.block()) instanceof BlockTNT tnt) {
					tnt.ignite(55);
				}
			}
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent<MGGamer> event) {
		if (event.block().id() == BlockIds.TNT) {
			Scheduler.delay(1, () -> {
				if (arenaState() == ArenaState.GAME) {
					if (world.getBlock(event.block()) instanceof BlockTNT tnt) {
						tnt.ignite(40);
					}
				}
			});
		}
	}
}
