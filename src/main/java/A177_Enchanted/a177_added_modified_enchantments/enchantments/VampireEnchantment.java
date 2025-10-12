package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VampireEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("vampire");
    }

    public VampireEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在头盔上
        return EnchantmentCategory.ARMOR_HEAD.canEnchant(stack.getItem());
    }

    /**
     * 检查玩家是否在主世界白天且头顶无遮挡
     * @param player 玩家
     * @return 是否满足燃烧条件
     */
    public static boolean shouldBurnInSunlight(Player player) {
        Level level = player.level();
        
        // 只在主世界生效
        if (!level.dimensionType().hasSkyLight()) {
            return false;
        }
        
        BlockPos playerPos = player.blockPosition();
        
        // 检查是否是白天 (0-12000 为白天)
        long dayTime = level.getDayTime() % 24000;
        if (dayTime >= 12000) { // 夜晚
            return false;
        }
        
        // 检查头顶是否有遮挡物
        BlockPos headPos = playerPos.above();
        if (level.canSeeSky(headPos)) {
            // 检查光照强度
            int skyLight = level.getBrightness(LightLayer.SKY, headPos);
            return skyLight >= 15; // 最强光照
        }
        
        return false;
    }

    /**
     * 检查玩家是否正在燃烧
     * @param player 玩家
     * @return 是否正在燃烧（不包括防火效果）
     */
    public static boolean isBurning(Player player) {
        // 只检查玩家是否正在燃烧，不包括防火效果
        return player.getRemainingFireTicks() > 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        // 检查玩家是否拥有吸血鬼附魔
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.VAMPIRE.get());

        if (enchantmentLevel > 0 && shouldBurnInSunlight(player)) {
            // 给玩家添加燃烧效果，持续3秒 (60 ticks)
            player.setSecondsOnFire(3);
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        // 当玩家攻击其他实体时触发
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            // 检查玩家是否拥有吸血鬼附魔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.VAMPIRE.get());
            
            if (enchantmentLevel > 0 && !isBurning(player)) {
                // 计算回复量（伤害的10%，每级增加10%，最少回复1点）
                float healAmount = Math.max(1.0f, event.getAmount() * 0.1f * enchantmentLevel);
                player.heal(healAmount);
            }
        }
    }
}