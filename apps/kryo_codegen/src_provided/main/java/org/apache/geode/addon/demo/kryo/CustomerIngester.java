
package org.apache.geode.addon.demo.kryo;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.demo.nw.data.avro.Customer;
import org.apache.geode.demo.nw.data.avro.Order;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;


public class CustomerIngester {
	private static Faker faker = new Faker();
	
	private static String REGION_PATH = "/nw/customers";
	private static int OBJECT_COUNT = 100;

	public static Customer createCustomer() {
		Customer customer = new Customer();
		Address address = faker.address();
		Company company = faker.company();
		PhoneNumber phone = faker.phoneNumber();
		customer.setAddress(address.fullAddress());
		customer.setCity(address.city());
		customer.setCompanyName(company.name());
		customer.setContactName(address.lastName());
		customer.setContactTitle(faker.job().title());
		customer.setCountry(address.country());
		customer.setCustomerId(faker.idNumber().invalidSvSeSsn());
		customer.setFax(phone.cellPhone());
		customer.setPhone(phone.phoneNumber());
		customer.setPostalCode(address.zipCode());
		customer.setRegion(address.stateAbbr());
		return customer;
	}

	public static void main(String...args) {
		ClientCache clientCache = new ClientCacheFactory().create();
		Region<CharSequence, Customer> region = clientCache.getRegion(REGION_PATH);
		for (int i = 0; i < OBJECT_COUNT; i++) {
			Customer customer = createCustomer();
			region.put(customer.getCustomerId(), customer);
		}
		clientCache.close();
		System.out.println("Data Class: " + Customer.class.getName());
		System.out.println("  Ingested: " + OBJECT_COUNT);
		System.out.println("    Region: " + REGION_PATH);
	}
}
