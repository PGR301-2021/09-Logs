package com.example.demo;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static java.util.Optional.ofNullable;

@RestController
public class BankAccountController implements ApplicationListener<ApplicationReadyEvent> {

    private Map<String, Account> theBank = new HashMap();
    private Logger logger = LoggerFactory.getLogger(BankAccountController.class);


    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    public BankAccountController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Transfers money from one account to another. Creates accounts at both ends if they do not exist.
     *
     * @param tx          a transaction object
     * @param fromAccount from account id
     * @param toAccount   to account id
     */
    @Timed
    @PostMapping(path = "/account/{fromAccount}/transfer/{toAccount}", consumes = "application/json", produces = "application/json")
    public void transfer(@RequestBody Transaction tx, @PathVariable String fromAccount, @PathVariable String toAccount) {
        logger.info("Money transfer from " + fromAccount + " to " + toAccount);
        meterRegistry.counter("transfer", "amount", String.valueOf(tx.getAmount())).increment();
        Account from = getOrCreateAccount(fromAccount);
        Account to = getOrCreateAccount(toAccount);
        from.setBalance(from.getBalance().subtract(valueOf(tx.getAmount())));
        to.setBalance(to.getBalance().add(valueOf(tx.getAmount())));
    }

    /**
     * Saves an account. Will create a new account if one does not exist.
     *
     * @param a the account Object
     * @return
     */

    @PostMapping(path = "/account", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> updateAccount(@RequestBody Account a) {
        logger.info("Account update/create " + a.getId());
        meterRegistry.counter("update_account").increment();
        if (a.getBalance().floatValue() < 0) {
            throw new IllegalArgumentException("Can't create an account with a negative balance");
        }
        Account account = getOrCreateAccount(a.getId());
        account.setBalance(a.getBalance());
        account.setCurrency(a.getCurrency());
        logger.info("Currency is " + a.getCurrency());
        theBank.put(a.getId(), a);
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    /**
     * Gets account info for an account
     *
     * @param accountId the account ID to get info from
     * @return
     */
    @GetMapping(path = "/account/{accountId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> balance(@PathVariable String accountId) {
        logger.info("Account balance request " + accountId);
        meterRegistry.counter("balance").increment();
        Account account = ofNullable(theBank.get(accountId)).orElseThrow(AccountNotFoundException::new);
        meterRegistry.gauge("account_balance", account.getBalance());
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    private Account getOrCreateAccount(String accountId) {
        if (theBank.get(accountId) == null) {
            Account a = new Account();
            a.setId(accountId);
            theBank.put(accountId, a);
        }
        return theBank.get(accountId);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("account_count", theBank, b -> b.values().size()).register(meterRegistry);
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "video not found")
    public static class AccountNotFoundException extends RuntimeException {
    }
}

