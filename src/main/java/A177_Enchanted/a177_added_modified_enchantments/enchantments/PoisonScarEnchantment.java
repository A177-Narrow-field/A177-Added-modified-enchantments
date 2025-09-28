package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class PoisonScarEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("poison_scar");
    }

    public PoisonScarEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 7;
    }

    @Override
    public boolean isDiscoverable() {
        // 从配置文件读取是否可发现
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : true;
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是剑时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 20;
    }//获取最大附魔等级

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;}

    @Override
    public boolean isTradeable() {
        // 从配置文件读取是否可交易
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTradeable.get() : false;
    }//不可通过交易获得

    @Override
    public boolean isTreasureOnly() {
        // 从配置文件读取是否为宝藏附魔
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : false;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && 
               other != ModEnchantments.WITHER_BLADE.get() &&
               other != ModEnchantments.WITHER_EDGE.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.POISON_SCAR.get());

            // 检查是否具有毒痕附魔
            if (level > 0 && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                // 增加1点固定伤害
                event.setAmount(event.getAmount() + 1.0f);

                // 给目标添加毒痕效果，每级增加3秒持续时间
                target.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        level * 60, // 每级5秒
                        0
                ));
            }
        }
    }
}