package com.xtremand.email.verification.config.converter;

import com.xtremand.email.verification.model.dto.chart.ChartRange;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToChartRangeConverter implements Converter<String, ChartRange> {

    @Override
    public ChartRange convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return switch (source.toUpperCase()) {
            case "1M" -> ChartRange.M1;
            case "3M" -> ChartRange.M3;
            case "6M" -> ChartRange.M6;
            case "1Y" -> ChartRange.Y1;
            case "3Y" -> ChartRange.Y3;
            case "5Y" -> ChartRange.Y5;
            default -> throw new IllegalArgumentException("Unknown chart range: " + source);
        };
    }
}