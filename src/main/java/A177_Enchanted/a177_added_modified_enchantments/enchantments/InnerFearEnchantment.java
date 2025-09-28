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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class InnerFearEnchantment extends Enchantment {

    // 缓存玩家当前的附魔状态，避免重复计算
    private static final WeakHashMap<Player, Boolean> PLAYER_INNER_FEAR_CACHE = new WeakHashMap<>();
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    // 移动速度修饰符UUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("C1D2E3F4-A5B6-7890-ABCD-EF1234567893");

    public InnerFearEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是胸甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }// 确保在附魔台中可以正确应用

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("inner_fear");
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }
        
        // 检查玩家是否在0层以下
        boolean belowY0 = player.position().y < 0;
        Boolean cachedState = PLAYER_INNER_FEAR_CACHE.get(player);
        
        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasInnerFear = !chestArmor.isEmpty() && chestArmor.isEnchanted() && 
            chestArmor.getEnchantmentLevel(ModEnchantments.INNER_FEAR.get()) > 0;
        
        boolean shouldApplyEffect = belowY0 && hasInnerFear;
        
        // 如果缓存状态与当前状态不同，则更新
        if (cachedState == null || cachedState != shouldApplyEffect) {
            updatePlayerMovementSpeed(player, shouldApplyEffect ? 1 : 0);
            PLAYER_INNER_FEAR_CACHE.put(player, shouldApplyEffect);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.CHEST) {
            return;
        }
        
        // 清除缓存
        PLAYER_INNER_FEAR_CACHE.remove(player);
        updatePlayerMovementSpeed(player, 0); // 移除速度修饰符
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        
        // 检查玩家是否装备了带有内心恐惧附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty() || chestplate.getEnchantmentLevel(ModEnchantments.INNER_FEAR.get()) <= 0) {
            return;
        }

        // 60%概率获得厄运效果持续30秒
        if (RANDOM.nextInt(100) < 60) {
            player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 600, 0)); // 10秒 = 200 ticks
        }
        
        // 40%概率获得失明效果持续4秒
        if (RANDOM.nextInt(100) < 40) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0)); // 4秒 = 80 ticks
        }
        
        // 30%概率概率获得虚弱效果持续3秒
        if (RANDOM.nextInt(100) < 30) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0)); // 3秒 = 60 ticks
        }
    }

    private static void updatePlayerMovementSpeed(Player player, int level) {
        // 移除旧的速度修饰符
        if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
        }
        
        // 如果等级大于0，添加新的速度修饰符
        if (level > 0) {
            // 减少40%移速
            if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                    new AttributeModifier(
                        SPEED_MODIFIER_UUID, 
                        "Inner fear movement speed", 
                        -0.4, 
                        AttributeModifier.Operation.MULTIPLY_TOTAL)
                );
            }
        }
    }
}