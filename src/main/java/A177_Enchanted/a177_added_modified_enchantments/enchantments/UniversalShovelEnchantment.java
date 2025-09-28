package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class UniversalShovelEnchantment extends Enchantment {
    // 挖掘距离增加的UUID
    public static final UUID BLOCK_REACH_MODIFIER_UUID = UUID.fromString("E1F23456-B5C6-D7E8-F9A0-234567890BCD");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("universal_shovel");
    }
    
    public UniversalShovelEnchantment() {
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
        // 只能附在铲子上
        return stack.getItem() instanceof ShovelItem;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 限制在附魔台上只能对铲子进行附魔
        return stack.getItem() instanceof ShovelItem && isDiscoverable();
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        // 当玩家挖掘方块时，根据附魔等级增加挖掘速度
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();

        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.UNIVERSAL_SHOVEL.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.UNIVERSAL_SHOVEL.get());
            // 每级增加2000%挖掘速度
            event.setNewSpeed(event.getOriginalSpeed() * (1.0f + level * 20.0f));
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时更新挖掘距离属性
        Player player = event.getEntity();
        updatePlayerBlockReach(player);
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时移除挖掘距离属性修饰符
        Player player = event.getEntity();
        removePlayerBlockReachModifier(player);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时更新挖掘距离属性
        Player player = event.getEntity();
        updatePlayerBlockReach(player);
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event) {
        // 当玩家装备变更时更新挖掘距离属性
        if (event.getEntity() instanceof Player player && event.getSlot() == EquipmentSlot.MAINHAND) {
            updatePlayerBlockReach(player);
        }
    }
    
    /**
     * 更新玩家的挖掘距离属性
     * @param player 玩家实体
     */
    private static void updatePlayerBlockReach(Player player) {
        // 移除旧的修饰符
        removePlayerBlockReachModifier(player);
        
        // 检查主手工具是否具有万能军锹附魔
        ItemStack tool = player.getMainHandItem();
        if (!tool.isEmpty() && tool.isEnchanted() && tool.getEnchantmentLevel(ModEnchantments.UNIVERSAL_SHOVEL.get()) > 0) {
            int level = tool.getEnchantmentLevel(ModEnchantments.UNIVERSAL_SHOVEL.get());
            
            // 添加挖掘距离修饰符（每级增加3格挖掘距离）
            if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
                AttributeModifier blockReachModifier = new AttributeModifier(
                        BLOCK_REACH_MODIFIER_UUID,
                        "Universal shovel block reach bonus",
                        level * 3.0,
                        AttributeModifier.Operation.ADDITION
                );
                player.getAttribute(ForgeMod.BLOCK_REACH.get()).addTransientModifier(blockReachModifier);
            }
        }
    }
    
    /**
     * 移除玩家的挖掘距离属性修饰符
     * @param player 玩家实体
     */
    private static void removePlayerBlockReachModifier(Player player) {
        if (player.getAttribute(ForgeMod.BLOCK_REACH.get()) != null) {
            AttributeModifier modifier = player.getAttribute(ForgeMod.BLOCK_REACH.get()).getModifier(BLOCK_REACH_MODIFIER_UUID);
            if (modifier != null) {
                player.getAttribute(ForgeMod.BLOCK_REACH.get()).removeModifier(BLOCK_REACH_MODIFIER_UUID);
            }
        }
    }
}