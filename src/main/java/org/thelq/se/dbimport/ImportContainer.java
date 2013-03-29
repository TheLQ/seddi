package org.thelq.se.dbimport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JTable;
import javax.swing.JTextField;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.thelq.se.dbimport.sources.DumpContainer;
import org.thelq.se.dbimport.sources.DumpEntry;

/**
 * A data class containing everything needed for importing a dumpContainer
 * @author Leon
 */
@Data
public class ImportContainer {
	protected DumpContainer dumpContainer;
	protected final Map<DumpEntry, DumpParser> parserMap = new ConcurrentHashMap();
	protected final Map<DumpEntry, DatabaseWriter> databaseWriterMap = new ConcurrentHashMap();
	protected SessionFactory sessionFactory;
	protected ServiceRegistry serviceRegistry;
	protected Configuration hibernateConfiguration;
	protected JTable guiTable;
	protected JTextField guiTablePrefix;
	protected String tablePrefix;
}
