package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

@Mod.EventBusSubscriber
public class FocusedSteadinessEnchantment extends Enchantment {
    // 每级击退抗性增加量
    private static final double KNOCKBACK_RESISTANCE_PER_LEVEL = 2.0;

    public FocusedSteadinessEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_LEGS, new EquipmentSlot[]{EquipmentSlot.LEGS});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    public AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.FOCUSED_STEADINESS;
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
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在腿部装甲上
        return stack.getItem() instanceof ArmorItem && 
            ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.LEGS;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            
            // 检查玩家是否穿着带有专注稳重附魔的腿部装甲
            ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!leggings.isEmpty() && leggings.isEnchanted() && 
                leggings.getEnchantmentLevel(ModEnchantments.FOCUSED_STEADINESS.get()) > 0) {
                
                int level = leggings.getEnchantmentLevel(ModEnchantments.FOCUSED_STEADINESS.get());
                
                // 检查玩家是否蹲下
                if (player.isCrouching()) {
                    // 增加击退抗性（每级+2点击退抗性）
                    double knockbackResistance = level * KNOCKBACK_RESISTANCE_PER_LEVEL;
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)
                        .setBaseValue(knockbackResistance);
                } else {
                    // 移除额外的击退抗性
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)
                        .setBaseValue(0.0);
                }
            } else {
                // 如果没有穿带有该附魔的护腿，确保击退抗性恢复正常
                if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)
                    .getBaseValue() > 0.0) {
                    player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)
                        .setBaseValue(0.0);
                }
            }
        }
    }
}