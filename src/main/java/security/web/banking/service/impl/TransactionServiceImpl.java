package security.web.banking.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import security.web.banking.domain.Transaction;
import security.web.banking.domain.TransactionType;
import security.web.banking.domain.User;
import security.web.banking.forms.TransactionForm;
import security.web.banking.repository.TransactionRepository;
import security.web.banking.repository.UserRepository;
import security.web.banking.service.TransactionService;
import security.web.banking.service.UserService;
import security.web.banking.utils.TransactionUtils;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Override
    public List<Transaction> findTransactionsByUserId(long id) {
        return transactionRepository.findTransactionsByUserId(id);
    }

    @Override
    public Transaction findTransactionsById(long id) {
        return transactionRepository.findTransactionById(id);
    }

    @Override
    public void processTransaction(TransactionForm transactionForm) {
        logger.info("Processing transaction.");

        long userId = transactionForm.getUserId();
        double amount = transactionForm.getAmount();
        TransactionType type = TransactionUtils.getType(transactionForm.getType());
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with userId=%s was not found", userId)));

        Transaction transaction = new Transaction(amount, type, user);

        switch (type) {
            case DEPOSIT:
                user.setAmount(user.getAmount() + amount);
                userRepository.save(user);
                transactionRepository.save(transaction);
                logger.info("Process deposit successfully.");
                break;
            case WITHDRAW:
                user.setAmount(user.getAmount() - amount);
                userRepository.save(user);
                transactionRepository.save(transaction);
                logger.info("Process withdraw successfully.");
                break;
        }
    }
}
