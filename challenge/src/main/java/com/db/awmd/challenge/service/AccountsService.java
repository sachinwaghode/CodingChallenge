package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  private NotificationService notificationService;
  
  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  
  public String transferFunds(String accountFromId, String accountToId, BigDecimal amount) {
	  String message;

	  if(amount.compareTo(BigDecimal.ZERO) == -1){
		  return "negative amount";
	  }
	  else if(this.getAccount(accountFromId).getBalance().compareTo(amount) == -1){
		  return "we do not support overdrafts!";
	  }else{
		  this.getAccount(accountFromId).setBalance(this.getAccount(accountFromId).getBalance().subtract(amount));
		  this.getAccount(accountToId).setBalance(this.getAccount(accountToId).getBalance().add(amount));
		  message = "Transfer amount of "+amount+" successful to id= "+accountToId;
		  notificationService.notifyAboutTransfer(this.getAccount(accountFromId), message);
		  notificationService.notifyAboutTransfer(this.getAccount(accountToId), message);
	  }

	  return message;
  }


}
