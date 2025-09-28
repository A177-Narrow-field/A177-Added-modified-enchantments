package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class DecapitationEnchantment extends Enchantment {
    
    // 存储实体类型与头颅物品的映射关系
    private static final Map<EntityType<?>, ItemStack> HEAD_MAP = new HashMap<>();
    
    static {
        // 初始化常见生物的头颅映射
        HEAD_MAP.put(EntityType.SKELETON, new ItemStack(Items.SKELETON_SKULL));
        HEAD_MAP.put(EntityType.WITHER_SKELETON, new ItemStack(Items.WITHER_SKELETON_SKULL));
        HEAD_MAP.put(EntityType.PLAYER, new ItemStack(Items.PLAYER_HEAD));
        HEAD_MAP.put(EntityType.ZOMBIE, new ItemStack(Items.ZOMBIE_HEAD));
        HEAD_MAP.put(EntityType.CREEPER, new ItemStack(Items.CREEPER_HEAD));
        HEAD_MAP.put(EntityType.ENDER_DRAGON, new ItemStack(Items.DRAGON_HEAD));
        HEAD_MAP.put(EntityType.PIGLIN, new ItemStack(Items.PIGLIN_HEAD));
    }

    public DecapitationEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack) && isDiscoverable();
    }//可以正确的出现在附魔台

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("decapitation");
    }
    
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现
    
    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在斧和锄上
        return stack.getItem() instanceof AxeItem ||
               stack.getItem() instanceof HoeItem;
    }
    
    /**
     * 计算掉落头颅的概率
     * @param level 附魔等级
     * @return 掉落概率 (0.0-1.0)
     */
    public static double getDropChance(int level) {
        // 每级增加5%概率，最高10级25%
        return Math.min(level * 0.05, 0.25);
    }
    
    /**
     * 创建带有实体数据的头颅物品
     * @param entity 实体
     * @return 头颅物品
     */
    public static ItemStack createHeadForEntity(LivingEntity entity) {
        ItemStack headStack = HEAD_MAP.get(entity.getType()).copy();
        
        // 如果是玩家头颅，需要添加玩家信息
        if (entity instanceof Player player && !headStack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("SkullOwner", player.getGameProfile().getName());
            headStack.setTag(tag);
        }
        // 其他生物的头颅不需要额外处理
        
        return headStack;
    }
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            Level level = player.level();
            if (level.isClientSide) return; // 只在服务端执行
            
            LivingEntity target = event.getEntity();
            
            // 检查目标是否可以掉落头颅
            if (!HEAD_MAP.containsKey(target.getType())) {
                return; // 目标没有对应的头颅
            }
            
            // 获取玩家主手物品的斩首附魔等级
            ItemStack mainHandItem = player.getMainHandItem();
            int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DECAPITATION.get(), mainHandItem);
            
            // 如果没有斩首附魔或者等级为0
            if (enchantmentLevel <= 0) {
                return;
            }
            
            // 计算掉落概率
            double dropChance = getDropChance(enchantmentLevel);
            
            // 随机判断是否掉落头颅
            if (Math.random() < dropChance) {
                // 创建头颅物品
                ItemStack headStack = createHeadForEntity(target);
                
                if (!headStack.isEmpty()) {
                    // 在目标位置掉落头颅
                    BlockPos targetPos = target.blockPosition();
                    level.addFreshEntity(
                        new ItemEntity(level, 
                                      targetPos.getX() + 0.5, 
                                      targetPos.getY() + 0.5, 
                                      targetPos.getZ() + 0.5, 
                                      headStack)
                    );
                }
            }
        }
    }
}