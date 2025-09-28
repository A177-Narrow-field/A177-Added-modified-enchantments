package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;

import java.util.Random;

@Mod.EventBusSubscriber
public class ThunderRetributionEnchantment extends Enchantment {
    // 检查间隔 (200 ticks = 10秒)
    private static final int CHECK_INTERVAL = 200;
    
    // 雷击间隔 (10秒)
    private static final int THUNDER_INTERVAL = 10 * 20;
    
    // 常规雷击概率 (5%)
    private static final double NORMAL_THUNDER_CHANCE = 0.05;
    
    // 雨天雷击概率 (50%)
    private static final double RAINY_THUNDER_CHANCE = 0.5;
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("thunder_retribution");
    }

    public ThunderRetributionEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
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
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }//确保在附魔台中可以正确应用

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            Level level = player.level();
            
            // 每隔一定时间执行一次检查
            if (player.tickCount % CHECK_INTERVAL == 0) {
                // 获取玩家头盔上的雷劫附魔等级
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.THUNDER_RETRIBUTION.get(), helmet);
                
                // 只在玩家拥有附魔时处理效果
                if (enchantmentLevel > 0) {
                    // 检查玩家是否有有效的头盔
                    if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem) || helmet.getDamageValue() >= helmet.getMaxDamage()) {
                        return; // 如果没有头盔或头盔已损坏，则不触发效果
                    }
                    
                    // 检查是否到了触发雷击的时间
                    if (player.tickCount % THUNDER_INTERVAL == 0) {
                        // 触发雷击检查
                        attemptThunderStrike(player);
                    }
                }
            }
        }
    }
    
    /**
     * 尝试对玩家进行雷击
     * @param player 玩家实体
     */
    private static void attemptThunderStrike(Player player) {
        Level level = player.level();
        
        // 检查是否为主世界
        if (!level.dimensionType().hasSkyLight()) {
            return;
        }
        
        BlockPos playerPos = player.blockPosition();
        
        // 检查玩家是否在露天（头顶没有方块遮挡）
        BlockPos headPos = playerPos.above(2); // 玩家眼睛位置
        if (!level.canSeeSky(headPos)) {
            return; // 玩家不在露天环境
        }
        
        // 计算雷击概率
        double thunderChance = level.isRaining() ? RAINY_THUNDER_CHANCE : NORMAL_THUNDER_CHANCE;
        
        // 判定是否触发雷击
        if (RANDOM.nextDouble() < thunderChance) {
            // 对玩家进行雷击
            BlockPos strikePos = playerPos; // 直接在玩家位置生成雷电　
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningBolt != null) {
                lightningBolt.moveTo(strikePos.getX(), strikePos.getY(), strikePos.getZ());
                level.addFreshEntity(lightningBolt);
            }
        }
    }
}