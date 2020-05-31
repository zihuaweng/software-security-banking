package security.web.banking.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import security.web.banking.forms.UserCreateForm;
import security.web.banking.service.UserService;

@Component
public class UserValidator implements Validator {

    private final double MAX_VALUE = 4294967295.99;
    private final String REGEX = "([1-9]\\d*(\\.\\d{1,2}$)?|[0-9]\\.\\d{1,2}$)";

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return UserCreateForm.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UserCreateForm userCreateForm = (UserCreateForm) o;

        // Username
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");
        String userName = userCreateForm.getUsername();
        if (userName.length() < 1 || userName.length() > 127 || !userName.matches("[_\\-\\.0-9a-z]{1,127}")) {
            errors.rejectValue("username", "Size.username");
            return;
        }

        if (userService.findUserByUsername(userName).isPresent()) {
            errors.rejectValue("username", "Duplicate.username");
            return;
        }

        // Password
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordConfirm", "NotEmpty");
        if (userCreateForm.getPassword().length() < 1 || userCreateForm.getPassword().length() > 127) {
            errors.rejectValue("password", "Size.password");
            return;
        }
        if (!userCreateForm.getPasswordConfirm().equals(userCreateForm.getPassword())) {
            errors.rejectValue("passwordConfirm", "Diff.passwordConfirm");
            return;
        }

        // Amount
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amountString", "NotEmpty");
        if (!userCreateForm.getAmountString().matches(REGEX)) {
            errors.rejectValue("amountString", "Value.amount");
            return;
        }
        double amount = Double.parseDouble(userCreateForm.getAmountString());
        if (amount >= MAX_VALUE) {
            errors.rejectValue("amountString", "Value.amount");
            return;
        }
        userCreateForm.setAmount(amount);
    }
}
