package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class WeakTraumaEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("weak_trauma");
    }
    
    public WeakTraumaEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
    public boolean isTreasureOnly() {
        // 从配置文件读取是否为宝藏附魔
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isTreasureOnly.get() : true;
    }//是宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是剑时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

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
    public boolean isDiscoverable() {
        // 从配置文件读取是否可发现
        AllEnchantmentsConfig.EnchantConfig config = getConfig();
        return config != null ? config.isDiscoverable.get() : false;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && 
               other != ModEnchantments.SLOW_HEAVY_BLOW.get() &&
               other != ModEnchantments.SLOW_HEAVY_WEAKNESS.get() ;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            int level = weapon.getEnchantmentLevel(ModEnchantments.WEAK_TRAUMA.get());

            if (level > 0 && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = event.getEntity();

                // 如果目标有虚弱效果，伤害增加25%
                if (target.hasEffect(MobEffects.WEAKNESS)) {
                    float newDamage = event.getAmount() * 1.25f;
                    
                    // 如果目标没有手持物品，再增加25%伤害
                    if (target.getMainHandItem().isEmpty()) {
                        newDamage *= 1.25f;
                    }
                    
                    event.setAmount(newDamage);
                }
            }
        }
    }
}