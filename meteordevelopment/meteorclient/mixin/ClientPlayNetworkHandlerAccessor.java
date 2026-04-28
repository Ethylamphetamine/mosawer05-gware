/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.network.message.LastSeenMessagesCollector
 *  net.minecraft.network.message.MessageChain$Packer
 *  net.minecraft.registry.DynamicRegistryManager$Immutable
 *  net.minecraft.resource.featuretoggle.FeatureSet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageChain;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientPlayNetworkHandler.class})
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor(value="chunkLoadDistance")
    public int getChunkLoadDistance();

    @Accessor(value="messagePacker")
    public MessageChain.Packer getMessagePacker();

    @Accessor(value="lastSeenMessagesCollector")
    public LastSeenMessagesCollector getLastSeenMessagesCollector();

    @Accessor(value="combinedDynamicRegistries")
    public DynamicRegistryManager.Immutable getCombinedDynamicRegistries();

    @Accessor(value="enabledFeatures")
    public FeatureSet getEnabledFeatures();
}

