package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class InfernalArmorEnchantment extends Enchantment {
    // 属性修饰符UUID（确保唯一性）
    private static final UUID TOUGHNESS_UUID = UUID.fromString("a1b2c3d4-5e6f-7890-1234-567890abcdef");
    private static final UUID SPEED_UUID = UUID.fromString("b2c3d4e5-6f78-9012-3456-7890abcdef12");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("c3d4e5f6-7890-1234-5678-90abcdef1234");
    
    // 存储玩家上次触发时间
    private static final Map<Player, Long> lastFireTimeMap = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("infernal_armor");
    }

    public InfernalArmorEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3; // 最高三级
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15; // 附魔成本计算
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get(); // 设为宝藏附魔
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 是否可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 根据配置决定是否可在附魔台获得
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && 
               other != ModEnchantments.BLAZING_WAR.get() &&
               other != ModEnchantments.INFERNAL_REBIRTH.get() &&
               other != ModEnchantments.FIRE_DEVOUR.get() &&
               other != ModEnchantments.BURNING_HEART.get() &&
               other != ModEnchantments.FLAME_SHELL.get() &&
               other != ModEnchantments.BURNING_FURY.get() &&
               other != ModEnchantments.DESIRE_FLAME.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFERNAL_ARMOR.get(), chestplate);
        
        if (level > 0) {
            // 添加属性修饰符
            addAttributeModifiers(player, level);
            
            // 如果玩家着火，立即熄灭
            if (player.getRemainingFireTicks() > 0) {
                player.clearFire();
            }
            
            // 周期性触发火焰冲击
            Level world = player.getCommandSenderWorld();
            long currentTime = world.getGameTime();
            long lastTime = lastFireTimeMap.getOrDefault(player, 0L);
            int interval = switch (level) {
                case 1 -> 60; // 3秒
                case 2 -> 40; // 2秒
                default -> 20; // 1秒
            };
            
            if (currentTime - lastTime >= interval) {
                triggerFireWave(player, level);
                lastFireTimeMap.put(player, currentTime);
            }
        } else {
            removeAttributeModifiers(player);
        }
    }

    private static void addAttributeModifiers(Player player, int level) {
        // 韧性 +10（固定值）
        if (player.getAttribute(Attributes.ARMOR_TOUGHNESS).getModifier(TOUGHNESS_UUID) == null) {
            player.getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(
                new AttributeModifier(TOUGHNESS_UUID, "Infernal Toughness", 10, AttributeModifier.Operation.ADDITION)
            );
        }
        
        // 移动速度 +20%
        if (player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(SPEED_UUID) == null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                new AttributeModifier(SPEED_UUID, "Infernal Speed", 0.20, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
        
        // 攻击/挖掘速度 +30%
        if (player.getAttribute(Attributes.ATTACK_SPEED).getModifier(ATTACK_SPEED_UUID) == null) {
            player.getAttribute(Attributes.ATTACK_SPEED).addPermanentModifier(
                new AttributeModifier(ATTACK_SPEED_UUID, "Infernal Attack Speed", 0.30, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
    }

    private static void removeAttributeModifiers(Player player) {
        if (player.getAttribute(Attributes.ARMOR_TOUGHNESS).getModifier(TOUGHNESS_UUID) != null) {
            player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_UUID);
        }
        if (player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(SPEED_UUID) != null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_UUID);
        }
        if (player.getAttribute(Attributes.ATTACK_SPEED).getModifier(ATTACK_SPEED_UUID) != null) {
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_UUID);
        }
    }

    private static void triggerFireWave(Player player, int level) {
        int burnTime = level == 1 ? 4 : 3; // 一级燃烧4秒，其他3秒
        double radius = 3.0;
        Level world = player.getCommandSenderWorld();
        
        List<Entity> entities = world.getEntities(player, player.getBoundingBox().inflate(radius));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && !living.equals(player)) {
                // 只对敌对生物生效，跳过友好生物（被动生物）
                // 使用更准确的敌对生物判断方式，确保包括Warden在内的敌对生物都会受到影响
                if (living.getType().getCategory().isFriendly() || living instanceof Player) {
                    continue; // 跳过友好生物和玩家
                }
                
                // 击退效果
                living.knockback(0.6, player.getX() - living.getX(), player.getZ() - living.getZ());

                living.hurt(player.damageSources().playerAttack(player), 2.0F);// 造成2点伤害

                living.setSecondsOnFire(burnTime);// 点燃敌人
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFERNAL_ARMOR.get(), chestplate);
        if (level <= 0) return;
        
        // 检查伤害是否超过生命值30%
        float maxHealth = player.getMaxHealth();
        if (event.getAmount() > maxHealth * 0.3f) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof LivingEntity livingAttacker) {
                // 伤害返还倍数 (1/2/3倍)
                float returnDamage = event.getAmount() * level;
                livingAttacker.hurt(player.damageSources().playerAttack(player), returnDamage);
                
                // 点燃攻击者
                livingAttacker.setSecondsOnFire(level == 1 ? 3 : 4); // 一级3秒，其他4秒
            }
        }
    }
}