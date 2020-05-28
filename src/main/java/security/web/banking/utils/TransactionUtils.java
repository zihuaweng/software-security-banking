package security.web.banking.utils;

import security.web.banking.domain.TransactionType;

public class TransactionUtils {
    public static TransactionType getType (String transaction) {
        switch (transaction) {
            case "withdraw":
                return TransactionType.WITHDRAW;
            case "deposit":
                return TransactionType.DEPOSIT;
            default:
                return null;
        }
    }
}
