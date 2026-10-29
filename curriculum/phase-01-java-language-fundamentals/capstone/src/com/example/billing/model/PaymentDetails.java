package com.example.billing.model;

import java.util.Objects;

/**
 * Sealed payment method hierarchy demonstrating Java 21 exhaustive pattern matching.
 */
public sealed interface PaymentDetails permits 
    PaymentDetails.CreditCard, 
    PaymentDetails.WireTransfer, 
    PaymentDetails.CryptoAsset {

    record CreditCard(String maskedPan, String cardType, int expiryYear) implements PaymentDetails {
        public CreditCard {
            Objects.requireNonNull(maskedPan, "maskedPan must not be null");
            Objects.requireNonNull(cardType, "cardType must not be null");
            if (expiryYear < 2026) {
                throw new IllegalArgumentException("Card has expired: " + expiryYear);
            }
        }
    }

    record WireTransfer(String iban, String bic, String beneficiaryReference) implements PaymentDetails {
        public WireTransfer {
            Objects.requireNonNull(iban, "iban must not be null");
            Objects.requireNonNull(bic, "bic must not be null");
        }
    }

    record CryptoAsset(String walletAddress, String blockchainNetwork) implements PaymentDetails {
        public CryptoAsset {
            Objects.requireNonNull(walletAddress, "walletAddress must not be null");
            Objects.requireNonNull(blockchainNetwork, "blockchainNetwork must not be null");
        }
    }
}
