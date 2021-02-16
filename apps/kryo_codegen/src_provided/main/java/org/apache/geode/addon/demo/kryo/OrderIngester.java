package org.apache.geode.addon.demo.kryo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.demo.nw.data.avro.Order;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;

public class OrderIngester {
	private static Faker faker = new Faker();
	private static Random random = new Random();

	private static String REGION_PATH = "/nw/orders";
	private static int OBJECT_COUNT = 100;

	public static Order createOrder() {
		Order order = new Order();
		Company company = faker.company();
		order.setCustomerId(faker.idNumber().invalidSvSeSsn());
		order.setEmployeeId(faker.idNumber().invalidSvSeSsn());
		order.setFreight(200 * random.nextDouble());
		order.setOrderDateObj(faker.date().past(7, TimeUnit.DAYS));
		order.setOrderId(faker.idNumber().invalidSvSeSsn());
		order.setRequiredDateObj(faker.date().future(20, TimeUnit.DAYS));
		Address address = faker.address();
		order.setShipAddress(address.fullAddress());
		order.setShipCity(address.city());
		order.setShipCountry(address.country());
		order.setShipName(company.name());
		order.setShippedDateObj(faker.date().past(4, TimeUnit.DAYS));
		order.setShipPostalCode(address.zipCode());
		order.setShipRegion(address.stateAbbr());
		order.setShipVia(Integer.toString(random.nextInt(5) + 1));
		return order;
	}

	public static void main(String... args) {
		ClientCache clientCache = new ClientCacheFactory().create();
		Region<CharSequence, Order> region = clientCache.getRegion(REGION_PATH);
		for (int i = 0; i < OBJECT_COUNT; i++) {
			Order order = createOrder();
			region.put(order.getCustomerId(), order);
		}
		clientCache.close();

		System.out.println("Data Class: " + Order.class.getName());
		System.out.println("  Ingested: " + OBJECT_COUNT);
		System.out.println("       Map: " + REGION_PATH);
	}
}
