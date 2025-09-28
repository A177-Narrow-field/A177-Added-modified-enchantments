package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class SnowRemovalBootEnchantment extends Enchantment {

    // 定义需要被清除的雪类方块集合
    private static final Set<Block> SNOW_BLOCKS = new HashSet<>();
    
    static {
        SNOW_BLOCKS.add(Blocks.SNOW);
        SNOW_BLOCKS.add(Blocks.SNOW_BLOCK);
        SNOW_BLOCKS.add(Blocks.POWDER_SNOW);
    }
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("snow_removal_boot");
    }

    public SnowRemovalBootEnchantment() {
        // 修改为只能应用于铲子类工具
        super(Rarity.COMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 修改为只能应用于铲子类工具
        return stack.getItem() instanceof ShovelItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 修改为只能应用于铲子类工具
        return this.category.canEnchant(stack.getItem()) && stack.getItem() instanceof ShovelItem && isDiscoverable();
    }

    @Override
    public boolean isTradeable() {
        // 该附魔可以作为交易内容出现
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        // 检查玩家是否蹲下
        if (!player.isCrouching()) {
            return;
        }

        // 检查玩家是否手持铲子类工具
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        boolean isHoldingShovel = mainHandItem.getItem() instanceof ShovelItem || offHandItem.getItem() instanceof ShovelItem;
        
        if (!isHoldingShovel) {
            return;
        }

        // 获取玩家手持的铲子附魔等级
        int snowRemovalLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SNOW_REMOVAL_BOOT.get(), mainHandItem);

        // 只有在有附魔时才处理效果
        if (snowRemovalLevel > 0) {
            tryRemoveSnow(player);
        } else {
            // 检查副手
            snowRemovalLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SNOW_REMOVAL_BOOT.get(), offHandItem);
            if (snowRemovalLevel > 0) {
                tryRemoveSnow(player);
            }
        }
    }

    /**
     * 处理玩家周围的雪类方块清除
     *
     * @param player 玩家实体
     */
    private static void tryRemoveSnow(Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        
        // 定义5*5区域
        int range = 5;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState checkBlockState = level.getBlockState(checkPos);
                    
                    // 检查是否为需要清除的雪类方块
                    if (SNOW_BLOCKS.contains(checkBlockState.getBlock())) {
                        // 破坏方块但不掉落物品
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
        }
    }
}