package com.banklab.fdservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.banklab.fdservice.client.CustomerDto;
import com.banklab.fdservice.client.CustomerServiceClient;
import com.banklab.fdservice.client.ProductDetails;
import com.banklab.fdservice.client.ProductPricingClient;
import com.banklab.fdservice.client.ProductPricingClientException;
import com.banklab.fdservice.client.RateDetails;
import com.banklab.fdservice.dto.AssignRoleRequest;
import com.banklab.fdservice.dto.CreateFdAccountRequest;
import com.banklab.fdservice.dto.InterestType;
import com.banklab.fdservice.dto.MaturityInstruction;
import com.banklab.fdservice.dto.RoleType;
import com.banklab.fdservice.entity.FdAccount;
import com.banklab.fdservice.repository.FdAccountRepository;
import com.banklab.fdservice.repository.FdAccountRoleRepository;

/**
 * Unit tests for FD creation logic with collaborators mocked (the interest calculator
 * is real — it's pure).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FdAccountServiceTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.parse("2026-01-01");

    @Mock
    private ProductPricingClient productPricingClient;
    @Mock
    private CustomerServiceClient customerServiceClient;
    @Mock
    private AccountNumberGeneratorService accountNumberGeneratorService;
    @Mock
    private FdAccountRepository accountRepository;
    @Mock
    private FdAccountRoleRepository roleRepository;
    @Mock
    private BusinessClockService businessClockService;
    @Mock
    private FdLedgerService ledgerService;

    private FdAccountService service;

    @BeforeEach
    void setUp() {
        service = new FdAccountService(productPricingClient, customerServiceClient, accountNumberGeneratorService,
                accountRepository, roleRepository, businessClockService, new InterestCalculator(), ledgerService);
        ReflectionTestUtils.setField(service, "defaultBranchCd", "001");
        ReflectionTestUtils.setField(service, "defaultInterestType", InterestType.COMPOUND);
        ReflectionTestUtils.setField(service, "defaultDayCountConvention", "ACT/ACT");

        when(customerServiceClient.getCustomer("CUST-001")).thenReturn(new CustomerDto("CUST-001", "Asha Rao", "ACTIVE"));
        when(productPricingClient.getProduct("FD-REG")).thenReturn(new ProductDetails(
                "FD-REG", "Regular Fixed Deposit", "INR", "QUARTERLY", "ON_MATURITY", "PAYOUT", null));
        when(productPricingClient.getRate("RATE-12M")).thenReturn(Optional.of(new RateDetails(
                "RATE-12M", "FD-REG", 12, new BigDecimal("6.50"), LocalDate.parse("2025-01-01"),
                LocalDate.parse("2027-12-31"))));
        when(accountNumberGeneratorService.generateAccountNumber("001")).thenReturn("FD001000042");
        when(businessClockService.currentBusinessDate()).thenReturn(BUSINESS_DATE);
        when(accountRepository.saveAndFlush(any(FdAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void bookingSnapshotsTermsOnTheBusinessDateQuotesMaturityAndPostsTheInitialDeposit() {
        var response = service.createAccount(request("RATE-12M", "INR", null, null, null, owner()));

        FdAccount saved = savedAccount();
        assertThat(saved.getIntTyp()).isEqualTo("COMPOUND");
        assertThat(saved.getIntRt()).isEqualByComparingTo("6.50");
        assertThat(saved.getCompoundFreq()).isEqualTo("QUARTERLY");
        assertThat(saved.getPayoutFreq()).isEqualTo("ON_MATURITY");
        assertThat(saved.getDayCountConv()).isEqualTo("ACT/ACT");
        assertThat(saved.getOpenDt()).isEqualTo(BUSINESS_DATE);
        assertThat(saved.getValueDt()).isEqualTo(BUSINESS_DATE);
        assertThat(saved.getMatDt()).isEqualTo(LocalDate.parse("2027-01-01"));
        assertThat(saved.getMatAmt()).isEqualByComparingTo("106660.15");
        assertThat(saved.getCcyDecimals()).isEqualTo(2);
        assertThat(saved.getPrincipalBal()).isEqualByComparingTo("100000.00");
        assertThat(saved.getSts()).isEqualTo("ACTIVE");
        assertThat(saved.getUserId()).isEqualTo(FdLedgerService.API_USER);
        assertThat(saved.getCrudValue()).isEqualTo("C");

        verify(roleRepository).saveAll(anyList());
        verify(ledgerService).postDeposit(same(saved), eq(BUSINESS_DATE), any(UUID.class), eq(FdLedgerService.API_USER));
        assertThat(response.acctNum()).isEqualTo("FD001000042");
        assertThat(response.interestType()).isEqualTo(InterestType.COMPOUND);
        assertThat(response.matAmt()).isEqualByComparingTo("106660.15");
    }

    @Test
    void explicitSimpleInterestWithMonthlyPayoutIsKeptAndMaturesAtPrincipal() {
        service.createAccount(request("RATE-12M", "INR", InterestType.SIMPLE, null, "MONTHLY", owner()));

        FdAccount saved = savedAccount();
        assertThat(saved.getIntTyp()).isEqualTo("SIMPLE");
        assertThat(saved.getPayoutFreq()).isEqualTo("MONTHLY");
        assertThat(saved.getMatAmt()).isEqualByComparingTo("100000.00");
    }

    @Test
    void productDefaultInterestTypeIsUsedWhenTheRequestOmitsIt() {
        when(productPricingClient.getProduct("FD-SR")).thenReturn(new ProductDetails(
                "FD-SR", "Senior Citizen Fixed Deposit", "INR", "QUARTERLY", "MONTHLY", "PAYOUT", "SIMPLE"));

        service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-SR", new BigDecimal("100000.00"), 12,
                "RATE-12M", null, "INR", null, null, null, MaturityInstruction.PAYOUT, owner()));

        FdAccount saved = savedAccount();
        assertThat(saved.getIntTyp()).isEqualTo("SIMPLE");
        assertThat(saved.getPayoutFreq()).isEqualTo("MONTHLY");
    }

    @Test
    void requestInterestTypeOverridesTheProductDefault() {
        when(productPricingClient.getProduct("FD-SR")).thenReturn(new ProductDetails(
                "FD-SR", "Senior Citizen Fixed Deposit", "INR", "QUARTERLY", "MONTHLY", "PAYOUT", "SIMPLE"));

        service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-SR", new BigDecimal("100000.00"), 12,
                "RATE-12M", null, "INR", null, null, InterestType.COMPOUND, MaturityInstruction.PAYOUT, owner()));

        assertThat(savedAccount().getIntTyp()).isEqualTo("COMPOUND");
    }

    @Test
    void unknownProductInterestTypeIsRejected() {
        when(productPricingClient.getProduct("FD-X")).thenReturn(new ProductDetails(
                "FD-X", "Odd Product", "INR", "QUARTERLY", "ON_MATURITY", "PAYOUT", "FLOATING"));

        assertThatThrownBy(() -> service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-X",
                new BigDecimal("100000.00"), 12, "RATE-12M", null, "INR", null, null, null,
                MaturityInstruction.PAYOUT, owner())))
                .isInstanceOf(InvalidInterestTermsException.class);
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    void principalWithMoreDecimalsThanTheCurrencyAllowsIsRejected() {
        assertThatThrownBy(() -> service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-REG",
                new BigDecimal("1000.555"), 12, "RATE-12M", null, "INR", null, null, null,
                MaturityInstruction.PAYOUT, owner())))
                .isInstanceOf(InvalidPrincipalException.class);
        assertThatThrownBy(() -> service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-REG",
                new BigDecimal("1000.5"), 12, "RATE-12M", null, "JPY", null, null, null,
                MaturityInstruction.PAYOUT, owner())))
                .isInstanceOf(InvalidPrincipalException.class);

        verify(accountRepository, never()).saveAndFlush(any());
        verify(accountNumberGeneratorService, never()).generateAccountNumber(anyString());
    }

    @Test
    void principalWithTrailingZerosBeyondTheCurrencyDecimalsIsAccepted() {
        service.createAccount(new CreateFdAccountRequest("CUST-001", "FD-REG", new BigDecimal("1000.500"), 12,
                "RATE-12M", null, "INR", null, null, null, MaturityInstruction.PAYOUT, owner()));

        assertThat(savedAccount().getPrincipalAmt()).isEqualByComparingTo("1000.50");
    }

    @Test
    void unknownRateIdIsRejectedBeforeAnAccountNumberIsMinted() {
        assertThatThrownBy(() -> service.createAccount(request("RATE-NOPE", "INR", null, null, null, owner())))
                .isInstanceOf(ProductPricingClientException.class)
                .hasMessageContaining("RATE-NOPE");

        verify(accountNumberGeneratorService, never()).generateAccountNumber(anyString());
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    void rateRowWithoutAnEffectiveRateIsRejectedAndNothingIsSaved() {
        when(productPricingClient.getRate("RATE-NIL")).thenReturn(Optional.of(new RateDetails(
                "RATE-NIL", "FD-REG", 12, null, LocalDate.parse("2025-01-01"), LocalDate.parse("2027-12-31"))));

        assertThatThrownBy(() -> service.createAccount(request("RATE-NIL", "INR", null, null, null, owner())))
                .isInstanceOf(InvalidInterestTermsException.class)
                .hasMessageContaining("FDA_INT_RT");

        verify(accountRepository, never()).saveAndFlush(any());
        verifyNoInteractions(ledgerService);
    }

    @Test
    void zeroDecimalCurrencySnapshotsZeroDecimals() {
        service.createAccount(request("RATE-12M", "JPY", null, null, null, owner()));

        FdAccount saved = savedAccount();
        assertThat(saved.getCcyDecimals()).isZero();
        assertThat(saved.getMatAmt().scale()).isZero();
    }

    @Test
    void unknownFrequencyIsRejectedBeforeAnythingIsSaved() {
        assertThatThrownBy(() -> service.createAccount(request("RATE-12M", "INR", null, "WEEKLY", null, owner())))
                .isInstanceOf(InvalidInterestTermsException.class);

        verify(accountRepository, never()).saveAndFlush(any());
        verifyNoInteractions(ledgerService);
    }

    @Test
    void secondGuarantorIsRejectedBeforeAnAccountNumberIsMinted() {
        List<AssignRoleRequest> roles = List.of(
                new AssignRoleRequest("CUST-001", RoleType.OWNER, true),
                new AssignRoleRequest("CUST-002", RoleType.GUARANTOR, false),
                new AssignRoleRequest("CUST-003", RoleType.GUARANTOR, false));

        assertThatThrownBy(() -> service.createAccount(request("RATE-12M", "INR", null, null, null, roles)))
                .isInstanceOf(RoleCardinalityViolationException.class);

        verify(accountNumberGeneratorService, never()).generateAccountNumber(anyString());
        verifyNoInteractions(ledgerService);
    }

    private FdAccount savedAccount() {
        ArgumentCaptor<FdAccount> captor = ArgumentCaptor.forClass(FdAccount.class);
        verify(accountRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private static List<AssignRoleRequest> owner() {
        return List.of(new AssignRoleRequest("CUST-001", RoleType.OWNER, true));
    }

    private static CreateFdAccountRequest request(String rateId, String currency, InterestType interestType,
            String compoundingFreq, String payoutFreq, List<AssignRoleRequest> roles) {
        return new CreateFdAccountRequest("CUST-001", "FD-REG", new BigDecimal("100000.00"), 12, rateId, null,
                currency, payoutFreq, compoundingFreq, interestType, MaturityInstruction.PAYOUT, roles);
    }
}
