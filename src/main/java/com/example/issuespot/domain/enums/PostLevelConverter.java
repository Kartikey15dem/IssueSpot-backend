package com.example.issuespot.domain.enums;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostLevelConverter implements AttributeConverter<PostLevel, String> {
    @Override public String convertToDatabaseColumn(PostLevel attribute) { return attribute == null ? null : attribute.name(); }
    @Override public PostLevel convertToEntityAttribute(String dbData) { try { return dbData == null ? null : PostLevel.valueOf(dbData); } catch(Exception e) { return PostLevel.LOCALITY; } }
}
