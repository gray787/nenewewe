package com.example.wizardingworld;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.particles.ParticleTypes;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.commands.Commands.*;

public class WizardingWorldMod implements ModInitializer {
    public static final String MOD_ID = "wizardingworld";
    public static final Map<String, Item> WANDS = new LinkedHashMap<>();
    public static final Map<String, Item> INGREDIENTS = new LinkedHashMap<>();
    public static final Map<String, Item> POTIONS = new LinkedHashMap<>();
    public static final Map<String, Item> BROOMS = new LinkedHashMap<>();
    public static final Map<String, Spell> SPELLS = new LinkedHashMap<>();
    public static final Set<String> LEARNED = ConcurrentHashMap.newKeySet();
    public static final Map<UUID, Integer> QUIZ_ANSWERS = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer[]> HOUSE_POINTS = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> LAST_CAST = new ConcurrentHashMap<>();
    public static final Map<UUID, String> SELECTED_SPELL = new ConcurrentHashMap<>();
    public static Item SPELL_SCROLL, ELDER_WAND, BROOM, QUAFFLE, BLUDGER, SNITCH, GALLEON, SICKLE, KNUT;

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
    private static Item register(String name, Item item) {
        return BuiltInRegistries.ITEM.register(id(name), item);
    }
    private static ItemStack named(Item item, String name, String lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(lore))));
        return stack;
    }

    @Override public void onInitialize() {
        registerItems();
        registerSpells();
        registerCommands();
        registerUseEvents();
    }

    private void registerItems() {
        String[] woods = {"acacia","alder","apple","ash","aspen","beech","blackthorn","black_walnut","cedar","cherry",
                "chestnut","cypress","dogwood","ebony","elm","english_oak","fir","hawthorn","hazel","holly",
                "hornbeam","larch","laurel","mahogany","maple","pear","pine","poplar","red_oak","redwood","rowan",
                "silver_lime","spruce","sycamore","tamarack","vine","walnut","willow","yew","dragonheart","phoenix_feather",
                "unicorn_hair","veela_hair","thestral_hair","basilisk_horn","thunderbird_tail","kelpie_hair",
                "wampus_cat_hair","snallygaster_heartstring"};
        for (String wood : woods) WANDS.put(wood, register(wood + "_wand", new Item(new Item.Properties().stacksTo(1))));
        ELDER_WAND = register("elder_wand", new Item(new Item.Properties().stacksTo(1).fireResistant()));
        SPELL_SCROLL = register("spell_scroll", new Item(new Item.Properties()));
        BROOM = register("broom", new Item(new Item.Properties().stacksTo(1)));
        QUAFFLE = register("quaffle", new Item(new Item.Properties().stacksTo(1)));
        BLUDGER = register("bludger", new Item(new Item.Properties().stacksTo(1)));
        SNITCH = register("golden_snitch", new Item(new Item.Properties().stacksTo(1)));
        GALLEON = register("galleon", new Item(new Item.Properties()));
        SICKLE = register("sickle", new Item(new Item.Properties()));
        KNUT = register("knut", new Item(new Item.Properties()));
        BROOMS.put("cleansweep_11", register("cleansweep_11", new Item(new Item.Properties().stacksTo(1))));
        BROOMS.put("nimbus_2000", register("nimbus_2000", new Item(new Item.Properties().stacksTo(1))));
        BROOMS.put("firebolt", register("firebolt", new Item(new Item.Properties().stacksTo(1).fireResistant())));
        BROOMS.put("comet_260", register("comet_260", new Item(new Item.Properties().stacksTo(1))));
        String[] ingredientIds = {"ingredient_001","ingredient_002","ingredient_003","ingredient_004","ingredient_005","ingredient_006","ingredient_007","ingredient_008","ingredient_009","ingredient_010","ingredient_011","ingredient_012","ingredient_013","ingredient_014","ingredient_015","ingredient_016","ingredient_017","ingredient_018","ingredient_019","ingredient_020","ingredient_021","ingredient_022","ingredient_023","ingredient_024","ingredient_025","ingredient_026","ingredient_027","ingredient_028","ingredient_029","ingredient_030","ingredient_031","ingredient_032","ingredient_033","ingredient_034","ingredient_035","ingredient_036","ingredient_037","ingredient_038","ingredient_039","ingredient_040","ingredient_041","ingredient_042","ingredient_043","ingredient_044","ingredient_045","ingredient_046","ingredient_047","ingredient_048","ingredient_049","ingredient_050","ingredient_051","ingredient_052","ingredient_053","ingredient_054","ingredient_055","ingredient_056","ingredient_057","ingredient_058","ingredient_059","ingredient_060","ingredient_061","ingredient_062","ingredient_063","ingredient_064","ingredient_065","ingredient_066","ingredient_067","ingredient_068","ingredient_069","ingredient_070","ingredient_071","ingredient_072","ingredient_073","ingredient_074","ingredient_075","ingredient_076","ingredient_077","ingredient_078","ingredient_079","ingredient_080","ingredient_081","ingredient_082","ingredient_083","ingredient_084","ingredient_085","ingredient_086","ingredient_087","ingredient_088","ingredient_089","ingredient_090","ingredient_091","ingredient_092","ingredient_093","ingredient_094","ingredient_095","ingredient_096","ingredient_097","ingredient_098","ingredient_099","ingredient_100","ingredient_101","ingredient_102","ingredient_103","ingredient_104","ingredient_105","ingredient_106","ingredient_107","ingredient_108","ingredient_109","ingredient_110","ingredient_111","ingredient_112","ingredient_113","ingredient_114","ingredient_115","ingredient_116","ingredient_117","ingredient_118","ingredient_119","ingredient_120","ingredient_121","ingredient_122","ingredient_123","ingredient_124","ingredient_125","ingredient_126","ingredient_127","ingredient_128","ingredient_129","ingredient_130","ingredient_131","ingredient_132","ingredient_133","ingredient_134","ingredient_135","ingredient_136","ingredient_137","ingredient_138","ingredient_139","ingredient_140","ingredient_141","ingredient_142","ingredient_143","ingredient_144","ingredient_145","ingredient_146","ingredient_147","ingredient_148","ingredient_149","ingredient_150","ingredient_151","ingredient_152","ingredient_153","ingredient_154","ingredient_155","ingredient_156","ingredient_157","ingredient_158","ingredient_159","ingredient_160","ingredient_161","ingredient_162","ingredient_163","ingredient_164","ingredient_165","ingredient_166","ingredient_167","ingredient_168","ingredient_169","ingredient_170","ingredient_171","ingredient_172","ingredient_173","ingredient_174","ingredient_175","ingredient_176","ingredient_177","ingredient_178","ingredient_179","ingredient_180","ingredient_181","ingredient_182","ingredient_183","ingredient_184","ingredient_185","ingredient_186","ingredient_187","ingredient_188","ingredient_189","ingredient_190","ingredient_191","ingredient_192","ingredient_193","ingredient_194","ingredient_195","ingredient_196","ingredient_197","ingredient_198","ingredient_199","ingredient_200"};
        for (String ingredient : ingredientIds) INGREDIENTS.put(ingredient, register(ingredient, new Item(new Item.Properties())));
        String[] potionIds = {"potion_001","potion_002","potion_003","potion_004","potion_005","potion_006","potion_007","potion_008","potion_009","potion_010","potion_011","potion_012","potion_013","potion_014","potion_015","potion_016","potion_017","potion_018","potion_019","potion_020","potion_021","potion_022","potion_023","potion_024","potion_025","potion_026","potion_027","potion_028","potion_029","potion_030","potion_031","potion_032","potion_033","potion_034","potion_035","potion_036","potion_037","potion_038","potion_039","potion_040","potion_041","potion_042","potion_043","potion_044","potion_045","potion_046","potion_047","potion_048","potion_049","potion_050","potion_051","potion_052","potion_053","potion_054","potion_055","potion_056","potion_057","potion_058","potion_059","potion_060","potion_061","potion_062","potion_063","potion_064","potion_065","potion_066","potion_067","potion_068","potion_069","potion_070","potion_071","potion_072","potion_073","potion_074","potion_075","potion_076","potion_077","potion_078","potion_079","potion_080","potion_081","potion_082","potion_083","potion_084","potion_085","potion_086","potion_087","potion_088","potion_089","potion_090","potion_091","potion_092","potion_093","potion_094","potion_095","potion_096","potion_097","potion_098","potion_099","potion_100"};
        for (String potion : potionIds) POTIONS.put(potion, register(potion, new Item(new Item.Properties().stacksTo(16))));
    }

    private void registerSpells() {
        try (var stream = WizardingWorldMod.class.getResourceAsStream("/data/wizardingworld/wizarding/spells.json")) {
            if (stream == null) throw new IllegalStateException("Missing spells.json");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var element : root.getAsJsonArray("spells")) {
                JsonObject entry = element.getAsJsonObject();
                String sid = entry.get("id").getAsString();
                String name = entry.get("name").getAsString();
                String description = entry.get("description").getAsString();
                String effectName = entry.get("effect").getAsString().toUpperCase(Locale.ROOT);
                Spell.Effect effect;
                try { effect = Spell.Effect.valueOf(effectName); }
                catch (IllegalArgumentException ignored) { effect = Spell.Effect.GLOW; }
                SPELLS.put(sid, new Spell(sid, name, description, effect));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Could not load the 2,000-spell archive", ex);
        }
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("hogwarts")
                .then(literal("spawn").executes(ctx -> buildHogwarts(ctx.getSource().getPlayerOrException())))
                .then(literal("sorting").executes(ctx -> startQuiz(ctx.getSource().getPlayerOrException())))
                .then(literal("diagon").then(literal("spawn").executes(ctx -> buildDiagon(ctx.getSource().getPlayerOrException()))))
                .then(literal("platform").then(literal("spawn").executes(ctx -> buildPlatform(ctx.getSource().getPlayerOrException()))))
                .then(literal("wallet").executes(ctx -> wallet(ctx.getSource().getPlayerOrException())))
                .then(literal("spells").executes(ctx -> spellList(ctx.getSource().getPlayerOrException())))
                .then(literal("spellpage").then(argument("page", IntegerArgumentType.integer(1,100)).executes(ctx -> spellPage(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "page")))))
                .then(literal("vendors").executes(ctx -> spawnVendors(ctx.getSource().getPlayerOrException())))
                .then(literal("quidditch").executes(ctx -> giveQuidditch(ctx.getSource().getPlayerOrException())))
                .then(literal("wand").then(literal("elder").executes(ctx -> give(ctx.getSource().getPlayerOrException(), named(ELDER_WAND,"The Elder Wand","All 2,000 spells unlocked; no casting cooldown.")))))
                .then(literal("answer").then(argument("house", IntegerArgumentType.integer(1,4)).executes(ctx -> answerQuiz(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"house")))))
                .then(literal("learn").then(argument("spell", IntegerArgumentType.integer(1,2000)).executes(ctx -> learnScroll(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"spell")))))
                .then(literal("scroll").then(argument("spell", IntegerArgumentType.integer(1,2000)).executes(ctx -> giveScroll(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"spell")))))
                .then(literal("spell").then(argument("spell", IntegerArgumentType.integer(1,2000)).executes(ctx -> selectSpell(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"spell")))))
                .then(literal("shop").then(literal("wand").executes(ctx -> shop(ctx.getSource().getPlayerOrException(),"wand")))
                    .then(literal("broom").executes(ctx -> shop(ctx.getSource().getPlayerOrException(),"broom")))
                    .then(literal("magic").executes(ctx -> shop(ctx.getSource().getPlayerOrException(),"magic"))))
                .then(literal("broomstop").executes(ctx -> stopBroom(ctx.getSource().getPlayerOrException())))
                .then(literal("currency").then(literal("galleon").executes(ctx -> give(ctx.getSource().getPlayerOrException(), new ItemStack(GALLEON,1))))
                    .then(literal("sickle").executes(ctx -> give(ctx.getSource().getPlayerOrException(), new ItemStack(SICKLE,1))))
                    .then(literal("knut").executes(ctx -> give(ctx.getSource().getPlayerOrException(), new ItemStack(KNUT,1)))))
            );
            dispatcher.register(literal("harrypotter")
                .then(literal("hogwarts").executes(ctx -> buildHogwarts(ctx.getSource().getPlayerOrException())))
                .then(literal("9").then(argument("platform", StringArgumentType.greedyString()).executes(ctx -> {
                    String platform = StringArgumentType.getString(ctx, "platform");
                    if (platform.equals("3/4")) return buildPlatform(ctx.getSource().getPlayerOrException());
                    ctx.getSource().sendFailure(Component.literal("Use /harrypotter 9 3/4"));
                    return 0;
                })))
                .then(literal("dursleys").executes(ctx -> buildDursleys(ctx.getSource().getPlayerOrException())))
                .then(literal("quidditch").then(literal("world").then(literal("cup").executes(ctx -> buildWorldCup(ctx.getSource().getPlayerOrException())))))
                .then(literal("elderwand").executes(ctx -> give(ctx.getSource().getPlayerOrException(), named(ELDER_WAND,"The Elder Wand","Access to the complete spell archive; no cooldowns."))))
                .then(literal("quidditchballs").executes(ctx -> giveQuidditch(ctx.getSource().getPlayerOrException())))
                .then(literal("wallet").executes(ctx -> wallet(ctx.getSource().getPlayerOrException())))
                .then(literal("spells").executes(ctx -> spellList(ctx.getSource().getPlayerOrException())))
                .then(literal("spellpage").then(argument("page", IntegerArgumentType.integer(1,100)).executes(ctx -> spellPage(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "page")))))
                .then(literal("spell").then(argument("spell", IntegerArgumentType.integer(1,2000)).executes(ctx -> selectSpell(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"spell")))))
                .then(literal("wand").then(argument("number", IntegerArgumentType.integer(1,50)).executes(ctx -> giveWand(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx,"number")))))
                .then(literal("ingredient").then(argument("number", IntegerArgumentType.integer(1,200)).executes(ctx -> giveIndexed(ctx.getSource().getPlayerOrException(), INGREDIENTS, IntegerArgumentType.getInteger(ctx,"number"), "ingredient"))))
                .then(literal("potion").then(argument("number", IntegerArgumentType.integer(1,100)).executes(ctx -> giveIndexed(ctx.getSource().getPlayerOrException(), POTIONS, IntegerArgumentType.getInteger(ctx,"number"), "potion"))))
                .then(literal("brooms").executes(ctx -> giveBrooms(ctx.getSource().getPlayerOrException())))
                .then(literal("cauldron").executes(ctx -> placeCauldron(ctx.getSource().getPlayerOrException())))
            );
        });
    }

    private void registerUseEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (world.isClientSide()) return InteractionResult.PASS;
            if (stack.is(ELDER_WAND) || WANDS.containsValue(stack.getItem())) {
                cast(player, stack.is(ELDER_WAND) ? "all" : null);
                return InteractionResult.SUCCESS;
            }
            if (stack.is(SPELL_SCROLL)) {
                if (player instanceof ServerPlayer sp) {
                    String scrollName = stack.getHoverName().getString().replace(" Scroll", "");
                    Spell spell = SPELLS.values().stream().filter(candidate -> candidate.name().equals(scrollName))
                            .findFirst().orElse(SPELLS.get("lumos"));
                    LEARNED.add(sp.getUUID()+":"+spell.id());
                    sp.sendSystemMessage(Component.literal("Learned " + spell.name() + ": " + spell.description()));
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (POTIONS.containsValue(stack.getItem())) {
                if (player instanceof ServerPlayer sp) {
                    sp.heal(8.0f);
                    sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 12, 1));
                    sp.sendSystemMessage(Component.literal("You drink a wizarding potion. Its restorative magic takes effect."));
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (BROOMS.containsValue(stack.getItem()) || stack.is(BROOM)) {
                if (player instanceof ServerPlayer sp) {
                    sp.getAbilities().mayfly = true;
                    sp.getAbilities().flying = true;
                    sp.onUpdateAbilities();
                    sp.sendSystemMessage(Component.literal("Your broom lifts you into the air! Sneak and use /hogwarts broomstop to dismount."));
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(QUAFFLE) || stack.is(BLUDGER) || stack.is(SNITCH)) {
                throwBall(player, stack, world);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    private static void cast(Player player, String ignored) {
        boolean elder = player.getMainHandItem().is(ELDER_WAND) || player.getOffhandItem().is(ELDER_WAND);
        String selected = SELECTED_SPELL.getOrDefault(player.getUUID(), "lumos");
        Spell spell = SPELLS.getOrDefault(selected, SPELLS.get("lumos"));
        if (elder) {
            player.displayClientMessage(Component.literal("The Elder Wand grants access to the entire spell archive."), true);
        } else if (!LEARNED.contains(player.getUUID()+":"+spell.id())) {
            player.displayClientMessage(Component.literal("You have not learned " + spell.name() + ". Find its scroll!"), true);
            return;
        }
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            var look = player.getLookAngle();
            var eye = player.getEyePosition();
            for (int i = 1; i <= 24; i++) {
                double t = i * 0.55;
                serverLevel.sendParticles(ParticleTypes.END_ROD, eye.x + look.x * t, eye.y + look.y * t,
                        eye.z + look.z * t, 1, 0.015, 0.015, 0.015, 0.0);
            }
        }
        switch (spell.effect()) {
            case LIGHT -> { player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20*30, 0)); }
            case EXTINGUISH -> player.removeEffect(MobEffects.NIGHT_VISION);
            case PULL_ITEMS -> level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, player.getBoundingBox().inflate(8))
                    .forEach(item -> item.setDeltaMovement(player.position().subtract(item.position()).normalize().scale(.55)));
            case SHIELD -> player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 2));
            case LEVITATE -> level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(7)).stream().findFirst()
                    .ifPresent(m -> m.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1)));
            case FIRE -> level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(7)).stream().findFirst()
                    .ifPresent(m -> m.igniteForSeconds(5));
            case WATER -> player.extinguishFire();
            case HEAL, REPAIR -> player.heal(6);
            case STUN, FREEZE, SLOW -> level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(7)).stream().findFirst()
                    .ifPresent(m -> m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 5)));
            case PATRONUS, GLOW, REVEAL -> level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(12))
                    .forEach(e -> { if (e instanceof Mob m) m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0)); });
            case CONFUSE -> level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(7)).stream().findFirst()
                    .ifPresent(m -> m.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0)));
            case DISARM, KNOCKBACK -> level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(7)).stream().findFirst()
                    .ifPresent(m -> m.knockback(1.5, player.getX()-m.getX(), player.getZ()-m.getZ()));
            case TELEPORT -> player.teleportTo(player.getX(), player.getY()+5, player.getZ());
            case CLEANSE -> player.removeAllEffects();
        }
        player.displayClientMessage(Component.literal("Cast " + spell.name() + " — " + spell.description()), true);
    }

    private static int buildHogwarts(ServerPlayer p) {
        BlockPos o=p.blockPosition();
        if (!p.level().getBlockState(o).isAir()) { p.sendSystemMessage(Component.literal("Spawn cancelled: build origin obstructed.")); return 0; }
        ServerLevel l=p.serverLevel();
        // All placement is air-only; preflight every footprint cell to avoid partial destructive placement.
        if (!areaClear(l,o.offset(-36,0,-24),o.offset(30,7,24))) {
            p.sendSystemMessage(Component.literal("Spawn cancelled: Hogwarts footprint is obstructed. Choose a clear area.")); return 0;
        }
        shell(l,o.offset(-9,0,-7),o.offset(9,6,7),Blocks.STONE_BRICKS.defaultBlockState());
        fillAir(l,o.offset(-8,1,-6),o.offset(8,1,6),Blocks.POLISHED_ANDESITE.defaultBlockState());
        fillAir(l,o.offset(-7,6,-5),o.offset(7,6,5),Blocks.DARK_OAK_PLANKS.defaultBlockState());
        l.setBlock(o.above(),Blocks.LECTERN.defaultBlockState(),3);
        l.setBlock(o.offset(0,2,0),Blocks.LANTERN.defaultBlockState(),3);
        // Great Hall tables
        fillAir(l,o.offset(-6,1,-3),o.offset(6,1,-3),Blocks.SPRUCE_PLANKS.defaultBlockState());
        fillAir(l,o.offset(-6,1,3),o.offset(6,1,3),Blocks.SPRUCE_PLANKS.defaultBlockState());
        // Hagrid's hut, offset east
        shell(l,o.offset(22,0,-4),o.offset(28,5,4),Blocks.SPRUCE_PLANKS.defaultBlockState());
        // Quidditch pitch, west of the hall; boundary is a visible fence.
        fillAir(l,o.offset(-34,0,-22),o.offset(-34,0,22),Blocks.GRASS_BLOCK.defaultBlockState());
        fillAir(l,o.offset(-34,1,-22),o.offset(34,1,22),Blocks.WHITE_CONCRETE.defaultBlockState());
        for (int y=1;y<=4;y++) {
            fillAir(l,o.offset(-32,y,-20),o.offset(-32,y,20),Blocks.OAK_FENCE.defaultBlockState());
            fillAir(l,o.offset(32,y,-20),o.offset(32,y,20),Blocks.OAK_FENCE.defaultBlockState());
            fillAir(l,o.offset(-32,y,-20),o.offset(32,y,-20),Blocks.OAK_FENCE.defaultBlockState());
            fillAir(l,o.offset(-32,y,20),o.offset(32,y,20),Blocks.OAK_FENCE.defaultBlockState());
        }
        // A compact Forbidden Forest grove beyond the pitch.
        Random random = new Random(263);
        for (int i=0;i<38;i++) {
            int x=-46+random.nextInt(22), z=-17+random.nextInt(35);
            BlockPos trunk=o.offset(x,1,z);
            for(int y=0;y<4+random.nextInt(3);y++) if(l.getBlockState(trunk.above(y)).isAir())
                l.setBlock(trunk.above(y),Blocks.DARK_OAK_LOG.defaultBlockState(),3);
            BlockPos crown=trunk.above(4);
            for(int dx=-2;dx<=2;dx++) for(int dy=-1;dy<=2;dy++) for(int dz=-2;dz<=2;dz++) {
                BlockPos leaf=crown.offset(dx,dy,dz);
                if(l.getBlockState(leaf).isAir() && Math.abs(dx)+Math.abs(dz)+Math.max(0,dy)<=4)
                    l.setBlock(leaf,Blocks.DARK_OAK_LEAVES.defaultBlockState(),3);
            }
        }
        p.sendSystemMessage(Component.literal("Hogwarts grounds created: Great Hall, Hagrid's hut, Forbidden Forest and Quidditch pitch."));
        return Command.SINGLE_SUCCESS;
    }

    private static int buildDursleys(ServerPlayer p) {
        ServerLevel l=p.serverLevel(); BlockPos o=p.blockPosition();
        if (!areaClear(l,o.offset(-9,0,-7),o.offset(9,7,7))) { p.sendSystemMessage(Component.literal("Privet Drive house cancelled: choose a clear area.")); return 0; }
        shell(l,o.offset(-6,0,-4),o.offset(6,5,4),Blocks.BRICKS.defaultBlockState());
        fillAir(l,o.offset(-5,1,-3),o.offset(5,1,3),Blocks.OAK_PLANKS.defaultBlockState());
        fillAir(l,o.offset(-1,1,-4),o.offset(1,2,-4),Blocks.GLASS_PANE.defaultBlockState());
        fillAir(l,o.offset(-4,2,-4),o.offset(-3,3,-4),Blocks.GLASS_PANE.defaultBlockState());
        fillAir(l,o.offset(3,2,-4),o.offset(4,3,-4),Blocks.GLASS_PANE.defaultBlockState());
        l.setBlock(o.offset(0,1,4),Blocks.OAK_DOOR.defaultBlockState(),3);
        l.setBlock(o.offset(0,1,0),Blocks.CRAFTING_TABLE.defaultBlockState(),3);
        l.setBlock(o.offset(2,1,1),Blocks.FURNACE.defaultBlockState(),3);
        p.sendSystemMessage(Component.literal("The Dursleys' house on Privet Drive has been built."));
        return Command.SINGLE_SUCCESS;
    }

    private static int buildWorldCup(ServerPlayer p) {
        ServerLevel l=p.serverLevel(); BlockPos o=p.blockPosition();
        if (!areaClear(l,o.offset(-34,0,-22),o.offset(34,12,22))) { p.sendSystemMessage(Component.literal("Quidditch World Cup cancelled: choose a clear area.")); return 0; }
        fillAir(l,o.offset(-30,0,-18),o.offset(30,0,18),Blocks.GRASS_BLOCK.defaultBlockState());
        fillAir(l,o.offset(-30,1,-18),o.offset(30,1,18),Blocks.WHITE_CONCRETE.defaultBlockState());
        for (int x : new int[]{-26,26}) for (int z : new int[]{-8,0,8}) {
            for (int y=1;y<=5;y++) l.setBlock(o.offset(x,y,z),Blocks.OAK_FENCE.defaultBlockState(),3);
            l.setBlock(o.offset(x,6,z),Blocks.GOLD_BLOCK.defaultBlockState(),3);
        }
        for (int z=-18;z<=18;z+=3) {
            l.setBlock(o.offset(-30,1,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),3);
            l.setBlock(o.offset(30,1,z),Blocks.SPRUCE_PLANKS.defaultBlockState(),3);
        }
        shell(l,o.offset(-8,1,20),o.offset(8,8,22),Blocks.RED_WOOL.defaultBlockState());
        l.setBlock(o.offset(0,1,19),Blocks.CAULDRON.defaultBlockState(),3);
        p.sendSystemMessage(Component.literal("Quidditch World Cup stadium created with pitch, goal hoops and grandstand."));
        return Command.SINGLE_SUCCESS;
    }

    private static boolean areaClear(ServerLevel l, BlockPos a, BlockPos b) {
        for (BlockPos pos : BlockPos.betweenClosed(a,b)) if (!l.getBlockState(pos).isAir()) return false;
        return true;
    }
    private static void fillAir(ServerLevel l, BlockPos a, BlockPos b, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(a,b)) if (l.getBlockState(pos).isAir()) l.setBlock(pos,state,3);
    }
    private static void shell(ServerLevel l, BlockPos a, BlockPos b, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(a,b)) {
            if (pos.getX()==a.getX() || pos.getX()==b.getX() || pos.getY()==a.getY() || pos.getY()==b.getY()
                    || pos.getZ()==a.getZ() || pos.getZ()==b.getZ()) {
                if (l.getBlockState(pos).isAir()) l.setBlock(pos,state,3);
            }
        }
    }
    private static int buildDiagon(ServerPlayer p) {
        ServerLevel l=p.serverLevel(); BlockPos o=p.blockPosition();
        if (!areaClear(l,o.offset(-10,0,-5),o.offset(10,5,5))) { p.sendSystemMessage(Component.literal("Diagon Alley cancelled: area obstructed.")); return 0; }
        shell(l,o.offset(-10,0,-5),o.offset(-4,4,5),Blocks.BRICKS.defaultBlockState());
        shell(l,o.offset(-3,0,-5),o.offset(3,4,5),Blocks.OAK_PLANKS.defaultBlockState());
        shell(l,o.offset(4,0,-5),o.offset(10,4,5),Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        spawnVendor(p,"Ollivanders — Wands",o.offset(-7,1,0));
        spawnVendor(p,"Quality Quidditch Supplies",o.offset(0,1,0));
        spawnVendor(p,"Magical Menagerie",o.offset(7,1,0));
        return Command.SINGLE_SUCCESS;
    }
    private static int buildPlatform(ServerPlayer p) {
        ServerLevel l=p.serverLevel(); BlockPos o=p.blockPosition();
        if (!areaClear(l,o.offset(-5,0,-3),o.offset(5,4,3))) { p.sendSystemMessage(Component.literal("Platform cancelled: area obstructed.")); return 0; }
        fillAir(l,o.offset(-5,0,-3),o.offset(5,0,3),Blocks.STONE_BRICKS.defaultBlockState());
        fillAir(l,o.offset(-4,1,-2),o.offset(4,1,2),Blocks.RED_NETHER_BRICKS.defaultBlockState());
        l.setBlock(o.above(),Blocks.LECTERN.defaultBlockState(),3);
        return Command.SINGLE_SUCCESS;
    }
    private static void spawnVendor(ServerPlayer p,String name,BlockPos pos) {
        Villager v=EntityTypes.VILLAGER.create(p.serverLevel(), EntitySpawnReason.COMMAND);
        if(v!=null){v.snapTo(pos,0,0);v.setCustomName(Component.literal(name));v.setCustomNameVisible(true);p.serverLevel().addFreshEntity(v);}
    }
    private static int spawnVendors(ServerPlayer p) {
        BlockPos o=p.blockPosition();
        spawnVendor(p,"Ollivanders — Wands",o.offset(2,0,0));
        spawnVendor(p,"Broom Emporium",o.offset(4,0,0));
        spawnVendor(p,"Magical Supplies",o.offset(6,0,0));
        p.sendSystemMessage(Component.literal("Three named wizarding vendors spawned. Use /hogwarts shop wand|broom|magic for stock."));
        return Command.SINGLE_SUCCESS;
    }
    private static int giveWand(ServerPlayer p, int number) {
        if (number == 50) return give(p, named(ELDER_WAND, "The Elder Wand", "All 2,000 spells unlocked; no cooldown."));
        Item item = WANDS.values().stream().skip(number - 1L).findFirst().orElse(null);
        if (item == null) return 0;
        String wood = WANDS.entrySet().stream().filter(e -> e.getValue() == item).map(Map.Entry::getKey).findFirst().orElse("wizard");
        return give(p, named(item, titleCase(wood) + " Wand", "A wizarding wand. Right-click to cast your selected spell."));
    }
    private static String titleCase(String value) {
        StringBuilder out = new StringBuilder();
        for (String part : value.split("_")) {
            if (out.length() > 0) out.append(' ');
            out.append(part.substring(0,1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return out.toString();
    }
    private static int giveIndexed(ServerPlayer p, Map<String, Item> registry, int number, String type) {
        Item item = registry.values().stream().skip(number - 1L).findFirst().orElse(null);
        if (item == null) return 0;
        String key = registry.entrySet().stream().filter(e -> e.getValue() == item).map(Map.Entry::getKey).findFirst().orElse(type);
        return give(p, named(item, titleCase(key), "Wizarding " + type + " catalog item #" + number));
    }
    private static int giveBrooms(ServerPlayer p) {
        for (var entry : BROOMS.entrySet()) p.getInventory().add(named(entry.getValue(), titleCase(entry.getKey()), "A flying broom. Right-click to take flight."));
        return Command.SINGLE_SUCCESS;
    }
    private static int placeCauldron(ServerPlayer p) {
        BlockPos target = p.blockPosition().relative(p.getDirection());
        if (!p.serverLevel().getBlockState(target).isAir()) {
            p.sendSystemMessage(Component.literal("There is no room to place a cauldron."));
            return 0;
        }
        p.serverLevel().setBlock(target, Blocks.CAULDRON.defaultBlockState(), 3);
        return Command.SINGLE_SUCCESS;
    }

    private static int wallet(ServerPlayer p) {
        p.sendSystemMessage(Component.literal("Currency: 1 Galleon = 17 Sickles; 1 Sickle = 29 Knuts (493 Knuts per Galleon)."));
        return Command.SINGLE_SUCCESS;
    }
    private static int spellList(ServerPlayer p) {
        p.sendSystemMessage(Component.literal("SPELL ARCHIVE — 2,000 spells. Showing page 1 of 100 (20 spells per page)."));
        return spellPage(p, 1);
    }
    private static int spellPage(ServerPlayer p, int page) {
        int start = (page - 1) * 20;
        p.sendSystemMessage(Component.literal("SPELL ARCHIVE — page " + page + "/100. Use /harrypotter spellpage <1-100>."));
        int index = start + 1;
        for (Spell spell : SPELLS.values().stream().skip(start).limit(20).toList()) {
            p.sendSystemMessage(Component.literal(index + ". " + spell.name() + " — " + spell.description() + " | select: /harrypotter spell " + index));
            index++;
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int give(ServerPlayer p, ItemStack stack) { p.getInventory().add(stack); return Command.SINGLE_SUCCESS; }
    private static int giveQuidditch(ServerPlayer p) {
        p.getInventory().add(named(QUAFFLE,"Quaffle","Scoring ball"));
        p.getInventory().add(named(BLUDGER,"Bludger","Quidditch hazard"));
        p.getInventory().add(named(SNITCH,"Golden Snitch","Catch to end the match"));
        return Command.SINGLE_SUCCESS;
    }
    private static int startQuiz(ServerPlayer p) {
        HOUSE_POINTS.put(p.getUUID(),new Integer[]{0,0,0,0});
        QUIZ_ANSWERS.put(p.getUUID(),0);
        p.sendSystemMessage(Component.literal("THE SORTING HAT — 5 questions. Answer each with /hogwarts answer <1-4>. 1 Gryffindor, 2 Hufflepuff, 3 Ravenclaw, 4 Slytherin."));
        p.sendSystemMessage(Component.literal("Q1: Which virtue matters most — courage, loyalty, wisdom or ambition?"));
        return Command.SINGLE_SUCCESS;
    }
    private static int answerQuiz(ServerPlayer p,int answer) {
        Integer[] pts=HOUSE_POINTS.get(p.getUUID());
        if(pts==null){p.sendSystemMessage(Component.literal("Start first: /hogwarts sorting"));return 0;}
        pts[answer-1]++; int n=QUIZ_ANSWERS.merge(p.getUUID(),1,Integer::sum);
        String[] qs={"Q2: Face a dragon, help a friend, solve a riddle, or lead a team?",
                "Q3: Choose a keepsake: sword, scarf, ancient book, or signet ring?",
                "Q4: Act boldly, stay dependable, think carefully, or plan strategically?",
                "Q5: Be remembered for courage, kindness, insight, or achievement?"};
        if(n<5) p.sendSystemMessage(Component.literal(qs[n-1]));
        else {
            String[] houses={"Gryffindor","Hufflepuff","Ravenclaw","Slytherin"};
            int best=0; for(int i=1;i<4;i++) if(pts[i]>pts[best]) best=i;
            p.sendSystemMessage(Component.literal("THE HAT HAS CHOSEN: "+houses[best].toUpperCase(Locale.ROOT)+"!"));
            HOUSE_POINTS.remove(p.getUUID()); QUIZ_ANSWERS.remove(p.getUUID());
        }
        return Command.SINGLE_SUCCESS;
    }
    private static int learnScroll(ServerPlayer p,int index) {
        Spell s=SPELLS.values().stream().skip(index-1).findFirst().orElse(null);
        if(s==null)return 0;
        LEARNED.add(p.getUUID()+":"+s.id());
        p.sendSystemMessage(Component.literal("Learned "+s.name()+": "+s.description()));
        return Command.SINGLE_SUCCESS;
    }
    private static int giveScroll(ServerPlayer p, int index) {
        Spell spell = SPELLS.values().stream().skip(index-1).findFirst().orElse(null);
        if (spell == null) return 0;
        ItemStack scroll = named(SPELL_SCROLL, spell.name()+" Scroll", spell.description());
        p.getInventory().add(scroll);
        p.sendSystemMessage(Component.literal("You received: "+spell.name()+" — "+spell.description()));
        return 1;
    }
    private static int selectSpell(ServerPlayer p, int index) {
        Spell spell = SPELLS.values().stream().skip(index-1).findFirst().orElse(null);
        if (spell == null) return 0;
        if (!LEARNED.contains(p.getUUID()+":"+spell.id()) && !p.getMainHandItem().is(ELDER_WAND) && !p.getOffhandItem().is(ELDER_WAND)) {
            p.sendSystemMessage(Component.literal("You have not learned that spell. Find its scroll with /hogwarts scroll "+index));
            return 0;
        }
        SELECTED_SPELL.put(p.getUUID(), spell.id());
        p.sendSystemMessage(Component.literal("Selected "+spell.name()+": "+spell.description()));
        return 1;
    }
    private static int shop(ServerPlayer p, String kind) {
        switch(kind) {
            case "wand" -> {
                p.getInventory().add(named(WANDS.get("holly"),"Holly Wand","Ollivanders wand"));
                p.getInventory().add(named(ELDER_WAND,"Elder Wand","The legendary Deathly Hallow wand"));
            }
            case "broom" -> {
                p.getInventory().add(named(BROOMS.get("cleansweep_11"),"Cleansweep 11","Starter broom"));
                p.getInventory().add(named(BROOMS.get("nimbus_2000"),"Nimbus 2000","Racing broom"));
                p.getInventory().add(named(BROOMS.get("firebolt"),"Firebolt","Elite racing broom"));
                p.getInventory().add(named(BROOMS.get("comet_260"),"Comet 260","Classic broom"));
            }
            default -> {
                p.getInventory().add(new ItemStack(SPELL_SCROLL,3));
                p.getInventory().add(new ItemStack(GALLEON,2));
                p.getInventory().add(new ItemStack(SICKLE,4));
                p.getInventory().add(new ItemStack(KNUT,8));
            }
        }
        p.sendSystemMessage(Component.literal("Vendor stock delivered. This prototype shop grants items without charging currency."));
        return 1;
    }
    private static int stopBroom(ServerPlayer p) {
        p.getAbilities().flying=false;
        p.getAbilities().mayfly=false;
        p.onUpdateAbilities();
        p.sendSystemMessage(Component.literal("Broom flight stopped."));
        return 1;
    }
    private static void throwBall(Player player, ItemStack stack, Level world) {
        if(world instanceof ServerLevel sl) {
            Snowball ball=new Snowball(sl,player,stack.copyWithCount(1));
            ball.shootFromRotation(player,player.getXRot(),player.getYRot(),0,1.6f,0.5f);
            sl.addFreshEntity(ball);
            stack.shrink(1);
        }
    }
}
