package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.Random;

@Mod.EventBusSubscriber
public class PlunderWealthEnchantment extends Enchantment {
    private static final Random RANDOM = new Random();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("plunder_wealth");
    }

    public PlunderWealthEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem || EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // 检查死亡实体是否为村民、掠夺者或流浪商人
        if ((event.getEntity() instanceof Villager || event.getEntity() instanceof Raider || event.getEntity() instanceof WanderingTrader) 
                && event.getSource().getEntity() instanceof Player player) {
            
            // 检查玩家主手装备是否有掠财附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.PLUNDER_WEALTH.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算掉落的绿宝石数量：1到4个基础数量，每级翻倍
                int baseEmeralds = RANDOM.nextInt(4) + 1; // 1-4个
                int emeraldCount = baseEmeralds * (int) Math.pow(2, level - 1); // 每级翻倍
                
                // 对于流浪商人，额外增加1-3个绿宝石
                if (event.getEntity() instanceof WanderingTrader) {
                    emeraldCount += RANDOM.nextInt(3) + 1;
                }
                
                // 创建绿宝石物品并添加到掉落列表
                ItemStack emeraldStack = new ItemStack(Items.EMERALD, emeraldCount);
                ItemEntity emeraldEntity = new ItemEntity(
                    event.getEntity().level(),
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    emeraldStack
                );
                
                event.getDrops().add(emeraldEntity);
            }
        }
    }
}