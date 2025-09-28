package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.CoreCollectionConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class CoreCollectionEnchantment extends Enchantment {
    // 定义可以被核心采集破坏并掉落的方块集合
    private static final Set<Block> ALLOWED_BLOCKS = new HashSet<>();

    public CoreCollectionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof PickaxeItem;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("core_collection");
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
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    /**
     * 重新加载配置文件中的方块列表
     */
    public static void reloadConfig() {
        // 清空现有的方块列表
        ALLOWED_BLOCKS.clear();
        
        // 从配置文件加载方块列表
        for (String blockId : CoreCollectionConfig.allowedBlocks.get()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
            if (resourceLocation != null) {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null && block != Blocks.AIR) {
                    ALLOWED_BLOCKS.add(block);
                } else {
                    // 使用System.out.println替代无法访问的LOGGER
                    System.out.println("核心采集附魔配置中包含无效方块: " + blockId);
                }
            } else {
                // 使用System.out.println替代无法访问的LOGGER
                System.out.println("核心采集附魔配置中包含无效方块ID: " + blockId);
            }
        }
        
        // 使用System.out.println替代无法访问的LOGGER
        System.out.println("核心采集附魔已加载 " + ALLOWED_BLOCKS.size() + " 个方块");
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        // 检查是否在服务端处理
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 检查被破坏的方块是否在允许列表中
        BlockPos pos = event.getPos();
        BlockState blockState = event.getLevel().getBlockState(pos);
        Block block = blockState.getBlock();

        if (!ALLOWED_BLOCKS.contains(block)) {
            return; // 如果不是允许的方块，直接返回
        }

        // 检查玩家是否使用带有核心采集附魔的镐子
        ItemStack tool = event.getEntity().getMainHandItem();
        int level = tool.getEnchantmentLevel(ModEnchantments.CORE_COLLECTION.get());

        if (level <= 0) {
            return; // 如果没有核心采集附魔，直接返回
        }

        // 手动创建并掉落方块物品
        Player player = event.getEntity();
        Level levelWorld = event.getLevel();
        
        // 获取方块对应的物品
        ItemStack blockItem = new ItemStack(block);
        
        // 在方块位置创建物品实体
        ItemEntity itemEntity = new ItemEntity(levelWorld, 
                pos.getX() + 0.5, 
                pos.getY() + 0.5, 
                pos.getZ() + 0.5, 
                blockItem);
        
        // 添加物品实体到世界
        levelWorld.addFreshEntity(itemEntity);
        
        // 破坏方块（不掉落物品，因为我们已经手动处理了）
        levelWorld.destroyBlock(pos, false, player);
        
        // 消耗40%的耐久度
        int maxDamage = tool.getMaxDamage();
        if (maxDamage > 0) {
            int damageToApply = Math.max(1, (int) (maxDamage * 0.4)); // 至少消耗1点耐久
            
            // 执行耐久度扣除
            tool.setDamageValue(tool.getDamageValue() + damageToApply);
            
            // 如果物品损坏，则破坏它
            if (tool.getDamageValue() >= maxDamage) {
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
        
        // 取消事件以防止正常的挖掘操作
        event.setCanceled(true);
    }
}