package com.abo47.questsandstuff.client.tablet.controls;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StyledTextFieldsTest {
    @Test
    void resourceLocationFieldUsesLdlibValidator() throws Exception {
        TextFieldWidget field = blankField("minecraft:stone");
        StyledTextFields.applyResourceLocationValidator(field);

        Function<String, String> validator = validator(field);
        assertEquals("minecraft:oak_log", validator.apply("Minecraft:Oak Log"));
        assertEquals("minecraft:stone", validator.apply("bad?id"));
    }

    @Test
    void compoundTagFieldUsesLdlibValidator() throws Exception {
        TextFieldWidget field = blankField("{}");
        StyledTextFields.applyCompoundTagValidator(field);

        Function<String, String> validator = validator(field);
        assertEquals("{foo:1b}", validator.apply("{foo:1b}"));
        assertEquals("{}", validator.apply("{foo"));
    }

    @Test
    void integerFieldUsesLdlibNumberValidator() throws Exception {
        TextFieldWidget field = blankField("5");
        StyledTextFields.applyIntegerValidator(field, 1, 10);

        Function<String, String> validator = validator(field);
        assertEquals("7", validator.apply("7"));
        assertEquals("10", validator.apply("15"));
        assertEquals("1", validator.apply("-2"));
        assertEquals("1", validator.apply(""));
        assertEquals("5", validator.apply("abc"));
    }

    @Test
    void floatFieldUsesLdlibNumberValidator() throws Exception {
        TextFieldWidget field = blankField("0.5");
        StyledTextFields.applyFloatValidator(field, 0.0f, 1.0f);

        Function<String, String> validator = validator(field);
        assertEquals("0.75", validator.apply("0.75"));
        assertEquals("1.0", validator.apply("1.25"));
        assertEquals("0.0", validator.apply("-0.1"));
        assertEquals("0.0", validator.apply(""));
        assertEquals("0.5", validator.apply("abc"));
    }

    @Test
    void percentageFieldUsesZeroToHundredValidator() throws Exception {
        TextFieldWidget field = blankField("45");
        StyledTextFields.applyPercentageValidator(field);

        Function<String, String> validator = validator(field);
        assertEquals("45", validator.apply("45"));
        assertEquals("100", validator.apply("120"));
        assertEquals("0", validator.apply("-5"));
        assertEquals("0", validator.apply(""));
        assertEquals("45", validator.apply("abc"));
    }

    @Test
    void identifierFieldUsesSafeNameValidator() throws Exception {
        TextFieldWidget field = blankField("old_id");
        StyledTextFields.applyIdentifierValidator(field);

        Function<String, String> validator = validator(field);
        assertEquals("my_chapter_01", validator.apply(" My Chapter!! 01 "));
        assertEquals("old_id", validator.apply("!!!"));
        assertEquals("old_id", validator.apply(null));
    }

    @SuppressWarnings("unchecked")
    private static Function<String, String> validator(TextFieldWidget field) throws Exception {
        Field validator = TextFieldWidget.class.getDeclaredField("textValidator");
        validator.setAccessible(true);
        return (Function<String, String>) validator.get(field);
    }

    private static TextFieldWidget blankField(String currentString) throws Exception {
        TextFieldWidget field = (TextFieldWidget) unsafe().allocateInstance(TextFieldWidget.class);
        Field current = TextFieldWidget.class.getDeclaredField("currentString");
        current.setAccessible(true);
        current.set(field, currentString);
        return field;
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}
