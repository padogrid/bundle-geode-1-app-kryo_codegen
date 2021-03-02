package org.apache.geode.addon.demo.kryo;

import com.netcrest.padogrid.tools.WrapperGenerator;

/**
 * This class shows you how to run WrapperGenerator programmatically.
 * 
 * @author dpark
 *
 */
public class MyWrapperGenerator {

	public static void main(String[] args) throws Exception {

		// All paths are relative to the working directory.	
		String sp = "org.apache.geode.demo.nw.data.avro.generated";
		String tp = " org.apache.geode.demo.nw.data.avro";
		String dir = "src/main/java";
		String jar = "lib/app-kryo-codegen-geode-1.0.0.jar";
		boolean overwrite = false;
		
		WrapperGenerator generator = new WrapperGenerator(sp, tp, jar, dir, WrapperGenerator.WrapperType.simple, overwrite);
		generator.generateWrappers();
	}
}
