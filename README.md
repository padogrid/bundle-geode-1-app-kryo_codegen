![PadoGrid](https://github.com/padogrid/padogrid/raw/develop/images/padogrid-3d-16x16.png) [*PadoGrid*](https://github.com/padogrid) | [*Catalogs*](https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md) | [*Manual*](https://github.com/padogrid/padogrid/wiki) | [*FAQ*](https://github.com/padogrid/padogrid/wiki/faq) | [*Releases*](https://github.com/padogrid/padogrid/releases) | [*Templates*](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates) | [*Pods*](https://github.com/padogrid/padogrid/wiki/Understanding-Padogrid-Pods) | [*Kubernetes*](https://github.com/padogrid/padogrid/wiki/Kubernetes) | [*Docker*](https://github.com/padogrid/padogrid/wiki/Docker) | [*Apps*](https://github.com/padogrid/padogrid/wiki/Apps) | [*Quick Start*](https://github.com/padogrid/padogrid/wiki/Quick-Start)

---

<!-- Platforms -->
[![PadoGrid 1.x](https://github.com/padogrid/padogrid/wiki/images/padogrid-padogrid-1.x.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-PadoGrid-1.x) [![Host OS](https://github.com/padogrid/padogrid/wiki/images/padogrid-host-os.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-Host-OS) [![VM](https://github.com/padogrid/padogrid/wiki/images/padogrid-vm.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-VM) [![Docker](https://github.com/padogrid/padogrid/wiki/images/padogrid-docker.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-Docker) [![Kubernetes](https://github.com/padogrid/padogrid/wiki/images/padogrid-kubernetes.drawio.svg)](https://github.com/padogrid/padogrid/wiki/Platform-Kubernetes)

# Geode/GemFire Kryo Code Generator

This bundle provides step-by-step instructions for generating and deploying Avro and `KryoSerializer` in Geode/GemFire. Using PadoGrid's code generator, you can on the fly generate and deploy Avro wrapper classes and the corresponding Kryo serializer.

## Installing Bundle

```bash
install_bundle -download bundle-geode-1-app-kryo_codegen
```

## Use Case

Kryo allows you to ingest POJO objects to Geode/GemFire without adding any Geode/GemFire dependencies. In this bundle, we introduce PadoGrid's code generator that generates `KryoSerializer` required by Geode/GemFire for registering custom serializers. To register POJO classes in Geode/GemFire, you must register each POJO class individually, making the Geode/GemFire configuration process difficult and error prone. PadoGrid's Kryo code generator simplifies the registration process by generating the `KryoSerializer` class that automatically groups all the POJO classes in a given package. Instead of registering each POJO class individually, you would just register `KryoSerializer`. 

The Kryo code generator also includes a wrapper class generator which extends POJO classes to allow you to override class methods as needed. This is particularly useful if you generate domain classes using IDL-based serialization tools such as Avro. Avro creates a compact binary data format that is ideal for storing POJO objects in Geode/GemFire. Avro, however, only supports primitive types and leaves the chore of marshalling and unmarshalling non-primitive types to the application. To close this gap, the wrapper class generator is provided to generate wrapper classes in which you can add your custom marshalling and unmarshalling code without affecting the Avro-generated code. 

For example, Avro does not support the `Date` class. To store a `Date` object, you would need to store its `long` value, i.e., `Date.getTime()` as the `long` type and the `logicalType` of `timestamp-millis` as follows.

```json
{
 "fields": [

     {"name": "orderDateLong", "type": "long", "logicalType": "timestamp-millis"}

 ]
}
```

To get the `Date` object back, the application must explicitly create a `Date` object, i.e., `new Date(long)`. With the wrapper class, this can be done by adding a method that returns the `Date` object. For example,

```java
public class Order extends org.apache.geode.demo.nw.data.avro.generated.__Order {
	public Order() {
		super();
	}

	public void setOrderDate(Date date) {
		super.setOrderDateLong(date.getTime());
	}

	public Date getOrderDate() {
		return new Date(super.getOrderDateLong());
	}
...
}
```

![Kryo Code Generator Demo](images/geode-app-kryo-codegen.jpg)

## Required Software

- Maven 3.x
- Geode 1.x or GemFire 9.x/10.x

## Configuring Bundle Environment

Make sure you have all the required products installed.

```bash
# To use Geode:
install_padogrid -product geode
update_padogrid -product geode

# To use GemFire:
# GemFire must be downloaded manually from the their website.
# The install_padogrid usage provides the download link.
install_padogrid -?
# Upon download, install it in $PADOGRID_ENV_BASE_PATH/products directory.
# For example, the following installs GemFire 10.1.0.
tar -C $PADOGRID_ENV_BASE_PATH/products -xzf ~/Downloads/vmware-gemfire-10.1.0.tgz
# Update the workspace environment with the installed GemFire version.
update_padogrid -product gemfire
```

        

## Running This Bundle

First, make sure you are switched into a Geode/GemFire cluster. You can create the default cluster by executing the following.

Geode:

```bash
# Geode cluster - creates 'mygeode' cluster
create_cluster -product geode
switch_cluster mygeode
```

GemFire:

```bash
# GemFire cluster - creates 'mygemfire' cluster
create_cluster -product gemfire
switch_cluster mygemfire
``` 

✏️  If you want to quickly test the bundle, you can jump to [Step 9](#9-configure-geodegemfire-configuration-file-cachexml-with-the-kryoserializer-class). It is recommended, however, that you go through the entire steps to get familiar with the code generation and deployment process.

### 1. Place Avro schema files in the `src/main/resources` directory

This bundle includes the following example schema files.

```bash
cd_app kryo_codegen
mkdir -p src/main/resources
cp etc/avro/* src/main/resources/
tree src/main
```

**Output:**

```console
src/main
└── resources
    ├── category.avsc
    ├── customer.avsc
    ├── employee.avsc
    └── order.avsc
```

Note that we do not have any Java code in the source directory. We start with a set of only Avro schema files and end up with all the necessary Java code for ingesting data into Geode/GemFire. The following shows the `customer.asvc` schema file contents.

**`customer.asvc`:**

```json
{
"namespace": "org.apache.geode.demo.nw.data.avro.generated",
 "type": "record",
 "name": "__Order",
 "fields": [
     {"name": "orderId", "type": "string"},
     {"name": "customerId", "type": "string"},
     {"name": "employeeId", "type": "string"},
     {"name": "orderDateLong", "type": "long", "logicalType": "timestamp-millis"},
     {"name": "requiredDateLong", "type": "long", "logicalType": "timestamp-millis"},
     {"name": "shippedDateLong", "type": "long", "logicalType": "timestamp-millis"},
     {"name": "shipVia", "type": "string"},
     {"name": "freight", "type": "double"},
     {"name": "shipName", "type": "string"},
     {"name": "shipAddress", "type": "string"},
     {"name": "shipCity", "type": "string"},
     {"name": "shipRegion", "type": ["string", "null"]},
     {"name": "shipPostalCode", "type": "string"},
     {"name": "shipCountry", "type": "string"}
 ]
}

```

### 2. Generate Avro classes using the Avro schema files

```bash
cd_app kryo_codegen
mvn package
```

The above Maven command generates the corresponding Java classes and packages them into the `app-kryo-codegen-geode-1.0.0.jar` file as follows.

```console
src/main
├── java
│   └── org
│       └── apache
│           └── geode
│               └── demo
│                   └── nw
│                       └── data
│                           └── avro
│                               └── generated
│                                   ├── __Category.java
│                                   ├── __Customer.java
│                                   ├── __Employee.java
│                                   └── __Order.java
└── resources
    ├── category.avsc
    ├── customer.avsc
    ├── employee.avsc
    └── order.avsc

lib
└── app-kryo-codegen-geode-1.0.0.jar
```

### 3. Generate Avro wrapper classes

Run the PadoGrid's `t_generate_wrappers` command to generate the wrapper classes that extend the generated Avro classes. You can use the Avro classes that were generated in the previous setp as they are but it is recommended that you generate the wrapper classes so that you can override the Avro class methods as needed. Let's generate wrapper classes by executing the following:

```bash
cd_app kryo_codegen
t_generate_wrappers -sp org.apache.geode.demo.nw.data.avro.generated \
   -tp org.apache.geode.demo.nw.data.avro \
   -dir src/main/java \
   -jar lib/app-kryo-codegen-geode-1.0.0.jar \
   -classpath lib
```

The above command creates the wrapper classes in the `org.apache.geode.demo.nw.data.avro` package as follows.

```console
src/main
├── java
│   └── org
│       └── apache
│           └── geode
│               └── demo
│                   └── nw
│                       └── data
│                           └── avro
│                               ├── Category.java
│                               ├── Customer.java
│                               ├── Employee.java
│                               ├── Order.java
│                               └── generated
│                                   ├── __Category.java
│                                   ├── __Customer.java
│                                   ├── __Employee.java
│                                   └── __Order.java
└── resources
    ├── category.avsc
    ├── customer.avsc
    ├── employee.avsc
    └── order.avsc
```

### 4. Compile the wrapper classes and create a jar file

The generated wrapper classes need to be compiled and packaged into a jar file. Run Maven again to generate the `lib/app-kryo-codegen-geode-1.0.0.jar` file that includes the wrapper classes.

```bash
cd_app kryo_codegen
mvn package
```

### 5. Generate KryoSerializer for the generated wrapper classes

With the wrapper classes in the jar file, we can now generate the `KyroSerializer` class that properly registers all the wrapper classes in Geode/GemFire. Execute the following command to generate `KryoSerializer`.

```bash
cd_app kryo_codegen
t_generate_kryo_serializer -id 1200 \
   -package org.apache.geode.demo.nw.data.avro \
   -dir src/main/java \
   -jar lib/app-kryo-codegen-geode-1.0.0.jar \
   -classpath lib
```

**Output:**

The above command outputs the following (Note the registration information. We'll be entering the ouputted serializer settings in the Geode/GemFire configuration file later.)

```console
  New class count: 4
Total class count: 4
[0] org.apache.geode.demo.nw.data.avro.Category
[1] org.apache.geode.demo.nw.data.avro.Customer
[2] org.apache.geode.demo.nw.data.avro.Employee
[3] org.apache.geode.demo.nw.data.avro.Order
KryoSerializer generated:
   C:\Users\dpark\Work\git\padogrid-bundles\geode-bundles\bundle-geode-1-app-kryo_codegen\src\main\java\org\apache\geode\demo\nw\data\avro\KryoSerializer.java

To register KryoSerializer, add the following lines in the Geode/GemFire configuration file.
    </serialization-registration>
         <serializer>
             <class-name>org.apache.geode.demo.nw.data.avro.KryoSerializer</class-name>
         </serializer>
    </serialization-registration>
```

The source directory now has `KryoSerializer.java` as shown below.


```console
src/main
├── java
│   └── org
│       └── apache
│           └── geode
│               └── demo
│                   └── nw
│                       └── data
│                           └── avro
│                               ├── Category.java
│                               ├── Customer.java
│                               ├── Employee.java
│                               ├── KryoSerializer.java
│                               ├── Order.java
│                               └── generated
│                                   ├── __Category.java
│                                   ├── __Customer.java
│                                   ├── __Employee.java
│                                   └── __Order.java
└── resources
    ├── category.avsc
    ├── customer.avsc
    ├── employee.avsc
    └── order.avsc
```

### 6. Compile the generated `KryoSerializer`

Once again, repackage the `lib/app-kryo-codegen-geode-1.0.0.jar` file by running Maven. At this time, the jar file also includes the generated classes including `KryoSerializer` which we need to register with Geode/GemFire.

```bash
cd_app kryo_codegen
mvn package
```

### 7. Build client apps

This bundle includes data ingestion clients that use the generated wrapper classes to ingest data into Geode/GemFire. The source code is located in the `src_provided` directory. Let's copy it to the `src` directory and rebuild the jar file.

```bash
# Copy client code
cd_app kryo_codegen
cp -r src_provided/* src/
```

The `src_provided` directory has the following files.

```console
src_provided/
└── main
    └── java
        └── org
            └── apache
                └── geode
                    ├── addon
                    │   └── demo
                    │       └── kryo
                    │           ├── CustomerIngester.java
                    │           ├── MyKryoGenerator.java
                    │           ├── MyWrapperGenerator.java
                    │           └── OrderIngester.java
                    └── demo
                        └── nw
                            └── data
                                └── avro
                                    └── Order.java
```

Rebuild the package.

```bash
# Rebuild
cd_app kryo_codegen
mvn package
```

:information_source: Take a look at `src_provided/main/java/org/apache/geode/demo/nw/data/avro/Order.java`. It extends the Avro generated class `__Order` to include `Date` objects.

### 8. Build and deploy a distribution tarball

The  `lib/app-kryo-codegen-geode-1.0.0.jar` is now ready to be deployed to the Geode/GemFire cluster. Since we are using Kryo and Avro, we also need to deploy their jar files along with all the dependencies. The previous Maven build step also generated a tarball, `app-kryo-codegen-geode-1.0.0.tar.gz`, that contains all the jar files. We need to untar the tarball in the workspace's `plugins` directory so that the jar files can be picked up by all the apps and clusters running in the same workspace.

```bash
# Deploy the generated tarball in the workspace plugins directory.
cd_app kryo_codegen
tar -C $PADOGRID_WORKSPACE/plugins/ -xzf target/assembly/app-kryo-codegen-geode-1.0.0.tar.gz
```

:information_source: Note that you would also need to deploy the tarball to external apps that connect to your Geode/GemFire cluster. For example, to deploy it to Kafka Connect, untar it in the connector's plugin directory.

### 9. Configure Geode/GemFire configuration file (`cache.xml`) with the `KryoSerializer` class

If you have skipped Steps 1-8, then you can run the `build_app` script, which carries out the Steps 1 to 8 in sequence. 

```bash
cd_app kryo_codegen/bin_sh
./build_app
```

If you have a schema registry running, then you can use the `-registry` option to retrieve the schemas instead. Please see the usage by running the following.

```bash
./build_app -?
```

Once you have either executed `build_app` or carried out Steps 1-8 individually, place the serialization information in the current cluster's Geode/GemFire configuration file.

```bash
# Create a Geode/GemFire cluster if you have not done so already
create_cluster -product geode -cluster mygeode

# Switch to your cluster and edit cache.xml
switch_cluster mygeode
vi etc/cache.xml
```

Copy the serializer configuration output from Step 5 and enter it in the `cache.xml` file as follows.  

```xml
<cache>
...
    <serialization-registration>
        <serializer>
            <class-name>org.apache.geode.demo.nw.data.avro.KryoSerializer</class-name>
        </serializer>
    </serialization-registration>
...
</cache>
```

### 10. Start your cluster

```bash
start_cluster
```

### 11. Ingest data

The scripts for running the client code are in the `bin_sh` directory. The ingestion scripts use the `etc/client-cache.xml` file which already registers the `KyroSerializer` class. Let's execute them.

```bash
# Ingest Customer and Order data
cd_app kryo_codegen/bin_sh

# Ingest data into the '/nw/customers' region.
./ingest_customers

# Ingest data into the '/nw/orders' region.
./ingest_orders
```

**`ingest_customers` Output:**

```
Data Class: org.apache.geode.demo.nw.data.avro.Customer
  Ingested: 100
       Map: /nw/customers
```

**`ingest_orders` Output:**

```
Data Class: org.apache.geode.demo.nw.data.avro.Order
  Ingested: 100
       Map: /nw/orders
```

### 12. Read ingested data

You can use the `read_cache` script to read the ingested data as follows. We have ingested data into the 'nw/customers' and '/nw/orders' regions.

```bash
./read_cache /nw/customers
./read_cache /nw/orders
```

## Executing Code Generator Programmatically 

You can also execute the code generators programmatically. The source code of `MyWrapperGenerator` and `MyKryoGenerator` is located in the `src_provided/main/java` directory and shown below.

###  WapperGenerator

```java
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
```

### GeodeKryoGenerator

```java
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
```

## Teardown

```bash
stop_cluster -all
```

---

![PadoGrid](https://github.com/padogrid/padogrid/raw/develop/images/padogrid-3d-16x16.png) [*PadoGrid*](https://github.com/padogrid) | [*Catalogs*](https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md) | [*Manual*](https://github.com/padogrid/padogrid/wiki) | [*FAQ*](https://github.com/padogrid/padogrid/wiki/faq) | [*Releases*](https://github.com/padogrid/padogrid/releases) | [*Templates*](https://github.com/padogrid/padogrid/wiki/Using-Bundle-Templates) | [*Pods*](https://github.com/padogrid/padogrid/wiki/Understanding-Padogrid-Pods) | [*Kubernetes*](https://github.com/padogrid/padogrid/wiki/Kubernetes) | [*Docker*](https://github.com/padogrid/padogrid/wiki/Docker) | [*Apps*](https://github.com/padogrid/padogrid/wiki/Apps) | [*Quick Start*](https://github.com/padogrid/padogrid/wiki/Quick-Start)
