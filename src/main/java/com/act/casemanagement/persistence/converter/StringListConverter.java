package com.act.casemanagement.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Converts List<String> to/from JSONB array. */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) return null;
        try { return MAPPER.writeValueAsString(attribute); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize string list", e); }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        try { return MAPPER.readValue(dbData, new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to deserialize string list", e); }
    }
}
