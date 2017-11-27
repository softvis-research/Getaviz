package bank.products;

import java.util.ArrayList;

public class BankBook extends AbstractProduct{
	
	public BankBook(long bookNumber){
		productNumber = bookNumber;
		executedTransactions = new ArrayList<Transaction>();
	}

	@Override
	protected void transaction(Transaction transcation) {
		// TODO Auto-generated method stub
		
	}
	
}
