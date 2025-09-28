package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class ManualFineRepairEnchantment extends Enchantment {
    
    private static final Random RANDOM = new Random();

    public ManualFineRepairEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("manual_fine_repair");
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
        // 只能附在有耐久度的物品上
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
        return this.canEnchant(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.MANUAL_ROUGH_REPAIR.get();
    }
    
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        handleManualFineRepair(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        handleManualFineRepair(event.getEntity());
    }
    
    private static void handleManualFineRepair(Player player) {
        // 检查玩家是否蹲下
        if (!player.isCrouching()) {
            return;
        }
        
        // 检查主手物品
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            return;
        }
        
        // 检查物品是否有手动精修附魔
        int level = heldItem.getEnchantmentLevel(ModEnchantments.MANUAL_FINE_REPAIR.get());
        if (level <= 0) {
            return;
        }
        
        // 检查物品耐久度是否符合条件（仅剩60%或仅剩60点耐久）
        int maxDamage = heldItem.getMaxDamage();
        int currentDamage = heldItem.getDamageValue();
        int remainingDurability = maxDamage - currentDamage;
        
        boolean isLowDurability = (remainingDurability <= 60) ||
                                  (maxDamage > 0 && ((double) remainingDurability / maxDamage) <= 0.6);
        
        if (!isLowDurability) {
            return;
        }
        
        // 40%概率修复1点耐久
        if (RANDOM.nextInt(100) < 40) {
            // 修复1点耐久
            if (currentDamage > 0) {
                heldItem.setDamageValue(currentDamage - 1);
                // 播放铁砧使用音效
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        } 
        // 10%概率损耗2点耐久
        else if (RANDOM.nextInt(100) < 10) {
            // 损耗2点耐久
            if (currentDamage + 2 <= maxDamage) {
                heldItem.setDamageValue(currentDamage + 2);
                // 播放物品损坏音效
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                // 如果损耗会导致物品损坏，则直接损坏物品
                heldItem.setDamageValue(maxDamage);
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}