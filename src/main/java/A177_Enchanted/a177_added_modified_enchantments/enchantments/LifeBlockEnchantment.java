package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Random;

@Mod.EventBusSubscriber
public class LifeBlockEnchantment extends Enchantment {
    // 基础格挡概率 (10%)
    private static final double BASE_BLOCK_CHANCE = 0.1;
    
    // 每级格挡概率提升 (10%)
    private static final double BLOCK_CHANCE_PER_LEVEL = 0.1;
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    // 格挡成功消耗的生命值
    private static final float HEALTH_COST = 4.0f;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("life_block");
    }

    public LifeBlockEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        // 与保护、荆棘附魔冲突
        return !(ench instanceof net.minecraft.world.item.enchantment.ProtectionEnchantment) && 
               !(ench instanceof net.minecraft.world.item.enchantment.ThornsEnchantment) &&
               super.checkCompatibility(ench);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 处理格挡效果
        if (event.getEntity() instanceof Player player) {
            // 获取玩家胸甲上的生命格挡附魔等级
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIFE_BLOCK.get(), chestplate);
            
            // 只在玩家拥有附魔时处理效果
            if (enchantmentLevel > 0) {
                // 检查玩家是否有有效的胸甲
                if (chestplate.isEmpty() || !(chestplate.getItem() instanceof ArmorItem) || chestplate.getDamageValue() >= chestplate.getMaxDamage()) {
                    return; // 如果没有胸甲或胸甲已损坏，则不进行格挡
                }
                
                // 计算格挡概率 (基础10% + 每级10%，最高5级=基础10%+4*10%=50%)
                double blockChance = Math.min(0.5, BASE_BLOCK_CHANCE + (enchantmentLevel - 1) * BLOCK_CHANCE_PER_LEVEL);
                
                // 判定是否格挡成功
                if (RANDOM.nextDouble() < blockChance) {
                    // 检查玩家是否有足够的生命值消耗
                    if (player.getHealth() > HEALTH_COST) {
                        // 消耗生命值
                        player.hurt(player.damageSources().generic(), HEALTH_COST);
                        
                        // 播放格挡音效
                        playBlockSound(player);
                        
                        // 减少伤害（格挡成功）
                        event.setAmount(0);
                    }
                }
            }
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