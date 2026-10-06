package com.banklab.fdservice.client;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.banklab.fdservice.client.UpstreamServiceException.Reason;

/**
 * Real REST client for Group 2's Product & Pricing service. Inactive by
 * default — see {@link ProductPricingClientStub} — until G2 actually ships
 * and fd.clients.product-pricing.stub-mode is set to false.
 */
@Component
@ConditionalOnProperty(name = "fd.clients.product-pricing.stub-mode", havingValue = "false")
public class ProductPricingClientImpl implements ProductPricingClient {

    private final RestClient restClient;

    public ProductPricingClientImpl(RestClient productPricingRestClient) {
        this.restClient = productPricingRestClient;
    }

    @Override
    public ProductDetails getProduct(String productCode) {
        try {
            return restClient.get()
                    .uri("/products/{productCode}", productCode)
                    .retrieve()
                    .body(ProductDetails.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ProductPricingClientException(
                        "Product not found for productCode=" + productCode, Reason.NOT_FOUND, e);
            }
            throw new ProductPricingClientException(
                    "Product & Pricing service error resolving productCode=" + productCode,
                    Reason.UPSTREAM_ERROR, e);
        } catch (RestClientException e) {
            throw new ProductPricingClientException(
                    "Product & Pricing service unreachable resolving productCode=" + productCode,
                    Reason.UPSTREAM_ERROR, e);
        }
    }

    @Override
    public Optional<RateDetails> getRate(String rateId) {
        return fetchOptional(rateId, "/rates/{id}", RateDetails.class, "rateId");
    }

    @Override
    public Optional<CategoryDetails> getCategory(String categoryCode) {
        return fetchOptional(categoryCode, "/categories/{id}", CategoryDetails.class, "categoryCode");
    }

    /**
     * Shared by getRate/getCategory, which both treat "absent" and "not found" the
     * same way (empty) and differ only in URI template and the field name used in
     * error messages. getProduct doesn't use this — it must throw NOT_FOUND rather
     * than return empty, since a product is required, not optional.
     */
    private <T> Optional<T> fetchOptional(String id, String uriTemplate, Class<T> responseType, String fieldName) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(uriTemplate, id)
                    .retrieve()
                    .body(responseType));
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw new ProductPricingClientException(
                    "Product & Pricing service error resolving " + fieldName + "=" + id, Reason.UPSTREAM_ERROR, e);
        } catch (RestClientException e) {
            throw new ProductPricingClientException(
                    "Product & Pricing service unreachable resolving " + fieldName + "=" + id, Reason.UPSTREAM_ERROR, e);
        }
    }
}
