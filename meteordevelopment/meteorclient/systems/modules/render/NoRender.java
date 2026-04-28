/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractBannerBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockEntityProvider
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.particle.ParticleType
 *  net.minecraft.particle.ParticleTypes
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.ParticleEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public class NoRender
extends Module {
    private final SettingGroup sgOverlay;
    private final SettingGroup sgHUD;
    private final SettingGroup sgWorld;
    private final SettingGroup sgEntity;
    private final Setting<Boolean> noPortalOverlay;
    private final Setting<Boolean> noSpyglassOverlay;
    private final Setting<Boolean> noNausea;
    private final Setting<Boolean> noPumpkinOverlay;
    private final Setting<Boolean> noPowderedSnowOverlay;
    private final Setting<Boolean> noFireOverlay;
    private final Setting<Boolean> noLiquidOverlay;
    private final Setting<Boolean> noInWallOverlay;
    private final Setting<Boolean> noVignette;
    private final Setting<Boolean> noGuiBackground;
    private final Setting<Boolean> noTotemAnimation;
    private final Setting<Boolean> noEatParticles;
    private final Setting<Boolean> noEnchantGlint;
    private final Setting<Boolean> noBossBar;
    private final Setting<Boolean> noScoreboard;
    private final Setting<Boolean> noCrosshair;
    private final Setting<Boolean> noTitle;
    private final Setting<Boolean> noHeldItemName;
    private final Setting<Boolean> noObfuscation;
    private final Setting<Boolean> noPotionIcons;
    private final Setting<Boolean> noMessageSignatureIndicator;
    private final Setting<Boolean> noWeather;
    private final Setting<Boolean> noBlindness;
    private final Setting<Boolean> noDarkness;
    private final Setting<Boolean> noFog;
    private final Setting<Boolean> noEnchTableBook;
    private final Setting<Boolean> noSignText;
    private final Setting<Boolean> noBlockBreakParticles;
    private final Setting<Boolean> noBlockBreakOverlay;
    private final Setting<Boolean> noSkylightUpdates;
    private final Setting<Boolean> noBeaconBeams;
    private final Setting<Boolean> noFallingBlocks;
    private final Setting<Boolean> noCaveCulling;
    private final Setting<Boolean> noMapMarkers;
    private final Setting<Boolean> noMapContents;
    private final Setting<BannerRenderMode> bannerRender;
    private final Setting<Boolean> noFireworkExplosions;
    private final Setting<List<ParticleType<?>>> particles;
    private final Setting<Boolean> noBarrierInvis;
    private final Setting<Boolean> noTextureRotations;
    private final Setting<List<Block>> blockEntities;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> dropSpawnPacket;
    private final Setting<Boolean> noArmor;
    private final Setting<Boolean> noInvisibility;
    private final Setting<Boolean> noGlowing;
    private final Setting<Boolean> noMobInSpawner;
    private final Setting<Boolean> noDeadEntities;
    private final Setting<Boolean> noNametags;

    public NoRender() {
        super(Categories.Render, "no-render", "Disables certain animations or overlays from rendering.");
        this.sgOverlay = this.settings.createGroup("Overlay");
        this.sgHUD = this.settings.createGroup("HUD");
        this.sgWorld = this.settings.createGroup("World");
        this.sgEntity = this.settings.createGroup("Entity");
        this.noPortalOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("portal-overlay")).description("Disables rendering of the nether portal overlay.")).defaultValue(false)).build());
        this.noSpyglassOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spyglass-overlay")).description("Disables rendering of the spyglass overlay.")).defaultValue(false)).build());
        this.noNausea = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("nausea")).description("Disables rendering of the nausea overlay.")).defaultValue(false)).build());
        this.noPumpkinOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pumpkin-overlay")).description("Disables rendering of the pumpkin head overlay")).defaultValue(false)).build());
        this.noPowderedSnowOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("powdered-snow-overlay")).description("Disables rendering of the powdered snow overlay.")).defaultValue(false)).build());
        this.noFireOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fire-overlay")).description("Disables rendering of the fire overlay.")).defaultValue(false)).build());
        this.noLiquidOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("liquid-overlay")).description("Disables rendering of the liquid overlay.")).defaultValue(false)).build());
        this.noInWallOverlay = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("in-wall-overlay")).description("Disables rendering of the overlay when inside blocks.")).defaultValue(false)).build());
        this.noVignette = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vignette")).description("Disables rendering of the vignette overlay.")).defaultValue(false)).build());
        this.noGuiBackground = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gui-background")).description("Disables rendering of the GUI background overlay.")).defaultValue(false)).build());
        this.noTotemAnimation = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("totem-animation")).description("Disables rendering of the totem animation when you pop a totem.")).defaultValue(false)).build());
        this.noEatParticles = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("eating-particles")).description("Disables rendering of eating particles.")).defaultValue(false)).build());
        this.noEnchantGlint = this.sgOverlay.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enchantment-glint")).description("Disables rending of the enchantment glint.")).defaultValue(false)).build());
        this.noBossBar = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("boss-bar")).description("Disable rendering of boss bars.")).defaultValue(false)).build());
        this.noScoreboard = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("scoreboard")).description("Disable rendering of the scoreboard.")).defaultValue(false)).build());
        this.noCrosshair = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("crosshair")).description("Disables rendering of the crosshair.")).defaultValue(false)).build());
        this.noTitle = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("title")).description("Disables rendering of the title.")).defaultValue(false)).build());
        this.noHeldItemName = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("held-item-name")).description("Disables rendering of the held item name.")).defaultValue(false)).build());
        this.noObfuscation = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("obfuscation")).description("Disables obfuscation styling of characters.")).defaultValue(false)).build());
        this.noPotionIcons = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potion-icons")).description("Disables rendering of status effect icons.")).defaultValue(false)).build());
        this.noMessageSignatureIndicator = this.sgHUD.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("message-signature-indicator")).description("Disables chat message signature indicator on the left of the message.")).defaultValue(false)).build());
        this.noWeather = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("weather")).description("Disables rendering of weather.")).defaultValue(false)).build());
        this.noBlindness = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blindness")).description("Disables rendering of blindness.")).defaultValue(false)).build());
        this.noDarkness = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("darkness")).description("Disables rendering of darkness.")).defaultValue(false)).build());
        this.noFog = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fog")).description("Disables rendering of fog.")).defaultValue(false)).build());
        this.noEnchTableBook = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enchantment-table-book")).description("Disables rendering of books above enchanting tables.")).defaultValue(false)).build());
        this.noSignText = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sign-text")).description("Disables rendering of text on signs.")).defaultValue(false)).build());
        this.noBlockBreakParticles = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-break-particles")).description("Disables rendering of block-break particles.")).defaultValue(false)).build());
        this.noBlockBreakOverlay = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-break-overlay")).description("Disables rendering of block-break overlay.")).defaultValue(false)).build());
        this.noSkylightUpdates = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("skylight-updates")).description("Disables rendering of skylight updates.")).defaultValue(false)).build());
        this.noBeaconBeams = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("beacon-beams")).description("Disables rendering of beacon beams.")).defaultValue(false)).build());
        this.noFallingBlocks = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("falling-blocks")).description("Disables rendering of falling blocks.")).defaultValue(false)).build());
        this.noCaveCulling = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cave-culling")).description("Disables Minecraft's cave culling algorithm.")).defaultValue(false)).onChanged(b -> this.mc.worldRenderer.reload())).build());
        this.noMapMarkers = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("map-markers")).description("Disables markers on maps.")).defaultValue(false)).build());
        this.noMapContents = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("map-contents")).description("Disable rendering of maps.")).defaultValue(false)).build());
        this.bannerRender = this.sgWorld.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("banners")).description("Changes rendering of banners.")).defaultValue(BannerRenderMode.Everything)).build());
        this.noFireworkExplosions = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("firework-explosions")).description("Disables rendering of firework explosions.")).defaultValue(false)).build());
        this.particles = this.sgWorld.add(((ParticleTypeListSetting.Builder)((ParticleTypeListSetting.Builder)new ParticleTypeListSetting.Builder().name("particles")).description("Particles to not render.")).build());
        this.noBarrierInvis = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("barrier-invisibility")).description("Disables barriers being invisible when not holding one.")).defaultValue(false)).build());
        this.noTextureRotations = this.sgWorld.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("texture-rotations")).description("Changes texture rotations and model offsets to use a constant value instead of the block position.")).defaultValue(false)).onChanged(b -> this.mc.worldRenderer.reload())).build());
        this.blockEntities = this.sgWorld.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("block-entities")).description("Block entities (chest, shulker block, etc.) to not render.")).filter(block -> block instanceof BlockEntityProvider && !(block instanceof AbstractBannerBlock)).build());
        this.entities = this.sgEntity.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Disables rendering of selected entities.")).build());
        this.dropSpawnPacket = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("drop-spawn-packets")).description("WARNING! Drops all spawn packets of entities selected in the above list.")).defaultValue(false)).build());
        this.noArmor = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("armor")).description("Disables rendering of armor on entities.")).defaultValue(false)).build());
        this.noInvisibility = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("invisibility")).description("Shows invisible entities.")).defaultValue(false)).build());
        this.noGlowing = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("glowing")).description("Disables rendering of the glowing effect")).defaultValue(false)).build());
        this.noMobInSpawner = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spawner-entities")).description("Disables rendering of spinning mobs inside of mob spawners")).defaultValue(false)).build());
        this.noDeadEntities = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dead-entities")).description("Disables rendering of dead entities")).defaultValue(false)).build());
        this.noNametags = this.sgEntity.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("nametags")).description("Disables rendering of entity nametags")).defaultValue(false)).build());
    }

    @Override
    public void onActivate() {
        if (this.noCaveCulling.get().booleanValue() || this.noTextureRotations.get().booleanValue()) {
            this.mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.noCaveCulling.get().booleanValue() || this.noTextureRotations.get().booleanValue()) {
            this.mc.worldRenderer.reload();
        }
    }

    public boolean noPortalOverlay() {
        return this.isActive() && this.noPortalOverlay.get() != false;
    }

    public boolean noSpyglassOverlay() {
        return this.isActive() && this.noSpyglassOverlay.get() != false;
    }

    public boolean noNausea() {
        return this.isActive() && this.noNausea.get() != false;
    }

    public boolean noPumpkinOverlay() {
        return this.isActive() && this.noPumpkinOverlay.get() != false;
    }

    public boolean noFireOverlay() {
        return this.isActive() && this.noFireOverlay.get() != false;
    }

    public boolean noLiquidOverlay() {
        return this.isActive() && this.noLiquidOverlay.get() != false;
    }

    public boolean noPowderedSnowOverlay() {
        return this.isActive() && this.noPowderedSnowOverlay.get() != false;
    }

    public boolean noInWallOverlay() {
        return this.isActive() && this.noInWallOverlay.get() != false;
    }

    public boolean noVignette() {
        return this.isActive() && this.noVignette.get() != false;
    }

    public boolean noGuiBackground() {
        return this.isActive() && this.noGuiBackground.get() != false;
    }

    public boolean noTotemAnimation() {
        return this.isActive() && this.noTotemAnimation.get() != false;
    }

    public boolean noEatParticles() {
        return this.isActive() && this.noEatParticles.get() != false;
    }

    public boolean noEnchantGlint() {
        return this.isActive() && this.noEnchantGlint.get() != false;
    }

    public boolean noBossBar() {
        return this.isActive() && this.noBossBar.get() != false;
    }

    public boolean noScoreboard() {
        return this.isActive() && this.noScoreboard.get() != false;
    }

    public boolean noCrosshair() {
        return this.isActive() && this.noCrosshair.get() != false;
    }

    public boolean noTitle() {
        return this.isActive() && this.noTitle.get() != false;
    }

    public boolean noHeldItemName() {
        return this.isActive() && this.noHeldItemName.get() != false;
    }

    public boolean noObfuscation() {
        return this.isActive() && this.noObfuscation.get() != false;
    }

    public boolean noPotionIcons() {
        return this.isActive() && this.noPotionIcons.get() != false;
    }

    public boolean noMessageSignatureIndicator() {
        return this.isActive() && this.noMessageSignatureIndicator.get() != false;
    }

    public boolean noWeather() {
        return this.isActive() && this.noWeather.get() != false;
    }

    public boolean noBlindness() {
        return this.isActive() && this.noBlindness.get() != false;
    }

    public boolean noDarkness() {
        return this.isActive() && this.noDarkness.get() != false;
    }

    public boolean noFog() {
        return this.isActive() && this.noFog.get() != false;
    }

    public boolean noEnchTableBook() {
        return this.isActive() && this.noEnchTableBook.get() != false;
    }

    public boolean noSignText() {
        return this.isActive() && this.noSignText.get() != false;
    }

    public boolean noBlockBreakParticles() {
        return this.isActive() && this.noBlockBreakParticles.get() != false;
    }

    public boolean noBlockBreakOverlay() {
        return this.isActive() && this.noBlockBreakOverlay.get() != false;
    }

    public boolean noSkylightUpdates() {
        return this.isActive() && this.noSkylightUpdates.get() != false;
    }

    public boolean noBeaconBeams() {
        return this.isActive() && this.noBeaconBeams.get() != false;
    }

    public boolean noFallingBlocks() {
        return this.isActive() && this.noFallingBlocks.get() != false;
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        if (this.noCaveCulling.get().booleanValue()) {
            event.cancel();
        }
    }

    public boolean noMapMarkers() {
        return this.isActive() && this.noMapMarkers.get() != false;
    }

    public boolean noMapContents() {
        return this.isActive() && this.noMapContents.get() != false;
    }

    public BannerRenderMode getBannerRenderMode() {
        if (!this.isActive()) {
            return BannerRenderMode.Everything;
        }
        return this.bannerRender.get();
    }

    public boolean noFireworkExplosions() {
        return this.isActive() && this.noFireworkExplosions.get() != false;
    }

    @EventHandler
    private void onAddParticle(ParticleEvent event) {
        if (this.noWeather.get().booleanValue() && event.particle.getType() == ParticleTypes.RAIN) {
            event.cancel();
        } else if (this.noFireworkExplosions.get().booleanValue() && event.particle.getType() == ParticleTypes.FIREWORK) {
            event.cancel();
        } else if (this.particles.get().contains(event.particle.getType())) {
            event.cancel();
        }
    }

    public boolean noBarrierInvis() {
        return this.isActive() && this.noBarrierInvis.get() != false;
    }

    public boolean noTextureRotations() {
        return this.isActive() && this.noTextureRotations.get() != false;
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (this.blockEntities.get().contains(event.blockEntity.getCachedState().getBlock())) {
            event.cancel();
        }
    }

    public boolean noEntity(Entity entity) {
        return this.isActive() && this.entities.get().contains(entity.getType());
    }

    public boolean noEntity(EntityType<?> entity) {
        return this.isActive() && this.entities.get().contains(entity);
    }

    public boolean getDropSpawnPacket() {
        return this.isActive() && this.dropSpawnPacket.get() != false;
    }

    public boolean noArmor() {
        return this.isActive() && this.noArmor.get() != false;
    }

    public boolean noInvisibility() {
        return this.isActive() && this.noInvisibility.get() != false;
    }

    public boolean noGlowing() {
        return this.isActive() && this.noGlowing.get() != false;
    }

    public boolean noMobInSpawner() {
        return this.isActive() && this.noMobInSpawner.get() != false;
    }

    public boolean noDeadEntities() {
        return this.isActive() && this.noDeadEntities.get() != false;
    }

    public boolean noNametags() {
        return this.isActive() && this.noNametags.get() != false;
    }

    public static enum BannerRenderMode {
        Everything,
        Pillar,
        None;

    }
}

