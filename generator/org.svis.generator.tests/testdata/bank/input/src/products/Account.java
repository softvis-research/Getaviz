package bank.products;

import java.util.ArrayList;

public class Account extends AbstractProduct{
	
	public Account(long accountNumber){
		productNumber = accountNumber;
		executedTransactions = new ArrayList<Transaction>();
	}

	@Override
	protected void transaction(Transaction transcation) {
		// TODO Auto-generated method stub
		
	}
	
	
}
