package org.thelq.se.dbimport;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 *
 * @author Leon
 */
@Slf4j
public class DatabaseWriter {
	@Getter
	protected static SessionFactory sessionFactory;
	protected static Configuration configuration;
	protected static ServiceRegistry serviceRegistry;

	protected static void init() throws HibernateException {
		configuration = new Configuration();
		configuration.configure();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}
	protected StatelessSession session;

	public DatabaseWriter() {
		session = sessionFactory.openStatelessSession();
		//session.setCacheMode(CacheMode.IGNORE);
		//session.setFlushMode(FlushMode.MANUAL);
	}

	public void insertData(String table, List<Map<String, String>> data) throws Exception {
		Transaction tx = session.beginTransaction();
		try {
			for (Map<String, String> curDataEntry : data)
				session.insert(table, curDataEntry);
			//session.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		}
	}
	
	public static void createTables() {
		SchemaExport exporter = new SchemaExport(serviceRegistry, configuration);
		exporter.setHaltOnError(true);
		log.debug("----- BEGIN CREATE -----");
		exporter.create(false, true);
	}

	public void close() {
		session.close();
	}
}
