/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.stackexchange.dbimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.SevenZipException;
import org.thelq.stackexchange.dbimport.sources.ArchiveDumpContainer;

/**
 *
 * @author lordquackstar
 */
@Slf4j
public class Test {
	public static void main(String[] args) throws FileNotFoundException, SevenZipException, InterruptedException, IOException {
		DatabaseWriter.setUsername("root");
		DatabaseWriter.setPassword("password");
		DatabaseWriter.setDialect("org.hibernate.dialect.MySQL5Dialect");
		DatabaseWriter.setDriver("com.mysql.jdbc.Driver");
		DatabaseWriter.setJdbcString("jdbc:mysql://127.0.0.1:3306/so_new?rewriteBatchedStatements=true");
		DatabaseWriter.setBatchSize(5000);
		DatabaseWriter.setGlobalPrefix("");

		//System.out.println("Pausing for debugger on " +  ManagementFactory.getRuntimeMXBean().getName());
		//new Scanner(System.in).nextLine();

		Controller controller = new Controller(false);
		File archive = new File("C:\\Users\\Leon\\Downloads\\Stack Exchange Data Dump - Aug 2012\\Content\\serverfault.com.7z");
		controller.addDumpContainer(new ArchiveDumpContainer(controller, archive))
				.setTablePrefix("serverfault_");
		controller.importAll(3, true);
		Utils.shutdownPool(controller.getGeneralThreadPool(), "general pool");
		log.info("--- End ---");
	}
}
