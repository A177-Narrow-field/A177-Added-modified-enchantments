package A177_Enchanted.a177_added_modified_enchantments.client.event;

import A177_Enchanted.a177_added_modified_enchantments.client.render.OreHighlightRenderHandler;
import A177_Enchanted.a177_added_modified_enchantments.entity.OreHighlightEntity;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "a177_added_modified_enchantments", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ORE_HIGHLIGHT.get(), OreHighlightRenderHandler::new);
    }
}