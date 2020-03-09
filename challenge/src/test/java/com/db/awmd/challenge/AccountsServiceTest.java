package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  
  private AccountsRepository accountsRepository;
  
  @MockBean
  private NotificationService notificationService;

  @Before
	public void prepareMockMvc() {
	  	accountsRepository = Mockito.mock(AccountsRepository.class);
	}

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  @Test
  public void transferFundsSucessfullWhenHavingFunds(){
	  ReflectionTestUtils.setField(accountsService,
				"accountsRepository", accountsRepository);
	  String accountFromId = "Id-123";
	  String accountToId = "Id-124"; 
	  BigDecimal amount= new BigDecimal(123.45);
	  
	  Mockito.when(accountsRepository.getAccount(Matchers.anyString()))
				.thenReturn(new Account("Id-123", new BigDecimal(123.45)));
	  
	  Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Matchers.anyString());
	  
	  assertThat(this.accountsService.transferFunds(accountFromId, accountToId, amount)).isEqualTo("Transfer amount of " + amount + " successful to id= " + accountToId);
  }
  
  @Test
  public void transferFundsFailsWhenSendingNegativeAmount(){
	  ReflectionTestUtils.setField(accountsService,
				"accountsRepository", accountsRepository);
	  String accountFromId = "Id-123";
	  String accountToId = "Id-124"; 
	  BigDecimal amount= new BigDecimal(123.45).negate();
	  
	  assertThat(this.accountsService.transferFunds(accountFromId, accountToId, amount)).isEqualTo("negative amount");
  }
  
  @Test
  public void transferFundsFailsWhenSenderAccountHavingNegativeAmount(){
	  ReflectionTestUtils.setField(accountsService,
				"accountsRepository", accountsRepository);
	  String accountFromId = "Id-125";
	  String accountToId = "Id-124"; 
	  BigDecimal amount= new BigDecimal(123.45);
	  
	  Mockito.when(accountsRepository.getAccount(Matchers.anyString()))
		.thenReturn(new Account("Id-125", new BigDecimal(123.45).negate()));
	  
	  assertThat(this.accountsService.transferFunds(accountFromId, accountToId, amount)).isEqualTo("we do not support overdrafts!");
  }
}
