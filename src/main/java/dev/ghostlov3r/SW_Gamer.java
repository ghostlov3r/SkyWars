package dev.ghostlov3r;

import dev.ghostlov3r.beengine.block.Blocks;
import dev.ghostlov3r.beengine.block.blocks.BlockSponge;
import dev.ghostlov3r.beengine.block.blocks.BlockTNT;
import dev.ghostlov3r.beengine.entity.effect.EffectInstance;
import dev.ghostlov3r.beengine.entity.effect.Effects;
import dev.ghostlov3r.beengine.event.block.BlockBreakEvent;
import dev.ghostlov3r.beengine.item.Item;
import dev.ghostlov3r.beengine.item.ItemIds;
import dev.ghostlov3r.beengine.item.Items;
import dev.ghostlov3r.beengine.player.PlayerInfo;
import dev.ghostlov3r.beengine.world.Particle;
import dev.ghostlov3r.beengine.world.Sound;
import dev.ghostlov3r.math.FRand;
import dev.ghostlov3r.minecraft.MinecraftSession;
import dev.ghostlov3r.minigame.MGGamer;
import dev.ghostlov3r.minigame.arena.Team;
import dev.ghostlov3r.nbt.NbtMap;

import javax.annotation.Nullable;
import java.util.List;

public class SW_Gamer extends MGGamer<SW_Arena, Team<SW_Arena, SW_Gamer>> {

	public SW_Gamer(MinecraftSession session, PlayerInfo info, boolean a, NbtMap data) {
		super(session, info, a, data);
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
			Items.IRON_PICKAXE(),
			Items.DIAMOND_PICKAXE(),

			Items.BOW(),
			Items.ARROW(),

			Items.FLINT_AND_STEEL(),
			Blocks.TNT().asItem(),

			Items.SNOWBALL(),
			// Items.SPLASH_POTION()

			Items.LEATHER_CAP(),
			Items.LEATHER_TUNIC(),
			Items.LEATHER_PANTS(),
			Items.LEATHER_BOOTS(),

			Items.CHAINMAIL_HELMET(),
			Items.CHAINMAIL_CHESTPLATE(),
			Items.CHAINMAIL_LEGGINGS(),
			Items.CHAINMAIL_BOOTS(),

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
				if (item.id() == ItemIds.TNT) {
					item.setCount(FRand.nextInt(1, 2));
				} else {
					item.setCount(FRand.random().nextInt(item.maxStackSize() + 1));
				}
				world.dropItem(event.block(), item.clone());
			}
			if (lucky) {
				world.addSound(event.block(), Sound.XP_COLLECT);
				world.addParticle(event.block().add(0.5f, 0.5f, 0.5f), Particle.FLAME);
				world.addSound(event.block(), Sound.XP_COLLECT);
			}
			if (FRand.random().nextInt(200) == 1) {
				world.setBlock(event.block(), Blocks.TNT());
				if (world.getBlock(event.block()) instanceof BlockTNT tnt) {
					tnt.ignite(40);
				}
			}
		}
	}
}
