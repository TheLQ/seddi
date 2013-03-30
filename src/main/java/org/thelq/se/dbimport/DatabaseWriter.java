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
	@Setter
	protected static String globalPrefix;
	@Setter
	protected static int batchSize;
	protected int count = 0;
	protected final String table;
	protected final ImportContainer container;

	public static void buildSessionFactory(ImportContainer container) throws HibernateException {
		container.setHibernateConfiguration(new Configuration());
		container.getHibernateConfiguration().configure();
		container.getHibernateConfiguration().setProperty("hibernate.connection.username", username);
		container.getHibernateConfiguration().setProperty("hibernate.connection.password", password);
		container.getHibernateConfiguration().setProperty("hibernate.connection.url", jdbcString);
		container.getHibernateConfiguration().setProperty("hibernate.connection.driver_class", driver);
		container.getHibernateConfiguration().setProperty("hibernate.dialect", dialect);
		container.getHibernateConfiguration().setProperty("hibernate.jdbc.batch_size", Integer.toString(batchSize));
		container.getHibernateConfiguration().setNamingStrategy(new PrefixNamingStrategy(globalPrefix + container.getTablePrefix()));
		container.setServiceRegistry(new ServiceRegistryBuilder().applySettings(container.getHibernateConfiguration().getProperties())
				.buildServiceRegistry());
		container.setSessionFactory(container.getHibernateConfiguration().buildSessionFactory(container.getServiceRegistry()));

		//Make a test connection so we know if this actually works
		Session testSession = container.getSessionFactory().openSession();
		testSession.beginTransaction();
		testSession.close();
		log.info("Database ready for " + Utils.getLongLocation(container));
	}
	protected Session session;

	public DatabaseWriter(ImportContainer container, String table) {
		this.table = table;
		this.container = container;
		session = container.getSessionFactory().openSession();
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
			log.info("Current data: " + data.toString());
			session.getTransaction().rollback();
			throw e;
		}
	}

	public void createTables() {
		SchemaExport exporter = new SchemaExport(container.getServiceRegistry(), container.getHibernateConfiguration());
		exporter.setHaltOnError(true);
		exporter.create(false, true);
		log.info("Finished creating tables for " + Utils.getLongLocation(container));
	}

	public void close() {
		//Make sure the session is fully done
		session.flush();
		session.clear();
		session.getTransaction().commit();
		session.close();
	}
}
