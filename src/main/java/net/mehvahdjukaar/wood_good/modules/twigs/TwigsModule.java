package net.mehvahdjukaar.wood_good.modules.twigs;

import lilypuree.decorative_blocks.core.DBItems;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.resourcepack.RPUtils;
import net.mehvahdjukaar.selene.resourcepack.ResType;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.LangBuilder;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Palette;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Respriter;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.SpriteUtils;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.TextureImage;
import net.mehvahdjukaar.wood_good.WoodGood;
import net.mehvahdjukaar.wood_good.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.wood_good.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.wood_good.modules.CompatModule;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.moddingplayground.twigs.block.TableBlock;
import vazkii.quark.content.building.module.WoodenPostsModule;

import java.util.*;

public class TwigsModule extends CompatModule {

    public TwigsModule(String modId) {
        super(modId);
    }

    private static final String TABLE_NAME = "table";
    public Map<WoodType, Block> TABLES = new HashMap<>();
    public Map<WoodType, Item> TABLE_TIMES = new HashMap<>();

    @Override
    public void registerWoodBlocks(IForgeRegistry<Block> registry, Collection<WoodType> woodTypes) {

        //tables
        for (WoodType w : woodTypes) {
            String name = w.getVariantId(TABLE_NAME, false);
            if (w.isVanilla() || !shouldRegisterEntry(name, registry)) continue;

            Block block = new TableBlock(BlockBehaviour.Properties.copy(w.planks).instabreak());
            TABLES.put(w, block);
            registry.register(block.setRegistryName(WoodGood.res(name)));
        }
    }

    @Override
    public void registerItems(IForgeRegistry<Item> registry) {
        TABLES.forEach((key, value) -> {
            Item i = new BlockItem(value, DBItems.modItemProperties);
            TABLE_TIMES.put(key, i);
            registry.register(i.setRegistryName(value.getRegistryName()));
        });
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event) {
        TABLES.values().forEach(t->ItemBlockRenderTypes.setRenderLayer(t,RenderType.cutout()));
    }

    @Override
    public void addStaticServerResources(ServerDynamicResourcesHandler handler, ResourceManager manager) {
        var pack = handler.dynamicPack;
        List<ResourceLocation> beams = new ArrayList<>();
        TABLES.forEach((wood, value) -> {
            pack.addSimpleBlockLootTable(value);
            beams.add(value.getRegistryName());
        });
        pack.addTag(modRes("tables"), beams, Registry.BLOCK_REGISTRY);
        pack.addTag(modRes("tables"), beams, Registry.ITEM_REGISTRY);

    }

    @Override
    public void addStaticClientResources(ClientDynamicResourcesHandler handler, ResourceManager manager, LangBuilder langBuilder) {
        //beams
        TABLES.forEach((w, v) -> langBuilder.addEntry(v, w.getNameForTranslation(TABLE_NAME)));

        this.addBlockResources(manager, handler, TABLES, "oak_table",
                ResType.ITEM_MODELS.getPath(modRes("oak_table")),
                ResType.BLOCK_MODELS.getPath(modRes("oak_table")),
                ResType.BLOCKSTATES.getPath(modRes("oak_table"))
        );
    }

    @Override
    public void addDynamicClientResources(ClientDynamicResourcesHandler handler, ResourceManager manager) {
        var pack = handler.dynamicPack;

        //beam
        try (TextureImage main = TextureImage.open(manager,
                modRes("block/oak_table"));
             TextureImage top = TextureImage.open(manager,
                     modRes("block/oak_table_top"));
             TextureImage bottom = TextureImage.open(manager,
                     modRes("block/oak_table_bottom"))) {

            Respriter respriterMain = Respriter.of(main);
            Respriter respriterTop = Respriter.of(top);
            Respriter respriterBottom = Respriter.of(bottom);

            TABLES.forEach((wood, table) -> {

                String id = table.getRegistryName().getPath();

                try (TextureImage plankTexture = TextureImage.open(manager,
                        RPUtils.findFirstBlockTextureLocation(manager, wood.planks))) {

                    List<Palette> targetPalette = SpriteUtils.extrapolateSignBlockPalette(plankTexture);

                    handler.addTextureIfNotPresent(manager, "block/" + id, () ->
                            respriterMain.recolorWithAnimation(targetPalette, plankTexture.getMetadata()));

                    handler.addTextureIfNotPresent(manager, "block/" + id + "_top", () ->
                            respriterTop.recolorWithAnimation(targetPalette, plankTexture.getMetadata()));

                    handler.addTextureIfNotPresent(manager, "block/" + id + "_bottom", () ->
                            respriterBottom.recolorWithAnimation(targetPalette, plankTexture.getMetadata()));

                } catch (Exception ex) {
                    handler.getLogger().error("Failed to generate Beam block texture for for {} : {}", table, ex);
                }
            });
        } catch (Exception ex) {
            handler.getLogger().error("Could not generate any Beam block texture : ", ex);
        }
    }


}
