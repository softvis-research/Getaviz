package bank.products;

public class Transaction {
	
	private AbstractProduct firstProduct;
	private AbstractProduct secondProduct;
	
	public Transaction(AbstractProduct firstProduct, AbstractProduct secondProduct, int balance){
		this.firstProduct = firstProduct;
		this.secondProduct = secondProduct;
	}
	
	public void execute(){
		
	}

	public AbstractProduct getFirstProduct() {
		return firstProduct;
	}

	public AbstractProduct getSecondProduct() {
		return secondProduct;
	}	
}
