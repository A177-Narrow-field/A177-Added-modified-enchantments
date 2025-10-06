package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.OreDetectorConfig;
import A177_Enchanted.a177_added_modified_enchantments.entity.OreHighlightEntity;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class OreDetectorEnchantment extends Enchantment {
    // 定义矿物集合
    private static final Set<Block> LOW_TIER_ORES = new HashSet<>();
    private static final Set<Block> MEDIUM_TIER_ORES = new HashSet<>();
    private static final Set<Block> HIGH_TIER_ORES = new HashSet<>();

    public OreDetectorEnchantment() {
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
        return AllEnchantmentsConfig.ENCHANTMENTS.get("ore_detector");
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
     * 重新加载配置文件中的矿物列表
     */
    public static void reloadConfig() {
        // 清空现有的矿物列表
        LOW_TIER_ORES.clear();
        MEDIUM_TIER_ORES.clear();
        HIGH_TIER_ORES.clear();

        // 从配置文件加载低级矿物列表
        for (String blockId : OreDetectorConfig.lowTierOres.get()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
            if (resourceLocation != null) {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null) {
                    LOW_TIER_ORES.add(block);
                }
            }
        }

        // 从配置文件加载中级矿物列表
        for (String blockId : OreDetectorConfig.mediumTierOres.get()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
            if (resourceLocation != null) {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null) {
                    MEDIUM_TIER_ORES.add(block);
                }
            }
        }

        // 从配置文件加载高级矿物列表
        for (String blockId : OreDetectorConfig.highTierOres.get()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(blockId);
            if (resourceLocation != null) {
                Block block = ForgeRegistries.BLOCKS.getValue(resourceLocation);
                if (block != null) {
                    HIGH_TIER_ORES.add(block);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        // 检查是否在服务端处理
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        
        // 检查玩家是否使用带有矿探附魔的镐子
        int level = tool.getEnchantmentLevel(ModEnchantments.ORE_DETECTOR.get());
        if (level <= 0) {
            return; // 如果没有矿探附魔，直接返回
        }

        // 检查工具是否在冷却中
        if (player.getCooldowns().isOnCooldown(tool.getItem())) {
            // 在冷却中，无法再次使用
            return;
        }

        // 创造模式玩家不需要消耗经验值
        if (!player.isCreative()) {
            // 检查玩家是否有足够的经验值（5点经验）
            if (player.totalExperience < 5) {
                player.displayClientMessage(Component.translatable("§c（exp）经验值不足"), true);
                return;
            }

            // 消耗5点经验值
            player.giveExperiencePoints(-5);
        }

        // 探测玩家周围范围内的矿物
        detectOres(player, event.getLevel());

        // 设置冷却时间（2秒）
        player.getCooldowns().addCooldown(tool.getItem(), 40); // 40 ticks = 2秒
    }

    /**
     * 探测玩家周围的矿物
     * @param player 玩家
     * @param level 世界
     */
    private static void detectOres(Player player, Level level) {
        BlockPos playerPos = player.blockPosition();
        // 检查玩家是否潜行，如果潜行使用配置文件中的潜行探测半径，否则使用配置文件中的探测半径
        int radius = player.isShiftKeyDown() ? OreDetectorConfig.sneakingDetectionRadius.get() : OreDetectorConfig.detectionRadius.get();

        // 检查玩家是否持有下界之星
        boolean holdingNetherStar = isHoldingNetherStar(player);

        int lowTierCount = 0;
        int mediumTierCount = 0;
        int highTierCount = 0;

        // 遍历玩家周围指定范围内的方块
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(pos);
                    Block block = blockState.getBlock();

                    // 检查是否为低级矿物
                    if (LOW_TIER_ORES.contains(block)) {
                        lowTierCount++;
                        // 只有在玩家持有下界之星时才生成高亮实体
                        if (holdingNetherStar) {
                            spawnHighlightEntity(level, pos, blockState);
                        }
                    }
                    // 检查是否为中级矿物
                    else if (MEDIUM_TIER_ORES.contains(block)) {
                        mediumTierCount++;
                        // 只有在玩家持有下界之星时才生成高亮实体
                        if (holdingNetherStar) {
                            spawnHighlightEntity(level, pos, blockState);
                        }
                    }
                    // 检查是否为高级矿物
                    else if (HIGH_TIER_ORES.contains(block)) {
                        highTierCount++;
                        // 只有在玩家持有下界之星时才生成高亮实体
                        if (holdingNetherStar) {
                            spawnHighlightEntity(level, pos, blockState);
                        }
                    }
                }
            }
        }

        // 向玩家发送探测结果
        sendDetectionResult((ServerPlayer) player, lowTierCount, mediumTierCount, highTierCount);
    }

    /**
     * 检查玩家是否持有下界之星
     * @param player 玩家
     * @return 是否持有下界之星
     */
    private static boolean isHoldingNetherStar(Player player) {
        // 检查主手和副手是否持有下界之星
        return player.getMainHandItem().is(net.minecraft.world.item.Items.NETHER_STAR) || 
               player.getOffhandItem().is(net.minecraft.world.item.Items.NETHER_STAR);
    }

    /**
     * 生成高亮实体
     * @param level 世界
     * @param pos 方块位置
     * @param blockState 方块状态
     */
    private static void spawnHighlightEntity(Level level, BlockPos pos, BlockState blockState) {
        if (level instanceof ServerLevel serverLevel) {
            OreHighlightEntity highlightEntity = new OreHighlightEntity(serverLevel, pos, blockState);
            serverLevel.addFreshEntity(highlightEntity);
        }
    }

    /**
     * 向玩家发送探测结果
     * @param player 玩家
     * @param lowTierCount 低级矿物数量
     * @param mediumTierCount 中级矿物数量
     * @param highTierCount 高级矿物数量
     */
    private static void sendDetectionResult(ServerPlayer player, int lowTierCount, int mediumTierCount, int highTierCount) {
        StringBuilder message = new StringBuilder();

        if (lowTierCount == 0 && mediumTierCount == 0 && highTierCount == 0) {
            // 没有发现任何矿物
            message.append("§c(None)没有探测到矿物");
        } else {
            // 发现了矿物
            message.append("探测到，");
            
            boolean first = true;
            if (lowTierCount > 0) {
                message.append("§8§l低级矿物§f(").append(lowTierCount).append(")");
                first = false;
            }
            
            if (mediumTierCount > 0) {
                if (!first) message.append("，");
                message.append("§e§l普通矿物§f(").append(mediumTierCount).append(")");
                first = false;
            }
            
            if (highTierCount > 0) {
                if (!first) message.append("，");
                message.append("§6§l高级矿物§f(").append(highTierCount).append(")");
            }
        }

        player.displayClientMessage(Component.literal(message.toString()), true);
    }
}