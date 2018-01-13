package bank;

import java.util.LinkedList;
import java.util.List;

import bank.customer.BusinessCustomer;
import bank.customer.PrivateCustomer;
import bank.products.Account;
import bank.products.BankBook;
import bank.products.Credit;
import bank.products.Transaction;


public class Bank {
	
	private String bankName;
	
	private List<PrivateCustomer> privateCustomers;
	private List<BusinessCustomer> businessCustomers;
	
	private List<Account> accounts;
	private List<Credit> credits;
	private List<BankBook> bankBooks;
	private List<Transaction> transactions; 

	public Bank(){
		privateCustomers = new LinkedList<PrivateCustomer>();
		businessCustomers = new LinkedList<BusinessCustomer>();
		
		accounts = new LinkedList<Account>();  
		credits = new LinkedList<Credit>();
		bankBooks = new LinkedList<BankBook>();
		transactions = new LinkedList<Transaction>();   
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Bank myBank = new Bank(); 
		myBank.run();
	}
	
	public void run(){		
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankName() {
		return bankName;
	}

	
	
	public List<PrivateCustomer> getPrivateCustomers() {
		return privateCustomers;
	}

	public List<BusinessCustomer> getBusinessCustomers() {
		return businessCustomers;
	}

		
	public List<Account> getAccounts() {
		return accounts;
	}

	public List<BankBook> getBankBooks() {
		return bankBooks;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public List<Credit> getCredits() {
		return credits;
	}

}
