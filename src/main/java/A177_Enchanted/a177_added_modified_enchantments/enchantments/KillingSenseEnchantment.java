package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class KillingSenseEnchantment extends Enchantment {
    // 基础高亮持续时间（秒）
    private static final int BASE_HIGHLIGHT_DURATION = 3;
    // 每级增加的高亮持续时间（秒）
    private static final int HIGHLIGHT_DURATION_PER_LEVEL = 3;

    public KillingSenseEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("killing_sense");
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
        return this.canEnchant(stack) && isDiscoverable();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查死亡实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof Mob && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有杀戮感知附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = mainHandItem.getEnchantmentLevel(ModEnchantments.KILLING_SENSE.get());
            
            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算高亮持续时间（tick）：基础3秒 + 每级3秒
                int duration = (BASE_HIGHLIGHT_DURATION + (level * HIGHLIGHT_DURATION_PER_LEVEL)) * 20;
                
                // 获取30格范围内的所有怪物
                AABB boundingBox = player.getBoundingBox().inflate(30.0D);
                player.level().getEntitiesOfClass(Mob.class, boundingBox).forEach(entity -> {
                    // 给怪物添加发光效果，只有该玩家能看到
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false, true), player);
                });
            }
        }
    }
}