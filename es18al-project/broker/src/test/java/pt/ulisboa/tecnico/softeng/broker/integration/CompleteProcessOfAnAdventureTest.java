package pt.ulisboa.tecnico.softeng.broker.integration;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

import pt.ulisboa.tecnico.softeng.activity.domain.Activity;
import pt.ulisboa.tecnico.softeng.activity.domain.ActivityOffer;
import pt.ulisboa.tecnico.softeng.activity.domain.ActivityProvider;
import pt.ulisboa.tecnico.softeng.bank.domain.Account;
import pt.ulisboa.tecnico.softeng.bank.domain.Bank;
import pt.ulisboa.tecnico.softeng.broker.domain.Adventure;
import pt.ulisboa.tecnico.softeng.broker.domain.Broker;
import pt.ulisboa.tecnico.softeng.broker.domain.Client;
import pt.ulisboa.tecnico.softeng.broker.domain.RollbackTestAbstractClass;
import pt.ulisboa.tecnico.softeng.car.domain.Car;
import pt.ulisboa.tecnico.softeng.car.domain.RentACar;
import pt.ulisboa.tecnico.softeng.hotel.domain.Hotel;
import pt.ulisboa.tecnico.softeng.hotel.domain.Room;
import pt.ulisboa.tecnico.softeng.tax.domain.Buyer;
import pt.ulisboa.tecnico.softeng.tax.domain.IRS;
import pt.ulisboa.tecnico.softeng.tax.domain.ItemType;
import pt.ulisboa.tecnico.softeng.tax.domain.Seller;

/*
  This is an end-to-end (integration) test of an adventure.

  It tests the following interactions:
    - Broker books an activity
        - activity module charges the amount of the activity to the bank account of the broker
        - activity module sends the invoice to the tax module
    - Broker books a room (if activity is longer than a day)
        - hotel module charges the amount of the room to the  bank account of the broker
        - hotel module sends the invoice to the tax module
    - [Optional] Broker rents a car
        - car module charges the amount of the room to the  bank account of the broker
        - car module sends the invoice to the tax module
    - Broker charges the Client the total amount plus its margin
    - Broker sends invoice to of the total amount plus margin to the tax module.

		Note that the payment process is not fully implemented, neither it is
		necessary to implement it now, but a payment should actually be a transfer
		between two accounts. Therefore, the TODO comments show what would be the
		balance in some of the accounts if the payment process was fully
		implemented. This implementation, is, however, left for a future
		development iteration.
 */
public class CompleteProcessOfAnAdventureTest extends RollbackTestAbstractClass {
	// Broker
	private static final String CODE = "BR01";
	private static final String NAME_OF_BROKER = "WeExplore";
	private static final String NIF_AS_BUYER = "111111111";
	private static final String NIF_AS_SELLER = "222222222";
	private static final String ADDRESS_OF_BROKER = "AddressOfBroker";

	// Adventure and Client
	private static final String NIF_OF_CLIENT = "333333333";
	private static final String DRIVING_LICENSE = "IMT1234";
	private static final int AGE = 25;
	private static final String ADDRESS_OF_CLIENT = "AddressOfClient";
	private static final String NAME_OF_CLIENT = "NameOfClient";
	private static final double MARGIN = 0.3;
	private final LocalDate begin = new LocalDate(2016, 12, 19);
	private final LocalDate end = new LocalDate(2016, 12, 21);

	// Activity
	private static final String NAME_OF_PROVIDER = "ExtremeAdventure";
	private static final String NIF_OF_PROVIDER = "444444444";
	private static final int CAPACITY = 25;
	private static final int MIN_AGE = 18;
	private static final int MAX_AGE = 75;
	private static final int ACTIVITY_COST = 30;

	// Hotel
	private static final String NAME_OF_HOTEL = "Hotel Lisboa";
	private static final String NIF_OF_HOTEL = "555555555";
	private static final double PRICE_SINGLE = 20.0;
	private static final double PRICE_DOUBLE = 30.0;

	// Car
	private static final String NAME_OF_RENT_A_CAR = "Drive Save";
	private static final String NIF_OF_RENT_A_CAR = "666666666";
	private static final String PLATE_OF_CAR = "22-33-HZ";
	private static final int PRICE_OF_CAR = 10;

	Adventure adventure;
	
	IRS irs;

	Seller brokerAsSeller;
	Buyer brokerAsBuyer;
	Buyer clientAsBuyer;
	Seller providerAsSeller;
	Seller hotelAsSeller;
	Seller rentACarAsSeller;

	Account brokerAccount;
	Account clientAccount;
	Account providerAccount;
	Account hotelAccount;
	Account rentACarAccount;

