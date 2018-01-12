package bank.products;

import java.util.ArrayList;

public class Credit extends AbstractProduct {

	
	public Credit(long creditNumber){
		productNumber = creditNumber;
		executedTransactions = new ArrayList<Transaction>();
	}

	@Override
	protected void transaction(Transaction transcation) {
		// TODO Auto-generated method stub
		
	}
	
	
}
