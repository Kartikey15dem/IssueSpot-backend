package com.example.issuespot.domain.enums;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {
    @Override public String convertToDatabaseColumn(MediaType attribute) { return attribute == null ? null : attribute.name(); }
    @Override public MediaType convertToEntityAttribute(String dbData) { try { return dbData == null ? null : MediaType.valueOf(dbData); } catch(Exception e) { return MediaType.TEXT; } }
}
