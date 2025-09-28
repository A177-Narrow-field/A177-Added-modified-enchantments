package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import A177_Enchanted.a177_added_modified_enchantments.config.TransferConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

@Mod.EventBusSubscriber
public class TransferEnchantment extends Enchantment {
    public TransferEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("transfer");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
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
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 可以应用于弓和弩
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    // 添加与其他附魔的冲突规则（如果需要的话）
    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment);
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player shooter) {
                // 获取射击者副手的物品
                ItemStack offhandItem = shooter.getOffhandItem();
                
                // 检查副手是否有物品
                if (offhandItem.isEmpty()) {
                    return;
                }
                
                // 创建要转移的物品副本
                ItemStack transferItem = offhandItem.copy();
                
                HitResult hitResult = event.getRayTraceResult();
                if (hitResult != null) {
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                        Entity target = entityHitResult.getEntity();
                        
                        // 检查击中的是否为玩家
                        if (target instanceof Player targetPlayer) {
                            // 清空射击者副手的物品
                            shooter.getOffhandItem().setCount(0);
                            
                            // 尝试将物品添加到目标玩家背包
                            if (!targetPlayer.getInventory().add(transferItem)) {
                                // 如果背包满了，将物品掉落在目标玩家位置
                                BlockPos targetPos = targetPlayer.blockPosition();
                                ItemEntity itemEntity = new ItemEntity(targetPlayer.level(), targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, transferItem);
                                targetPlayer.level().addFreshEntity(itemEntity);
                            }
                        }
                    } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        Level level = arrow.level();
                        
                        // 检查击中的方块是否有方块实体
                        BlockEntity blockEntity = level.getBlockEntity(blockPos);
                        if (blockEntity instanceof Container container) {
                            // 检查容器是否在阻止列表中
                            String blockEntityName = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType()).toString();
                            if (TransferConfig.blockedContainers.get().contains(blockEntityName)) {
                                // 如果容器在阻止列表中，不做任何操作（不复制物品）
                                return;
                            } else {
                                // 清空射击者副手的物品
                                shooter.getOffhandItem().setCount(0);
                                
                                // 尝试将物品添加到容器中
                                if (addItemToContainer(container, transferItem)) {
                                    // 成功添加到容器中
                                    container.setChanged();
                                } else {
                                    // 如果容器满了，将物品掉落在方块位置
                                    ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, transferItem);
                                    level.addFreshEntity(itemEntity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 尝试将物品添加到容器中
     * @param container 容器
     * @param itemStack 要添加的物品
     * @return 是否成功添加
     */
    private static boolean addItemToContainer(Container container, ItemStack itemStack) {
        // 遍历容器的槽位，寻找可以合并的槽位
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slotItem = container.getItem(i);
            if (slotItem.isEmpty()) {
                // 空槽位，直接放入
                container.setItem(i, itemStack);
                return true;
            } else if (ItemStack.isSameItemSameTags(slotItem, itemStack) && slotItem.getCount() < slotItem.getMaxStackSize()) {
                // 可以合并的物品
                int spaceLeft = slotItem.getMaxStackSize() - slotItem.getCount();
                int transferAmount = Math.min(itemStack.getCount(), spaceLeft);
                
                slotItem.grow(transferAmount);
                itemStack.shrink(transferAmount);
                
                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }
        
        // 检查是否还有剩余物品需要放置
        if (!itemStack.isEmpty()) {
            // 遍历容器的槽位，寻找空槽位
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (container.getItem(i).isEmpty()) {
                    container.setItem(i, itemStack);
                    return true;
                }
            }
        } else {
            return true;
        }
        
        // 容器已满，无法添加更多物品
        return false;
    }
}