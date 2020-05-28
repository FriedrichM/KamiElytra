package net.kamielytra.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeyReflectionHelper {
    private static Method unpressKeyMethod;
    private static Field pressedField;
    private static Field pressTimeField;

    public static void gatherFields() {
        unpressKeyMethod = ReflectionHelper.findMethod(KeyBinding.class, "unpressKey", "func_74505_d");
        unpressKeyMethod.setAccessible(true);

        pressedField = ReflectionHelper.findField(KeyBinding.class, "pressed", "field_74513_e", "h");
        pressedField.setAccessible(true);
        pressTimeField = ReflectionHelper.findField(KeyBinding.class, "pressTime", "field_151474_i", "i");
        pressTimeField.setAccessible(true);
    }

    public static void unpressKey(KeyBinding keyBinding) {
        try {
            unpressKeyMethod.invoke(keyBinding);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throwReflectionError("unpressKey", KeyBinding.class);
        }
    }

    public static void pressKey(KeyBinding binding) {
        try {
            pressedField.set(binding, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throwReflectionError("pressed", KeyBinding.class);
        }
    }

    public static void increasePressTime(KeyBinding binding) {
        try {
            pressTimeField.set(binding, pressTimeField.getInt(binding) + 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throwReflectionError("pressTime", KeyBinding.class);
        }
    }

    private static void throwReflectionError(String field, Class<?> clazz) {
        String error = String.format("Ran into an issue regarding reflection with field %s from %s. REPORT THIS TO THE MOD AUTHOR!", field, clazz.getName());
        throw new RuntimeException(error);
    }
}