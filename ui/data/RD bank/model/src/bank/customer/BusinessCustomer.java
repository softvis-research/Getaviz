package bank.customer;

import java.util.LinkedList;
import java.util.List;

import bank.products.Account;
import bank.products.Credit;


public class BusinessCustomer {

	private String name;
	
	private List<Account> accounts;
	private List<Credit> credits;
	

	public BusinessCustomer(String name) {		
		this.name = name;
		
		accounts = new LinkedList<Account>();
		credits = new LinkedList<Credit>();
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
	
	

}
