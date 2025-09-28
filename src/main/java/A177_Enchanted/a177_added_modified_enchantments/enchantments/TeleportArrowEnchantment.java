package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.WeakHashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class TeleportArrowEnchantment extends Enchantment {
    // 用于存储箭矢和武器的映射关系
    private static final WeakHashMap<UUID, ItemStack> ARROW_WEAPON_MAP = new WeakHashMap<>();
    
    public TeleportArrowEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("teleport_arrow");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 35;
    }

    @Override
    public int getMaxCost(int level) {
        return 55;
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

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        // 检查使用的武器是否有传送箭附魔
        Player player = event.getEntity();
        ItemStack weapon = event.getBow();
        
        if (weapon != null) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.TELEPORT_ARROW.get());
            if (level > 0) {
                // 标记玩家即将发射的箭矢
                player.getTags().add("teleport_arrow_shooter");
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由有传送箭附魔的武器射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有传送箭附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.TELEPORT_ARROW.get());
                if (level > 0) {
                    // 标记箭矢带有传送箭附魔
                    arrow.getTags().add("teleport_arrow");
                    
                    // 存储武器信息用于冷却和耐久消耗
                    arrow.getTags().add("teleport_bow");
                    // 使用PersistentData存储武器信息
                    weapon.save(arrow.getPersistentData().getCompound("teleport_bow"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof ServerPlayer player) {
                // 检查箭矢是否带有传送箭附魔
                if (arrow.getTags().contains("teleport_arrow")) {
                    HitResult hitResult = event.getRayTraceResult();
                    
                    // 处理实体击中事件
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        Entity targetEntity = entityHitResult.getEntity();
                        // 传送玩家到目标实体位置（略微偏移以避免卡在实体内）
                        teleportPlayer(player, targetEntity.getX(), targetEntity.getY() + 1.0, targetEntity.getZ(), arrow);
                    }
                    // 处理方块击中事件
                    else if (hitResult instanceof BlockHitResult blockHitResult) {
                        BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
                        // 传送玩家到目标方块位置
                        teleportPlayer(player, blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, arrow);
                    }
                }
            }
        }
    }
    
    private static void teleportPlayer(ServerPlayer player, double x, double y, double z, AbstractArrow arrow) {
        Level level = player.level();
        
        // 检查传送位置是否安全（不是固体方块内）
        BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
        if (!level.getBlockState(pos).isAir()) {
            y += 1.0; // 如果目标位置有方块，向上移动一格
        }
        
        // 检查玩家是否穿戴石粒人附魔胸甲，如果是则免疫传送伤害
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasStonePelletMan = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), chestplate) > 0;
        
        // 执行传送
        player.teleportTo(x, y, z);
        
        // 播放传送声音
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, new BlockPos((int)x, (int)y, (int)z), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 对玩家造成传送伤害（扣除最大生命值的60%），除非穿戴石粒人附魔胸甲
        if (!hasStonePelletMan) {
            float damage = player.getMaxHealth() * 0.6f;
            player.hurt(level.damageSources().fall(), damage);
        }
        
        // 消耗武器10%耐久
        if (arrow.getTags().contains("teleport_bow")) {
            // 获取玩家当前手持的武器
            ItemStack weapon = player.getMainHandItem();
            if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                weapon = player.getOffhandItem();
            }
            
            // 确保武器仍然具有传送箭附魔
            if (weapon.getEnchantmentLevel(ModEnchantments.TELEPORT_ARROW.get()) > 0) {
                weapon.hurtAndBreak((int) (weapon.getMaxDamage() * 0.1), player, (p) -> {
                    p.broadcastBreakEvent(player.getUsedItemHand());
                });
                
                // 设置武器3秒冷却时间（60 ticks）
                player.getCooldowns().addCooldown(weapon.getItem(), 60);
            }
        }
    }
}