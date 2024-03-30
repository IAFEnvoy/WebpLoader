package net.iafenvoy.webploader.mixin;

import net.burningtnt.webp.SimpleWEBPLoader;
import net.burningtnt.webp.utils.RGBABuffer;
import net.iafenvoy.webploader.WebpLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//Use class path string here since TextureImage is a protected class.
@Mixin(targets = {"net.minecraft.client.renderer.texture.SimpleTexture$TextureImage"})
public class TextureImageMixin {
    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;read(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;"), index = 0)
    private static InputStream handleLoad(InputStream inputStream) {
        ByteArrayOutputStream clonedBytes = webpLoader$cloneInputStream(inputStream);
        if (clonedBytes == null) return inputStream;
        InputStream stream1 = new ByteArrayInputStream(clonedBytes.toByteArray());
        InputStream stream2 = new ByteArrayInputStream(clonedBytes.toByteArray());
        try {
            RGBABuffer.AbsoluteRGBABuffer webpBuffer = SimpleWEBPLoader.decode(stream1);
            return new ByteArrayInputStream(webpBuffer.getRGBAData());
        } catch (IOException e) {
            WebpLoader.LOGGER.debug(e.getMessage());
            return stream2;
        }
    }

    @Unique
    private static ByteArrayOutputStream webpLoader$cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
