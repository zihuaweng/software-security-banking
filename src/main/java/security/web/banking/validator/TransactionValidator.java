package security.web.banking.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import security.web.banking.domain.TransactionType;
import security.web.banking.forms.TransactionForm;
import security.web.banking.utils.TransactionUtils;

@Component
public class TransactionValidator implements Validator {

    private final double MAX_VALUE = 4294967295.99;
    private final String REGEX = "([1-9]\\d*(\\.\\d{1,2}$)?|[0-9]\\.\\d{1,2}$)";

    @Override
    public boolean supports(Class<?> aClass) {
        return TransactionForm.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        TransactionForm transactionForm = (TransactionForm) o;
        // Invalid amount
        if (transactionForm.getAmountString() != null) {
            if (!transactionForm.getAmountString().matches(REGEX)) {
                errors.rejectValue("amountString", "Value.amount");
                return;
            } else {
                transactionForm.setAmount(Double.parseDouble(transactionForm.getAmountString()));
            }
        } else if (transactionForm.getNumString() != null && TransactionUtils.valid(transactionForm.getNumString())) {
                transactionForm.setAmount(Double.parseDouble(transactionForm.getNumString()));
        } else {
            errors.rejectValue("amountString", "Value.amount");
        }

        if (transactionForm.getAmount() > MAX_VALUE) {
            errors.rejectValue("amountString", "Value.amount");
            return;
        }

        // Not enough balance
        if (TransactionUtils.getType(transactionForm.getType()) == TransactionType.WITHDRAW &&
                transactionForm.getAmount() > transactionForm.getUserCurrentAmount()) {
            errors.rejectValue("amountString", "Value.transaction.amount");
        }
    }
}
