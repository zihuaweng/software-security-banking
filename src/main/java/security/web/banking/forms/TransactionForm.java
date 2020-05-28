package security.web.banking.forms;

import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
public class TransactionForm {

    private String amountString;
    private double amount;
    @NotEmpty
    private String type;
    @NotEmpty
    private long userId;
    private double userCurrentAmount;
    private String string;

    public TransactionForm() {}

    public String getAmountString() {
        return amountString;
    }

    public void setAmountString(String amountString) {
        this.amountString = amountString;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getUserCurrentAmount() {
        return userCurrentAmount;
    }

    public void setUserCurrentAmount(double userCurrentAmount) {
        this.userCurrentAmount = userCurrentAmount;
    }

    public String getNumString() {
        return string;
    }

    public void setNumString(String string) {
        this.string = string;
    }

    public void reset() {
        this.amountString = null;
        this.string = null;
    }
}
