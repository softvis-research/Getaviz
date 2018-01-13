package bank.customer;

import java.util.LinkedList;
import java.util.List;

import bank.products.Account;
import bank.products.BankBook;
import bank.products.Credit;


public class PrivateCustomer {
	
	private String name;
	
	private List<Account> accounts;
	private List<Credit> credits;
	private List<BankBook> bankBook;

	public PrivateCustomer(String name) {
		this.name = name;
		
		accounts = new LinkedList<Account>();
		credits = new LinkedList<Credit>();
		bankBook = new LinkedList<BankBook>();
		
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public List<Credit> getCredits() {
		return credits;
	}

	public List<BankBook> getBankBook() {
		return bankBook;
	}

	
	
	
}
