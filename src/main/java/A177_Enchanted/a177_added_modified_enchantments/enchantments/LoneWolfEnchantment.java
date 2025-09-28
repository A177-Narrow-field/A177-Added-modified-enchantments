package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class LoneWolfEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("lone_wolf");
    }

    public LoneWolfEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }//可以在附魔台

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != ModEnchantments.LEADER.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        int level = weapon.getEnchantmentLevel(ModEnchantments.LONE_WOLF.get());
        if (level <= 0) return;

        // 检查附近8格内的玩家
        Level world = player.getCommandSenderWorld();
        List<Player> nearbyPlayers = world.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(8.0D))
                .stream()
                .filter(p -> p != player && p.isAlive() && !p.isInvisible())
                .collect(Collectors.toList());

        boolean hasSameEnchantedPlayer = false;

        for (Player nearbyPlayer : nearbyPlayers) {
            ItemStack nearbyWeapon = nearbyPlayer.getMainHandItem();
            int nearbyLevel = nearbyWeapon.getEnchantmentLevel(ModEnchantments.LONE_WOLF.get());
            if (nearbyLevel > 0) {
                hasSameEnchantedPlayer = true;
                break;
            }
        }

        // 如果附近有相同附魔的玩家，不造成伤害
        if (hasSameEnchantedPlayer) {
            event.setAmount(0.0F);
            return;
        }

        // 如果附近没有玩家，增加伤害
        if (nearbyPlayers.isEmpty()) {
            float additionalDamage = event.getAmount() * level * 0.5f;
            event.setAmount(event.getAmount() + additionalDamage);
        }
    }
}