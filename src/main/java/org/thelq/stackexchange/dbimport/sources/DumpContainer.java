package org.thelq.stackexchange.dbimport.sources;

import com.google.common.collect.ImmutableList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * A data class containing everything needed for importing a dumpContainer
 * @author Leon
 */
@Data
public abstract class DumpContainer {
	protected SessionFactory sessionFactory;
	protected ServiceRegistry serviceRegistry;
	protected Configuration hibernateConfiguration;
	protected final Object hibernateCreateLock = new Object();
	protected JLabel guiHeader;
	protected JTextField guiTablePrefix;
	protected String tablePrefix;

	public abstract ImmutableList<DumpEntry> getEntries();
	
	public abstract String getLocation();

	public abstract String getName();

	public abstract String getType();
}
