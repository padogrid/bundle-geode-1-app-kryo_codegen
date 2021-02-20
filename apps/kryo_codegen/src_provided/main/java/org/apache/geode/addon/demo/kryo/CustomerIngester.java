
package org.apache.geode.addon.demo.kryo;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.demo.nw.data.avro.Customer;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;

public class CustomerIngester {

	public final static String PROPERTY_executableName = "executable.name";

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

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, CustomerIngester.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Ingest mock data into the '" + REGION_PATH + "' region");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " [grid_name] [-?]");
		writeLine();
		writeLine("   Ingests mock data into the '" + REGION_PATH + "' region. If the optional grid name is specified, then");
		writeLine("   it ingests into the '/grid_name" + REGION_PATH + "' region conforming to the Pado specification.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   grid_name");
		writeLine("             Name of the grid. If specified, then the grid name serves as the top-level region,");
		writeLine("             i.e., '/grid_name" + REGION_PATH + "'. The 'etc/client-cache.xml' file must also define the");
		writeLine("             top-level region. This option provides a way to ingest data into a Pado cluster");
		writeLine("             such that the Pado Desktop can be used to browse data.");
		writeLine();
	}

	public static void main(String... args) {
		String gridName = null;
		if (args.length > 0) {
			gridName = args[0];
			if (gridName.equals("-?")) {
				usage();
				System.exit(0);
			}
		}
		String regionPath;
		if (gridName != null) {
			regionPath = "/" + gridName + REGION_PATH;
		} else {
			regionPath = REGION_PATH;
		}
		writeLine("Ingesting data into " + regionPath + "...");
		
		ClientCache clientCache = new ClientCacheFactory().create();
		Region<CharSequence, Customer> region = clientCache.getRegion(regionPath);
		if (region == null) {
			System.err.println("ERROR: Region undefined: [" + regionPath + "]. Command aborted.");
			System.exit(-1);
			clientCache.close();
		}
		for (int i = 0; i < OBJECT_COUNT; i++) {
			Customer customer = createCustomer();
			region.put(customer.getCustomerId(), customer);
		}
		clientCache.close();
		System.out.println("Data Class: " + Customer.class.getName());
		System.out.println("  Ingested: " + OBJECT_COUNT);
		System.out.println("    Region: " + regionPath);
	}
}
