package com.example.banking.test;

import com.example.banking.domain.BankAccount;
import com.example.banking.model.AccountId;
import com.example.banking.model.AccountState;
import com.example.banking.model.AccountType;
import com.example.banking.model.Currency;
import com.example.banking.model.Customer;
import com.example.banking.model.CustomerTier;
import com.example.banking.model.Money;
import com.example.banking.model.TransactionRecord;
import com.example.banking.model.TransactionResult;
import com.example.banking.model.TransactionType;
import com.example.banking.service.BankingCoreEngine;

import java.util.Objects;

public class BankingEngineIntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("PHASE 02 CAPSTONE: BANKING DOMAIN ENGINE TEST SUITE");
        System.out.println("==================================================\n");

        testAccountBuilderAndInvariants();
        testLedgerDefensiveImmutability();
        testDepositAndWithdrawalLifecycle();
        testAccountStateFsmTransitions();
        testAccountTypeYieldCalculation();
        testCoreEngineTransferWithFeeAndAtomicity();
        testRiskEngineHighValueChallenge();
        testObjectContractsAndCredentialMasking();

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Test Results: %d Passed, %d Failed\n", passed, failed);
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.err.println("CAPSTONE INTEGRATION TEST SUITE FAILED");
            System.exit(1);
        } else {
            System.out.println("ALL PHASE 02 CAPSTONE TESTS PASSED CLEANLY (100%).");
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        if (!Objects.equals(expected, actual)) {
            System.err.printf("FAIL: %s (Expected %s, got %s)\n", name, expected, actual);
            failed++;
        } else {
            System.out.printf("PASS: %s\n", name);
            passed++;
        }
    }

    private static void assertTrue(boolean condition, String name) {
        if (!condition) {
            System.err.printf("FAIL: %s (Condition was false)\n", name);
            failed++;
        } else {
            System.out.printf("PASS: %s\n", name);
            passed++;
        }
    }

    // 1. BUILDER AND INVARIANTS
    private static void testAccountBuilderAndInvariants() {
        Customer cust = new Customer("C-101", "Alice Vance", "alice@blackmesa.org", CustomerTier.STANDARD);
        AccountId accId = new AccountId("ACC-ALICE01");

        BankAccount account = BankAccount.builder()
            .accountId(accId)
            .customer(cust)
            .accountType(AccountType.CHECKING)
            .currency(Currency.USD)
            .initialBalanceCents(50000L) // $500.00
            .build();

        assertEquals(accId, account.getAccountId(), "Builder initialized AccountId");
        assertEquals(AccountState.ACTIVE, account.getState(), "Default initial state is ACTIVE");
        assertEquals(50000L, account.getBalance().amountCents(), "Opening balance recorded");
        assertEquals(1, account.getLedger().size(), "Opening balance generated initial transaction in ledger");

        // Negative check: Invalid AccountId format
        boolean caughtInvalidId = false;
        try {
            new AccountId("invalid_format_id");
        } catch (IllegalArgumentException e) {
            caughtInvalidId = true;
        }
        assertTrue(caughtInvalidId, "AccountId rejected invalid formatting string");
    }

    // 2. DEFENSIVE IMMUTABILITY
    private static void testLedgerDefensiveImmutability() {
        Customer cust = new Customer("C-102", "Bob Smith", "bob@example.com", CustomerTier.PREMIUM);
        BankAccount account = BankAccount.builder()
            .accountId(new AccountId("ACC-BOB01"))
            .customer(cust)
            .initialBalanceCents(10000L)
            .build();

        boolean caughtUnsupported = false;
        try {
            account.getLedger().clear();
        } catch (UnsupportedOperationException e) {
            caughtUnsupported = true;
        }
        assertTrue(caughtUnsupported, "Encapsulation: Directly mutating getLedger() is rejected");
        assertEquals(1, account.getLedger().size(), "Aggregate internal ledger remains intact");
    }

    // 3. DEPOSIT AND WITHDRAWAL
    private static void testDepositAndWithdrawalLifecycle() {
        Customer cust = new Customer("C-103", "Charlie Day", "charlie@paddys.com", CustomerTier.STANDARD);
        BankAccount account = BankAccount.builder()
            .accountId(new AccountId("ACC-CHARLIE01"))
            .customer(cust)
            .initialBalanceCents(20000L) // $200.00
            .build();

        TransactionRecord depRecord = account.deposit(Money.of(15000L, Currency.USD), "Payroll Deposit");
        assertEquals(35000L, account.getBalance().amountCents(), "Deposit updated balance to $350.00");
        assertEquals(TransactionType.DEPOSIT, depRecord.type(), "Deposit transaction record typed correctly");

        TransactionRecord wthRecord = account.withdraw(Money.of(10000L, Currency.USD), "ATM Cash");
        assertEquals(25000L, account.getBalance().amountCents(), "Withdrawal updated balance to $250.00");
        assertEquals(TransactionType.WITHDRAWAL, wthRecord.type(), "Withdrawal transaction record typed correctly");

        // Negative check: Overdraft rejected
        boolean caughtOverdraft = false;
        try {
            account.withdraw(Money.of(999999L, Currency.USD), "Overdraft Attempt");
        } catch (IllegalStateException e) {
            caughtOverdraft = true;
        }
        assertTrue(caughtOverdraft, "Overdraft rejected when funds are insufficient");
    }

    // 4. RICH ENUM FSM
    private static void testAccountStateFsmTransitions() {
        Customer cust = new Customer("C-104", "Dennis Reynolds", "dennis@paddys.com", CustomerTier.VIP);
        BankAccount account = BankAccount.builder()
            .accountId(new AccountId("ACC-DENNIS01"))
            .customer(cust)
            .initialBalanceCents(100000L)
            .build();

        // Valid transition: ACTIVE -> FROZEN
        account.transitionState(AccountState.FROZEN);
        assertEquals(AccountState.FROZEN, account.getState(), "Account transitioned to FROZEN");

        // Transactions blocked while FROZEN
        boolean caughtBlocked = false;
        try {
            account.deposit(Money.of(5000L, Currency.USD), "Test blocked deposit");
        } catch (IllegalStateException e) {
            caughtBlocked = true;
        }
        assertTrue(caughtBlocked, "Transactions blocked while in FROZEN state");

        // Valid transition: FROZEN -> ACTIVE -> CLOSED
        account.transitionState(AccountState.ACTIVE);
        account.transitionState(AccountState.CLOSED);
        assertEquals(AccountState.CLOSED, account.getState(), "Account transitioned to terminal CLOSED state");

        // Prohibited transition: CLOSED -> ACTIVE
        boolean caughtIllegalTransition = false;
        try {
            account.transitionState(AccountState.ACTIVE);
        } catch (IllegalStateException e) {
            caughtIllegalTransition = true;
        }
        assertTrue(caughtIllegalTransition, "Terminal CLOSED state prohibits reactivation");
    }

    // 5. ACCOUNT TYPE YIELD CALCULATION
    private static void testAccountTypeYieldCalculation() {
        Customer cust = new Customer("C-105", "Mac", "mac@paddys.com", CustomerTier.STANDARD);
        // Savings Account: 3.50% APY
        BankAccount savings = BankAccount.builder()
            .accountId(new AccountId("ACC-SAVINGS01"))
            .customer(cust)
            .accountType(AccountType.SAVINGS)
            .initialBalanceCents(1_200_000L) // $12,000.00
            .build();

        // Monthly interest on $12,000 at 3.5% APY = (1,200,000 * 350) / 120,000 = 3500 cents ($35.00)
        TransactionRecord interestRecord = savings.applyMonthlyInterest();
        assertTrue(interestRecord != null, "Savings generated monthly interest");
        assertEquals(3500L, interestRecord.amount().amountCents(), "Savings monthly interest is exactly $35.00");
        assertEquals(1_203_500L, savings.getBalance().amountCents(), "Savings balance incremented by interest");

        // Checking Account: 0% yield
        BankAccount checking = BankAccount.builder()
            .accountId(new AccountId("ACC-CHECKING01"))
            .customer(cust)
            .accountType(AccountType.CHECKING)
            .initialBalanceCents(50000L)
            .build();

        TransactionRecord checkingYield = checking.applyMonthlyInterest();
        assertTrue(checkingYield == null, "Checking yields zero interest");
    }

    // 6. CORE ENGINE TRANSFER WITH FEE AND ATOMICITY
    private static void testCoreEngineTransferWithFeeAndAtomicity() {
        BankingCoreEngine engine = new BankingCoreEngine();

        Customer standardCust = new Customer("C-106", "Dee", "dee@paddys.com", CustomerTier.STANDARD);
        Customer vipCust = new Customer("C-107", "Frank Reynolds", "frank@warthog.com", CustomerTier.VIP);

        BankAccount standardAccount = BankAccount.builder()
            .accountId(new AccountId("ACC-DEE01"))
            .customer(standardCust)
            .initialBalanceCents(50000L) // $500.00
            .build();

        BankAccount vipAccount = BankAccount.builder()
            .accountId(new AccountId("ACC-FRANK01"))
            .customer(vipCust)
            .initialBalanceCents(1000000L) // $10,000.00
            .build();

        // Transfer $100.00 (10000 cents) from Standard to VIP.
        // Standard fee = 1.00% ($1.00) -> Total debit = $101.00 (10100 cents).
        TransactionResult result = engine.processTransfer(
            standardAccount, 
            vipAccount, 
            Money.of(10000L, Currency.USD), 
            "Rent payment"
        );

        // JAVA 21 PATTERN MATCHING SWITCH EVALUATION
        boolean transferSucceeded = false;
        switch (result) {
            case TransactionResult.Success s -> {
                transferSucceeded = true;
                assertTrue(s.confirmationCode().startsWith("XFR-"), "Confirmation code generated");
            }
            case TransactionResult.RequiresChallenge rc -> fail("Unexpected challenge");
            case TransactionResult.Rejected r -> fail("Unexpected rejection: " + r.details());
        }
        assertTrue(transferSucceeded, "Transfer processed successfully");

        // Verify balances: Standard debited 10,100 cents ($500.00 - $101.00 = $399.00 = 39900 cents)
        assertEquals(39900L, standardAccount.getBalance().amountCents(), "Source debited amount plus fee");
        // Target credited exactly 10,000 cents ($10,000.00 + $100.00 = $10,100.00 = 1010000 cents)
        assertEquals(1010000L, vipAccount.getBalance().amountCents(), "Target credited exact transfer amount");
    }

    // 7. RISK ENGINE HIGH VALUE CHALLENGE
    private static void testRiskEngineHighValueChallenge() {
        BankingCoreEngine engine = new BankingCoreEngine();

        Customer standardCust = new Customer("C-108", "Rick Sanchez", "rick@c137.org", CustomerTier.STANDARD);
        // Standard transfer limit is $1,000.00 (100_000 cents)
        BankAccount acc1 = BankAccount.builder()
            .accountId(new AccountId("ACC-RICK01"))
            .customer(standardCust)
            .initialBalanceCents(5_000_000L) // $50,000.00
            .build();

        BankAccount acc2 = BankAccount.builder()
            .accountId(new AccountId("ACC-MORTY01"))
            .customer(standardCust)
            .initialBalanceCents(10000L)
            .build();

        // Attempting to transfer $5,000.00 exceeds Standard daily limit ($1,000.00)
        TransactionResult result = engine.processTransfer(
            acc1, 
            acc2, 
            Money.of(500_000L, Currency.USD), 
            "Interdimensional portal funding"
        );

        boolean challenged = false;
        switch (result) {
            case TransactionResult.RequiresChallenge rc -> {
                challenged = true;
                assertEquals("HIGH_VALUE_WIRE_AUTHORIZATION", rc.challengeType(), "Challenge type identifies wire review");
            }
            case TransactionResult.Success s -> fail("High value transfer should have been challenged");
            case TransactionResult.Rejected r -> fail("Transfer was rejected instead of challenged");
        }
        assertTrue(challenged, "Risk engine triggered step-up challenge for transfer exceeding tier limit");
    }

    // 8. OBJECT CONTRACTS AND CREDENTIAL MASKING
    private static void testObjectContractsAndCredentialMasking() {
        Customer cust = new Customer("C-109", "Bruce Wayne", "bruce@wayne.com", CustomerTier.VIP);
        AccountId id = new AccountId("ACC-BATMAN01");

        BankAccount a1 = BankAccount.builder().accountId(id).customer(cust).initialBalanceCents(1000L).build();
        BankAccount a2 = BankAccount.builder().accountId(id).customer(cust).initialBalanceCents(9999L).build();
        BankAccount a3 = BankAccount.builder().accountId(new AccountId("ACC-JOKER99")).customer(cust).build();

        assertEquals(a1, a2, "Equals: Accounts with identical AccountId are logically equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "HashCode: Equal accounts have identical hash code");
        assertTrue(!a1.equals(a3), "Distinct accounts are not equal");

        // Verify toString masks account identifier
        String repr = a1.toString();
        assertTrue(repr.contains("ACC-****01"), "toString masks account middle digits: " + repr);
        assertTrue(!repr.contains("ACC-BATMAN01"), "toString does NOT leak raw account number");
    }

    private static void fail(String message) {
        System.err.println("FAIL: " + message);
        failed++;
    }
}
