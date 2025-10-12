package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.WeakHashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SwapTeleportEnchantment extends Enchantment {
    // 用于存储箭矢和武器的映射关系
    private static final WeakHashMap<UUID, ItemStack> ARROW_WEAPON_MAP = new WeakHashMap<>();
    
    public SwapTeleportEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("swap_teleport");
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
        // 检查使用的武器是否有反传矢附魔
        Player player = event.getEntity();
        ItemStack weapon = event.getBow();
        
        if (weapon != null) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.SWAP_TELEPORT.get());
            if (level > 0) {
                // 标记玩家即将发射的箭矢
                player.getTags().add("swap_teleport_shooter");
                
                // 立即开始冷却并消耗耐久
                startCooldownAndDamage(player, weapon);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由有反传矢附魔的武器射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有反传矢附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.SWAP_TELEPORT.get());
                if (level > 0) {
                    // 标记箭矢带有反传矢附魔
                    arrow.getTags().add("swap_teleport");
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
                // 检查箭矢是否带有反传矢附魔
                if (arrow.getTags().contains("swap_teleport")) {
                    // 检查是否已经执行过交换（避免重复执行）
                    if (arrow.getTags().contains("swap_teleport_executed")) {
                        return;
                    }
                    
                    HitResult hitResult = event.getRayTraceResult();
                    
                    // 处理实体击中事件
                    if (hitResult instanceof EntityHitResult entityHitResult) {
                        Entity targetEntity = entityHitResult.getEntity();
                        // 检查目标是否为生物实体
                        if (targetEntity instanceof LivingEntity targetLiving) {
                            // 标记已经执行过交换
                            arrow.getTags().add("swap_teleport_executed");
                            // 交换玩家和目标的位置
                            swapPositions(player, targetLiving, arrow);
                        }
                    }
                }
            }
        }
    }
    
    private static void startCooldownAndDamage(Player player, ItemStack weapon) {
        // 消耗武器10%耐久
        weapon.hurtAndBreak((int) (weapon.getMaxDamage() * 0.1), player, (p) -> {
            p.broadcastBreakEvent(player.getUsedItemHand());
        });
        
        // 设置武器6秒冷却时间（120 ticks）
        player.getCooldowns().addCooldown(weapon.getItem(), 120);
    }
    
    private static void swapPositions(ServerPlayer player, LivingEntity target, AbstractArrow arrow) {
        Level level = player.level();
        
        // 保存玩家当前位置
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        
        // 保存目标当前位置
        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        
        // 检查玩家是否穿戴石粒人附魔胸甲，如果是则免疫传送伤害
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasStonePelletMan = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.STONE_PELLET_MAN.get(), chestplate) > 0;
        
        // 交换位置
        player.teleportTo(targetX, targetY, targetZ);
        target.teleportTo(playerX, playerY, playerZ);
        
        // 播放传送声音
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, new BlockPos((int)targetX, (int)targetY, (int)targetZ), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 对玩家造成传送伤害（类似末影珍珠），除非穿戴石粒人附魔胸甲
        if (!hasStonePelletMan) {
            player.hurt(level.damageSources().fall(), 10.0F);
        }
    }
}