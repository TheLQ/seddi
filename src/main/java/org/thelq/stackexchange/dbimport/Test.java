/**
 * Copyright (C) 2013 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Unified StackExchange Data Dump Importer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, softwar
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thelq.stackexchange.dbimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.thelq.stackexchange.dbimport.sources.ArchiveDumpContainer;

/**
 *
 * @author lordquackstar
 */
@Slf4j
public class Test {
	public static void main(String[] args) throws FileNotFoundException, InterruptedException, IOException {
		DatabaseWriter.setUsername("root");
		DatabaseWriter.setPassword("password");
		DatabaseWriter.setDialect("org.hibernate.dialect.MySQL5Dialect");
		DatabaseWriter.setDriver("com.mysql.jdbc.Driver");
		DatabaseWriter.setJdbcString("jdbc:mysql://127.0.0.1:3306/stackexchange?rewriteBatchedStatements=true");
		DatabaseWriter.setBatchSize(5000);
		DatabaseWriter.setGlobalPrefix("");

		//System.out.println("Pausing for debugger on " +  ManagementFactory.getRuntimeMXBean().getName());
		//new Scanner(System.in).nextLine();

		Controller controller = new Controller(false);
		File archive = new File("C:\\Users\\Leon\\Downloads\\Stack Exchange Data Dump - Jun 2013\\Content\\serverfault.com.7z");
		controller.addDumpContainer(new ArchiveDumpContainer(controller, archive))
				.setTablePrefix("serverfault_");
		controller.importAll(3, true);
		Utils.shutdownPool(controller.getGeneralThreadPool(), "general pool");
		log.info("--- End ---");
	}
}
