package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.HashMap;
import java.util.Map;

public class AllEnchantmentsConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Map<String, EnchantConfig> ENCHANTMENTS = new HashMap<>();
    
    // 定义所有拳甲附魔的配置项
    public static final EnchantConfig CRUSHING_FIST_CHESTPLATE;
    public static final EnchantConfig SPRING_FIST_CHESTPLATE;
    public static final EnchantConfig DIAMOND_OBSIDIAN_FIST_CHESTPLATE;
    public static final EnchantConfig FIST_PUNCH_CHESTPLATE;
    public static final EnchantConfig SEVERE_INJURY_FIST;
    public static final EnchantConfig BONE_BREAKER_FIST;
    public static final EnchantConfig EMPTY_MIND_FIST;
    public static final EnchantConfig FOCUSED_FIST;
    public static final EnchantConfig RED_RUSH_FIST;
    public static final EnchantConfig SPRINT_FIST;
    public static final EnchantConfig TENDON_FIST;
    public static final EnchantConfig REACH_PUNCH_CHESTPLATE;
    public static final EnchantConfig BOXING_ARMOR;
    
    // 定义所有时间轴类附魔的配置项
    public static final EnchantConfig TIME_AXIS_DECAY;
    public static final EnchantConfig TIME_AXIS_MENDING;
    public static final EnchantConfig ADVANCED_TIME_AXIS_MENDING;
    public static final EnchantConfig PRIMITIVE_TIME_AXIS_MENDING;
    public static final EnchantConfig PRIMITIVE_MENDING;
    
    // 定义矿探附魔的配置项
    public static final EnchantConfig ORE_DETECTOR;
    public static final EnchantConfig STOMACH_POUCH;
    public static final EnchantConfig GLUTTONOUS_POUCH;
    public static final EnchantConfig LAVA_WALKER;
    public static final EnchantConfig SHADOW_WALKER;
    public static final EnchantConfig LIGHTBURN_WALKER;
    public static final EnchantConfig METEORITE_STOMP;
    public static final EnchantConfig CLOUD_WALKER;
    public static final EnchantConfig HIGH_STEP;
    
    // 定义诅咒之刃附魔的配置项
    public static final EnchantConfig CURSED_BLADE;
    
    // 定义凋零类附魔的配置项
    public static final EnchantConfig WITHER_EDGE;
    public static final EnchantConfig WITHER_BLADE;
    
    // 定义毒痕类附魔的配置项
    public static final EnchantConfig POISON_SCAR;
    public static final EnchantConfig POISON_FANG;
    
    // 定义重击类附魔的配置项
    public static final EnchantConfig SLOW_HEAVY_BLOW;
    public static final EnchantConfig SLOW_HEAVY_WEAKNESS;
    
    // 定义虚弱类附魔的配置项
    public static final EnchantConfig WEAK_HEAVY_INJURY;
    public static final EnchantConfig WEAK_TRAUMA;
    
    // 定义速击附魔的配置项
    public static final EnchantConfig SWIFT_STRIKE;
    
    // 定义连斩附魔的配置项
    public static final EnchantConfig SWEEPING_STRIKE;
    
    // 定义急速冲击附魔的配置项
    public static final EnchantConfig SWIFT_IMPACT;
    
    // 定义迟缓附魔的配置项
    public static final EnchantConfig SLOWNESS;
    
    // 定义短寸附魔的配置项
    public static final EnchantConfig SHORT_DIMENSION;
    
    // 定义延申附魔的配置项
    public static final EnchantConfig REACH_EXTENSION;
    
    // 定义恐惧附魔的配置项
    public static final EnchantConfig FEAR;
    
    // 定义速食附魔的配置项
    public static final EnchantConfig SWIFT_FEAST;
    
    // 定义噬痛附魔的配置项
    public static final EnchantConfig PAIN_FEAST;
    
    // 定义内心恐惧附魔的配置项
    public static final EnchantConfig INNER_FEAR;
    
    // 定义氧力悬停附魔的配置项
    public static final EnchantConfig HOVER;
    
    // 定义气短附魔的配置项
    public static final EnchantConfig SHORT_BREATH;
    
    // 定义地缚附魔的配置项
    public static final EnchantConfig EARTHBOUND;

    // 定义初级跃力附魔的配置项
    public static final EnchantConfig PRIMITIVE_LEAP_FORCE;
    
    // 定义跃力附魔的配置项
    public static final EnchantConfig LEAP_FORCE;

    // 定义初级轻盈附魔的配置项
    public static final EnchantConfig PRIMITIVE_LIGHTWEIGHT;
    
    // 定义轻盈附魔的配置项
    public static final EnchantConfig LIGHTWEIGHT;

    // 定义初级急行附魔的配置项
    public static final EnchantConfig BASIC_SPEED;

    // 定义急行附魔的配置项
    public static final EnchantConfig SWIFT_STEP;

    // 定义蛰袭之嫌附魔的配置项
    public static final EnchantConfig WASPISH;

    // 定义群戮附魔的配置项
    public static final EnchantConfig MASSACRE;
    
    // 定义独斗附魔的配置项
    public static final EnchantConfig SOLO_COMBAT;
    
    // 定义强化锋利附魔的配置项
    public static final EnchantConfig SHARPENED_SHARPNESS;
    
    // 定义极锋附魔的配置项
    public static final EnchantConfig ULTRA_EDGE;

    // 定义暴击类附魔的配置项
    public static final EnchantConfig BASIC_CRITICAL;
    public static final EnchantConfig CRITICAL_CHANCE;
    public static final EnchantConfig FOCUSED_CRITICAL;
    public static final EnchantConfig LOW_DAMAGE_CRITICAL;
    public static final EnchantConfig DAMAGE_CRITICAL;
    public static final EnchantConfig CRITICAL_OVERCLOCK;

    // 定义横扫类附魔的配置项
    public static final EnchantConfig SWEEPING_ENHANCEMENT;
    public static final EnchantConfig SWEEPING_SLASH;
    public static final EnchantConfig SWEEPING_FIRE;
    public static final EnchantConfig SWEEPING_BURN_SLASH;

    // 定义防火类附魔的配置项
    public static final EnchantConfig FIRE_PROTECTION;
    public static final EnchantConfig DESIRE_FLAME;
    
    // 定义胸甲类附魔的配置项
    public static final EnchantConfig BLAZING_WAR;
    public static final EnchantConfig INFERNAL_ARMOR;
    public static final EnchantConfig INFERNAL_REBIRTH;
    public static final EnchantConfig FIRE_DEVOUR;
    public static final EnchantConfig BURNING_HEART;
    public static final EnchantConfig FLAME_SHELL;
    public static final EnchantConfig BURNING_FURY;
    
    // 定义24号祝福附魔的配置项
    public static final EnchantConfig BLESSING_NO24;
    
    // 定义格挡类附魔的配置项
    public static final EnchantConfig SIMPLE_BLOCK;
    public static final EnchantConfig LIFE_BLOCK;
    
    // 定义诅咒类附魔的配置项
    public static final EnchantConfig UNDEAD_CURSE;
    
    // 定义灵魂类附魔的配置项
    public static final EnchantConfig SOULBOUND;

    // 定义惧慑附魔的配置项
    public static final EnchantConfig DANGEROUS_AURA;
    
    
    // 定义腐蚀类附魔的配置项
    public static final EnchantConfig NETHER_CORROSION;
    public static final EnchantConfig END_CORROSION;
    public static final EnchantConfig OVERWORLD_CORROSION;
    public static final EnchantConfig ARMOR_PIERCING;
    public static final EnchantConfig ARMOR_BREAK;
    public static final EnchantConfig FLINT_STRIKE;
    
    // 定义掠财附魔的配置项
    public static final EnchantConfig PLUNDER_WEALTH;
    
    // 定义脚臭附魔的配置项
    public static final EnchantConfig STINKY_FEET;
    
    // 定义超级脚臭附魔的配置项
    public static final EnchantConfig SUPER_STINKY_FEET;
    
    // 定义盾震附魔的配置项
    public static final EnchantConfig SHIELD_SHOCK;
    
    // 定义仇引附魔的配置项
    public static final EnchantConfig HATRED_TAUNT;
    
    // 定义新附魔的配置项
    public static final EnchantConfig FAST_FALL;
    public static final EnchantConfig CRASH_LANDING;
    public static final EnchantConfig STAGGERING_BLOW;
    public static final EnchantConfig CORE_COLLECTION;
    public static final EnchantConfig BODY_SNATCH;
    public static final EnchantConfig WHISPER;
    public static final EnchantConfig ENEMY_TAUNT;
    public static final EnchantConfig OVERWORLD_CURSE;
    public static final EnchantConfig NETHER_CURSE;
    public static final EnchantConfig END_CURSE;
    
    // 定义弓类附魔的配置项
    public static final EnchantConfig HEART_OF_FLINT;
    public static final EnchantConfig BOUNCE;
    public static final EnchantConfig RANGER;
    public static final EnchantConfig STEADY_SHOT;
    
    // 新增附魔配置项
    public static final EnchantConfig REPEATING_BOLT;
    public static final EnchantConfig PIERCING;
    public static final EnchantConfig PEREGRINE;
    public static final EnchantConfig SOUL_ARROW;
    public static final EnchantConfig WILDFIRE_ARROW;
    public static final EnchantConfig DRAGON_BREATH_ARROW;
    public static final EnchantConfig WATER_ARROW;
    public static final EnchantConfig DROUGHT_ARROW;
    public static final EnchantConfig HEAVY_IMPACT;
    public static final EnchantConfig TELEPORT_ARROW;
    public static final EnchantConfig SWAP_TELEPORT;
    public static final EnchantConfig TOY_BOW;
    public static final EnchantConfig POINT_SHOOT;
    public static final EnchantConfig TRANSFER;
    
    // 定义脚部搭建附魔的配置项
    public static final EnchantConfig FOOT_BLOCK;
    
    // 定义范围脚部搭建附魔的配置项
    public static final EnchantConfig RANGE_FOOT_BLOCK;
    
    // 定义恐慌逃离附魔的配置项
    public static final EnchantConfig PANIC_ESCAPE;
    
    // 定义绝境逃离附魔的配置项
    public static final EnchantConfig DESPERATE_ESCAPE;
    
    // 定义头盔类附魔的配置项
    public static final EnchantConfig SENSE;
    public static final EnchantConfig KING;
    
    // 新增策马靴附魔配置项
    public static final EnchantConfig HORSE_BOOTS;
    public static final EnchantConfig GENERAL;
    public static final EnchantConfig COMMANDER;
    public static final EnchantConfig UNDEAD_COMMANDER;
    public static final EnchantConfig ILLAGER_COMMANDER;
    public static final EnchantConfig NOISE_ANNOYANCE;
    // 新增匪首和吸血鬼附魔配置项
    public static final EnchantConfig CHIEFTAIN;
    public static final EnchantConfig VAMPIRE;

    // 定义剑类附魔的配置项
    public static final EnchantConfig LONE_WOLF;
    public static final EnchantConfig LEADER;
    public static final EnchantConfig BLOODTHIRSTY;
    public static final EnchantConfig BLOOD_EDGE;
    public static final EnchantConfig STEALTH_HUNTER;
    public static final EnchantConfig SIGHING_STRIKE;
    public static final EnchantConfig EXHAUSTION;
    public static final EnchantConfig HEADHUNTER;
    
    // 定义啃食附魔的配置项
    public static final EnchantConfig BITE;
    
    // 定义敌速追行附魔的配置项
    public static final EnchantConfig TAUNT_EXCITEMENT;
    
    // 定义胸闷附魔的配置项
    public static final EnchantConfig CHEST_TIGHTNESS;
    
    // 定义骨折附魔的配置项
    public static final EnchantConfig FRACTURE;
    
    // 定义伤裂附魔的配置项
    public static final EnchantConfig WOUND_RIFT;
    
    // 定义伤残附魔的配置项
    public static final EnchantConfig MAIM;
    
    // 定义盔损附魔的配置项
    public static final EnchantConfig ARMOR_DAMAGE;
    
    // 定义盔碎附魔的配置项
    public static final EnchantConfig ARMOR_SHATTER;
    
    // 定义镰伤附魔的配置项
    public static final EnchantConfig SICKLE_WOUND;
    
    // 定义镰斩附魔的配置项
    public static final EnchantConfig SICKLE_SLASH;
    
    // 定义经验收割附魔的配置项
    public static final EnchantConfig EXPERIENCE_HARVEST;
    
    // 定义召军附魔的配置项
    public static final EnchantConfig ARMY_SUMMON;
    
    // 定义翅韧战甲附魔的配置项
    public static final EnchantConfig ELYTRA_ARMOR;
    
    // 定义石粒人附魔的配置项
    public static final EnchantConfig STONE_PELLET_MAN;
    
    // 定义作物收割附魔的配置项
    public static final EnchantConfig CROP_HARVEST;
    
    // 定义掉落收割附魔的配置项
    public static final EnchantConfig DROP_REAPER;
    
    // 定义耕耘靴子附魔的配置项
    public static final EnchantConfig TILLAGE_BOOT;
    
    // 定义除草靴附魔的配置项
    public static final EnchantConfig WEED_REMOVAL_BOOT;
    
    // 定义祭魂镰斩附魔的配置项
    public static final EnchantConfig SOUL_REAPING_SICKLE;
    
    // 定义付魂斩附魔的配置项
    public static final EnchantConfig SOUL_PAYING_CUT;
    
    // 定义斩杀率附魔的配置项
    public static final EnchantConfig EXECUTE_RATE;
    
    // 定义斩杀力附魔的配置项
    public static final EnchantConfig EXECUTE_POWER;
    
    // 定义铲刃附魔的配置项
    public static final EnchantConfig SHARP_EDGE;
    
    // 定义铲重击附魔的配置项
    public static final EnchantConfig SHOVEL_HEAVY_BLOW;
    
    // 定义铲戟附魔的配置项
    public static final EnchantConfig SHOVEL_TRIDENT;
    
    // 定义翘甲附魔的配置项
    public static final EnchantConfig SHOVEL_ARMOR_PRIZE;
    
    // 定义功能军锹附魔的配置项
    public static final EnchantConfig MILITARY_SHOVEL;
    
    // 定义万能军锹附魔的配置项
    public static final EnchantConfig UNIVERSAL_SHOVEL;
    
    // 定义铲刃附魔的配置项
    public static final EnchantConfig SHOVEL_STRIKE;
    
    // 定义融雪靴附魔的配置项
    public static final EnchantConfig SNOW_REMOVAL_BOOT;
    
    // 定义斩首附魔的配置项
    public static final EnchantConfig DECAPITATION;

    // 定义输血附魔的配置项
    public static final EnchantConfig BLOOD_TRANSFUSION;
    
    // 定义血契附魔的配置项
    public static final EnchantConfig BLOOD_PACT;
    
    // 定义呼吸回血附魔的配置项
    public static final EnchantConfig BREATH_HEAL;

    // 定义高原缺氧附魔的配置项
    public static final EnchantConfig HIGH_ALTITUDE_OXYGEN_DEFICIENCY;
    
    // 定义憋气附魔的配置项
    public static final EnchantConfig HOLD_BREATH;
    
    // 定义回收附魔的配置项
    public static final EnchantConfig RECYCLE;
    
    // 定义力弱附魔的配置项
    public static final EnchantConfig WEAKNESS;
    
    // 定义断弦附魔的配置项
    public static final EnchantConfig SNAP_STRING;

    // 定义钝碾附魔的配置项
    public static final EnchantConfig BLUNT_CRUSHING;

    // 定义锋刃附魔的配置项
    public static final EnchantConfig SHARP_EDGE_ENCHANTMENT;
    
    // 定义残忍附魔的配置项
    public static final EnchantConfig CRUELTY;
    
    // 定义食力斩附魔的配置项
    public static final EnchantConfig FOOD_POWER_STRIKE;
    
    // 定义骑士附魔的配置项
    public static final EnchantConfig KNIGHT;
    
    // 定义专注伏击附魔的配置项
    public static final EnchantConfig FOCUSED_AMBUSH;
    
    // 定义专注稳重附魔的配置项
    public static final EnchantConfig FOCUSED_STEADINESS;
    
    // 定义无双附魔的配置项
    public static final EnchantConfig DUAL_WIELD;
    
    // 定义牧师祝福附魔的配置项
    public static final EnchantConfig PRIEST_BLESSING;
    
    // 定义雷劫附魔的配置项
    public static final EnchantConfig THUNDER_RETRIBUTION;
    
    // 定义耗氧冲刺附魔的配置项
    public static final EnchantConfig OXYGEN_DEPLETING_SPRINT;
    
    // 定义缺氧急行附魔的配置项
    public static final EnchantConfig HYPOXIA_SPRINT;

    // 定义雨润附魔的配置项
    public static final EnchantConfig RAIN_NOURISHMENT;
    
    // 定义深层缺氧（深层压力）附魔的配置项
    public static final EnchantConfig DEEP_PRESSURE;
    
    // 定义海洋庇佑附魔的配置项
    public static final EnchantConfig OCEAN_BLESSING;
    
    // 定义铁肺附魔的配置项
    public static final EnchantConfig IRON_LUNG;
    
    // 定义绝境坚韧附魔的配置项
    public static final EnchantConfig DESPERATE_RESILIENCE;
    
    // 定义绝境杀戮附魔的配置项
    public static final EnchantConfig DESPERATE_KILL;
    
    // 定义心动律动附魔的配置项
    public static final EnchantConfig HEARTBEAT_RHYTHM;
    
    // 定义武道律动附魔的配置项
    public static final EnchantConfig MARTIAL_RHYTHM;
    
    // 定义杀戮感知附魔的配置项
    public static final EnchantConfig KILLING_SENSE;
    
    // 定义烈火创伤附魔的配置项
    public static final EnchantConfig BURNING_TRAUMA;
    
    // 定义强力反伤附魔的配置项
    public static final EnchantConfig STRONG_THORNS;
    
    // 定义斥魂爆震附魔的配置项
    public static final EnchantConfig SOUL_REPULSION;
    
    // 定义掉落吸引附魔的配置项
    public static final EnchantConfig LOOT_ATTRACTION;
    
    // 定义肉铠附魔的配置项
    public static final EnchantConfig VITALITY_BOOST;

    // 定义锻体附魔的配置项
    public static final EnchantConfig EXERCISE;
    
    // 定义饕餮附魔的配置项
    public static final EnchantConfig GLUTTONY;
    
    // 定义磐蛮附魔的配置项
    public static final EnchantConfig STURDY_BARBARIAN;
    
    // 定义钢躯附魔的配置项
    public static final EnchantConfig STEEL_BODY;
    
    // 定义肥硕附魔的配置项
    public static final EnchantConfig OBESITY;
    
    // 定义身经百战附魔的配置项
    public static final EnchantConfig VETERAN;
    
    // 定义手动粗修附魔的配置项
    public static final EnchantConfig MANUAL_ROUGH_REPAIR;
    
    // 定义手动精修附魔的配置项
    public static final EnchantConfig MANUAL_FINE_REPAIR;
    
    // 定义战痕累累附魔的配置项
    public static final EnchantConfig BATTLE_SCARRED;

    // 定义力量突效附魔的配置项
    public static final EnchantConfig POWER_BURST;
    
    // 定义力量续效附魔的配置项
    public static final EnchantConfig POWER_SUSTAINED;
    
    // 定义急迫突效附魔的配置项
    public static final EnchantConfig HASTE_BURST;
    
    // 定义急迫续效附魔的配置项
    public static final EnchantConfig HASTE_SUSTAINED;
    
    // 定义抗性突效附魔的配置项
    public static final EnchantConfig RESISTANCE_BURST;
    
    // 定义抗性续效附魔的配置项
    public static final EnchantConfig RESISTANCE_SUSTAINED;
    
    // 定义夜朦附魔的配置项
    public static final EnchantConfig NIGHT_VISION;
    
    // 定义栖所附魔的配置项
    public static final EnchantConfig HABITAT;
    
    // 定义篝愈附魔的配置项
    public static final EnchantConfig CAMPFIRE_HEAL;
    
    // 定义初露锋芒附魔的配置项
    public static final EnchantConfig PROMISING_BLADE;
    
    // 定义久经沙场附魔的配置项
    public static final EnchantConfig BATTLE_HARDENED_WEAPON;
    
    // 定义潮涌之心附魔的配置项
    public static final EnchantConfig HEART_OF_TIDE;
    
    // 定义干渴之心附魔的配置项
    public static final EnchantConfig HEART_OF_DROUGHT;
    
    static {
        BUILDER.comment("拳甲类附魔配置").push("fist_enchantments");

        // 为每个拳甲附魔添加配置
        // 碎岩拳甲：不是宝藏，可发现，可交易
        CRUSHING_FIST_CHESTPLATE = createEnchantConfig("crushing_fist_chestplate", "碎岩拳甲", 
                false, true, true);
        ENCHANTMENTS.put("crushing_fist_chestplate", CRUSHING_FIST_CHESTPLATE);
        
        // 咏春拳甲：不是宝藏，可发现，可交易
        SPRING_FIST_CHESTPLATE = createEnchantConfig("spring_fist_chestplate", "咏春拳甲", 
                false, true, true);
        ENCHANTMENTS.put("spring_fist_chestplate", SPRING_FIST_CHESTPLATE);
        
        // 钻耀拳甲：是宝藏，不可发现，不可交易
        DIAMOND_OBSIDIAN_FIST_CHESTPLATE = createEnchantConfig("diamond_obsidian_fist_chestplate", "钻耀拳甲", 
                true, false, false);
        ENCHANTMENTS.put("diamond_obsidian_fist_chestplate", DIAMOND_OBSIDIAN_FIST_CHESTPLATE);
        
        // 直拳拳甲：不是宝藏，可发现，可交易
        FIST_PUNCH_CHESTPLATE = createEnchantConfig("fist_punch_chestplate", "直拳拳甲", 
                false, true, true);
        ENCHANTMENTS.put("fist_punch_chestplate", FIST_PUNCH_CHESTPLATE);
        
        // 重伤拳甲：不是宝藏，可发现，不可交易
        SEVERE_INJURY_FIST = createEnchantConfig("severe_injury_fist", "重伤拳甲", 
                false, true, false);
        ENCHANTMENTS.put("severe_injury_fist", SEVERE_INJURY_FIST);
        
        // 碎骨拳甲：不是宝藏，可发现，不可交易
        BONE_BREAKER_FIST = createEnchantConfig("bone_breaker_fist", "碎骨拳甲", 
                false, true, false);
        ENCHANTMENTS.put("bone_breaker_fist", BONE_BREAKER_FIST);
        
        // 空明拳甲：不是宝藏，可发现，不可交易
        EMPTY_MIND_FIST = createEnchantConfig("empty_mind_fist", "空明拳甲", 
                false, true, false);
        ENCHANTMENTS.put("empty_mind_fist", EMPTY_MIND_FIST);
        
        // 专注拳甲：不是宝藏，可发现，不可交易
        FOCUSED_FIST = createEnchantConfig("focused_fist", "专注拳甲", 
                false, true, false);
        ENCHANTMENTS.put("focused_fist", FOCUSED_FIST);
        
        // 赤冲拳甲：不是宝藏，可发现，不可交易
        RED_RUSH_FIST = createEnchantConfig("red_rush_fist", "赤冲拳甲", 
                false, true, false);
        ENCHANTMENTS.put("red_rush_fist", RED_RUSH_FIST);
        
        // 冲刺拳甲：不是宝藏，可发现，可交易
        SPRINT_FIST = createEnchantConfig("sprint_fist", "冲刺拳甲", 
                false, true, true);
        ENCHANTMENTS.put("sprint_fist", SPRINT_FIST);
        
        // 断筋拳甲：不是宝藏，可发现，不可交易
        TENDON_FIST = createEnchantConfig("tendon_fist", "断筋拳甲", 
                false, true, false);
        ENCHANTMENTS.put("tendon_fist", TENDON_FIST);
        
        // 直延拳甲：不是宝藏，可发现，可交易
        REACH_PUNCH_CHESTPLATE = createEnchantConfig("reach_punch_chestplate", "直延拳甲", 
                false, true, true);
        ENCHANTMENTS.put("reach_punch_chestplate", REACH_PUNCH_CHESTPLATE);
        
        // 拳击甲胄：不是宝藏，可发现，可交易
        BOXING_ARMOR = createEnchantConfig("boxing_armor", "拳击甲胄", 
                false, true, true);
        ENCHANTMENTS.put("boxing_armor", BOXING_ARMOR);

        BUILDER.pop();
        
        // 时间轴类附魔配置
        BUILDER.comment("时间轴类附魔配置").push("time_axis_enchantments");
        
        TIME_AXIS_DECAY = createEnchantConfig("time_axis_decay", "时间轴衰变",
                false, true, true);// 不是宝藏，可发现，可交易
        ENCHANTMENTS.put("time_axis_decay", TIME_AXIS_DECAY);
        
        TIME_AXIS_MENDING = createEnchantConfig("time_axis_mending", "时间轴修补",
                false, true, false);// 不是宝藏，可发现，不可交易
        ENCHANTMENTS.put("time_axis_mending", TIME_AXIS_MENDING);
        
        ADVANCED_TIME_AXIS_MENDING = createEnchantConfig("advanced_time_axis_mending", "高级时间轴修补",
                true, false, false);// 是宝藏，不可发现，不可交易
        ENCHANTMENTS.put("advanced_time_axis_mending", ADVANCED_TIME_AXIS_MENDING);
        
        PRIMITIVE_TIME_AXIS_MENDING = createEnchantConfig("primitive_time_axis_mending", "初级时间轴修补",
                false, true, true);// 不是宝藏，可发现，可交易
        ENCHANTMENTS.put("primitive_time_axis_mending", PRIMITIVE_TIME_AXIS_MENDING);
        
        PRIMITIVE_MENDING = createEnchantConfig("primitive_mending", "初级经验修补",
                false, true, false);// 不是宝藏，可发现，不可交易
        ENCHANTMENTS.put("primitive_mending", PRIMITIVE_MENDING);
        
        // 胃袋类附魔配置
        BUILDER.comment("胃袋类附魔配置").push("stomach_pouch_enchantments");
        
        STOMACH_POUCH = createEnchantConfig("stomach_pouch", "胃袋",
                false, true, true);
        ENCHANTMENTS.put("stomach_pouch", STOMACH_POUCH);
        
        GLUTTONOUS_POUCH = createEnchantConfig("gluttonous_pouch", "大胃袋",
                false, true, false);
        ENCHANTMENTS.put("gluttonous_pouch", GLUTTONOUS_POUCH);
        
        // 矿探附魔配置
        BUILDER.comment("矿探附魔配置").push("ore_detector_enchantments");
        
        // 矿探附魔配置 - 是宝藏，不可发现，不可交易
        ORE_DETECTOR = createEnchantConfig("ore_detector", "矿探",
                true, false, false);
        ENCHANTMENTS.put("ore_detector", ORE_DETECTOR);
        
        // 行走类附魔配置
        BUILDER.comment("行走类附魔配置").push("walker_enchantments");
        
        LAVA_WALKER = createEnchantConfig("lava_walker", "熔岩行者",
                false, true, true);
        ENCHANTMENTS.put("lava_walker", LAVA_WALKER);
        
        SHADOW_WALKER = createEnchantConfig("shadow_walker", "暗影急行",
                false, true, true);
        ENCHANTMENTS.put("shadow_walker", SHADOW_WALKER);
        
        LIGHTBURN_WALKER = createEnchantConfig("lightburn_walker", "光灼行者",
                false, true, true);
        ENCHANTMENTS.put("lightburn_walker", LIGHTBURN_WALKER);
        
        // 踏地类附魔配置
        BUILDER.comment("踏地类附魔配置").push("stomp_enchantments");
        
        METEORITE_STOMP = createEnchantConfig("meteorite_stomp", "陨铁踏",
                false, true, false);
        ENCHANTMENTS.put("meteorite_stomp", METEORITE_STOMP);
        
        CLOUD_WALKER = createEnchantConfig("cloud_walker", "踏云",
                false, true, true);
        ENCHANTMENTS.put("cloud_walker", CLOUD_WALKER);
        
        // 其他附魔配置
        BUILDER.comment("其他附魔配置").push("other_enchantments");
        
        HIGH_STEP = createEnchantConfig("high_step", "高跨",
                false, true, true);
        ENCHANTMENTS.put("high_step", HIGH_STEP);
        
        // 诅咒之刃附魔配置 - 不是宝藏，可发现，可交易
        CURSED_BLADE = createEnchantConfig("cursed_blade", "诅咒之刃",
                false, true, true);
        ENCHANTMENTS.put("cursed_blade", CURSED_BLADE);
        
        // 凋零刃附魔配置 - 不是宝藏，可发现，可交易
        WITHER_EDGE = createEnchantConfig("wither_edge", "凋零刃",
                false, true, true);
        ENCHANTMENTS.put("wither_edge", WITHER_EDGE);
        
        // 凋零锏附魔配置 - 是宝藏，不可发现，不可交易
        WITHER_BLADE = createEnchantConfig("wither_blade", "凋零锏",
                true, false, false);
        ENCHANTMENTS.put("wither_blade", WITHER_BLADE);
        
        // 毒痕附魔配置 - 不是宝藏，可发现，可交易
        POISON_SCAR = createEnchantConfig("poison_scar", "毒痕",
                false, true, true);
        ENCHANTMENTS.put("poison_scar", POISON_SCAR);
        
        // 毒噬附魔配置 - 不是宝藏，可发现，不可交易
        POISON_FANG = createEnchantConfig("poison_fang", "毒噬",
                false, true, false);
        ENCHANTMENTS.put("poison_fang", POISON_FANG);
        
        // 缓慢重击附魔配置 - 不是宝藏，可发现，可交易
        SLOW_HEAVY_BLOW = createEnchantConfig("slow_heavy_blow", "缓慢重击",
                false, true, true);
        ENCHANTMENTS.put("slow_heavy_blow", SLOW_HEAVY_BLOW);
        
        // 缓重弱点附魔配置 - 不是宝藏，可发现，不可交易
        SLOW_HEAVY_WEAKNESS = createEnchantConfig("slow_heavy_weakness", "缓重弱点",
                false, true, false);
        ENCHANTMENTS.put("slow_heavy_weakness", SLOW_HEAVY_WEAKNESS);
        
        // 虚弱重伤附魔配置 - 不是宝藏，可发现，可交易
        WEAK_HEAVY_INJURY = createEnchantConfig("weak_heavy_injury", "虚弱重伤",
                false, true, true);
        ENCHANTMENTS.put("weak_heavy_injury", WEAK_HEAVY_INJURY);
        
        // 虚弱创伤附魔配置 - 不是宝藏，可发现，不可交易
        WEAK_TRAUMA = createEnchantConfig("weak_trauma", "虚弱创伤",
                false, true, false);
        ENCHANTMENTS.put("weak_trauma", WEAK_TRAUMA);
        
        // 速击附魔配置 - 不是宝藏，可发现，不可交易
        SWIFT_STRIKE = createEnchantConfig("swift_strike", "速击",
                false, true, false);
        ENCHANTMENTS.put("swift_strike", SWIFT_STRIKE);
        
        // 连斩附魔配置 - 不是宝藏，可发现，可交易
        SWEEPING_STRIKE = createEnchantConfig("sweeping_strike", "连斩",
                false, true, true);
        ENCHANTMENTS.put("sweeping_strike", SWEEPING_STRIKE);
        
        // 急速冲击附魔配置 - 不是宝藏，可发现，不可交易
        SWIFT_IMPACT = createEnchantConfig("swift_impact", "急速冲击",
                false, true, false);
        ENCHANTMENTS.put("swift_impact", SWIFT_IMPACT);
        
        // 迟缓附魔配置 - 不是宝藏，可发现，可交易
        SLOWNESS = createEnchantConfig("slowness", "迟缓",
                false, true, true);
        ENCHANTMENTS.put("slowness", SLOWNESS);
        
        // 短寸附魔配置 - 不是宝藏，可发现，可交易
        SHORT_DIMENSION = createEnchantConfig("short_dimension", "短寸",
                false, true, true);
        ENCHANTMENTS.put("short_dimension", SHORT_DIMENSION);
        
        // 延申附魔配置 - 不是宝藏，可发现，不可交易
        REACH_EXTENSION = createEnchantConfig("reach_extension", "延申",
                false, true, false);
        ENCHANTMENTS.put("reach_extension", REACH_EXTENSION);
        
        // 恐惧附魔配置 - 不是宝藏，可发现，可交易
        FEAR = createEnchantConfig("fear", "恐惧",
                false, true, true);
        ENCHANTMENTS.put("fear", FEAR);
        
        // 速食附魔配置 - 不是宝藏，可发现，可交易
        SWIFT_FEAST = createEnchantConfig("swift_feast", "速食",
                false, true, true);
        ENCHANTMENTS.put("swift_feast", SWIFT_FEAST);
        
        // 噬痛附魔配置 - 不是宝藏，可发现，可交易
        PAIN_FEAST = createEnchantConfig("pain_feast", "噬痛",
                false, true, true);
        ENCHANTMENTS.put("pain_feast", PAIN_FEAST);
        
        // 内心恐惧附魔配置 - 不是宝藏，可发现，可交易
        INNER_FEAR = createEnchantConfig("inner_fear", "内心恐惧",
                false, true, true);
        ENCHANTMENTS.put("inner_fear", INNER_FEAR);
        
        // 气短附魔配置 - 不是宝藏，可发现，可交易
        SHORT_BREATH = createEnchantConfig("short_breath", "气短",
                false, true, true);
        ENCHANTMENTS.put("short_breath", SHORT_BREATH);
        
        // 地缚附魔配置 - 不是宝藏，可发现，可交易
        EARTHBOUND = createEnchantConfig("earthbound", "地缚",
                false, true, true);
        ENCHANTMENTS.put("earthbound", EARTHBOUND);
        
        BUILDER.pop();
        
        // 跃力类附魔配置
        BUILDER.comment("跃力类附魔配置").push("leap_force_enchantments");
        
        // 初级跃力附魔配置 - 不是宝藏，可发现，可交易
        PRIMITIVE_LEAP_FORCE = createEnchantConfig("primitive_leap_force", "初级跃力",
                false, true, true);
        ENCHANTMENTS.put("primitive_leap_force", PRIMITIVE_LEAP_FORCE);
        
        // 跃力附魔配置 - 不是宝藏，可发现，可交易
        LEAP_FORCE = createEnchantConfig("leap_force", "跃力",
                false, true, true);
        ENCHANTMENTS.put("leap_force", LEAP_FORCE);
        
        BUILDER.pop();
        
        // 轻盈类附魔配置
        BUILDER.comment("轻盈类附魔配置").push("lightweight_enchantments");
        
        // 初级轻盈附魔配置 - 不是宝藏，可发现，可交易
        PRIMITIVE_LIGHTWEIGHT = createEnchantConfig("primitive_lightweight", "初级轻盈",
                false, true, true);
        ENCHANTMENTS.put("primitive_lightweight", PRIMITIVE_LIGHTWEIGHT);
        
        // 轻盈附魔配置 - 不是宝藏，可发现，不可交易
        LIGHTWEIGHT = createEnchantConfig("lightweight", "轻盈",
                false, true, false);
        ENCHANTMENTS.put("lightweight", LIGHTWEIGHT);
        
        BUILDER.pop();
        
        // 初级急行附魔配置
        BUILDER.comment("初级急行附魔配置").push("basic_speed_enchantments");
        
        // 初级急行附魔配置 - 稀有度：不寻常，不是宝藏，可发现，可交易
        BASIC_SPEED = createEnchantConfig("basic_speed", "初级急行",
                false, true, true);
        ENCHANTMENTS.put("basic_speed", BASIC_SPEED);
        
        BUILDER.pop();
        
        // 急行附魔配置
        BUILDER.comment("急行附魔配置").push("swift_step_enchantments");
        
        // 急行附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        SWIFT_STEP = createEnchantConfig("swift_step", "急行",
                false, true, false);
        ENCHANTMENTS.put("swift_step", SWIFT_STEP);
        
        BUILDER.pop();
        
        // 蛰袭之嫌附魔配置
        BUILDER.comment("蛰袭之嫌附魔配置").push("waspish_enchantments");
        
        // 蛰袭之嫌附魔配置 - 不是宝藏，可发现，可交易
        WASPISH = createEnchantConfig("waspish", "蛰袭之嫌",
                false, true, true);
        ENCHANTMENTS.put("waspish", WASPISH);
        
        BUILDER.pop();
        
        // 群体战斗类附魔配置
        BUILDER.comment("群体战斗类附魔配置").push("mass_combat_enchantments");
        
        // 群戮附魔配置 - 不是宝藏，可发现，可交易
        MASSACRE = createEnchantConfig("massacre", "群戮",
                false, true, true);
        ENCHANTMENTS.put("massacre", MASSACRE);
        
        // 独斗附魔配置 - 不是宝藏，可发现，可交易
        SOLO_COMBAT = createEnchantConfig("solo_combat", "独斗",
                false, true, true);
        ENCHANTMENTS.put("solo_combat", SOLO_COMBAT);
        
        BUILDER.pop();
        
        // 锋利类附魔配置
        BUILDER.comment("锋利类附魔配置").push("sharpness_enchantments");
        
        // 强化锋利附魔配置 - 不是宝藏，可发现，可交易
        SHARPENED_SHARPNESS = createEnchantConfig("sharpened_sharpness", "强化锋利",
                false, true, true);
        ENCHANTMENTS.put("sharpened_sharpness", SHARPENED_SHARPNESS);
        
        // 极锋附魔配置 - 不是宝藏，可发现，可交易
        ULTRA_EDGE = createEnchantConfig("ultra_edge", "极锋",
                false, true, true);
        ENCHANTMENTS.put("ultra_edge", ULTRA_EDGE);
        
        BUILDER.pop();
        
        // 暴击类附魔配置
        BUILDER.comment("暴击类附魔配置").push("critical_enchantments");
        
        // 低暴击率附魔配置 - 不是宝藏，可发现，可交易
        BASIC_CRITICAL = createEnchantConfig("basic_critical", "低暴击率",
                false, true, true);
        ENCHANTMENTS.put("basic_critical", BASIC_CRITICAL);
        
        // 暴击率附魔配置 - 不是宝藏，可发现，可交易
        CRITICAL_CHANCE = createEnchantConfig("critical_chance", "暴击率",
                false, true, true);
        ENCHANTMENTS.put("critical_chance", CRITICAL_CHANCE);
        
        // 专心暴击附魔配置 - 是宝藏，不可发现，不可交易
        FOCUSED_CRITICAL = createEnchantConfig("focused_critical", "专心暴击",
                true, false, false);
        ENCHANTMENTS.put("focused_critical", FOCUSED_CRITICAL);
        
        // 低伤暴击附魔配置 - 不是宝藏，可发现，可交易
        LOW_DAMAGE_CRITICAL = createEnchantConfig("low_damage_critical", "低伤暴击",
                false, true, true);
        ENCHANTMENTS.put("low_damage_critical", LOW_DAMAGE_CRITICAL);
        
        // 普伤暴击附魔配置 - 不是宝藏，可发现，可交易
        DAMAGE_CRITICAL = createEnchantConfig("damage_critical", "普伤暴击",
                false, true, true);
        ENCHANTMENTS.put("damage_critical", DAMAGE_CRITICAL);
        
        // 超频暴击附魔配置 - 是宝藏，不可发现，不可交易
        CRITICAL_OVERCLOCK = createEnchantConfig("critical_overclock", "超频暴击",
                true, false, false);
        ENCHANTMENTS.put("critical_overclock", CRITICAL_OVERCLOCK);
        
        BUILDER.pop();
        
        // 横扫类附魔配置
        BUILDER.comment("横扫类附魔配置").push("sweeping_enchantments");
        
        // 强化横扫附魔配置 - 不是宝藏，可发现，可交易
        SWEEPING_ENHANCEMENT = createEnchantConfig("sweeping_enhancement", "强化横扫",
                false, true, true);
        ENCHANTMENTS.put("sweeping_enhancement", SWEEPING_ENHANCEMENT);
        
        // 歼斩横扫附魔配置 - 不是宝藏，可发现，可交易
        SWEEPING_SLASH = createEnchantConfig("sweeping_slash", "歼斩横扫",
                false, true, true);
        ENCHANTMENTS.put("sweeping_slash", SWEEPING_SLASH);
        
        // 火之横扫附魔配置 - 不是宝藏，可发现，可交易
        SWEEPING_FIRE = createEnchantConfig("sweeping_fire", "火之横扫",
                false, true, true);
        ENCHANTMENTS.put("sweeping_fire", SWEEPING_FIRE);
        
        // 炽魂横扫附魔配置 - 不是宝藏，可发现，可交易
        SWEEPING_BURN_SLASH = createEnchantConfig("sweeping_burn_slash", "炽魂横扫",
                false, true, true);
        ENCHANTMENTS.put("sweeping_burn_slash", SWEEPING_BURN_SLASH);
        
        BUILDER.pop();
        
        // 防火类附魔配置
        BUILDER.comment("防火类附魔配置").push("fire_protection_enchantments");
        
        // 焚火庇护附魔配置 - 不是宝藏，可发现，可交易
        FIRE_PROTECTION = createEnchantConfig("fire_protection", "焚火庇护",
                false, true, true);
        ENCHANTMENTS.put("fire_protection", FIRE_PROTECTION);
        
        // 欲火神修附魔配置 - 不是宝藏，可发现，不可交易
        DESIRE_FLAME = createEnchantConfig("desire_flame", "欲火神修",
                false, true, false);
        ENCHANTMENTS.put("desire_flame", DESIRE_FLAME);
        
        BUILDER.pop();
        
        // 篝愈附魔配置 - 不是宝藏，可发现，不可交易
        CAMPFIRE_HEAL = createEnchantConfig("campfire_heal", "篝愈",
                false, true, false);
        ENCHANTMENTS.put("campfire_heal", CAMPFIRE_HEAL);
        
        // 惧慑附魔配置 - 不是宝藏，可发现，不可交易
        DANGEROUS_AURA = createEnchantConfig("dangerous_aura", "惧慑",
                false, true, false);
        ENCHANTMENTS.put("dangerous_aura", DANGEROUS_AURA);
        
        BUILDER.pop();
        
        // 力弱附魔配置
        BUILDER.comment("力弱附魔配置").push("weakness_enchantments");
        
        // 力弱附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        WEAKNESS = createEnchantConfig("weakness", "力弱",
                false, true, true);
        ENCHANTMENTS.put("weakness", WEAKNESS);
        
        BUILDER.pop();
        
        // 断弦附魔配置
        BUILDER.comment("断弦附魔配置").push("snap_string_enchantments");
        
        // 断弦附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        SNAP_STRING = createEnchantConfig("snap_string", "断弦",
                false, true, true);
        ENCHANTMENTS.put("snap_string", SNAP_STRING);
        
        BUILDER.pop();
        
        // 回收附魔配置
        BUILDER.comment("回收附魔配置").push("recycle_enchantments");
        
        // 回收附魔配置 - 稀有度：稀有，不是宝藏，可发现，可交易
        RECYCLE = createEnchantConfig("recycle", "回收",
                false, true, true);
        ENCHANTMENTS.put("recycle", RECYCLE);
        
        BUILDER.pop();
        
        // 高原缺氧附魔配置
        BUILDER.comment("高原缺氧附魔配置").push("high_altitude_oxygen_deficiency_enchantments");
        
        // 高原缺氧附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        HIGH_ALTITUDE_OXYGEN_DEFICIENCY = createEnchantConfig("high_altitude_oxygen_deficiency", "高原缺氧",
                false, true, true);
        ENCHANTMENTS.put("high_altitude_oxygen_deficiency", HIGH_ALTITUDE_OXYGEN_DEFICIENCY);
        
        BUILDER.pop();
        
        // 憋气附魔配置
        BUILDER.comment("憋气附魔配置").push("hold_breath_enchantments");
        
        // 憋气附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        HOLD_BREATH = createEnchantConfig("hold_breath", "憋气",
                false, true, true);
        ENCHANTMENTS.put("hold_breath", HOLD_BREATH);
        
        BUILDER.pop();
        
        // 输血附魔配置
        BUILDER.comment("输血附魔配置").push("blood_transfusion_enchantments");
        
        // 输血附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        BLOOD_TRANSFUSION = createEnchantConfig("blood_transfusion", "输血",
                false, true, true);
        ENCHANTMENTS.put("blood_transfusion", BLOOD_TRANSFUSION);
        
        BUILDER.pop();
        
        // 血契附魔配置
        BUILDER.comment("血契附魔配置").push("blood_pact_enchantments");
        
        // 血契附魔配置 - 稀有度：不常见，不是宝藏，可发现，可交易
        BLOOD_PACT = createEnchantConfig("blood_pact", "血契",
                false, true, true);
        ENCHANTMENTS.put("blood_pact", BLOOD_PACT);
        
        BUILDER.pop();
        
        // 呼吸回血附魔配置
        BUILDER.comment("呼吸回血附魔配置").push("breath_heal_enchantments");
        
        // 呼吸回血附魔配置 - 稀有度：稀有，是宝藏，不可发现，不可交易
        BREATH_HEAL = createEnchantConfig("breath_heal", "呼吸回血",
                true, false, false);
        ENCHANTMENTS.put("breath_heal", BREATH_HEAL);
        
        BUILDER.pop();
        // 胸甲类附魔配置
        BUILDER.comment("胸甲类附魔配置").push("chestplate_enchantments");
        
        // 焃战附魔配置 - 不是宝藏，可发现，可交易
        BLAZING_WAR = createEnchantConfig("blazing_war", "焃战",
                false, true, true);
        ENCHANTMENTS.put("blazing_war", BLAZING_WAR);
        
        // 焚斥之心附魔配置 - 是宝藏，不可发现，不可交易
        INFERNAL_ARMOR = createEnchantConfig("infernal_armor", "焚斥之心",
                true, false, false);
        ENCHANTMENTS.put("infernal_armor", INFERNAL_ARMOR);
        
        // 炙浴附魔配置 - 不是宝藏，可发现，不可交易
        INFERNAL_REBIRTH = createEnchantConfig("infernal_rebirth", "炙浴",
                false, true, false);
        ENCHANTMENTS.put("infernal_rebirth", INFERNAL_REBIRTH);
        
        // 火噬附魔配置 - 不是宝藏，可发现，可交易
        FIRE_DEVOUR = createEnchantConfig("fire_devour", "火噬",
                false, true, true);
        ENCHANTMENTS.put("fire_devour", FIRE_DEVOUR);
        
        // 焚心附魔配置 - 不是宝藏，可发现，可交易
        BURNING_HEART = createEnchantConfig("burning_heart", "焚心",
                false, true, true);
        ENCHANTMENTS.put("burning_heart", BURNING_HEART);
        
        // 炎壳附魔配置 - 是宝藏，不可发现，不可交易
        FLAME_SHELL = createEnchantConfig("flame_shell", "炎壳",
                true, false, false);
        ENCHANTMENTS.put("flame_shell", FLAME_SHELL);
        
        // 焚怒附魔配置 - 不是宝藏，可发现，不可交易
        BURNING_FURY = createEnchantConfig("burning_fury", "焚怒",
                false, true, false);
        ENCHANTMENTS.put("burning_fury", BURNING_FURY);
        
        // 潮涌之心附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        HEART_OF_TIDE = createEnchantConfig("heart_of_tide", "潮涌之心",
                false, true, false);
        ENCHANTMENTS.put("heart_of_tide", HEART_OF_TIDE);
        
        // 干渴之心附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        HEART_OF_DROUGHT = createEnchantConfig("heart_of_drought", "干渴之心",
                false, true, false);
        ENCHANTMENTS.put("heart_of_drought", HEART_OF_DROUGHT);
        
        BUILDER.pop();
        
        // 24号祝福附魔配置
        BUILDER.comment("24号祝福附魔配置").push("blessing_no24_enchantments");
        
        // 24号祝福附魔配置 - 不是宝藏，可发现，不可交易
        BLESSING_NO24 = createEnchantConfig("blessing_no24", "24号祝福",
                false, true, false);
        ENCHANTMENTS.put("blessing_no24", BLESSING_NO24);
        
        BUILDER.pop();
        
        // 格挡类附魔配置
        BUILDER.comment("格挡类附魔配置").push("block_enchantments");
        
        // 格挡附魔配置 - 不是宝藏，可发现，不可交易
        SIMPLE_BLOCK = createEnchantConfig("simple_block", "格挡",
                false, true, false);
        ENCHANTMENTS.put("simple_block", SIMPLE_BLOCK);
        
        // 生命格挡附魔配置 - 不是宝藏，可发现，不可交易
        LIFE_BLOCK = createEnchantConfig("life_block", "生命格挡",
                false, true, false);
        ENCHANTMENTS.put("life_block", LIFE_BLOCK);
        
        BUILDER.pop();
        
        // 诅咒类附魔配置
        BUILDER.comment("诅咒类附魔配置").push("curse_enchantments");
        
        // 亡灵诅咒附魔配置 - 不是宝藏，可发现，可交易
        UNDEAD_CURSE = createEnchantConfig("undead_curse", "亡灵诅咒",
                false, true, true);
        ENCHANTMENTS.put("undead_curse", UNDEAD_CURSE);
        
        BUILDER.pop();
        
        // 灵魂类附魔配置
        BUILDER.comment("灵魂类附魔配置").push("soul_enchantments");
        
        // 灵魂持有附魔配置 - 不是宝藏，可发现，可交易
        SOULBOUND = createEnchantConfig("soulbound", "灵魂持有",
                false, true, true);
        ENCHANTMENTS.put("soulbound", SOULBOUND);
        
        BUILDER.pop();
        
        // 腐蚀类附魔配置
        BUILDER.comment("腐蚀类附魔配置").push("corrosion_enchantments");
        
        // 下届腐蚀附魔配置 - 不是宝藏，可发现，可交易
        NETHER_CORROSION = createEnchantConfig("nether_corrosion", "下届腐蚀",
                false, true, true);
        ENCHANTMENTS.put("nether_corrosion", NETHER_CORROSION);
        
        // 末地腐蚀附魔配置 - 不是宝藏，可发现，可交易
        END_CORROSION = createEnchantConfig("end_corrosion", "末地腐蚀",
                false, true, true);
        ENCHANTMENTS.put("end_corrosion", END_CORROSION);
        
        // 主界腐蚀附魔配置 - 不是宝藏，可发现，可交易
        OVERWORLD_CORROSION = createEnchantConfig("overworld_corrosion", "主界腐蚀",
                false, true, true);
        ENCHANTMENTS.put("overworld_corrosion", OVERWORLD_CORROSION);
        
        BUILDER.pop();
        
        // 坠落类附魔配置
        BUILDER.comment("坠落类附魔配置").push("fall_enchantments");
        
        // 急坠急停附魔配置 - 不是宝藏，可发现，可交易
        FAST_FALL = createEnchantConfig("fast_fall", "急坠急停",
                false, true, true);
        ENCHANTMENTS.put("fast_fall", FAST_FALL);
        
        // 坠机附魔配置 - 不是宝藏，可发现，可交易
        CRASH_LANDING = createEnchantConfig("crash_landing", "坠机",
                false, true, true);
        ENCHANTMENTS.put("crash_landing", CRASH_LANDING);
        
        // 顿挫附魔配置 - 不是宝藏，可发现，可交易
        STAGGERING_BLOW = createEnchantConfig("staggering_blow", "顿挫",
                false, true, true);
        ENCHANTMENTS.put("staggering_blow", STAGGERING_BLOW);
        
        // 核心采集附魔配置 - 是宝藏，不可发现，不可交易
        CORE_COLLECTION = createEnchantConfig("core_collection", "核心采集",
                true, false, false);
        ENCHANTMENTS.put("core_collection", CORE_COLLECTION);
        
        // 摄魂镰附魔配置 - 不是宝藏，可发现，不可交易
        BODY_SNATCH = createEnchantConfig("body_snatch", "摄魂镰",
                false, true, false);
        ENCHANTMENTS.put("body_snatch", BODY_SNATCH);
        
        // 轻语附魔配置 - 不是宝藏，可发现，不可交易
        WHISPER = createEnchantConfig("whisper", "轻语",
                false, true, false);
        ENCHANTMENTS.put("whisper", WHISPER);
        
        // 敌引附魔配置 - 是宝藏，不可发现，不可交易
        ENEMY_TAUNT = createEnchantConfig("enemy_taunt", "敌引",
                true, false, false);
        ENCHANTMENTS.put("enemy_taunt", ENEMY_TAUNT);
        
        // 锥心附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        ARMOR_PIERCING = createEnchantConfig("armor_piercing", "锥心",
                false, true, false);
        ENCHANTMENTS.put("armor_piercing", ARMOR_PIERCING);
        
        // 破铠附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        ARMOR_BREAK = createEnchantConfig("armor_break", "破铠",
                false, true, false);
        ENCHANTMENTS.put("armor_break", ARMOR_BREAK);
        
        // 燧击附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        FLINT_STRIKE = createEnchantConfig("flint_strike", "燧击",
                false, true, false);
        ENCHANTMENTS.put("flint_strike", FLINT_STRIKE);
        
        BUILDER.pop();
        
        // 弓类附魔配置
        BUILDER.comment("弓类附魔配置").push("bow_enchantments");
        
        // 燧心附魔配置 - 不是宝藏，可发现，可交易
        HEART_OF_FLINT = createEnchantConfig("heart_of_flint", "燧心",
                false, true, true);
        ENCHANTMENTS.put("heart_of_flint", HEART_OF_FLINT);
        
        // 弹射附魔配置 - 不是宝藏，可发现，可交易
        BOUNCE = createEnchantConfig("bounce", "弹射",
                false, true, true);
        ENCHANTMENTS.put("bounce", BOUNCE);
        
        // 游侠附魔配置 - 不是宝藏，可发现，不可交易
        RANGER = createEnchantConfig("ranger", "游侠",
                false, true, false);
        ENCHANTMENTS.put("ranger", RANGER);
        
        // 磐射附魔配置 - 不是宝藏，可发现，不可交易
        STEADY_SHOT = createEnchantConfig("steady_shot", "磐射",
                false, true, false);
        ENCHANTMENTS.put("steady_shot", STEADY_SHOT);
        
        // 重程弩附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        REPEATING_BOLT = createEnchantConfig("repeating_bolt", "重程弩",
                false, true, false);
        ENCHANTMENTS.put("repeating_bolt", REPEATING_BOLT);
        
        // 穿体附魔配置 - 稀有度：不常见，不是宝藏，可发现，可交易
        PIERCING = createEnchantConfig("piercing", "穿体",
                false, true, true);
        ENCHANTMENTS.put("piercing", PIERCING);
        
        // 游隼附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        PEREGRINE = createEnchantConfig("peregrine", "游隼",
                false, true, false);
        ENCHANTMENTS.put("peregrine", PEREGRINE);
        
        // 游魂矢附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        SOUL_ARROW = createEnchantConfig("soul_arrow", "游魂矢",
                false, true, false);
        ENCHANTMENTS.put("soul_arrow", SOUL_ARROW);
        
        // 燎原矢附魔配置 - 稀有度：不常见，不是宝藏，可发现，不可交易
        WILDFIRE_ARROW = createEnchantConfig("wildfire_arrow", "燎原矢",
                false, true, false);
        ENCHANTMENTS.put("wildfire_arrow", WILDFIRE_ARROW);
        
        // 龙息矢附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        DRAGON_BREATH_ARROW = createEnchantConfig("dragon_breath_arrow", "龙息矢",
                false, true, false);
        ENCHANTMENTS.put("dragon_breath_arrow", DRAGON_BREATH_ARROW);
        
        // 水矢附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        WATER_ARROW = createEnchantConfig("water_arrow", "水矢",
                false, true, false);
        ENCHANTMENTS.put("water_arrow", WATER_ARROW);
        
        // 干矢附魔配置 - 稀有度：不常见，不是宝藏，可发现，不可交易
        DROUGHT_ARROW = createEnchantConfig("drought_arrow", "干矢",
                false, true, false);
        ENCHANTMENTS.put("drought_arrow", DROUGHT_ARROW);
        
        // 沉击附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        HEAVY_IMPACT = createEnchantConfig("heavy_impact", "沉击",
                false, true, false);
        ENCHANTMENTS.put("heavy_impact", HEAVY_IMPACT);
        
        // 传送矢附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        TELEPORT_ARROW = createEnchantConfig("teleport_arrow", "传送矢",
                false, true, false);
        ENCHANTMENTS.put("teleport_arrow", TELEPORT_ARROW);
        
        // 置换矢附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        SWAP_TELEPORT = createEnchantConfig("swap_teleport", "置换矢",
                false, true, false);
        ENCHANTMENTS.put("swap_teleport", SWAP_TELEPORT);
        
        // 玩具附魔配置 - 稀有度：普通，不是宝藏，可发现，不可交易
        TOY_BOW = createEnchantConfig("toy_bow", "玩具",
                false, true, false);
        ENCHANTMENTS.put("toy_bow", TOY_BOW);
        
        // 点射附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        POINT_SHOOT = createEnchantConfig("point_shoot", "点射",
                false, true, false);
        ENCHANTMENTS.put("point_shoot", POINT_SHOOT);
        
        // 传输附魔配置 - 稀有度：不常见，不是宝藏，可发现，不可交易
        TRANSFER = createEnchantConfig("transfer", "传输",
                false, true, false);
        ENCHANTMENTS.put("transfer", TRANSFER);
        
        BUILDER.pop();
        
        // 脚部搭建附魔配置
        BUILDER.comment("脚部搭建附魔配置").push("foot_block_enchantments");
        
        // 脚部搭建附魔配置 - 不是宝藏，可发现，不可交易
        FOOT_BLOCK = createEnchantConfig("foot_block", "脚部搭建",
                false, true, false);
        ENCHANTMENTS.put("foot_block", FOOT_BLOCK);
        
        // 新增策马靴附魔配置 - 不是宝藏，可发现，不可交易
        HORSE_BOOTS = createEnchantConfig("horse_boots", "策马靴",
                false, true, false);
        ENCHANTMENTS.put("horse_boots", HORSE_BOOTS);
        
        BUILDER.pop();
        
        // 范围脚部搭建附魔配置
        BUILDER.comment("范围脚部搭建附魔配置").push("range_foot_block_enchantments");
        
        // 范围脚部搭建附魔配置 - 不是宝藏，可发现，不可交易
        RANGE_FOOT_BLOCK = createEnchantConfig("range_foot_block", "范围脚部搭建",
                false, true, false);
        ENCHANTMENTS.put("range_foot_block", RANGE_FOOT_BLOCK);
        
        BUILDER.pop();
        
        // 恐慌逃离附魔配置
        BUILDER.comment("恐慌逃离附魔配置").push("panic_escape_enchantments");
        
        // 恐慌逃离附魔配置 - 不是宝藏，可发现，不可交易
        PANIC_ESCAPE = createEnchantConfig("panic_escape", "恐慌逃离",
                false, true, false);
        ENCHANTMENTS.put("panic_escape", PANIC_ESCAPE);
        
        BUILDER.pop();
        
        // 绝境逃离附魔配置
        BUILDER.comment("绝境逃离附魔配置").push("desperate_escape_enchantments");
        
        // 绝境逃离附魔配置 - 不是宝藏，可发现，可交易
        DESPERATE_ESCAPE = createEnchantConfig("desperate_escape", "绝境逃离",
                false, true, true);
        ENCHANTMENTS.put("desperate_escape", DESPERATE_ESCAPE);
        
        BUILDER.pop();
        
        // 诅咒类附魔配置
        BUILDER.comment("诅咒类附魔配置").push("curse_enchantments");
        
        // 主界诅咒附魔配置 - 不是宝藏，可发现，可交易
        OVERWORLD_CURSE = createEnchantConfig("overworld_curse", "主界诅咒",
                false, true, true);
        ENCHANTMENTS.put("overworld_curse", OVERWORLD_CURSE);
        
        // 下界诅咒附魔配置 - 不是宝藏，可发现，可交易
        NETHER_CURSE = createEnchantConfig("nether_curse", "下界诅咒",
                false, true, true);
        ENCHANTMENTS.put("nether_curse", NETHER_CURSE);
        
        // 末地诅咒附魔配置 - 不是宝藏，可发现，可交易
        END_CURSE = createEnchantConfig("end_curse", "末地诅咒",
                false, true, true);
        ENCHANTMENTS.put("end_curse", END_CURSE);
        
        BUILDER.pop();
        
        // 头盔类附魔配置
        BUILDER.comment("头盔类附魔配置").push("helmet_enchantments");
        
        // 感知附魔配置 - 不是宝藏，可发现，可交易
        SENSE = createEnchantConfig("sense", "感知",
                false, true, true);
        ENCHANTMENTS.put("sense", SENSE);
        
        // 国王附魔配置 - 不是宝藏，可发现，不可交易
        KING = createEnchantConfig("king", "国王",
                false, true, false);
        ENCHANTMENTS.put("king", KING);
        
        // 将军附魔配置 - 不是宝藏，可发现，不可交易
        GENERAL = createEnchantConfig("general", "将军",
                false, true, false);
        ENCHANTMENTS.put("general", GENERAL);
        
        // 统领附魔配置 - 不是宝藏，可发现，不可交易
        COMMANDER = createEnchantConfig("commander", "统领",
                false, true, false);
        ENCHANTMENTS.put("commander", COMMANDER);
        
        // 亡灵统帅附魔配置 - 不是宝藏，可发现，不可交易
        UNDEAD_COMMANDER = createEnchantConfig("undead_commander", "亡灵统帅",
                false, true, false);
        ENCHANTMENTS.put("undead_commander", UNDEAD_COMMANDER);
        
        // 灾厄统领附魔配置 - 不是宝藏，可发现，不可交易
        ILLAGER_COMMANDER = createEnchantConfig("illager_commander", "灾厄统领",
                false, true, false);
        ENCHANTMENTS.put("illager_commander", ILLAGER_COMMANDER);
        
        // 音躁附魔配置 - 不是宝藏，可发现，可交易
        NOISE_ANNOYANCE = createEnchantConfig("noise_annoyance", "音躁",
                false, true, true);
        ENCHANTMENTS.put("noise_annoyance", NOISE_ANNOYANCE);
        
        // 匪首附魔配置 - 不是宝藏，可发现，不可交易
        CHIEFTAIN = createEnchantConfig("chieftain", "匪首",
                false, true, false);
        ENCHANTMENTS.put("chieftain", CHIEFTAIN);
        
        // 吸血鬼附魔配置 - 不是宝藏，可发现，不可交易
        VAMPIRE = createEnchantConfig("vampire", "吸血鬼",
                false, true, false);
        ENCHANTMENTS.put("vampire", VAMPIRE);
        
        BUILDER.pop();
        
        // 剑类附魔配置
        BUILDER.comment("剑类附魔配置").push("sword_enchantments");
        
        // 孤狼附魔配置 - 不是宝藏，可发现，可交易
        LONE_WOLF = createEnchantConfig("lone_wolf", "孤狼",
                false, true, true);
        ENCHANTMENTS.put("lone_wolf", LONE_WOLF);
        
        // 领袖附魔配置 - 不是宝藏，可发现，可交易
        LEADER = createEnchantConfig("leader", "领袖",
                false, true, true);
        ENCHANTMENTS.put("leader", LEADER);
        
        // 嗜血附魔配置 - 不是宝藏，可发现，不可交易
        BLOODTHIRSTY = createEnchantConfig("bloodthirsty", "嗜血",
                false, true, false);
        ENCHANTMENTS.put("bloodthirsty", BLOODTHIRSTY);
        
        // 饮血棱附魔配置 - 是宝藏，不可发现，不可交易
        BLOOD_EDGE = createEnchantConfig("blood_edge", "饮血棱",
                true, false, false);
        ENCHANTMENTS.put("blood_edge", BLOOD_EDGE);
        
        // 匿猎附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        STEALTH_HUNTER = createEnchantConfig("stealth_hunter", "匿猎",
                false, true, false);
        ENCHANTMENTS.put("stealth_hunter", STEALTH_HUNTER);
        
        // 力叹斩附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        SIGHING_STRIKE = createEnchantConfig("sighing_strike", "力叹斩",
                false, true, false);
        ENCHANTMENTS.put("sighing_strike", SIGHING_STRIKE);
        
        // 力竭附魔配置 - 稀有度：普通，不是宝藏，可发现，可交易
        EXHAUSTION = createEnchantConfig("exhaustion", "力竭",
                false, true, true);
        ENCHANTMENTS.put("exhaustion", EXHAUSTION);
        
        // 弑魁附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        HEADHUNTER = createEnchantConfig("headhunter", "弑魁",
                false, true, false);
        ENCHANTMENTS.put("headhunter", HEADHUNTER);
        
        // 啃食附魔配置
        BUILDER.comment("啃食附魔配置").push("bite_enchantments");
        
        // 啃食附魔配置 - 不是宝藏，可发现，可交易
        BITE = createEnchantConfig("bite", "啃食",
                false, true, true);
        ENCHANTMENTS.put("bite", BITE);
        
        BUILDER.pop();
        
        // 敌速追行附魔配置
        BUILDER.comment("敌速追行附魔配置").push("taunt_excitement_enchantments");
        
        // 敌速追行附魔配置 - 不是宝藏，可发现，可交易
        TAUNT_EXCITEMENT = createEnchantConfig("taunt_excitement", "敌速追行",
                false, true, true);
        ENCHANTMENTS.put("taunt_excitement", TAUNT_EXCITEMENT);
        
        BUILDER.pop();
        
        // 胸闷附魔配置
        BUILDER.comment("胸闷附魔配置").push("chest_tightness_enchantments");
        
        // 胸闷附魔配置 - 不是宝藏，可发现，可交易
        CHEST_TIGHTNESS = createEnchantConfig("chest_tightness", "胸闷",
                false, true, true);
        ENCHANTMENTS.put("chest_tightness", CHEST_TIGHTNESS);
        
        BUILDER.pop();
        
        // 骨折附魔配置
        BUILDER.comment("骨折附魔配置").push("fracture_enchantments");
        
        // 骨折附魔配置 - 不是宝藏，可发现，可交易
        FRACTURE = createEnchantConfig("fracture", "骨折",
                false, true, true);
        ENCHANTMENTS.put("fracture", FRACTURE);
        
        BUILDER.pop();
        
        // 伤裂附魔配置
        BUILDER.comment("伤裂附魔配置").push("wound_rift_enchantments");
        
        // 伤裂附魔配置 - 不是宝藏，可发现，可交易
        WOUND_RIFT = createEnchantConfig("wound_rift", "伤裂",
                false, true, true);
        ENCHANTMENTS.put("wound_rift", WOUND_RIFT);
        
        BUILDER.pop();
        
        // 伤残附魔配置
        BUILDER.comment("伤残附魔配置").push("maim_enchantments");
        
        // 伤残附魔配置 - 不是宝藏，可发现，可交易
        MAIM = createEnchantConfig("maim", "伤残",
                false, true, true);
        ENCHANTMENTS.put("maim", MAIM);
        
        BUILDER.pop();
        
        // 盔损附魔配置
        BUILDER.comment("盔损附魔配置").push("armor_damage_enchantments");
        
        // 盔损附魔配置 - 不是宝藏，可发现，可交易
        ARMOR_DAMAGE = createEnchantConfig("armor_damage", "盔损",
                false, true, true);
        ENCHANTMENTS.put("armor_damage", ARMOR_DAMAGE);
        
        BUILDER.pop();
        
        // 盔碎附魔配置
        BUILDER.comment("盔碎附魔配置").push("armor_shatter_enchantments");
        
        // 盔碎附魔配置 - 不是宝藏，可发现，可交易
        ARMOR_SHATTER = createEnchantConfig("armor_shatter", "盔碎",
                false, true, true);
        ENCHANTMENTS.put("armor_shatter", ARMOR_SHATTER);
        
        // 镰伤附魔配置
        BUILDER.comment("镰伤附魔配置").push("sickle_wound_enchantments");
        
        // 镰伤附魔配置 - 不是宝藏，可发现，不可交易
        SICKLE_WOUND = createEnchantConfig("sickle_wound", "镰伤",
                false, true, false);
        ENCHANTMENTS.put("sickle_wound", SICKLE_WOUND);
        
        BUILDER.pop();
        
        // 镰斩附魔配置
        BUILDER.comment("镰斩附魔配置").push("sickle_slash_enchantments");
        
        // 镰斩附魔配置 - 不是宝藏，可发现，不可交易
        SICKLE_SLASH = createEnchantConfig("sickle_slash", "镰斩",
                false, true, false);
        ENCHANTMENTS.put("sickle_slash", SICKLE_SLASH);
        
        BUILDER.pop();
        
        // 召军附魔配置
        BUILDER.comment("召军附魔配置").push("army_summon_enchantments");
        
        // 召军附魔配置 - 稀有度：非常稀有，不是宝藏，可发现，不可交易
        ARMY_SUMMON = createEnchantConfig("army_summon", "召军",
                false, true, false);
        ENCHANTMENTS.put("army_summon", ARMY_SUMMON);
        
        BUILDER.pop();
        
        // 翅韧战甲附魔配置
        BUILDER.comment("翅韧战甲附魔配置").push("elytra_armor_enchantments");
        
        // 翅韧战甲附魔配置 - 稀有度：稀有，不是宝藏，可发现，可交易
        ELYTRA_ARMOR = createEnchantConfig("elytra_armor", "翅韧战甲",
                false, true, true);
        ENCHANTMENTS.put("elytra_armor", ELYTRA_ARMOR);
        
        BUILDER.pop();
        
        // 石粒人附魔配置
        BUILDER.comment("石粒人附魔配置").push("stone_pellet_man_enchantments");
        
        // 石粒人附魔配置 - 稀有度：稀有，不是宝藏，可发现，不可交易
        STONE_PELLET_MAN = createEnchantConfig("stone_pellet_man", "石粒人",
                false, true, false);
        ENCHANTMENTS.put("stone_pellet_man", STONE_PELLET_MAN);
        
        BUILDER.pop();
        
        // 经验收割附魔配置
        BUILDER.comment("经验收割附魔配置").push("experience_harvest_enchantments");
        
        // 经验收割附魔配置 - 不是宝藏，可发现，不可交易
        EXPERIENCE_HARVEST = createEnchantConfig("experience_harvest", "经验收割",
                false, true, false);
        ENCHANTMENTS.put("experience_harvest", EXPERIENCE_HARVEST);
        
        BUILDER.pop();
        
        // 作物收割附魔配置
        BUILDER.comment("作物收割附魔配置").push("crop_harvest_enchantments");
        
        // 作物收割附魔配置 - 不是宝藏，可发现，不可交易
        CROP_HARVEST = createEnchantConfig("crop_harvest", "作物收割",
                false, true, false);
        ENCHANTMENTS.put("crop_harvest", CROP_HARVEST);
        
        BUILDER.pop();
        
        // 掉落收割附魔配置
        BUILDER.comment("掉落收割附魔配置").push("drop_reaper_enchantments");
        
        // 掉落收割附魔配置 - 不是宝藏，可发现，不可交易
        DROP_REAPER = createEnchantConfig("drop_reaper", "掉落收割",
                false, true, false);
        ENCHANTMENTS.put("drop_reaper", DROP_REAPER);
        
        // 铲刃附魔配置
        BUILDER.comment("铲刃附魔配置").push("sharp_edge_enchantments");
        
        // 铲刃附魔配置 - 不是宝藏，可发现，不可交易
        SHARP_EDGE = createEnchantConfig("sharp_edge", "铲刃",
                false, true, false);
        ENCHANTMENTS.put("sharp_edge", SHARP_EDGE);
        
        // 铲重击附魔配置
        BUILDER.comment("铲重击附魔配置").push("shovel_heavy_blow_enchantments");
        
        // 铲重击附魔配置 - 不是宝藏，可发现，不可交易
        SHOVEL_HEAVY_BLOW = createEnchantConfig("shovel_heavy_blow", "铲重击",
                false, true, false);
        ENCHANTMENTS.put("shovel_heavy_blow", SHOVEL_HEAVY_BLOW);
        
        // 铲戟附魔配置
        BUILDER.comment("铲戟附魔配置").push("shovel_trident_enchantments");
        
        // 铲戟附魔配置 - 不是宝藏，可发现，不可交易
        SHOVEL_TRIDENT = createEnchantConfig("shovel_trident", "铲戟",
                false, true, false);
        ENCHANTMENTS.put("shovel_trident", SHOVEL_TRIDENT);
        
        // 翘甲附魔配置
        BUILDER.comment("翘甲附魔配置").push("shovel_armor_prize_enchantments");
        
        // 翘甲附魔配置 - 不是宝藏，可发现，不可交易
        SHOVEL_ARMOR_PRIZE = createEnchantConfig("shovel_armor_prize", "翘甲",
                false, true, false);
        ENCHANTMENTS.put("shovel_armor_prize", SHOVEL_ARMOR_PRIZE);
        
        // 功能军锹附魔配置
        BUILDER.comment("功能军锹附魔配置").push("military_shovel_enchantments");
        
        // 功能军锹附魔配置 - 不是宝藏，可发现，可交易
        MILITARY_SHOVEL = createEnchantConfig("military_shovel", "功能军锹",
                false, true, true);
        ENCHANTMENTS.put("military_shovel", MILITARY_SHOVEL);
        
        // 万能军锹附魔配置
        BUILDER.comment("万能军锹附魔配置").push("universal_shovel_enchantments");
        
        // 万能军锹附魔配置 - 不是宝藏，可发现，不可交易
        UNIVERSAL_SHOVEL = createEnchantConfig("universal_shovel", "万能军锹",
                false, true, false);
        ENCHANTMENTS.put("universal_shovel", UNIVERSAL_SHOVEL);
        
        // 铲刃附魔配置
        BUILDER.comment("铲刃附魔配置").push("shovel_strike_enchantments");
        
        // 铲刃附魔配置 - 不是宝藏，可发现，不可交易
        SHOVEL_STRIKE = createEnchantConfig("shovel_strike", "铲刃",
                false, true, false);
        ENCHANTMENTS.put("shovel_strike", SHOVEL_STRIKE);
        
        // 融雪靴附魔配置
        BUILDER.comment("融雪靴附魔配置").push("snow_removal_boot_enchantments");
        
        // 融雪靴附魔配置 - 不是宝藏，可发现，可交易
        SNOW_REMOVAL_BOOT = createEnchantConfig("snow_removal_boot", "融雪靴",
                false, true, true);
        ENCHANTMENTS.put("snow_removal_boot", SNOW_REMOVAL_BOOT);
        
        // 耕耘锄附魔配置
        BUILDER.comment("耕耘锄附魔配置").push("tillage_boot_enchantments");
        
        // 耕耘锄附魔配置 - 不是宝藏，可发现，可交易
        TILLAGE_BOOT = createEnchantConfig("tillage_boot", "耕耘靴子",
                false, true, true);
        ENCHANTMENTS.put("tillage_boot", TILLAGE_BOOT);
        
        // 除草锄附魔配置
        BUILDER.comment("除草锄附魔配置").push("weed_removal_boot_enchantments");
        
        // 除草锄附魔配置 - 不是宝藏，可发现，可交易
        WEED_REMOVAL_BOOT = createEnchantConfig("weed_removal_boot", "除草靴",
                false, true, true);
        ENCHANTMENTS.put("weed_removal_boot", WEED_REMOVAL_BOOT);
        
        // 祭魂镰斩附魔配置
        BUILDER.comment("祭魂镰斩附魔配置").push("soul_reaping_sickle_enchantments");
        
        // 祭魂镰斩附魔配置 - 是宝藏，不可发现，不可交易
        SOUL_REAPING_SICKLE = createEnchantConfig("soul_reaping_sickle", "祭魂镰斩",
                true, false, false);
        ENCHANTMENTS.put("soul_reaping_sickle", SOUL_REAPING_SICKLE);
        
        BUILDER.pop();
        
        // 付魂斩附魔配置
        BUILDER.comment("付魂斩附魔配置").push("soul_paying_cut_enchantments");
        
        // 付魂斩附魔配置 - 不是宝藏，可发现，不可交易
        SOUL_PAYING_CUT = createEnchantConfig("soul_paying_cut", "付魂斩",
                false, true, false);
        ENCHANTMENTS.put("soul_paying_cut", SOUL_PAYING_CUT);
        
        // 斩杀率附魔配置
        BUILDER.comment("斩杀率附魔配置").push("execute_rate_enchantments");
        
        // 斩杀率附魔配置 - 不是宝藏，可发现，不可交易
        EXECUTE_RATE = createEnchantConfig("execute_rate", "斩杀率",
                false, true, false);
        ENCHANTMENTS.put("execute_rate", EXECUTE_RATE);
        
        BUILDER.pop();
        
        // 斩杀力附魔配置
        BUILDER.comment("斩杀力附魔配置").push("execute_power_enchantments");
        
        // 斩杀力附魔配置 - 不是宝藏，可发现，不可交易
        EXECUTE_POWER = createEnchantConfig("execute_power", "斩杀力",
                false, true, false);
        ENCHANTMENTS.put("execute_power", EXECUTE_POWER);
        
        BUILDER.pop();
        
        // 斩首附魔配置
        BUILDER.comment("斩首附魔配置").push("decapitation_enchantments");
        
        // 斩首附魔配置 - 不是宝藏，可发现，可交易
        DECAPITATION = createEnchantConfig("decapitation", "斩首",
                false, true, true);
        ENCHANTMENTS.put("decapitation", DECAPITATION);
        
        BUILDER.pop();
        
        // 钝碾附魔配置
        BUILDER.comment("钝碾附魔配置").push("blunt_crushing_enchantments");
        
        // 钝碾附魔配置 - 不是宝藏，可发现，可交易
        BLUNT_CRUSHING = createEnchantConfig("blunt_crushing", "钝碾",
                false, true, true);
        ENCHANTMENTS.put("blunt_crushing", BLUNT_CRUSHING);
        
        BUILDER.pop();
        
        // 锋刃附魔配置
        BUILDER.comment("锋刃附魔配置").push("sharp_edge_weapon_enchantments");
        
        // 锋刃附魔配置 - 不是宝藏，可发现，可交易
        SHARP_EDGE_ENCHANTMENT = createEnchantConfig("sharp_edge_weapon", "锋刃",
                false, true, true);
        ENCHANTMENTS.put("sharp_edge_weapon", SHARP_EDGE_ENCHANTMENT);
        

        BUILDER.pop();
        
        // 食力斩附魔配置
        BUILDER.comment("食力斩附魔配置").push("food_power_strike_enchantments");
        
        // 食力斩附魔配置 - 不是宝藏，可发现，可交易
        FOOD_POWER_STRIKE = createEnchantConfig("food_power_strike", "食力斩",
                false, true, true);
        ENCHANTMENTS.put("food_power_strike", FOOD_POWER_STRIKE);
        
        BUILDER.pop();
        
        // 残忍附魔配置
        BUILDER.comment("残忍附魔配置").push("cruelty_enchantments");
        
        // 残忍附魔配置 - 不是宝藏，可发现，可交易
        CRUELTY = createEnchantConfig("cruelty", "残忍",
                false, true, true);
        ENCHANTMENTS.put("cruelty", CRUELTY);
        
        BUILDER.pop();
        
        // 骑士附魔配置
        BUILDER.comment("骑士附魔配置").push("knight_enchantments");
        
        // 骑士附魔配置 - 不是宝藏，可发现，可交易
        KNIGHT = createEnchantConfig("knight", "骑士",
                false, true, true);
        ENCHANTMENTS.put("knight", KNIGHT);
        
        BUILDER.pop();
        
        // 专注伏击附魔配置
        BUILDER.comment("专注伏击附魔配置").push("focused_ambush_enchantments");
        
        // 专注伏击附魔配置 - 不是宝藏，可发现，可交易
        FOCUSED_AMBUSH = createEnchantConfig("focused_ambush", "专注伏击",
                false, true, true);
        ENCHANTMENTS.put("focused_ambush", FOCUSED_AMBUSH);
        
        BUILDER.pop();
        
        // 无双附魔配置
        BUILDER.comment("无双附魔配置").push("dual_wield_enchantments");
        
        // 无双附魔配置 - 不是宝藏，可发现，不可交易
        DUAL_WIELD = createEnchantConfig("dual_wield", "无双",
                false, true, false);
        ENCHANTMENTS.put("dual_wield", DUAL_WIELD);
        
        BUILDER.pop();
        
        // 牧师祝福附魔配置
        BUILDER.comment("牧师祝福附魔配置").push("priest_blessing_enchantments");
        
        // 牧师祝福附魔配置 - 不是宝藏，可发现，不可交易
        PRIEST_BLESSING = createEnchantConfig("priest_blessing", "牧师祝福",
                false, true, false);
        ENCHANTMENTS.put("priest_blessing", PRIEST_BLESSING);
        
        BUILDER.pop();
        
        // 雷劫附魔配置
        BUILDER.comment("雷劫附魔配置").push("thunder_retribution_enchantments");
        
        // 雷劫附魔配置 - 不是宝藏，可发现，不可交易
        THUNDER_RETRIBUTION = createEnchantConfig("thunder_retribution", "雷劫",
                false, true, false);
        ENCHANTMENTS.put("thunder_retribution", THUNDER_RETRIBUTION);
        
        BUILDER.pop();
        
        // 耗氧冲刺附魔配置
        BUILDER.comment("耗氧冲刺附魔配置").push("oxygen_depleting_sprint_enchantments");
        
        // 耗氧冲刺附魔配置 - 不是宝藏，可发现，可交易
        OXYGEN_DEPLETING_SPRINT = createEnchantConfig("oxygen_depleting_sprint", "耗氧冲刺",
                false, true, true);
        ENCHANTMENTS.put("oxygen_depleting_sprint", OXYGEN_DEPLETING_SPRINT);
        
        BUILDER.pop();
        
        // 缺氧急行附魔配置
        BUILDER.comment("缺氧急行附魔配置").push("hypoxia_sprint_enchantments");
        
        // 缺氧急行附魔配置 - 不是宝藏，可发现，可交易
        HYPOXIA_SPRINT = createEnchantConfig("hypoxia_sprint", "缺氧急行",
                false, true, true);
        ENCHANTMENTS.put("hypoxia_sprint", HYPOXIA_SPRINT);
        
        BUILDER.pop();
        
        // 雨润附魔配置
        BUILDER.comment("雨润附魔配置").push("rain_nourishment_enchantments");
        
        // 雨润附魔配置 - 不是宝藏，可发现，不可交易
        RAIN_NOURISHMENT = createEnchantConfig("rain_nourishment", "雨润",
                false, true, false);
        ENCHANTMENTS.put("rain_nourishment", RAIN_NOURISHMENT);
        
        BUILDER.pop();
        
        // 深层缺氧（深层压力）附魔配置
        BUILDER.comment("深层缺氧（深层压力）附魔配置").push("deep_pressure_enchantments");
        
        // 深层缺氧（深层压力）附魔配置 - 不是宝藏，可发现，可交易
        DEEP_PRESSURE = createEnchantConfig("deep_pressure", "深层缺氧（深层压力）",
                false, true, true);
        ENCHANTMENTS.put("deep_pressure", DEEP_PRESSURE);
        
        BUILDER.pop();
        
        // 海洋庇佑附魔配置
        BUILDER.comment("海洋庇佑附魔配置").push("ocean_blessing_enchantments");
        
        // 海洋庇佑附魔配置 - 不是宝藏，可发现，不可交易
        OCEAN_BLESSING = createEnchantConfig("ocean_blessing", "海洋庇佑",
                false, true, false);
        ENCHANTMENTS.put("ocean_blessing", OCEAN_BLESSING);
        
        BUILDER.pop();
        
        // 铁肺附魔配置
        BUILDER.comment("铁肺附魔配置").push("iron_lung_enchantments");
        
        // 铁肺附魔配置 - 不是宝藏，可发现，不可交易
        IRON_LUNG = createEnchantConfig("iron_lung", "铁肺",
                false, true, false);
        ENCHANTMENTS.put("iron_lung", IRON_LUNG);
        
        BUILDER.pop();
        
        // 绝境坚韧附魔配置
        BUILDER.comment("绝境坚韧附魔配置").push("desperate_resilience_enchantments");
        
        // 绝境坚韧附魔配置 - 不是宝藏，可发现，不可交易
        DESPERATE_RESILIENCE = createEnchantConfig("desperate_resilience", "绝境坚韧",
                false, true, false);
        ENCHANTMENTS.put("desperate_resilience", DESPERATE_RESILIENCE);
        
        BUILDER.pop();
        
        // 绝境杀戮附魔配置
        BUILDER.comment("绝境杀戮附魔配置").push("desperate_kill_enchantments");
        
        // 绝境杀戮附魔配置 - 不是宝藏，可发现，不可交易
        DESPERATE_KILL = createEnchantConfig("desperate_kill", "绝境杀戮",
                false, true, false);
        ENCHANTMENTS.put("desperate_kill", DESPERATE_KILL);
        
        BUILDER.pop();
        
        // 心动律动附魔配置
        BUILDER.comment("心动律动附魔配置").push("heartbeat_rhythm_enchantments");
        
        // 心动律动附魔配置 - 不是宝藏，可发现，不可交易
        HEARTBEAT_RHYTHM = createEnchantConfig("heartbeat_rhythm", "心动律动",
                false, true, false);
        ENCHANTMENTS.put("heartbeat_rhythm", HEARTBEAT_RHYTHM);
        
        BUILDER.pop();
        
        // 武道律动附魔配置
        BUILDER.comment("武道律动附魔配置").push("martial_rhythm_enchantments");
        
        // 武道律动附魔配置 - 不是宝藏，可发现，不可交易
        MARTIAL_RHYTHM = createEnchantConfig("martial_rhythm", "武道律动",
                false, true, false);
        ENCHANTMENTS.put("martial_rhythm", MARTIAL_RHYTHM);
        
        BUILDER.pop();
        
        // 杀戮感知附魔配置
        BUILDER.comment("杀戮感知附魔配置").push("killing_sense_enchantments");
        
        // 杀戮感知附魔配置 - 不是宝藏，可发现，可交易
        KILLING_SENSE = createEnchantConfig("killing_sense", "杀戮感知",
                false, true, true);
        ENCHANTMENTS.put("killing_sense", KILLING_SENSE);
        
        BUILDER.pop();
        
        // 烈火创伤附魔配置
        BUILDER.comment("烈火创伤附魔配置").push("burning_trauma_enchantments");
        
        // 烈火创伤附魔配置 - 不是宝藏，可发现，可交易
        BURNING_TRAUMA = createEnchantConfig("burning_trauma", "烈火创伤",
                false, true, true);
        ENCHANTMENTS.put("burning_trauma", BURNING_TRAUMA);
        
        // 强力反伤附魔配置 - 是宝藏，不可发现，不可交易
        STRONG_THORNS = createEnchantConfig("strong_thorns", "强力反伤",
                true, false, false);
        ENCHANTMENTS.put("strong_thorns", STRONG_THORNS);
        
        // 斥魂爆震附魔配置 - 不是宝藏，可发现，不可交易
        SOUL_REPULSION = createEnchantConfig("soul_repulsion", "斥魂爆震",
                false, true, false);
        ENCHANTMENTS.put("soul_repulsion", SOUL_REPULSION);
        
        // 掉落吸引附魔配置 - 不是宝藏，可发现，可交易
        LOOT_ATTRACTION = createEnchantConfig("loot_attraction", "掉落吸引",
                false, true, true);
        ENCHANTMENTS.put("loot_attraction", LOOT_ATTRACTION);
        
        // 掠财附魔配置 - 不是宝藏，可发现，不可交易
        PLUNDER_WEALTH = createEnchantConfig("plunder_wealth", "掠财",
                false, true, false);
        ENCHANTMENTS.put("plunder_wealth", PLUNDER_WEALTH);
        
        // 脚臭附魔配置 - 不是宝藏，可发现，可交易
        STINKY_FEET = createEnchantConfig("stinky_feet", "脚臭",
                false, true, true);
        ENCHANTMENTS.put("stinky_feet", STINKY_FEET);
        
        // 超级脚臭附魔配置 - 不是宝藏，可发现，不可交易
        SUPER_STINKY_FEET = createEnchantConfig("super_stinky_feet", "超级脚臭",
                false, true, false);
        ENCHANTMENTS.put("super_stinky_feet", SUPER_STINKY_FEET);
        
        // 盾震附魔配置 - 不是宝藏，可发现，不可交易
        SHIELD_SHOCK = createEnchantConfig("shield_shock", "盾震",
                false, true, false);
        ENCHANTMENTS.put("shield_shock", SHIELD_SHOCK);
        
        // 仇引附魔配置 - 不是宝藏，可发现，可交易
        HATRED_TAUNT = createEnchantConfig("hatred_taunt", "仇引",
                false, true, true);
        ENCHANTMENTS.put("hatred_taunt", HATRED_TAUNT);
        
        // 氧力悬停附魔配置 - 不是宝藏，可发现，可交易
        HOVER = createEnchantConfig("hover", "氧力悬停",
                false, true, true);
        ENCHANTMENTS.put("hover", HOVER);
        
        // 专注稳重附魔配置 - 不是宝藏，可发现，不可交易
        FOCUSED_STEADINESS = createEnchantConfig("focused_steadiness", "专注稳重",
                false, true, false);
        ENCHANTMENTS.put("focused_steadiness", FOCUSED_STEADINESS);
        
        // 肉铠附魔配置 - 不是宝藏，可发现，可交易
        VITALITY_BOOST = createEnchantConfig("vitality_boost", "肉铠",
                false, true, true);
        ENCHANTMENTS.put("vitality_boost", VITALITY_BOOST);

        // 久经沙场附魔配置 - 不是宝藏，可发现，不可交易
        BATTLE_HARDENED_WEAPON = createEnchantConfig("battle_hardened_weapon", "久经沙场",
                false, true, false);
        ENCHANTMENTS.put("battle_hardened_weapon", BATTLE_HARDENED_WEAPON);

        // 锻体附魔配置 - 不是宝藏，可发现，不可交易
        EXERCISE = createEnchantConfig("exercise", "锻体",
                false, true, false);
        ENCHANTMENTS.put("exercise", EXERCISE);
        
        // 饕餮附魔配置 - 不是宝藏，可发现，不可交易
        GLUTTONY = createEnchantConfig("gluttony", "饕餮",
                false, true, false);
        ENCHANTMENTS.put("gluttony", GLUTTONY);
        
        // 磐蛮附魔配置 - 不是宝藏，可发现，不可交易
        STURDY_BARBARIAN = createEnchantConfig("sturdy_barbarian", "磐蛮",
                false, true, false);
        ENCHANTMENTS.put("sturdy_barbarian", STURDY_BARBARIAN);
        
        // 钢躯附魔配置 - 不是宝藏，可发现，不可交易
        STEEL_BODY = createEnchantConfig("steel_body", "钢躯",
                false, true, false);
        ENCHANTMENTS.put("steel_body", STEEL_BODY);
        
        // 肥硕附魔配置 - 不是宝藏，可发现，不可交易
        OBESITY = createEnchantConfig("obesity", "肥硕",
                false, true, false);
        ENCHANTMENTS.put("obesity", OBESITY);
        
        // 身经百战附魔配置
        BUILDER.comment("身经百战附魔配置").push("veteran_enchantments");
        
        // 身经百战附魔配置 - 是宝藏，不可发现，不可交易
        VETERAN = createEnchantConfig("veteran", "身经百战",
                true, false, false);
        ENCHANTMENTS.put("veteran", VETERAN);
        
        // 手动粗修附魔配置 - 不是宝藏，可发现，可交易
        MANUAL_ROUGH_REPAIR = createEnchantConfig("manual_rough_repair", "手动粗修",
                false, true, true);
        ENCHANTMENTS.put("manual_rough_repair", MANUAL_ROUGH_REPAIR);
        
        // 手动精修附魔配置 - 不是宝藏，可发现，不可交易
        MANUAL_FINE_REPAIR = createEnchantConfig("manual_fine_repair", "手动精修",
                false, true, false);
        ENCHANTMENTS.put("manual_fine_repair", MANUAL_FINE_REPAIR);
        
        // 初露锋芒附魔配置 - 不是宝藏，可发现，不可交易
        PROMISING_BLADE = createEnchantConfig("promising_blade", "初露锋芒",
                false, true, false);
        ENCHANTMENTS.put("promising_blade", PROMISING_BLADE);
        
        // 战痕累累附魔配置 - 不是宝藏，可发现，可交易
        BATTLE_SCARRED = createEnchantConfig("battle_scarred", "战痕累累",
                false, true, true);
        ENCHANTMENTS.put("battle_scarred", BATTLE_SCARRED);

        
        // 力量突效附魔配置 - 不是宝藏，可发现，可交易
        POWER_BURST = createEnchantConfig("power_burst", "力量突效",
                false, true, true);
        ENCHANTMENTS.put("power_burst", POWER_BURST);
        
        // 力量续效附魔配置 - 不是宝藏，可发现，不可交易
        POWER_SUSTAINED = createEnchantConfig("power_sustained", "力量续效",
                false, true, false);
        ENCHANTMENTS.put("power_sustained", POWER_SUSTAINED);
        
        // 急迫突效附魔配置 - 不是宝藏，可发现，可交易
        HASTE_BURST = createEnchantConfig("haste_burst", "急迫突效",
                false, true, true);
        ENCHANTMENTS.put("haste_burst", HASTE_BURST);
        
        // 急迫续效附魔配置 - 不是宝藏，可发现，不可交易
        HASTE_SUSTAINED = createEnchantConfig("haste_sustained", "急迫续效",
                false, true, false);
        ENCHANTMENTS.put("haste_sustained", HASTE_SUSTAINED);
        
        // 抗性突效附魔配置 - 不是宝藏，可发现，不可交易
        RESISTANCE_BURST = createEnchantConfig("resistance_burst", "抗性突效",
                false, true, false);
        ENCHANTMENTS.put("resistance_burst", RESISTANCE_BURST);
        
        // 抗性续效附魔配置 - 不是宝藏，可发现，不可交易
        RESISTANCE_SUSTAINED = createEnchantConfig("resistance_sustained", "抗性续效",
                false, true, false);
        ENCHANTMENTS.put("resistance_sustained", RESISTANCE_SUSTAINED);
        
        // 夜朦附魔配置 - 不是宝藏，可发现，不可交易
        NIGHT_VISION = createEnchantConfig("night_vision", "夜朦",
                false, true, false);
        ENCHANTMENTS.put("night_vision", NIGHT_VISION);
        
        // 栖所附魔配置 - 不是宝藏，可发现，不可交易
        HABITAT = createEnchantConfig("habitat", "栖所",
                false, true, false);
        ENCHANTMENTS.put("habitat", HABITAT);

        BUILDER.pop();
    }
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static EnchantConfig createEnchantConfig(
            String id, 
            String name, 
            boolean isTreasureOnly, 
            boolean isDiscoverable, 
            boolean isTradeable) {
        
        BUILDER.comment(name + "配置").push(id);
        
        ForgeConfigSpec.BooleanValue treasureConfig = BUILDER.define("is_treasure_only", isTreasureOnly);
        ForgeConfigSpec.BooleanValue discoverableConfig = BUILDER.define("is_discoverable", isDiscoverable);
        ForgeConfigSpec.BooleanValue tradeableConfig = BUILDER.define("is_tradeable", isTradeable);
        
        EnchantConfig config = new EnchantConfig(
            treasureConfig,
            discoverableConfig,
            tradeableConfig
        );
        
        BUILDER.pop();
        return config;
    }

    public static class EnchantConfig {
        public final ForgeConfigSpec.BooleanValue isTreasureOnly;
        public final ForgeConfigSpec.BooleanValue isDiscoverable;
        public final ForgeConfigSpec.BooleanValue isTradeable;

        public EnchantConfig(
                ForgeConfigSpec.BooleanValue isTreasureOnly,
                ForgeConfigSpec.BooleanValue isDiscoverable,
                ForgeConfigSpec.BooleanValue isTradeable) {
            this.isTreasureOnly = isTreasureOnly;
            this.isDiscoverable = isDiscoverable;
            this.isTradeable = isTradeable;
        }
    }
}