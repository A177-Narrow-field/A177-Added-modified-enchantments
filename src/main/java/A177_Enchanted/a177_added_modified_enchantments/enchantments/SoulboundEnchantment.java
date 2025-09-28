package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.utils.CuriosHelper;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class SoulboundEnchantment extends Enchantment {
    private static final String SOULBOUND_ITEMS_TAG = "SoulboundItems";
    
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
        if (event.getEntity() instanceof Player player) {
            // 创建一个列表来存储带有灵魂持有附魔的物品
            List<ItemStack> soulboundItems = new ArrayList<>();
            
            // 检查玩家物品栏中的所有物品（包括背包）
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                    soulboundItems.add(stack.copy());
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
            
            // 检查所有装备槽
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR || slot == EquipmentSlot.OFFHAND) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (!stack.isEmpty() && stack.getEnchantmentLevel(ModEnchantments.SOULBOUND.get()) > 0) {
                        soulboundItems.add(stack.copy());
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            
            // 检查Curios饰品栏
            CuriosHelper.handleCuriosItemsOnDeath(player, soulboundItems);
            
            // 将带有灵魂持有附魔的物品保存到玩家数据中，以便在重生时恢复
            if (!soulboundItems.isEmpty()) {
                // 将物品列表存储到玩家的持久化数据中
                CompoundTag persistentData = player.getPersistentData();
                ListTag soulboundItemsTag = new ListTag();
                
                for (ItemStack stack : soulboundItems) {
                    CompoundTag itemTag = new CompoundTag();
                    stack.save(itemTag);
                    soulboundItemsTag.add(itemTag);
                }
                
                persistentData.put(SOULBOUND_ITEMS_TAG, soulboundItemsTag);
                player.getInventory().setChanged();
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            
            // 检查原玩家是否有存储的灵魂绑定物品
            CompoundTag persistentData = originalPlayer.getPersistentData();
            if (persistentData.contains(SOULBOUND_ITEMS_TAG, Tag.TAG_LIST)) {
                ListTag soulboundItemsTag = persistentData.getList(SOULBOUND_ITEMS_TAG, Tag.TAG_COMPOUND);
                
                // 将物品添加到新玩家的物品栏中
                for (int i = 0; i < soulboundItemsTag.size(); i++) {
                    CompoundTag itemTag = soulboundItemsTag.getCompound(i);
                    ItemStack stack = ItemStack.of(itemTag);
                    
                    if (!stack.isEmpty()) {
                        boolean itemHandled = false;
                        
                        // 首先尝试将物品添加到Curios槽位（如果物品应该放在那里）
                        itemHandled = CuriosHelper.tryPlaceInCurios(newPlayer, stack);
                        
                        // 如果不能放在Curios槽位，则尝试放在普通物品栏
                        if (!itemHandled) {
                            // 尝试将物品添加到玩家物品栏
                            if (!newPlayer.getInventory().add(stack)) {
                                // 如果无法添加到物品栏，则掉落在玩家位置
                                newPlayer.drop(stack, false);
                            }
                        }
                    }
                }
                
                // 清除原玩家的数据
                persistentData.remove(SOULBOUND_ITEMS_TAG);
            }
        }
    }
}