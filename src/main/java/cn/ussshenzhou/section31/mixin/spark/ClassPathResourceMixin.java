package cn.ussshenzhou.section31.mixin.spark;

import cn.ussshenzhou.section31.Section31;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spark.resource.ClassPathResource;

/**
 * @author USS_Shenzhou
 */
@Mixin(ClassPathResource.class)
public class ClassPathResourceMixin {

    @Shadow
    private ClassLoader classLoader;

    @Redirect(method = "<init>(Ljava/lang/String;Ljava/lang/ClassLoader;)V", at = @At(value = "FIELD", target = "Lspark/resource/ClassPathResource;classLoader:Ljava/lang/ClassLoader;", opcode = Opcodes.PUTFIELD))
    private void section31UseSectionClassloaderResource(ClassPathResource instance, ClassLoader value) {
        classLoader = Section31.class.getClassLoader();
    }
}
