package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class TendonFistEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("tendon_fist");
    }

    public TendonFistEnchantment() {
        // 恢复为硬编码的稀有度，不再从配置文件读取
        super(Enchantment.Rarity.VERY_RARE, 
              EnchantmentCategory.ARMOR_CHEST, 
              new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 25;
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
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = (LivingEntity) event.getEntity();
            
            // 检查玩家是否空手
            ItemStack mainHandItem = player.getMainHandItem();
            boolean isFistFighting = mainHandItem.isEmpty();

            // 检查玩家胸甲是否有断筋拳甲附魔
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TENDON_FIST.get(), chestplate);

            // 只有在玩家空手且胸甲有附魔时才应用效果
            if (isFistFighting && enchantmentLevel > 0) {
                // 每级施加2秒缓慢效果
                int slownessDuration = enchantmentLevel * 40; // 40 ticks = 2 seconds per level
                // 缓慢等级等于附魔等级
                int slownessAmplifier = enchantmentLevel - 1; // Amplifier is 0-based (level 1 = amplifier 0)
                
                // 对目标施加缓慢效果
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, slownessAmplifier));
            }
        }
    }
}