package org.thelq.stackexchange.dbimport;

import org.thelq.stackexchange.dbimport.sources.DumpContainer;
import java.util.Map;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
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
	protected final DumpContainer container;

	public static void buildSessionFactory(DumpContainer container) throws HibernateException {
		container.setHibernateConfiguration(new Configuration());
		container.getHibernateConfiguration().configure();
		container.getHibernateConfiguration().setProperty("hibernate.connection.username", username);
		container.getHibernateConfiguration().setProperty("hibernate.connection.password", password);
		container.getHibernateConfiguration().setProperty("hibernate.connection.url", jdbcString);
		container.getHibernateConfiguration().setProperty("hibernate.connection.driver_class", driver);
		container.getHibernateConfiguration().setProperty("hibernate.dialect", dialect);
		container.getHibernateConfiguration().setProperty("hibernate.jdbc.batch_size", Integer.toString(batchSize));
		container.getHibernateConfiguration().setNamingStrategy(new PrefixNamingStrategy(
				StringUtils.defaultString(globalPrefix) + StringUtils.defaultString(container.getTablePrefix())));
		container.setServiceRegistry(new StandardServiceRegistryBuilder()
				.applySettings(container.getHibernateConfiguration().getProperties())
				.build());
		container.setSessionFactory(container.getHibernateConfiguration().buildSessionFactory(container.getServiceRegistry()));

		//Make a test connection so we know if this actually works
		Session testSession = container.getSessionFactory().openSession();
		testSession.beginTransaction();
		testSession.close();
		log.info("Database ready for " + Utils.getLongLocation(container));
	}
	protected Session session;

	public DatabaseWriter(DumpContainer container, String table) {
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
			session.getTransaction().rollback();
			throw e;
		}
	}

	public void close() {
		//Make sure the session is fully done
		session.flush();
		session.clear();
		session.getTransaction().commit();
		session.close();
	}
	
	public static void createTables(DumpContainer container) {
		SchemaExport exporter = new SchemaExport(container.getServiceRegistry(), container.getHibernateConfiguration());
		exporter.setHaltOnError(true);
		exporter.create(false, true);
		log.info("Finished creating tables for " + Utils.getLongLocation(container));
	}
}
