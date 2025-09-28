package A177_Enchanted.a177_added_modified_enchantments.events;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class HighStepEventHandler {

    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    private static final double BASE_STEP_HEIGHT = 0.6;
    private static final double STEP_HEIGHT_PER_LEVEL = 0.5;
    private static final double MAX_STEP_HEIGHT = 10.0;
    private static final double HEADROOM_CHECK_HEIGHT = 1.0;

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUUID();

        if (player.isCrouching()) {
            player.setMaxUpStep((float) BASE_STEP_HEIGHT);
            return;
        }

        // 每次tick都重新计算附魔等级，确保实时更新
        int highStepLevel = calculateTotalEnchantmentLevel(player);

        // 更新缓存
        Integer cachedLevel = PLAYER_ENCHANTMENT_CACHE.get(playerId);
        if (cachedLevel == null || cachedLevel != highStepLevel) {
            PLAYER_ENCHANTMENT_CACHE.put(playerId, highStepLevel);
        }

        if (highStepLevel > 0) {
            if (hasEnoughHeadroom(player)) {
                double newStepHeight = Math.min(BASE_STEP_HEIGHT + (highStepLevel * STEP_HEIGHT_PER_LEVEL), MAX_STEP_HEIGHT);
                player.setMaxUpStep((float) newStepHeight);
            } else {
                player.setMaxUpStep((float) BASE_STEP_HEIGHT);
            }
        } else {
            player.setMaxUpStep((float) BASE_STEP_HEIGHT);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player &&
                (event.getSlot() == EquipmentSlot.FEET || event.getSlot() == EquipmentSlot.LEGS)) {
            // 立即更新玩家的跨越高度
            updatePlayerStepHeightImmediately(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYER_ENCHANTMENT_CACHE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        PLAYER_ENCHANTMENT_CACHE.remove(event.getEntity().getUUID());
        // 维度切换后立即更新
        updatePlayerStepHeightImmediately(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        PLAYER_ENCHANTMENT_CACHE.remove(event.getEntity().getUUID());
        // 重生后立即更新
        updatePlayerStepHeightImmediately(event.getEntity());
    }

    private static boolean hasEnoughHeadroom(Player player) {
        AABB boundingBox = player.getBoundingBox();
        AABB headroomBox = new AABB(
                boundingBox.minX,
                boundingBox.maxY,
                boundingBox.minZ,
                boundingBox.maxX,
                boundingBox.maxY + HEADROOM_CHECK_HEIGHT,
                boundingBox.maxZ
        );
        return player.level().noCollision(headroomBox);
    }

    private static int calculateTotalEnchantmentLevel(Player player) {
        int totalLevel = 0;

        ItemStack feetStack = player.getItemBySlot(EquipmentSlot.FEET);
        if (!feetStack.isEmpty()) {
            totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HIGH_STEP.get(), feetStack);
        }

        ItemStack legsStack = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!legsStack.isEmpty()) {
            totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HIGH_STEP.get(), legsStack);
        }

        return totalLevel;
    }

    /**
     * 立即更新玩家的跨越高度
     */
    private static void updatePlayerStepHeightImmediately(Entity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int highStepLevel = calculateTotalEnchantmentLevel(player);
        PLAYER_ENCHANTMENT_CACHE.put(player.getUUID(), highStepLevel);

        if (player.isCrouching()) {
            player.setMaxUpStep((float) BASE_STEP_HEIGHT);
        } else if (highStepLevel > 0) {
            if (hasEnoughHeadroom(player)) {
                double newStepHeight = Math.min(BASE_STEP_HEIGHT + (highStepLevel * STEP_HEIGHT_PER_LEVEL), MAX_STEP_HEIGHT);
                player.setMaxUpStep((float) newStepHeight);
            } else {
                player.setMaxUpStep((float) BASE_STEP_HEIGHT);
            }
        } else {
            player.setMaxUpStep((float) BASE_STEP_HEIGHT);
        }
    }

    /**
     * 强制重置玩家的跨越高度（用于调试）
     */
    public static void resetPlayerStepHeight(Player player) {
        player.setMaxUpStep((float) BASE_STEP_HEIGHT);
        PLAYER_ENCHANTMENT_CACHE.remove(player.getUUID());
    }
}