	@Override
	public void populate4Test() {
		// tax
		this.irs = new IRS();
		
		new ItemType(this.irs, "SPORT", 10);
		new ItemType(this.irs, "HOUSING", 10);
		new ItemType(this.irs, "RENTAL", 10);
		new ItemType(this.irs, "ADVENTURE", 10);

		//BrokerAsSeller
		//this.brokerAsSeller = new Seller(IRS.getIRS(), NIF_AS_SELLER, NAME_OF_BROKER, ADDRESS_OF_BROKER);
		this.brokerAsSeller = new Seller();
		this.brokerAsSeller.setIrs(this.irs);
		this.brokerAsSeller.setNIF(NIF_AS_SELLER);
		this.brokerAsSeller.setName(NAME_OF_BROKER);
		this.brokerAsSeller.setAddress(ADDRESS_OF_BROKER);
		//borkerAsBuyer
		//this.brokerAsBuyer = new Buyer(IRS.getIRS(), NIF_AS_BUYER, NAME_OF_BROKER, ADDRESS_OF_BROKER);
		this.brokerAsBuyer = new Buyer();
		this.brokerAsBuyer.setIrs(this.irs);
		this.brokerAsBuyer.setNIF(NIF_AS_BUYER);
		this.brokerAsBuyer.setName(NAME_OF_BROKER);
		this.brokerAsBuyer.setAddress(ADDRESS_OF_BROKER);
		//clientAsBuyer
		//this.clientAsBuyer = new Buyer(IRS.getIRS(), NIF_OF_CLIENT, NAME_OF_CLIENT, ADDRESS_OF_CLIENT);
		this.clientAsBuyer = new Buyer();
		this.clientAsBuyer.setIrs(this.irs);
		this.clientAsBuyer.setNIF(NIF_OF_CLIENT);
		this.clientAsBuyer.setName(NAME_OF_CLIENT);
		this.clientAsBuyer.setAddress(ADDRESS_OF_CLIENT);
		//providerAsSeller
		//this.providerAsSeller = new Seller(IRS.getIRS(), NIF_OF_PROVIDER, NAME_OF_PROVIDER, "AddressOfProvider");
		this.providerAsSeller = new Seller();
		this.providerAsSeller.setIrs(this.irs);
		this.providerAsSeller.setNIF(NIF_OF_PROVIDER);
		this.providerAsSeller.setName(NAME_OF_PROVIDER);
		this.providerAsSeller.setAddress("AddressOfProvider");
		//hotelAsSeller
		//this.hotelAsSeller = new Seller(IRS.getIRS(), NIF_OF_HOTEL, NAME_OF_HOTEL, "AddressOfHotel");
		this.hotelAsSeller = new Seller();
		this.hotelAsSeller.setIrs(this.irs);
		this.hotelAsSeller.setNIF(NIF_OF_HOTEL);
		this.hotelAsSeller.setName(NAME_OF_HOTEL);
		this.hotelAsSeller.setAddress("AddressOfHotel");
		//rentACarAsSeller
		//this.rentACarAsSeller = new Seller(IRS.getIRS(), NIF_OF_RENT_A_CAR, NAME_OF_RENT_A_CAR, "AddressOfRentACar");
		this.rentACarAsSeller = new Seller();
		this.rentACarAsSeller.setIrs(this.irs);
		this.rentACarAsSeller.setNIF(NIF_OF_RENT_A_CAR);
		this.rentACarAsSeller.setName(NAME_OF_RENT_A_CAR);
		this.rentACarAsSeller.setAddress("AddressOfRentACar");
		
		// bank
		Bank bank = new Bank("Money", "BK01");
		pt.ulisboa.tecnico.softeng.bank.domain.Client brokerClient = new pt.ulisboa.tecnico.softeng.bank.domain.Client(
				bank, NAME_OF_BROKER);
		this.brokerAccount = new Account(bank, brokerClient);

		pt.ulisboa.tecnico.softeng.bank.domain.Client clientClient = new pt.ulisboa.tecnico.softeng.bank.domain.Client(
				bank, NAME_OF_CLIENT);
		this.clientAccount = new Account(bank, clientClient);

		pt.ulisboa.tecnico.softeng.bank.domain.Client providerClient = new pt.ulisboa.tecnico.softeng.bank.domain.Client(
				bank, NAME_OF_PROVIDER);
		this.providerAccount = new Account(bank, providerClient);

		pt.ulisboa.tecnico.softeng.bank.domain.Client hotelClient = new pt.ulisboa.tecnico.softeng.bank.domain.Client(
				bank, NAME_OF_HOTEL);
		this.hotelAccount = new Account(bank, hotelClient);

		pt.ulisboa.tecnico.softeng.bank.domain.Client rentACarClient = new pt.ulisboa.tecnico.softeng.bank.domain.Client(
				bank, NAME_OF_RENT_A_CAR);
		this.rentACarAccount = new Account(bank, rentACarClient);

		// broker
		Broker broker = new Broker(CODE, NAME_OF_BROKER, this.brokerAsSeller.getNIF(), this.brokerAsBuyer.getNIF(),
				this.brokerAccount.getIBAN());
		this.adventure = new Adventure(broker, this.begin, this.end,
				new Client(broker, this.clientAccount.getIBAN(), this.clientAsBuyer.getNIF(), DRIVING_LICENSE, AGE),
				MARGIN, true);

		// activity
		ActivityProvider provider = new ActivityProvider("XtremX", NAME_OF_PROVIDER, this.providerAsSeller.getNIF(),
				this.providerAccount.getIBAN());
		Activity activity = new Activity(provider, "Bush Walking", MIN_AGE, MAX_AGE, CAPACITY);
		new ActivityOffer(activity, this.begin, this.end, ACTIVITY_COST);

		// hotel
		Hotel hotel = new Hotel("XPTO123", NAME_OF_HOTEL, this.hotelAsSeller.getNIF(), this.hotelAccount.getIBAN(),
				PRICE_SINGLE, PRICE_DOUBLE);
		new Room(hotel, "01", Room.Type.SINGLE);

		// car
		RentACar rentACar = new RentACar(NAME_OF_RENT_A_CAR, this.rentACarAsSeller.getNIF(),
				this.rentACarAccount.getIBAN());
		new Car(PLATE_OF_CAR, 10, PRICE_OF_CAR, rentACar);
	}

