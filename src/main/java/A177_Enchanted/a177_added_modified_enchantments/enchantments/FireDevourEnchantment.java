package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class FireDevourEnchantment extends Enchantment {
    // 存储玩家上次回复饥饿度时间
    private static final Map<UUID, Long> lastFeedTimeMap = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("fire_devour");
    }
    
    public FireDevourEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 根据配置决定是否可在附魔台获得
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        // 与焚斥之心、炙浴和焚火庇护冲突
        return super.checkCompatibility(enchantment) && 
               enchantment != ModEnchantments.INFERNAL_ARMOR.get() &&
               enchantment != ModEnchantments.FIRE_PROTECTION.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_DEVOUR.get(), chestplate);

        if (level > 0) {
            // 如果玩家着火，立即熄灭并回复饥饿度
            if (player.getRemainingFireTicks() > 0) {
                player.clearFire();
                
                // 检查回复饥饿度冷却时间 (0.5秒 = 10 ticks)
                UUID playerId = player.getUUID();
                long currentTime = player.level().getGameTime();
                long lastFeedTime = lastFeedTimeMap.getOrDefault(playerId, 0L);
                
                if (currentTime - lastFeedTime >= 10) {
                    // 回复饥饿度 (每级1点) 和 额外饱食度 (固定1点)
                    player.getFoodData().eat(level, 0.0f);
                    player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() + 1.0f);
                    
                    // 更新上次回复饥饿度时间
                    lastFeedTimeMap.put(playerId, currentTime);
                }
            }
        }
    }
}