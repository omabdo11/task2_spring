package com.spring.boot.service.impl;

import com.spring.boot.dto.AccountDto;
import com.spring.boot.exceptions.IdMisMatchException;
import com.spring.boot.mapper.AccountMapper;
import com.spring.boot.model.Account;
import com.spring.boot.model.Role;
import com.spring.boot.repo.AccountRepo;
import com.spring.boot.service.AccountService;
import jakarta.transaction.SystemException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepo accountRepo;


    @Override
    public List<AccountDto> getApplications() {

        // use query SELECT a FROM Account a JOIN FETCH a.roles
        List<Account> accounts = accountRepo.findAllWithRoles();

       // accounts.stream().forEach(account -> account.setRoles(null));
        return extractAccounts(accounts);
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) throws IdMisMatchException, SystemException {
        if (Objects.nonNull(accountDto.getId())) {
            throw new IdMisMatchException("error.id.invalid");
        }

        Optional<Account> accountExist = accountRepo.findByUserName(accountDto.getUserName());

        if (accountExist.isPresent()) {
            throw new SystemException("there exist account with same username: " + accountDto.getUserName());
        }

        Account account = AccountMapper.ACCOUNT_MAPPER.toAccount(accountDto);

        accountRepo.save(account);
        return accountDto;
    }

    @Override
    public AccountDto updateAccount(AccountDto accountDto) throws SystemException {
        if (Objects.isNull(accountDto.getId())) {
            throw new SystemException("id must be not null");
        }

        checkAccountExist(accountDto.getId());

        Account account = AccountMapper.ACCOUNT_MAPPER.toAccount(accountDto);

        accountRepo.save(account);
        return accountDto;
    }

    @Override
    public boolean deleteAccount(Long id) throws SystemException {
        Optional<Account> optionalAccount = accountRepo.findById(id);
        if (optionalAccount.isEmpty()) {
            return false;
        }
        accountRepo.deleteById(id);
        return true;
    }

    @Override
    public List<AccountDto> search(String searchValue) throws SystemException {
        if (Objects.isNull(searchValue)) {
            throw new SystemException("searchValue must be not null");
        }
        List<Account> accounts = accountRepo.findByUserNameContainingIgnoreCase(searchValue);

        return extractAccounts(accounts);
    }

    @Override
    public List<AccountDto> getByPhone(String phone) throws SystemException {
        if (Objects.isNull(phone)) {
            throw new SystemException("searchValue must be not null");
        }
        List<Account> accounts = accountRepo.findByAccountPhoneV2(phone);

        return extractAccounts(accounts);
    }

    @Override
    public AccountDto getAccountByName(String name) throws SystemException {
        Optional<Account> accountExist = accountRepo.findByUserName(name);

//        accountExist.get().getRoles().stream().forEach(role -> role.setAccounts(null));

        if (!accountExist.isPresent()) {
            throw new SystemException("there exist account with same username: " + name);
        }

        return AccountMapper.ACCOUNT_MAPPER.toAccountDto(accountExist.get());
    }

    private void checkAccountExist(Long id) throws SystemException {
        Optional<Account> optionalAccount = accountRepo.findById(id);

        if (optionalAccount.isEmpty()) {
            throw new SystemException("account not exist with id " + id);
        }
    }


    private List<AccountDto> extractAccounts(List<Account> accounts) {
        List<AccountDto> accountVMS = new ArrayList<>();
        for (Account ac : accounts) {
            AccountDto accountDto = AccountMapper.ACCOUNT_MAPPER.toAccountDto(ac);
//            accountDto.setPassword(null);
            /*
            AccountDto accountDto = new AccountDto();
            accountDto.setId(ac.getId());
            accountDto.setUserName(ac.getUserName());
            accountDto.setPhoneNumber(ac.getPhoneNumber());
            accountDto.setLength(String.valueOf(ac.getUserName().length()));
*/
            accountVMS.add(accountDto);
        }
        return accountVMS;
    }
}
