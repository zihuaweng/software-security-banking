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

    public static boolean valid (String string) {
        return string.matches("-?([1-9]\\d*(\\.\\d{1,2}$)?|[0-9]\\.\\d{1,2}$)");
    }
}
