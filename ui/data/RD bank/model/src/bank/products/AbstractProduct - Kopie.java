package bank.products;

import java.util.List;

public abstract class AbstractProduct {
	
	protected long productNumber;
	protected int balance;
	protected boolean inTransaction = false;
	protected List<Transaction> executedTransactions;
	
	public int getBalance() {
		return balance;
	}
	
	public long getProductNumber() {
		return productNumber;
	}
	
	public boolean isInTransaction(){
		return inTransaction;
	}
	
	public List<Transaction> getExecutedTransactions(){
		return executedTransactions;
	}
	
	public void executeTransaction(Transaction transcation){
		inTransaction = true;
		transaction(transcation);
		inTransaction = false;
	}
	
	protected abstract void transaction(Transaction transcation);
}
