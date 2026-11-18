package com.trektravel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary and discount calculation utility using high-precision BigDecimal.
 */
public class PriceUtil {

    public static BigDecimal calculateDiscountedPricePerPerson(BigDecimal price, int discountPercent) {
        if (price == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (discountPercent <= 0) return price.setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - discountPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateDiscountAmount(BigDecimal price, int discountPercent, int participants) {
        if (price == null || discountPercent <= 0 || participants <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal gross = price.multiply(BigDecimal.valueOf(participants));
        BigDecimal discountFactor = BigDecimal.valueOf(discountPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return gross.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotal(BigDecimal price, int discountPercent, int participants) {
        if (price == null || participants <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discountedRate = calculateDiscountedPricePerPerson(price, discountPercent);
        return discountedRate.multiply(BigDecimal.valueOf(participants)).setScale(2, RoundingMode.HALF_UP);
    }
}
