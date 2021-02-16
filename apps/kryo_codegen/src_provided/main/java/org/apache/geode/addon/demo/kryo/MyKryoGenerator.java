package org.apache.geode.addon.demo.kryo;

import com.netcrest.padogrid.tools.GeodeKryoGenerator;

/**
 * This class shows you how to run KryGenerator programmatically.
 * 
 * @author dpark
 *
 */
public class MyKryoGenerator {

	public static void main(String[] args) throws Exception {

		// All paths are relative to the working directory.
		String packageName = " org.apache.geode.demo.nw.data.avro";
		String jarPath = "lib/app-kryo-codegen-geode-1.0.0.jar";
		int typeId = 1200;
		String srcDir = "src/main/java";

		GeodeKryoGenerator generator = new GeodeKryoGenerator(packageName, jarPath, typeId, srcDir);
		generator.generateKryoSerializer();
	}
}
