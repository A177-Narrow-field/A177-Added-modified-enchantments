package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModBlocks;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class LavaWalkerEnchantment extends Enchantment {
    // 用于跟踪每个玩家的下次生成时间
    private static final Map<UUID, Long> PLAYER_NEXT_PROCESS_TIME = new HashMap<>();
    
    public LavaWalkerEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 10;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.LAVA_WALKER.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.LAVA_WALKER.isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.LAVA_WALKER.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && 
               AllEnchantmentsConfig.LAVA_WALKER.isDiscoverable.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        
        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否装备了熔岩行者附魔的靴子
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LAVA_WALKER.get(), boots);

        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) {
            PLAYER_NEXT_PROCESS_TIME.remove(player.getUUID());
            return;
        }

        ServerLevel levelServer = (ServerLevel) player.level();
        long currentTime = levelServer.getGameTime();
        
        // 获取玩家下次处理时间
        long nextProcessTime = PLAYER_NEXT_PROCESS_TIME.getOrDefault(player.getUUID(), 0L);
        
        // 根据附魔等级确定处理间隔：1级每20tick，2级每10tick，3级每1tick
        int interval = switch (level) {
            case 1 -> 20;
            case 2 -> 10;
            case 3 -> 1;
            default -> 20;
        };
        
        // 如果还没到处理时间，则跳过
        if (currentTime < nextProcessTime) {
            return;
        }
        
        // 更新下次处理时间
        PLAYER_NEXT_PROCESS_TIME.put(player.getUUID(), currentTime + interval);
        
        // 获取玩家脚下的位置
        BlockPos playerPos = player.blockPosition();
        
        // 在玩家脚下和脚上8x8范围内查找岩浆方块并替换为硬化岩浆或空气
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                // 处理脚下方块（y-1）
                BlockPos posBelow = playerPos.offset(x, -1, z);
                processLavaBlock(levelServer, posBelow);
                
                // 处理脚上方块（y+1）
                BlockPos posAbove = playerPos.offset(x, 0, z);
                processLavaBlock(levelServer, posAbove);
            }
        }
    }
    
    /**
     * 处理指定位置的岩浆方块
     * @param levelServer 服务端世界
     * @param pos 方块位置
     */
    private static void processLavaBlock(ServerLevel levelServer, BlockPos pos) {
        BlockState blockState = levelServer.getBlockState(pos);
        FluidState fluidState = levelServer.getFluidState(pos);
        
        // 如果是岩浆方块，则替换为硬化岩浆方块
        if (blockState.is(Blocks.LAVA)) {
            // 检查是否为源头岩浆
            if (fluidState.isSource()) {
                // 只将源头岩浆替换为硬化岩浆块
                levelServer.setBlock(pos, ModBlocks.HARDENED_LAVA_BLOCK.get().defaultBlockState(), 3);
            } else {
                // 将流动的岩浆直接移除（替换为空气）
                levelServer.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 检查是否是玩家受到攻击
        if (!(event.getEntity() instanceof Player player)) return;

        // 检查玩家是否装备了熔岩行者附魔的靴子
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LAVA_WALKER.get(), boots);

        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) return;

        // 获取伤害源
        DamageSource source = event.getSource();

        // 检查是否是站在岩浆块上受到的伤害
        var damageSources = player.damageSources();
        if (source == damageSources.hotFloor()) {
            // 取消岩浆块伤害
            event.setCanceled(true);
        }
    }
}