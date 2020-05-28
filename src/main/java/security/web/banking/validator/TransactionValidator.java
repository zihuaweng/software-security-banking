package security.web.banking.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import security.web.banking.domain.TransactionType;
import security.web.banking.forms.TransactionForm;
import security.web.banking.utils.TransactionUtils;

@Component
public class TransactionValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return TransactionForm.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        TransactionForm transactionForm = (TransactionForm) o;
        // Invalid amount
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amountString", "NotEmpty");
        if (!transactionForm.getAmountString().matches("(^[1-9]\\d*(\\.\\d{1,2}$)?|^[0-9]\\.\\d{1,2}$)")) {
            errors.rejectValue("amountString", "Value.userForm.amount");
            return;
        }
        transactionForm.setAmount(Double.parseDouble(transactionForm.getAmountString()));

        if (transactionForm.getAmount() > 4294967295.99) {
            errors.rejectValue("amountString", "Value.userForm.maxAmount");
            return;
        }

        // Not enough balance
        if (TransactionUtils.getType(transactionForm.getType()) == TransactionType.WITHDRAW &&
                transactionForm.getAmount() > transactionForm.getUserCurrentAmount()) {
            errors.rejectValue("amountString", "Value.transactionForm.amount");
        }
    }
}
