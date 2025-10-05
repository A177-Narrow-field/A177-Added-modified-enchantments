package A177_Enchanted.a177_added_modified_enchantments.events;

import A177_Enchanted.a177_added_modified_enchantments.entity.OreHighlightEntity;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OreHighlightRenderHandler extends EntityRenderer<OreHighlightEntity> {
    protected OreHighlightRenderHandler(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(OreHighlightEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        
        // 检查玩家是否持有带有矿探附魔的物品
        if (mc.player == null || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ORE_DETECTOR.get(), mc.player.getMainHandItem()) <= 0) {
            return;
        }
        
        BlockPos pos = entity.getOriginPos();
        BlockState state = entity.level().getBlockState(pos);
        
        // 获取方块渲染器
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        
        // 使用透明渲染类型
        RenderType renderType = RenderType.translucent();
        VertexConsumer builder = buffer.getBuffer(renderType);
        
        // 渲染方块
        matrixStack.pushPose();
        matrixStack.translate(-0.5, -0.5, -0.5); // 调整位置使方块居中
        blockRenderer.renderSingleBlock(state, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(OreHighlightEntity p_114482_) {
        // 不使用纹理
        return null;
    }
}