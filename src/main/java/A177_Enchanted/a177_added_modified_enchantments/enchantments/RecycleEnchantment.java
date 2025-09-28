package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class RecycleEnchantment extends Enchantment {
    public static final String RECYCLE_ARROW_ITEM_TAG = "RecycleArrowItem";
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("recycle");
    }
    
    public RecycleEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 3 * level;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
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
        return (EnchantmentCategory.BOW.canEnchant(stack.getItem()) || stack.getItem() instanceof CrossbowItem) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    // 添加与穿体、弹射、无限附魔的冲突规则
    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) 
                && enchantment != ModEnchantments.PIERCING.get()
                && enchantment != ModEnchantments.BOUNCE.get()
                && enchantment != net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS;
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        // 检查使用的武器是否有回收附魔
        Player player = event.getEntity();
        ItemStack weapon = event.getBow();
        
        if (weapon != null) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.RECYCLE.get());
            if (level > 0) {
                // 获取玩家将要射出的箭的类型
                ItemStack arrowStack = getArrowStack(player);
                if (!arrowStack.isEmpty()) {
                    // 将箭的信息存储在玩家的标签中，以便在箭实体创建时使用
                    CompoundTag arrowData = new CompoundTag();
                    arrowStack.save(arrowData);
                    player.getPersistentData().put(RECYCLE_ARROW_ITEM_TAG, arrowData);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 检查玩家是否存储了箭的信息
                if (player.getPersistentData().contains(RECYCLE_ARROW_ITEM_TAG)) {
                    // 将箭的信息存储到箭实体中
                    CompoundTag arrowData = player.getPersistentData().getCompound(RECYCLE_ARROW_ITEM_TAG);
                    arrow.getPersistentData().put(RECYCLE_ARROW_ITEM_TAG, arrowData);
                    // 清除玩家数据中的箭信息
                    player.getPersistentData().remove(RECYCLE_ARROW_ITEM_TAG);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有回收附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.RECYCLE.get());
                if (level > 0) {
                    // 计算回收概率 (每级20%)
                    int recycleChance = level * 20;
                    RandomSource random = player.getRandom();
                    
                    // 判断是否触发回收
                    if (random.nextInt(100) < recycleChance) {
                        // 创建箭矢物品
                        ItemStack arrowItem = getArrowItemFromArrowEntity(arrow);
                        
                        // 尝试将箭矢添加到玩家背包
                        if (!player.getInventory().add(arrowItem)) {
                            // 如果背包满了，将箭矢掉落在玩家位置
                            BlockPos playerPos = player.blockPosition();
                            ItemEntity itemEntity = new ItemEntity(player.level(), playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5, arrowItem);
                            player.level().addFreshEntity(itemEntity);
                        }
                        
                        // 箭矢消失并回收
                        arrow.discard();
                    }
                }
            }
        }
    }
    
    /**
     * 根据箭实体类型获取对应的物品
     */
    private static ItemStack getArrowItemFromArrowEntity(AbstractArrow arrow) {
        // 检查箭是否存储了物品信息
        if (arrow.getPersistentData().contains(RECYCLE_ARROW_ITEM_TAG)) {
            CompoundTag arrowData = arrow.getPersistentData().getCompound(RECYCLE_ARROW_ITEM_TAG);
            ItemStack arrowItem = ItemStack.of(arrowData);
            // 确保只回收一个箭
            arrowItem.setCount(1);
            return arrowItem;
        }
        
        // 默认返回普通箭
        return new ItemStack(Items.ARROW, 1);
    }
    
    /**
     * 获取玩家将要射出的箭的类型
     */
    private static ItemStack getArrowStack(Player player) {
        // 检查玩家的物品栏中是否有箭
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = player.getInventory().getItem(i);
            if (itemstack.getItem() instanceof ArrowItem) {
                return itemstack.copy(); // 返回副本而不是原始物品
            }
        }
        return ItemStack.EMPTY;
    }
}