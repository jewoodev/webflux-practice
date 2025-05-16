package com.heri2go.chat.validator;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.lang.Enum;

public class CustomEnumDeserializer extends StdDeserializer<Enum<?>> implements ContextualDeserializer {

    public CustomEnumDeserializer() {
        this(null);
    }

    protected CustomEnumDeserializer(Class<?> vc) {
        super(vc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String enumName = p.getValueAsString();
        if (enumName == null) return null;
        Class<? extends Enum> enumType = (Class<? extends Enum>) this._valueClass;
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equals(enumName))
                .findAny()
                .orElse(null);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new CustomEnumDeserializer(property.getType().getRawClass());
    }
}
