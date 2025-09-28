package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class PriestBlessingEnchantment extends Enchantment {
    // 每1秒清除一次负面效果 (20 ticks = 1秒)
    private static final int CLEAR_INTERVAL = 20;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("priest_blessing");
    }

    public PriestBlessingEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD;
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            
            // 每隔一定时间执行一次
            if (player.tickCount % CLEAR_INTERVAL == 0) {
                // 获取玩家头盔上的牧师祝福附魔等级
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PRIEST_BLESSING.get(), helmet);
                
                // 只在玩家拥有附魔时处理效果
                if (enchantmentLevel > 0) {
                    // 检查玩家是否有有效的头盔
                    if (helmet.isEmpty() || !(helmet.getItem() instanceof ArmorItem) || helmet.getDamageValue() >= helmet.getMaxDamage()) {
                        return; // 如果没有头盔或头盔已损坏，则不进行清除
                    }
                    
                    // 清除所有负面效果
                    clearNegativeEffects(player);
                }
            }
        }
    }
    
    /**
     * 清除玩家身上的所有负面效果
     * @param player 玩家实体
     */
    private static void clearNegativeEffects(Player player) {
        List<MobEffectInstance> effectsToRemove = new ArrayList<>();
        
        // 收集所有负面效果
        for (MobEffectInstance effect : player.getActiveEffects()) {
            // 检查效果是否为负面效果
            if (!effect.getEffect().isBeneficial()) {
                effectsToRemove.add(effect);
            }
        }
        
        // 移除收集到的负面效果
        for (MobEffectInstance effect : effectsToRemove) {
            player.removeEffect(effect.getEffect());
        }
    }
}