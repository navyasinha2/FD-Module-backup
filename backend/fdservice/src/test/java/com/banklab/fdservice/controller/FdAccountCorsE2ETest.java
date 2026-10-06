package com.banklab.fdservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.banklab.fdservice.client.ProductDetails;
import com.banklab.fdservice.client.ProductPricingClient;
import com.banklab.fdservice.client.RateDetails;
import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.entity.FdAccountSequence;
import com.banklab.fdservice.repository.FdAccountRepository;
import com.banklab.fdservice.repository.FdAccountRoleRepository;
import com.banklab.fdservice.repository.FdAccountSequenceRepository;
import com.banklab.fdservice.support.FdTestDatabase;

/**
 * End-to-end proof of the full frontend -> backend -> DB path added while G1 is
 * unavailable: a cross-origin request shaped exactly like the browser form's
 * submission (see dev/frontend), through the real Spring Security filter chain
 * (unlike {@link FdAccountCreationIntegrationTest}, filters are NOT disabled
 * here — CORS is implemented as a filter, so disabling filters would hide a
 * misconfigured SecurityConfig), resolving the customer via the real
 * CustomerServiceClientStub (not mocked — this proves the G1 stub's wiring),
 * and landing a row in the DB. Only Group 2's Product & Pricing service is
 * mocked, since it doesn't exist in this test environment either.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FdAccountCorsE2ETest {

    private static final String BRANCH_CD = "001";
    private static final String EXPECTED_ACCT_NUM = "FD001000001";
    private static final String FRONTEND_ORIGIN = "http://localhost:3000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FdAccountRepository accountRepository;

    @Autowired
    private FdAccountRoleRepository roleRepository;

    @Autowired
    private FdAccountSequenceRepository sequenceRepository;

    @MockitoBean
    private ProductPricingClient productPricingClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Ledger rows (FK → FD_ACCOUNTS) must be cleared before accounts; see FdTestDatabase.
        new FdTestDatabase(jdbcTemplate).clearFdData();
        roleRepository.deleteAll();
        accountRepository.deleteAll();

        sequenceRepository.saveAndFlush(FdAccountSequence.builder()
                .branchCd(BRANCH_CD)
                .branchName("Head Office")
                .prefix("FD")
                .lastSeq(0L)
                .seqWidth(6)
                .build());

        when(productPricingClient.getProduct("FD-REG")).thenReturn(new ProductDetails(
                "FD-REG", "Regular Fixed Deposit", "USD", "QUARTERLY", "QUARTERLY", "PAYOUT", "COMPOUND"));
        when(productPricingClient.getRate("RATE-12M")).thenReturn(Optional.of(new RateDetails(
                "RATE-12M", "FD-REG", 12, new BigDecimal("6.50"), LocalDate.parse("2025-01-01"),
                LocalDate.parse("2027-12-31"))));
    }

    @Test
    void browserPreflightIsAllowedFromTheFrontendOrigin() throws Exception {
        mockMvc.perform(options("/fd-accounts")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")));
    }

    @Test
    void formSubmissionOverCorsPersistsAccountToDatabase() throws Exception {
        // Exactly the shape dev/frontend/js/app.js#buildPayload sends.
        String formSubmissionBody = """
                {
                  "custId": "CUST-001",
                  "productCode": "FD-REG",
                  "principal": 75000.00,
                  "tenureMonths": 24,
                  "rateId": "RATE-12M",
                  "currencyCode": "USD",
                  "maturityInstruction": "PAYOUT",
                  "initialRoles": [
                    { "custId": "CUST-001", "roleType": "OWNER", "isPrimary": true }
                  ]
                }
                """;

        mockMvc.perform(post("/fd-accounts")
                        .header(HttpHeaders.ORIGIN, FRONTEND_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(formSubmissionBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND_ORIGIN))
                .andExpect(jsonPath("$.custId").value("CUST-001"))
                .andExpect(jsonPath("$.custNameSnap").value("Asha Rao"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Re-fetch independently from the DB, the same idiom as
        // FdAccountCreationIntegrationTest, rather than trusting the HTTP response.
        Optional<FdAccount> persisted = accountRepository.findByAcctNum(EXPECTED_ACCT_NUM);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getCustId()).isEqualTo("CUST-001");
        assertThat(persisted.get().getPrincipalAmt()).isEqualByComparingTo("75000.00");
        assertThat(persisted.get().getCustNameSnap()).isEqualTo("Asha Rao");
    }
}
