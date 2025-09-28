package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.resources.ResourceKey;
import java.util.Set;

@Mod.EventBusSubscriber
public class OceanBlessingEnchantment extends Enchantment {
    // 水中伤害减免百分比 (40%)
    private static final double DAMAGE_REDUCTION = 0.4;
    
    // 生命值回复间隔 (20 ticks = 1秒)
    private static final int HEAL_INTERVAL = 20;
    
    // 回复的生命值
    private static final float HEAL_AMOUNT = 1.0f;
    
    // 海洋生物群系集合
    private static final Set<ResourceKey<Biome>> OCEAN_BIOME_KEYS = Set.of(
        Biomes.OCEAN,
        Biomes.DEEP_OCEAN,
        Biomes.WARM_OCEAN,
        Biomes.LUKEWARM_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.FROZEN_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN
    );
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("ocean_blessing");
    }

    public OceanBlessingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
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
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
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
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench);
    }
    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为玩家
        if (event.getEntity() instanceof Player player) {
            // 检查玩家是否在水中
            if (player.isInWater()) {
                // 获取玩家胸甲上的海洋庇佑附魔等级
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OCEAN_BLESSING.get(), chestplate);
                
                // 只在玩家拥有附魔时处理效果
                if (enchantmentLevel > 0) {
                    // 检查玩家是否有有效的胸甲
                    if (chestplate.isEmpty() || !(chestplate.getItem() instanceof ArmorItem) || chestplate.getDamageValue() >= chestplate.getMaxDamage()) {
                        return; // 如果没有胸甲或胸甲已损坏，则不减免伤害
                    }
                    
                    // 减少40%的伤害
                    float reducedDamage = event.getAmount() * (float) DAMAGE_REDUCTION;
                    event.setAmount(event.getAmount() - reducedDamage);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            Level level = player.level();
            
            // 每隔一定时间执行一次生命值回复
            if (player.tickCount % HEAL_INTERVAL == 0) {
                // 获取玩家胸甲上的海洋庇佑附魔等级
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OCEAN_BLESSING.get(), chestplate);
                
                // 只在玩家拥有附魔时处理效果
                if (enchantmentLevel > 0) {
                    // 检查玩家是否有有效的胸甲
                    if (chestplate.isEmpty() || !(chestplate.getItem() instanceof ArmorItem) || chestplate.getDamageValue() >= chestplate.getMaxDamage()) {
                        return; // 如果没有胸甲或胸甲已损坏，则不回复生命值
                    }
                    
                    // 检查玩家是否在海洋群系
                    BlockPos playerPos = player.blockPosition();
                    ResourceKey<Biome> biomeKey = level.getBiome(playerPos).unwrapKey().orElse(null);
                    
                    if (biomeKey != null && isOceanBiome(biomeKey)) {
                        // 回复1点生命值
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(HEAL_AMOUNT);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 检查生物群系是否为海洋群系
     * @param biomeKey 生物群系资源键
     * @return 是否为海洋群系
     */
    private static boolean isOceanBiome(ResourceKey<Biome> biomeKey) {
        return OCEAN_BIOME_KEYS.contains(biomeKey);
    }
}