package dev.ghostlov3r;

import beengine.block.Block;
import beengine.block.BlockFactory;
import beengine.block.BlockIds;
import beengine.block.Blocks;
import beengine.block.blocks.BlockSponge;
import beengine.block.blocks.BlockTNT;
import beengine.entity.effect.EffectInstance;
import beengine.entity.effect.Effects;
import beengine.entity.hostile.EntityBlaze;
import beengine.entity.hostile.EntityCreeper;
import beengine.entity.hostile.EntitySkeleton;
import beengine.entity.hostile.EntityZombie;
import beengine.entity.util.Location;
import beengine.event.block.BlockBreakEvent;
import beengine.event.block.BlockPlaceEvent;
import beengine.item.*;
import beengine.item.enchantment.EnchantmentInstance;
import beengine.item.enchantment.Enchantments;
import beengine.item.items.ItemPickaxe;
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
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SW_Gamer extends MGGamer<SW_Arena, Team<SW_Arena, SW_Gamer>> {

	private final XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();

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

	/*private static final List<Item> randomLoot = List.of(
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
	);*/

	private static final List<Item> BLOCKS = Arrays.asList(
		Blocks.BRICKS().asItem(),
		Blocks.SANDSTONE().asItem(),
		Blocks.OBSIDIAN().asItem(),
		Blocks.STONE().asItem()
	);

	public class Context extends GameContext {
		int brokenLBs;
	}

	@Override
	public MGGamer<SW_Arena, Team<SW_Arena, SW_Gamer>>.GameContext instantiateGameContext() {
		return new Context();
	}

	@Nullable
	@Override
	public Context gameCtx() {
		return (Context) super.gameCtx();
	}

	@Override
	public void onBlockBreak(BlockBreakEvent<MGGamer> event) {
		if (event.block() instanceof BlockSponge) {
			event.setDrops(Item.EMPTY_ARRAY);

			boolean lucky = true;


			/*if (FRand.nextInt(0, 2) == 0) {
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
			}*/

			Consumer<Item> dropItem = item -> world.dropItem(event.block(), item);

			int broken = gameCtx().brokenLBs;

			if (broken == 0) {
				// При старте первый сломаный лакиблок всегда (с учетом адапт. сложн.):
				// Блоки, от 16 до 64 штук
				// Ведро воды
				// Удочка
				// Кирка (алм, жел; эфф 0-2 (70% шанс наложения); прочн 0-2 (70%))
				// Броня (фулл сет) (алм, жел; защ 0-2(70%); прочн 0-2(70%))

				dropItem.accept(BLOCKS.get(random.nextInt(BLOCKS.size())).clone().setCount(FRand.nextInt(random, 16, 64)));

				dropItem.accept(Items.WATER_BUCKET());
				dropItem.accept(Items.FISHING_ROD()); // TODO удочка в Beengine дебильная

				ItemPickaxe pickaxe = random.nextBoolean()
						? Items.DIAMOND_PICKAXE()
						: Items.IRON_PICKAXE();

				int rnd = random.nextInt(100);
				if (rnd > 33) {
					pickaxe.addEnchantment(new EnchantmentInstance(Enchantments.UNBREAKING, rnd > 66 ? 2 : 1));
				}

				dropItem.accept(pickaxe);

				List<Item> fullSet = random.nextBoolean()
					? Arrays.asList(Items.DIAMOND_HELMET(), Items.DIAMOND_CHESTPLATE(), Items.DIAMOND_LEGGINGS(), Items.DIAMOND_BOOTS())
					: Arrays.asList(Items.IRON_HELMET(), Items.IRON_CHESTPLATE(), Items.IRON_LEGGINGS(), Items.IRON_BOOTS());

				rnd = random.nextInt(100);
				if (rnd > 33) {
					for (Item item : fullSet) {
						item.addEnchantment(new EnchantmentInstance(Enchantments.PROTECTION, rnd > 66 ? 2 : 1));
					}
				}

				fullSet.forEach(dropItem);
			}
			else { // Не первый лаки блок

				int category = FRand.nextInt(random, 1, broken);
				if (category > 10)
					category = 10;

				int variant = FRand.nextInt(random, 1, 2);

				// Вариант 1 - дроп предметов
				// Вариант 2 - изменение игровых условий

				switch (category) {
					case 1 -> {
						switch (variant) {
							case 1 -> { // Один элемент брони (алм, жел; защ 0-5; прочн 0-5)
								Item item =
								switch (FRand.nextInt(random, 1, 8)) {
									case 1 -> Items.DIAMOND_HELMET();
									case 2 -> Items.DIAMOND_CHESTPLATE();
									case 3 -> Items.DIAMOND_LEGGINGS();
									case 4 -> Items.DIAMOND_BOOTS();
									case 5 -> Items.IRON_HELMET();
									case 6 -> Items.IRON_CHESTPLATE();
									case 7 -> Items.IRON_LEGGINGS();
									case 8 -> Items.IRON_BOOTS();
									default -> null;
								};
								if (item != null) {
									item.addEnchantment(new EnchantmentInstance(Enchantments.PROTECTION, FRand.nextInt(random, 1, 4)));
									item.addEnchantment(new EnchantmentInstance(Enchantments.UNBREAKING, FRand.nextInt(random, 1, 3)));

									dropItem.accept(item);
								}
							}
							case 2 -> { // Спавнит зажженный динамит в 3х0х3 над лб
								world.setBlock(event.block().addY(1), Blocks.TNT());
								if (world.getBlock(event.block()) instanceof BlockTNT tnt) {
									tnt.ignite(55);
								}
							}
						}
					}
					case 2 -> {
						switch (variant) {
							case 1 -> { // Меч (80%) ИЛИ топор (18%) ИЛИ иной инструмент (2%)
								Item item = switch (FRand.nextInt(random, 1, 12)) {
									case 1, 2, 3, 4, 5, 6, 7 -> switch (FRand.nextInt(random, 1, 4)) {
										case 1 -> Items.DIAMOND_SWORD();
										case 2 -> Items.IRON_SWORD();
										case 3 -> Items.GOLDEN_SWORD();
										case 4 -> Items.STONE_SWORD();
										default -> null;
									};
									case 8, 9, 10 -> switch (FRand.nextInt(random, 1, 4)) {
										case 1 -> Items.DIAMOND_AXE();
										case 2 -> Items.IRON_AXE();
										case 3 -> Items.GOLDEN_AXE();
										case 4 -> Items.STONE_AXE();
										default -> null;
									};
									case 11 -> switch (FRand.nextInt(random, 1, 4)) {
										case 1 -> Items.DIAMOND_PICKAXE();
										case 2 -> Items.IRON_PICKAXE();
										case 3 -> Items.GOLDEN_PICKAXE();
										case 4 -> Items.STONE_PICKAXE();
										default -> null;
									};
									case 12 -> switch (FRand.nextInt(random, 1, 4)) {
										case 1 -> Items.DIAMOND_SHOVEL();
										case 2 -> Items.IRON_SHOVEL();
										case 3 -> Items.GOLDEN_SHOVEL();
										case 4 -> Items.STONE_SHOVEL();
										default -> null;
									};
									default -> null;
								};
							}
							case 2 -> { // Создает 3 крипера
								for (int i = 0; i < 3; i++) {
									EntityCreeper creeper = new EntityCreeper(new Location(event.block(), world, FRand.nextInt(0, 359), 0));
									creeper.spawn();
								}
							}
						}
					}
					case 3 -> {
						switch (variant) {
							case 1 -> { // УДАЧЛИВ. Золотой сет (защ 7-10)
								Arrays.asList(Items.GOLDEN_HELMET(), Items.GOLDEN_CHESTPLATE(), Items.GOLDEN_LEGGINGS(), Items.GOLDEN_BOOTS()).forEach(item -> {
									item.addEnchantment(new EnchantmentInstance(Enchantments.PROTECTION, random.nextBoolean() ? 3 : 4));
									dropItem.accept(item);
								});
							}
							case 2 -> { // Создает орду из 20 зомби
								for (int i = 0; i < 10; i++) {
									EntityZombie creeper = new EntityZombie(new Location(event.block(), world, FRand.nextInt(0, 359), 0));
									creeper.spawn();
								}
							}
						}
					}
					case 4 -> {
						switch (variant) {
							case 1 -> { // ОЧЕНЬ УДАЧЛИВ. Алм. Нагр. (Защ 9-10; прочн 5)
								Item item = Items.DIAMOND_CHESTPLATE();
								item.addEnchantment(new EnchantmentInstance(Enchantments.PROTECTION, 4));
								item.addEnchantment(new EnchantmentInstance(Enchantments.UNBREAKING, 3));
								//item.addEnchantment(new EnchantmentInstance(Enchantments., 3)); // Еще что-то
							}
							case 2 -> { // ОЧ. НЕУДАЧЛ. Создает скелета с луком на откидывание 3
								EntitySkeleton creeper = new EntitySkeleton(new Location(event.block(), world, FRand.nextInt(0, 359), 0));

								// TODO почему то equipment не public

								creeper.spawn();
							}
						}
					}
					case 5 -> {
						switch (variant) {
							case 1 -> { // УДАЧЛИВ. Лук и стрелы (16-64) (бескон(70%), сила 2-3(50%), поджог 1-2 (50%))
								Item bow = Items.BOW();

								if (random.nextInt(100) < 30) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.INFINITY));
								}

								if (random.nextInt(100) < 50) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.POWER));
								}

								if (random.nextInt(100) < 50) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.FLAME));
								}

								dropItem.accept(bow);

								dropItem.accept(Items.ARROW().setCount(FRand.nextInt(random, 16, 64)));
							}
							case 2 -> { // Создает коробку 3х3х3 с открытым верхом вокруг игрока, в центре которой появляется зажженный тнт
								for (int yy = 0; yy <= 1; yy++) {
									for (int xx = -1; xx <= 1; xx++) {
										for (int zz = -1; zz <= 1; zz++) {
											if (xx == 0 || zz == 0)
												continue;

											world.setBlock(floorX() + xx, floorY() + yy, floorZ() + zz, Blocks.STONE());
										}
									}
								}

								world.setBlock(this.addY(1), Blocks.TNT());
								if (world.getBlock(this.addY(1)) instanceof BlockTNT tnt) {
									tnt.ignite(80);
								}
							}
						}
					}
					case 6 -> {
						switch (variant) {
							case 1 -> { // ОЧЕНЬ УДАЧЛИВ. Лук и стрелы (беск (70%, сила 4-5 (70%), поджог 2-4 (70%), откидывание 1-5 (100%))
								Item bow = Items.BOW();

								if (random.nextInt(100) < 50) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.INFINITY));
								}

								if (random.nextInt(100) < 70) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.POWER, FRand.nextInt(random, 3, 4)));
								}

								if (random.nextInt(100) < 60) {
									bow.addEnchantment(new EnchantmentInstance(Enchantments.FLAME));
								}

								bow.addEnchantment(new EnchantmentInstance(Enchantments.PUNCH, random.nextBoolean() ? 1 : 2));

								dropItem.accept(bow);

								dropItem.accept(Items.ARROW().setCount(FRand.nextInt(random, 16, 64)));
							}
							case 2 -> { // Создает паутину в игроке
								world.setBlock(this, Blocks.COBWEB());
							}
						}
					}
					case 7 -> {
						switch (variant) {
							case 1 -> { // УДАЧЛИВ. Око эндера 1-3
								dropItem.accept(Items.ENDER_PEARL().setCount(FRand.nextInt(random, 1, 3)));
							}
							case 2 -> { // Заменяет в радиусе 32 блоков от игрока всё на блоки слизи
								Block slime = BlockFactory.get(BlockIds.SLIME);

								for (int x = -10; x < 10; x++) {
									for (int y = -10; y < 10; y++) {
										for (int z = -10; z < 10; z++) {
											int block = world.getBlockId(floorX() + x, floorY() + y, floorZ() + z);
											if (block == 0 || block == BlockIds.SPONGE)
												continue;

											world.setBlock(floorX() + x, floorY() + y, floorZ() + z, slime);
										}
									}
								}
							}
						}
					}
					case 8 -> {
						switch (variant) {
							case 1 -> { // УДАЧЛИВ. Сгусток лизи (строит платформу 3х0х3 из слизи под игроком при падении в бездну (на 10 высоте), либо при использовании) дает зелье с эффектом прыгучесть 50 на 3 секунды
								dropItem.accept(Items.SLIMEBALL());

								// TODO обработка юза и падения в бездну
							}
							case 2 -> { // Создает платформу под игроком в радиусе 8 и эффект прыгучесть 3 на 30 сек
								for (int x = -8; x < 8; x++) {
									for (int z = -8; z < 8; z++) {
										int block = world.getBlockId(floorX() + x, floorY() - 1, floorZ() + z);
										if (block == BlockIds.SPONGE)
											continue;

										world.setBlock(floorX() + x, floorY() - 1, floorZ() + z, Blocks.STONE());
									}
								}

								effects().add(new EffectInstance(Effects.JUMP_BOOST, 30 * 20, 3));
							}
						}
					}
					case 9 -> {
						switch (variant) {
							case 1 -> { // Взрывное зелье (урон 1-2 или лечение 1-2 или огнестойкость 1-2 или регенерация 1-2 или сила 1-2)
								Item item = switch (FRand.nextInt(random, 1, 5)) {
									case 1 -> ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.FIRE_RESISTANCE.ordinal());
									case 2 -> ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.HARMING.ordinal());
									case 3 -> ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.REGENERATION.ordinal());
									case 4 -> ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.HEALING.ordinal());
									case 5 -> ItemFactory.get(ItemIds.SPLASH_POTION, PotionType.STRENGTH.ordinal());
									default -> null;
								};
								if (item != null) {
									dropItem.accept(item);
								}
							}
							case 2 -> { // Создает ифрита
								EntityBlaze creeper = new EntityBlaze(new Location(event.block(), world, FRand.nextInt(0, 359), 0));

								creeper.spawn();
							}
						}
					}
					case 10 -> {
						switch (variant) {
							case 1, 2 -> { // Динамит и зажигалка
								dropItem.accept(Blocks.TNT().asItem().setCount(FRand.nextInt(random, 1, 3)));
								dropItem.accept(Items.FLINT_AND_STEEL());
							}
						}
					}
				}
			}


			if (lucky) { // TODO разные звуки и партиклы
				world.addSound(event.block(), Sound.XP_COLLECT);
				world.addParticle(event.block().add(0.5f, 0.7f, 0.5f), Particle.FLAME);
				world.addSound(event.block(), Sound.XP_COLLECT);
			}
			/*if (FRand.random().nextInt(200) == 1) {
				world.setBlock(event.block(), Blocks.TNT());
				if (world.getBlock(event.block()) instanceof BlockTNT tnt) {
					tnt.ignite(55);
				}
			}*/

			++gameCtx().brokenLBs;
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
