package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class HeartOfTideEnchantment extends Enchantment {
    // 用于属性修饰符的UUID
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("c8612d9e-0f4a-4f8e-8b8a-1c9d6e4f1a2b");
    
    public HeartOfTideEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("heart_of_tide");
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return isDiscoverable() && canEnchant(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != ModEnchantments.HEART_OF_DROUGHT.get();
    }

    // 添加属性修饰符来显示附魔效果
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        // 只有当物品被装备在胸甲槽位时才应用属性修饰符
        if (event.getSlotType() == EquipmentSlot.CHEST) {
            ItemStack stack = event.getItemStack();
            int level = EnchantmentHelper.getItemEnchantmentLevel(
                    A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments.HEART_OF_TIDE.get(), 
                    stack);
            
            if (level > 0) {
                // 添加护甲韧性加成修饰符
                event.addModifier(Attributes.ARMOR_TOUGHNESS, 
                    new AttributeModifier(ARMOR_TOUGHNESS_UUID, "Heart of tide armor toughness", 5.0, AttributeModifier.Operation.ADDITION));
            }
        }
    }
    
    // 玩家每刻更新事件处理
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (!player.level().isClientSide && event.phase == TickEvent.Phase.END) { // 只在服务端执行，且在结束阶段
            // 获取玩家装备的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestplate.isEmpty()) {
                // 检查胸甲是否具有潮汐之心附魔
                int level = EnchantmentHelper.getItemEnchantmentLevel(
                        A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments.HEART_OF_TIDE.get(), 
                        chestplate);
                if (level > 0) {
                    // 检查玩家是否在水中或雨中
                    if (isInWaterOrRain(player)) {
                        // 给予玩家潮涌能量效果，持续6秒
                        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 120, 0, true, false));
                    }
                    
                    // 检查玩家是否有潮涌能量效果，如果有则每秒恢复1点血量（每隔20 tick 恢复1点血）
                    if (player.hasEffect(MobEffects.CONDUIT_POWER)) {
                        // 每20个tick（即1秒）恢复1点血量
                        if (player.tickCount % 20 == 0) {
                            if (player.getHealth() < player.getMaxHealth()) {
                                player.setHealth(player.getHealth() + 1.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    // 检查玩家是否处于水中或雨中
    public static boolean isInWaterOrRain(Player player) {
        return player.isInWater() || player.level().isRainingAt(player.blockPosition());
    }
}