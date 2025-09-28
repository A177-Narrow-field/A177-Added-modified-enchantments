package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EndCorrosionEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("end_corrosion");
    }

    public EndCorrosionEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
        });
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在有耐久的物品上
        return stack.isDamageableItem();
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

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        // 只在服务端执行逻辑
        if (event.player.level().isClientSide) {
            return;
        }

        // 每30秒检查一次（600 ticks）
        if (event.player.tickCount % 600 != 0) {
            return;
        }

        Player player = event.player;
        Level level = player.level();

        // 检查玩家是否在末地
        if (!level.dimension().location().toString().equals("minecraft:the_end")) {
            return;
        }

        // 检查玩家身上的所有物品
        for (ItemStack stack : player.getInventory().items) {
            checkAndDamageItem(stack, player);
        }
        
        // 检查玩家装备的物品
        for (ItemStack stack : player.getInventory().armor) {
            checkAndDamageItem(stack, player);
        }
        
        // 检查玩家主手和副手的物品
        checkAndDamageItem(player.getMainHandItem(), player);
        checkAndDamageItem(player.getOffhandItem(), player);
    }

    /**
     * 检查并损坏附有末地腐蚀附魔的物品
     * @param stack 物品堆
     * @param player 玩家
     */
    private static void checkAndDamageItem(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return;
        }

        // 检查物品是否有末地腐蚀附魔
        if (stack.getEnchantmentLevel(ModEnchantments.END_CORROSION.get()) <= 0) {
            return;
        }

        // 检查物品是否还有足够的耐久度可以消耗
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        int remainingDurability = maxDamage - currentDamage;

        // 如果物品仅剩下5%或10点耐久就不会继续扣耐久
        double remainingPercentage = (double) remainingDurability / maxDamage;
        if (remainingPercentage <= 0.05 || remainingDurability <= 10) {
            return;
        }

        // 消耗1%的耐久（最少扣1点）
        int damageToApply = Math.max(1, maxDamage / 100);
        stack.hurtAndBreak(damageToApply, player, (p) -> {});
    }
}