	@Test
	public void successEndToEnd() {
		int numberOfDays = this.end.getDayOfYear() - this.begin.getDayOfYear();

		assertEquals(Adventure.State.RESERVE_ACTIVITY, this.adventure.getState().getValue());

		this.brokerAccount.deposit(ACTIVITY_COST);
		this.adventure.process();

		assertEquals(0, this.brokerAccount.getBalance(), 0.0f);
		// TODO: assertEquals(ACTIVITY_COST, this.providerAccount.getBalance(), 0.0f);
		assertEquals(0.15, this.brokerAsBuyer.taxReturn(this.begin.getYear()), 0.0f);
		assertEquals(3.0, this.providerAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(Adventure.State.BOOK_ROOM, this.adventure.getState().getValue());

		this.brokerAccount.deposit(PRICE_SINGLE * numberOfDays);
		this.adventure.process();

		assertEquals(0, this.brokerAccount.getBalance(), 0.0f);
		// TODO: assertEquals(PRICE_SINGLE * numberOfDays,
		// this.hotelAccount.getBalance(), 0.0f);
		assertEquals(0.35, this.brokerAsBuyer.taxReturn(this.begin.getYear()), 0.0f);
		assertEquals(4.0, this.hotelAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(Adventure.State.RENT_VEHICLE, this.adventure.getState().getValue());

		this.brokerAccount.deposit(PRICE_OF_CAR * numberOfDays);
		this.adventure.process();

		assertEquals(0, this.brokerAccount.getBalance(), 0.0d);
		// TODO: assertEquals(PRICE_OF_CAR * numberOfDays,
		// this.rentACarAccount.getBalance(), 0.0f);
		assertEquals(0.45, this.brokerAsBuyer.taxReturn(this.begin.getYear()), 0.001);
		assertEquals(2.0, this.rentACarAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(Adventure.State.PROCESS_PAYMENT, this.adventure.getState().getValue());

		this.clientAccount
				.deposit((ACTIVITY_COST + PRICE_SINGLE * numberOfDays + PRICE_OF_CAR * numberOfDays) * (1 + MARGIN));

		this.adventure.process();

		assertEquals(0, this.clientAccount.getBalance(), 0.0f);
		// TODO: assertEquals(78, this.brokerAccount.getBalance(), 0.0f);
		assertEquals(Adventure.State.TAX_PAYMENT, this.adventure.getState().getValue());

		this.adventure.process();

		assertEquals(0.585, this.clientAsBuyer.taxReturn(this.begin.getYear()), 0.0f);
		assertEquals(11.7, this.brokerAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(Adventure.State.CONFIRMED, this.adventure.getState().getValue());

		this.adventure.process();

		assertEquals(0, this.clientAccount.getBalance(), 0.0f);
		// TODO: assertEquals(78, this.brokerAccount.getBalance(), 0.0f);
		// TODO: assertEquals(PRICE_OF_CAR, this.rentACarAccount.getBalance(), 0.0f);
		// TODO: assertEquals(PRICE_SINGLE, this.hotelAccount.getBalance(), 0.0f);
		// TODO: assertEquals(ACTIVITY_COST, this.providerAccount.getBalance(), 0.0f);
		assertEquals(0.585, this.clientAsBuyer.taxReturn(this.begin.getYear()), 0.0f);
		assertEquals(0.45, this.brokerAsBuyer.taxReturn(this.begin.getYear()), 0.001f);
		assertEquals(11.7, this.brokerAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(3.0, this.providerAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(4.0, this.hotelAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(2.0, this.rentACarAsSeller.toPay(this.begin.getYear()), 0.0f);
		assertEquals(Adventure.State.CONFIRMED, this.adventure.getState().getValue());
	}

}