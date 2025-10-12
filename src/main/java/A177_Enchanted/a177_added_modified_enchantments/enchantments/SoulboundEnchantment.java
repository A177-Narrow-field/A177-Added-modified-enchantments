package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.utils.CuriosHelper;

import java.util.ArrayList;
import java.util.List;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod.EventBusSubscriber
public class SoulboundEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("soulbound");
    }

    public SoulboundEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int level) {
        return 1;
    }

    @Override
    public int getMaxCost(int level) {
        return 30;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();}// 不可通过交易获得

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment);
    }// 与其他附魔不冲突

    @Override
    public boolean canEnchant(ItemStack stack) {
        return true;
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // 在死亡不掉落模式下，不需要特殊处理灵魂绑定物品
            if (player.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            
            // 创建一个列表来存储带有灵魂持有附魔的物品信息
            List<ItemStack> soulboundItems = new ArrayList<>();
            
            // 检查玩家物品栏中的所有物品（包括背包）
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                    soulboundItems.add(stack.copy());
                    // 从原位置清除物品
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            
            // 检查所有装备槽（包括副手）
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot == EquipmentSlot.OFFHAND) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                        soulboundItems.add(stack.copy());
                        // 从原位置清除物品
                        player.setItemSlot(slot, ItemStack.EMPTY);

                    }
                }
            }
            
            // 检查Curios槽位中的物品
            if (CuriosHelper.CURIOS_LOADED) {
                CuriosApi.getCuriosInventory(player).ifPresent(inventory -> 
                    inventory.getCurios().forEach((id, slotInventory) -> {
                        IDynamicStackHandler stacks = slotInventory.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                                soulboundItems.add(stack.copy());
                                // 从原位置清除物品
                                stacks.setStackInSlot(i, ItemStack.EMPTY);

                            }
                        }
                    })
                );
            }
            
            // 将带有灵魂持有附魔的物品保存到玩家数据中，以便在重生时检查
            if (!soulboundItems.isEmpty()) {
                // 将物品列表存储到玩家的持久化数据中
                net.minecraft.nbt.CompoundTag persistentData = player.getPersistentData();
                net.minecraft.nbt.ListTag soulboundItemsTag = new net.minecraft.nbt.ListTag();
                
                for (ItemStack stack : soulboundItems) {
                    net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
                    stack.save(itemTag);
                    soulboundItemsTag.add(itemTag);
                }
                
                persistentData.put("SoulboundItems", soulboundItemsTag);
            }
            
            // 清除Curios槽位中带有灵魂绑定附魔的物品
            if (CuriosHelper.CURIOS_LOADED) {
                CuriosApi.getCuriosInventory(player).ifPresent(inventory -> 
                    inventory.getCurios().forEach((id, slotInventory) -> {
                        IDynamicStackHandler stacks = slotInventory.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                                stacks.setStackInSlot(i, ItemStack.EMPTY);
                            }
                        }
                    })
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && !event.getEntity().level().isClientSide()) {
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            
            // 在死亡不掉落模式下，不需要特殊处理灵魂绑定物品
            if (newPlayer.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            
            // 检查原玩家是否有存储的灵魂绑定物品
            net.minecraft.nbt.CompoundTag persistentData = originalPlayer.getPersistentData();
            if (persistentData.contains("SoulboundItems", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag soulboundItemsTag = persistentData.getList("SoulboundItems", net.minecraft.nbt.Tag.TAG_COMPOUND);
                
                // 恢复所有保存的灵魂绑定物品
                for (int i = 0; i < soulboundItemsTag.size(); i++) {
                    net.minecraft.nbt.CompoundTag itemTag = soulboundItemsTag.getCompound(i);
                    ItemStack soulboundStack = ItemStack.of(itemTag);
                    
                    if (!soulboundStack.isEmpty()) {
                        // 直接将物品添加到玩家背包中
                        if (!newPlayer.getInventory().add(soulboundStack)) {
                            // 如果背包满了，将物品掉落在玩家位置
                            newPlayer.drop(soulboundStack, false);
                        }
                    }
                }
                
                // 清除原玩家的数据
                persistentData.remove("SoulboundItems");
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 在死亡不掉落模式下，不需要处理掉落事件
            if (player.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) {
                return;
            }
            
            // 遍历所有将要掉落的物品
            event.getDrops().removeIf(itemEntity -> {
                ItemStack stack = itemEntity.getItem();
                // 如果物品有灵魂绑定附魔，则取消其掉落
                return stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0;
            });
        }
    }
}