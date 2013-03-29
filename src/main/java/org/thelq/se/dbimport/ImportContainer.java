
package org.thelq.se.dbimport;

import javax.swing.JTable;
import javax.swing.JTextField;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.thelq.se.dbimport.sources.DumpContainer;

/**
 * A data class containing everything needed for importing a dumpContainer
 * @author Leon
 */
@Data
public class ImportContainer {
	protected DumpContainer dumpContainer;
	protected DumpParser parser;
	protected DatabaseWriter databaseWriter;
	protected SessionFactory sessionFactory;
	protected ServiceRegistry serviceRegistry;
	protected Configuration hibernateConfiguration;
	protected JTable guiTable;
	protected JTextField guiTablePrefix;
	protected String tablePrefix;
}
