package security.web.banking.service;

import security.web.banking.domain.Transaction;
import security.web.banking.forms.TransactionForm;

import java.util.List;

public interface TransactionService {

    List<Transaction> findTransactionsByUserId(long id);
    Transaction findTransactionsById(long id);
    void processTransaction(TransactionForm transactionForm);
    long deleteTransactionsByUserId(long id);
}
