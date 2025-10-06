package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SimpleBlockEnchantment extends Enchantment {
    // 缓存玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 基础格挡概率 (30%)
    private static final double BASE_BLOCK_CHANCE = 0.3;
    
    // 每级格挡概率提升 (15%)
    private static final double BLOCK_CHANCE_PER_LEVEL = 0.15;
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("simple_block");
    }

    public SimpleBlockEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack) && isDiscoverable(); // 在附魔台可以出现，但仅限于胸甲
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否可被附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可被附魔

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 是否可被发现

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        // 与保护、荆棘和大胃袋附魔冲突
        return !(ench instanceof net.minecraft.world.item.enchantment.ProtectionEnchantment) && 
               !(ench instanceof net.minecraft.world.item.enchantment.ThornsEnchantment) &&
               !(ench instanceof GluttonousPouchEnchantment) &&
               super.checkCompatibility(ench);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 处理格挡效果
        if (event.getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            
            // 获取缓存的附魔等级，如果不存在则计算并缓存
            int enchantmentLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
            if (enchantmentLevel == -1) {
                enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SIMPLE_BLOCK.get(), player);
                PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            }
            
            // 只在玩家拥有附魔时处理效果
            if (enchantmentLevel > 0) {
                // 检查玩家是否有有效的胸甲
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.isEmpty() || !(chestplate.getItem() instanceof ArmorItem) || chestplate.getDamageValue() >= chestplate.getMaxDamage()) {
                    return; // 如果没有胸甲或胸甲已损坏，则不进行格挡
                }
                
                // 计算格挡概率 (基础30% + 每级15%，最高5级=基础30%+4*15%=90%)
                double blockChance = Math.min(0.9, BASE_BLOCK_CHANCE + (enchantmentLevel - 1) * BLOCK_CHANCE_PER_LEVEL);
                
                // 判定是否格挡成功
                if (RANDOM.nextDouble() < blockChance) {
                    // 播放格挡音效
                    playBlockSound(player);
                    
                    // 消耗耐久度
                    consumeDurability(player, enchantmentLevel);
                    
                    // 完全格挡伤害
                    event.setCanceled(true);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && event.getSlot() == EquipmentSlot.CHEST) {
            // 当胸甲装备发生变化时，清除该玩家的缓存，以便重新计算
            UUID playerId = player.getUUID();
            PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清除缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清除缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
    }
    
    /**
     * 消耗装备耐久度
     * @param player 玩家实体
     * @param level 附魔等级
     */
    private static void consumeDurability(Player player, int level) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chestplate.isEmpty() && chestplate.getItem() instanceof ArmorItem) {
            int maxDurability = chestplate.getMaxDamage();
            // 固定消耗：6%最大耐久度，最少6点
            int percentDamage = 6;
            
            int damageToConsume = Math.max(6, (int) (maxDurability * (percentDamage / 100.0)));
            
            // 减少耐久度
            chestplate.hurtAndBreak(damageToConsume, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.CHEST));
        }
    }
    
    /**
     * 播放格挡音效
     * @param player 玩家实体
     */
    private static void playBlockSound(Player player) {
        if (!player.level().isClientSide) {
            // 播放原版盾牌格挡音效
            player.level().playSound(
                null, // 不指定特定玩家
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SHIELD_BLOCK, // 使用原版盾牌格挡音效
                SoundSource.PLAYERS,
                1.0f, // 音量
                1.0f + (RANDOM.nextFloat() * 0.4f - 0.2f) // 音调 (0.8 - 1.2)
            );
        }
    }
}