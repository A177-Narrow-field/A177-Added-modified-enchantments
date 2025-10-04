package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulPayingCutEnchantment extends Enchantment {

    public SoulPayingCutEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.SOUL_PAYING_CUT.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.SOUL_PAYING_CUT.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.SOUL_PAYING_CUT.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有付魂斩附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = mainHandItem.getEnchantmentLevel(ModEnchantments.SOUL_PAYING_CUT.get());

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算玩家最大生命值的60%
                float playerMaxHealth = player.getMaxHealth();
                float healthToLose = playerMaxHealth * 0.6f;

                // 使用魔法伤害扣除玩家血量，确保有死亡提示和物品掉落
                player.hurt(player.damageSources().magic(), healthToLose);

                // 计算敌人最大生命值的15%作为真实伤害
                LivingEntity target = (LivingEntity) event.getEntity();
                float targetMaxHealth = target.getMaxHealth();
                float trueDamage = targetMaxHealth * 0.15f;

                // 应用真实伤害（直接设置伤害值）
                event.setAmount(trueDamage);

                // 扣除武器49%的耐久度
                if (mainHandItem.isDamageableItem()) {
                    int damage = Math.max(1, mainHandItem.getMaxDamage() * 49 / 100); // 49%耐久度，至少消耗1点
                    mainHandItem.hurtAndBreak(damage, player, (p) -> {
                        p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                    });
                }
            }
        }
    }
}