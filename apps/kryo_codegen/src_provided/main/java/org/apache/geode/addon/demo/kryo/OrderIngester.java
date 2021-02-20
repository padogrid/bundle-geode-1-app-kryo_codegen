package org.apache.geode.addon.demo.kryo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.demo.nw.data.avro.Order;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;

public class OrderIngester {
	public final static String PROPERTY_executableName = "executable.name";

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

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, OrderIngester.class.getName());
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
		Region<CharSequence, Order> region = clientCache.getRegion(regionPath);
		if (region == null) {
			System.err.println("ERROR: Region undefined: [" + regionPath + "]. Command aborted.");
			System.exit(-1);
			clientCache.close();
		}
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
