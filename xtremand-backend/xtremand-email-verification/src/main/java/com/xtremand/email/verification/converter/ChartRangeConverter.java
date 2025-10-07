package com.xtremand.email.verification.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.xtremand.email.verification.model.dto.chart.ChartRange;

@Component
public class ChartRangeConverter implements Converter<String, ChartRange> {

    @Override
    public ChartRange convert(String source) {
        try {
            return ChartRange.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid range value. Allowed values are: M1, M3, M6, Y1, Y3, Y5");
        }
    }
}