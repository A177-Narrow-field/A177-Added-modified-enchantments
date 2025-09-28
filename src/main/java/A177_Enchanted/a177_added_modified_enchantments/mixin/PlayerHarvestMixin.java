package A177_Enchanted.a177_added_modified_enchantments.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

@Mixin(Player.class)
public class PlayerHarvestMixin {


    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void onHasCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.isEmpty()) {
            // 检查玩家是否装备了带有附魔的胸甲
            ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            if (!chestplate.isEmpty()) {
                int crushingFistLevel = chestplate.getEnchantmentLevel(ModEnchantments.CRUSHING_FIST_CHESTPLATE.get());
                int diamondObsidianFistLevel = chestplate.getEnchantmentLevel(ModEnchantments.DIAMOND_OBSIDIAN_FIST_CHESTPLATE.get());

                // 根据附魔类型确定挖掘等级
                Tier handTier = null;
                if (diamondObsidianFistLevel > 0) {
                    handTier = Tiers.NETHERITE; // 3级挖掘等级
                } else if (crushingFistLevel > 0) {
                    handTier = Tiers.STONE; // 1级挖掘等级
                }

                // 空手时检查方块是否可以被相应等级挖掘
                if (handTier != null && isCorrectTierForDrops(state, handTier)) {
                    cir.setReturnValue(true);
                }
            }
        } else {
            // 检查玩家是否手持带有功能军锹或万能军锹附魔的铲子
            int militaryShovelLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.MILITARY_SHOVEL.get());
            int universalShovelLevel = mainHandItem.getEnchantmentLevel(ModEnchantments.UNIVERSAL_SHOVEL.get());
            
            // 根据附魔类型确定挖掘等级
            Tier handTier = null;
            if (universalShovelLevel > 0) {
                handTier = Tiers.NETHERITE; // 3级挖掘等级，与钻石黑曜石拳相同
            } else if (militaryShovelLevel > 0) {
                handTier = Tiers.STONE; // 1级挖掘等级，与粉碎拳相同
            }
            
            // 检查方块是否可以被相应等级挖掘
            if (handTier != null && isCorrectTierForDrops(state, handTier)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void onGetDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        // 移除了挖掘速度加成功能，只保留基础的空手挖掘功能
    }

    // 检查空手的挖掘等级是否足够挖掘这个方块
    private boolean isCorrectTierForDrops(BlockState state, Tier handTier) {
        if (state.requiresCorrectToolForDrops()) {
            // 检查方块的挖掘等级要求
            int requiredLevel = getRequiredHarvestLevel(state);
            return requiredLevel <= handTier.getLevel();
        }
        return true; // 不需要特定工具的方块都可以挖掘
    }

    // 获取方块需要的挖掘等级（简化版）
    private int getRequiredHarvestLevel(BlockState state) {
        // 特殊处理黑曜石，它需要钻石级别的工具
        if (state.is(Blocks.OBSIDIAN)) {
            return 3; // 钻石是第3级
        }

        // 特殊处理钻石矿石，它需要铁级别的工具
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return 2; // 需要铁级工具
        }

        // 其他方块基于挖掘时间判断
        // 石头、铁矿石等需要1级，钻石矿石等需要2级
        return state.getBlock().defaultDestroyTime() > 3.0f ? 2 : 1;
    }
}