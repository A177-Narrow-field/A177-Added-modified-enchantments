package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.events.AirSupplyEventHandler;

import java.util.UUID;

@Mod.EventBusSubscriber
public class ExhaustionEnchantment extends Enchantment {
    // 最大伤害减少百分比
    private static final double MAX_DAMAGE_REDUCTION = 0.9; // 最多减少90%

    public ExhaustionEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("exhaustion");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
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
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手物品是否有力竭附魔
            ItemStack weapon = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXHAUSTION.get(), weapon);
            
            // 如果有力竭附魔
            if (level > 0) {
                UUID playerId = player.getUUID();
                
                // 获取玩家当前氧气值
                int currentAir = AirSupplyEventHandler.getCustomAirSupply(playerId);
                if (currentAir == -1) {
                    // 如果没有自定义氧气值，使用玩家当前氧气值
                    currentAir = player.getAirSupply();
                }
                
                // 计算伤害减少百分比：氧气越少，伤害越低
                // 最大氧气值为300，当前氧气值越低，伤害减少越多
                // 当氧气值为0时，减少 MAX_DAMAGE_REDUCTION * level / getMaxLevel() 的伤害
                double damageReduction = (300.0 - currentAir) / 300.0 * MAX_DAMAGE_REDUCTION * level / 3;
                
                // 确保伤害减少不超过最大值
                if (damageReduction > MAX_DAMAGE_REDUCTION) {
                    damageReduction = MAX_DAMAGE_REDUCTION;
                }
                
                // 应用伤害减少
                float newDamage = event.getAmount() * (1.0f - (float)damageReduction);
                event.setAmount(newDamage);
            }
        }
    }
}