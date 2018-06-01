package pt.ulisboa.tecnico.softeng.tax.domain;

import org.junit.Test;

import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class BuyertaxReturnMethodTest {
	private static final int niceyear = 1970;
	private static final int wrongyear = 1969;
	private static final int yearwithnoinvoice = 2001;
	private static final float value = 33;
	private final LocalDate date = new LocalDate(1970, 1, 1);
	private final LocalDate date2 = new LocalDate(2001, 1, 1);
	private Buyer buyer;
	private Seller seller;
	private ItemType item;
	
	@Before
	public void setUp() {
		buyer = new Buyer("123123987", "Joao", "Lisboa, Alameda rua Almirante Reis nº 20");
		seller = new Seller("123123988", "Maria", "Lisboa, Sete Rios rua 20 nº 12");
		item = new ItemType("jogos", 20);
		Invoice i1 = new Invoice (value, date, item, seller, buyer);
	}

	
	@Test
	public void sucess() {
		float retorno = buyer.taxReturn(niceyear);
		Assert.assertNotNull(retorno);
	}
	
	@Test(expected = TaxException.class)
	public void invalidYear() {
		buyer.taxReturn(wrongyear);
	}
	
	@Test(expected = TaxException.class)
	public void YearwithNoInvoices() {
		buyer.taxReturn(yearwithnoinvoice);
	}
	
	@Test(expected = TaxException.class)
	public void sellerwithnoinvoice() {
		Buyer buyer2 = new Buyer("123123989", "Ana", "Lisboa, Sete Rios rua 20 nº 12");
		Invoice i1 = new Invoice (value, date2, item, seller, buyer2);
		buyer.taxReturn(yearwithnoinvoice);
	}
	
	@After
	public void tearDown() {
		IRS.taxpayers.clear();
		IRS.itemtypes.clear();
		TaxPayer.invoices.clear();
	}
	
}