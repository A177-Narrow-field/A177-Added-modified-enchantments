package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber
public class HasteSurgeEnchantment extends Enchantment {

    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("haste_burst");
    }

    private static final Random RANDOM = new Random();
    // 存储玩家下次可以触发效果的时间
    private static final Map<Player, Integer> PLAYER_NEXT_TRIGGER_TIME = new HashMap<>();

    public HasteSurgeEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附魔在挖掘工具上（镐、斧、锹）
        return super.canEnchant(stack) ||
                stack.getItem() instanceof net.minecraft.world.item.PickaxeItem ||
                stack.getItem() instanceof net.minecraft.world.item.AxeItem ||
                stack.getItem() instanceof net.minecraft.world.item.ShovelItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 只在服务器端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();

        // 检查工具是否有急迫突效附魔
        int hasteSurgeLevel = tool.getEnchantmentLevel(ModEnchantments.HASTE_SURGE.get());

        if (hasteSurgeLevel > 0) {
            // 获取当前游戏刻
            int currentTick = player.tickCount;
            
            // 获取玩家下次触发时间，如果不存在则设为当前时间
            int nextTriggerTime = PLAYER_NEXT_TRIGGER_TIME.getOrDefault(player, currentTick);
            
            // 如果当前时间已达到或超过下次触发时间
            if (currentTick >= nextTriggerTime) {
                // 设置下次触发时间为1秒后（20游戏刻）
                PLAYER_NEXT_TRIGGER_TIME.put(player, currentTick + 20);
                
                // 使用整数随机数来确保概率正确
                // 每级10%概率，所以1级=10%，2级=20%...10级=100%
                int randomValue = RANDOM.nextInt(100); // 0-99

                if (randomValue < hasteSurgeLevel * 10) {
                    // 给玩家急迫效果（6秒，等级0）
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DIG_SPEED,
                            120,  // 6秒 * 20 ticks/秒
                            0,    // 等级0（急迫I）
                            false,  // 显示粒子
                            true   // 显示图标
                    ));
                }
            }
        }
    }
}