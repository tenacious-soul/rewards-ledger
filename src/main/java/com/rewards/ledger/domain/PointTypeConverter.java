package com.rewards.ledger.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PointTypeConverter implements AttributeConverter<PointType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PointType pointType) {
        return pointType == null ? null : pointType.getId();
    }

    @Override
    public PointType convertToEntityAttribute(Integer id) {
        return id == null ? null : PointType.fromId(id);
    }
}
