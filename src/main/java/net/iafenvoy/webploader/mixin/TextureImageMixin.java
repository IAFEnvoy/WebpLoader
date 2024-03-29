package net.iafenvoy.webploader.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.burningtnt.webp.SimpleWEBPLoader;
import net.burningtnt.webp.utils.RGBABuffer;
import net.iafenvoy.webploader.WebpLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

//Use class path string here since TextureImage is a protected class.
@Mixin(targets = {"net.minecraft.client.renderer.texture.SimpleTexture$TextureImage"})
public class TextureImageMixin {
    @Redirect(method = "load", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/platform/NativeImage;read(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;"))
    private static NativeImage handleLoad(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        try {
            String resourcePath = resourceLocation.getPath();
            Resource resource = resourceManager.getResource(resourceLocation);
            if (resourcePath.endsWith(".webp")) {
                WebpLoader.LOGGER.info("Loading " + resourceLocation);
                InputStream inputStream = resource.getInputStream();
                RGBABuffer.AbsoluteRGBABuffer rgbaBuffer = SimpleWEBPLoader.decode(inputStream);
                return NativeImage.read(ByteBuffer.wrap(rgbaBuffer.getRGBAData()));
            }
            return NativeImage.read(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
