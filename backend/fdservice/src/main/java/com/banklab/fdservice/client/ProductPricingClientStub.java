package com.banklab.fdservice.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Stand-in for Group 2's Product & Pricing service, which — like Group 1's
 * customer service — hasn't been coded yet. Active by default (see
 * fd.clients.product-pricing.stub-mode); once G2 ships, set that property to
 * false and configure fd.clients.product-pricing.base-url to switch to
 * {@link ProductPricingClientImpl} without touching any caller.
 *
 * Same shape as {@link CustomerServiceClientStub}: hardcoded data for the
 * product code our own tests/fixtures already use, and synthesized-but-valid
 * data for anything else so ad hoc testing (e.g. via dev/frontend) isn't
 * restricted to a handful of magic values.
 */
@Component
@ConditionalOnProperty(name = "fd.clients.product-pricing.stub-mode", havingValue = "true", matchIfMissing = true)
public class ProductPricingClientStub implements ProductPricingClient {

    private static final Map<String, ProductDetails> KNOWN_PRODUCTS = Map.of(
            "FD-REG", new ProductDetails(
                    "FD-REG", "Regular Fixed Deposit", "USD", "QUARTERLY", "QUARTERLY", "PAYOUT", "COMPOUND"),
            "FD-SR", new ProductDetails(
                    "FD-SR", "Senior Citizen Fixed Deposit", "USD", "QUARTERLY", "MONTHLY", "PAYOUT", "SIMPLE"));

    @Override
    public ProductDetails getProduct(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            throw new ProductPricingClientException(
                    "Product not found for productCode=" + productCode, ProductPricingClientException.Reason.NOT_FOUND);
        }
        ProductDetails known = KNOWN_PRODUCTS.get(productCode);
        if (known != null) {
            return known;
        }
        return new ProductDetails(productCode, "Product " + productCode, "USD", "QUARTERLY", "QUARTERLY", "PAYOUT",
                "COMPOUND");
    }

    @Override
    public Optional<RateDetails> getRate(String rateId) {
        if (!StringUtils.hasText(rateId)) {
            return Optional.empty();
        }
        return Optional.of(new RateDetails(
                rateId, null, 3, new BigDecimal("6.50"), LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1)));
    }

    @Override
    public Optional<CategoryDetails> getCategory(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) {
            return Optional.empty();
        }
        return Optional.of(new CategoryDetails(categoryCode, "Category " + categoryCode));
    }
}
