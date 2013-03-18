package org.thelq.se.dbimport;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
	protected int count = 0;
	protected final int batchSize;
	protected final String table;

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
		log.info("Test connection successful, database is inited");
	}
	protected Session session;

	public DatabaseWriter(String table, int batchSize) {
		this.table = table;
		this.batchSize = batchSize;
		session = sessionFactory.openSession();
		session.setCacheMode(CacheMode.IGNORE);
		session.setFlushMode(FlushMode.MANUAL);
		session.beginTransaction();
	}

	public void insertData(Map<String, Object> data) throws Exception {
		try {
			session.save(table, data);
			count++;
			if (count % batchSize == 0) {
				session.flush();
				session.clear();
			}
		} catch (Exception e) {
			session.getTransaction().rollback();
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
		session.getTransaction().commit();
		session.close();
	}
}
