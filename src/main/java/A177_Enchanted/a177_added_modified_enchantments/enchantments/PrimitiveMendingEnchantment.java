package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class PrimitiveMendingEnchantment extends Enchantment {
    public PrimitiveMendingEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.isDamageableItem();// 可以在物品上使用
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.isDamageableItem();// 可以在附魔台使用
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.PRIMITIVE_MENDING.isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.PRIMITIVE_MENDING.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.PRIMITIVE_MENDING.isDiscoverable.get();
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        // 与经验修补附魔冲突
        return super.checkCompatibility(enchantment) && !"minecraft:mending".equals(enchantment.getDescriptionId());
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();

        // 获取玩家身上的所有可损坏物品，包括物品栏和穿戴的装备
        List<ItemStack> damagedItems = new ArrayList<>();

        // 添加物品栏中的物品
        player.getInventory().items.stream()
                .filter(stack -> stack.isDamaged() && net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PRIMITIVE_MENDING.get(), stack) > 0)
                .forEach(damagedItems::add);

        // 添加穿戴的装备
        player.getArmorSlots().forEach(stack -> {
            if (stack.isDamaged() && net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PRIMITIVE_MENDING.get(), stack) > 0) {
                damagedItems.add(stack);
            }
        });

        // 如果没有附魔了初级经验修补的物品，直接返回
        if (damagedItems.isEmpty()) {
            return;
        }

        int xpValue = orb.getValue();
        int remainingXp = xpValue;

        // 遍历所有附魔了初级经验修补的物品
        for (ItemStack stack : damagedItems) {
            if (remainingXp <= 0) {
                break;
            }

            int enchantLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PRIMITIVE_MENDING.get(), stack);
            if (enchantLevel <= 0) {
                continue;
            }

            // 计算每个经验球能恢复的耐久度
            int restoredDurability = 0;
            for (int i = 0; i < remainingXp; i++) {
                // 每级提供10%的概率恢复1点耐久
                if (player.level().random.nextFloat() < enchantLevel * 0.1f) {
                    restoredDurability++;
                }
            }

            // 应用耐久恢复
            if (restoredDurability > 0) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - restoredDurability));
                remainingXp -= Math.min(remainingXp, restoredDurability);
            }
        }

        // 更新经验球的值
        orb.value = remainingXp;

        // 如果经验球没有剩余经验值，则移除它
        if (remainingXp <= 0) {
            orb.discard();
        }
    }
}