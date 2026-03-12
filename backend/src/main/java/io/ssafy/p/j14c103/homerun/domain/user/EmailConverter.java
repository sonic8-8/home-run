package io.ssafy.p.j14c103.homerun.domain.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Email.of(dbData);
    }
}
