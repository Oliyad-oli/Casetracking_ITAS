package com.act.casemanagement.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.Map;

/** Converts List<Map<String,Object>> to/from JSONB (used for fieldsDefinition). */
@Converter
public class MapListJsonbConverter implements AttributeConverter<List<Map<String,Object>>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String,Object>> attribute) {
        if (attribute == null) return null;
        try { return MAPPER.writeValueAsString(attribute); }
        catch (Exception e) { throw new IllegalStateException("Failed to serialize map list", e); }
    }

    @Override
    public List<Map<String,Object>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        try { return MAPPER.readValue(dbData, new TypeReference<>() {}); }
        catch (Exception e) { throw new IllegalStateException("Failed to deserialize map list", e); }
    }
}
