package gg.codie.mineonline.patches.minecraft;

import gg.codie.mineonline.patches.ClassicPointerInfoGetLocationAdvice;
import gg.codie.mineonline.patches.ComponentGetLocationOnScreenAdvice;
import gg.codie.mineonline.patches.RobotMouseMoveAdvice;
import gg.codie.mineonline.patches.lwjgl.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

public class ClassicMousePatch {
    public static void fixNativeCursorClassic() {
        try {
            new ByteBuddy()
                    .redefine(ClassicMousePatch.class.getClassLoader().loadClass("org.lwjgl.input.Mouse"))
                    // Determine whether the cursor is focused.
                    .visit(Advice.to(LWJGLMouseSetNativeCursorAdvice.class).on(ElementMatchers.named("setNativeCursor")))
                    // Possibly unneeded
                    .visit(Advice.to(LWJGLMouseSetCursorPositionAdvice.class).on(ElementMatchers.named("setCursorPosition")))
                    // Get DX instead.
                    .visit(Advice.to(LWJGLMouseGetXAdvice.class).on(ElementMatchers.named("getX")))
                    // Get DY instead.
                    .visit(Advice.to(LWJGLMouseGetYAdvice.class).on(ElementMatchers.named("getY")))
                    .make()
                    .load(Class.forName("org.lwjgl.input.Mouse").getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            new ByteBuddy()
                    .redefine(IndevMousePatch.class.getClassLoader().loadClass("org.lwjgl.input.Cursor"))
                    // Allow a cursor to be created whether compatible or not, it's never used but it's existence is queried.
                    .visit(Advice.to(LWJGLCursorGetCapabilitiesAdvice.class).on(ElementMatchers.named("getCapabilities")))
                    .make()
                    .load(Class.forName("org.lwjgl.input.Cursor").getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            new ByteBuddy()
                    .redefine(ClassicMousePatch.class.getClassLoader().loadClass("java.awt.Robot"))
                    // Used to grab Minecraft's robot instance, which is used to center the mouse.
                    .visit(Advice.to(RobotMouseMoveAdvice.class).on(ElementMatchers.named("mouseMove")))
                    .make()
                    .load(Class.forName("java.awt.Robot").getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            new ByteBuddy()
                    .redefine(ClassicMousePatch.class.getClassLoader().loadClass("java.awt.PointerInfo"))
                    // Used in getting the mouse movements, might be able to refactor this out now.
                    .visit(Advice.to(ClassicPointerInfoGetLocationAdvice.class).on(ElementMatchers.named("getLocation")))
                    .make()
                    .load(Class.forName("java.awt.PointerInfo").getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

            new ByteBuddy()
                    .redefine(ClassicMousePatch.class.getClassLoader().loadClass("java.awt.Component"))
                    // Used in getting the mouse movements, might be able to refactor this out now.
                    .visit(Advice.to(ComponentGetLocationOnScreenAdvice.class).on(ElementMatchers.named("getLocationOnScreen")))
                    .make()
                    .load(Class.forName("java.awt.Component").getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
