package org.thelq.se.dbimport;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
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
	@Setter
	protected static String username;
	@Setter
	protected static String password;
	@Setter
	protected static String jdbcString;
	@Setter
	protected static String driver;
	@Setter
	protected static String dialect;

	public static void init() throws HibernateException {
		configuration = new Configuration();
		configuration.configure();
		configuration.setProperty("hibernate.connection.username", username);
		configuration.setProperty("hibernate.connection.password", password);
		configuration.setProperty("hibernate.connection.url", jdbcString);
		configuration.setProperty("hibernate.connection.driver_class", driver);
		configuration.setProperty("hibernate.dialect", dialect);
		serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);

		//Make a test connection so we know if this actually works
		Session testSession = sessionFactory.openSession();
		testSession.beginTransaction();
		testSession.close();
	}
	protected StatelessSession session;

	public DatabaseWriter() {
		session = sessionFactory.openStatelessSession();
		//session.setCacheMode(CacheMode.IGNORE);
		//session.setFlushMode(FlushMode.MANUAL);
	}

	public void insertData(String table, List<Map<String, Object>> data) throws Exception {
		Transaction tx = session.beginTransaction();
		try {
			for (Map<String, Object> curDataEntry : data)
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
