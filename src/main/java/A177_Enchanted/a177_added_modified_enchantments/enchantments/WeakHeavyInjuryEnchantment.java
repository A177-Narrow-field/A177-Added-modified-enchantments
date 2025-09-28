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
public class WeakHeavyInjuryEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("weak_heavy_injury");
    }

    public WeakHeavyInjuryEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return stack.getItem() instanceof SwordItem;
    }

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
               other != ModEnchantments.SLOW_HEAVY_BLOW.get() &&
               other != ModEnchantments.SLOW_HEAVY_WEAKNESS.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.WEAK_HEAVY_INJURY.get());

            // 检查是否具有虚弱重伤附魔
            if (level > 0 && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();

                // 给目标添加虚弱效果
                // 每级提升1级虚弱等级和增加3秒持续时间
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        level * 60, // 每级3秒 (60 ticks = 3秒)
                        level - 1   // 等级从0开始，所以1级为虚弱I，2级为虚弱II，以此类推
                ));
            }
        }
    }
}