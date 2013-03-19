/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.se.dbimport;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;

/**
 *
 * @author Leon
 */
@Slf4j
public class Benchmark {
	public static void main(String[] args) {
		File file = new File("C:/Users/Leon/Downloads/Stack Exchange Data Dump - Aug 2012/Meta.SO/posthistory.xml");
		DumpParser parser = new DumpParser(file);
		parser.setDbWriter(new MockDatabaseWriter());
		Map<String, Type> properties = new HashMap();
		properties.put("postHistoryTypeId", new MockType(Byte.class));
		properties.put("postId", new MockType(Integer.class));
		properties.put("revisionGuid", new MockType(String.class));
		properties.put("creationDate", new MockType(Date.class));
		properties.put("userId", new MockType(Integer.class));
		properties.put("userDisplayName", new MockType(String.class));
		properties.put("comment", new MockType(String.class));
		properties.put("text", new MockType(String.class));
		parser.setProperties(properties);
		StopWatch timer = new StopWatch();
		timer.start();
		while (!parser.isEndOfFile())
			parser.parseNextEntry();
		timer.stop();
		log.info("Parse time: " + timer.toString());
	}

	protected static class MockDatabaseWriter extends DatabaseWriter {
		@Override
		public void insertData(Map<String, Object> data) throws Exception {
		}

		@Override
		public void close() {
		}
	}

	@Getter
	@RequiredArgsConstructor
	protected static class MockType implements Type {
		protected final Class returnedClass;

		public boolean isAssociationType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isCollectionType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isEntityType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isAnyType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isComponentType() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getColumnSpan(Mapping mapping) throws MappingException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int[] sqlTypes(Mapping mapping) throws MappingException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Size[] dictatedSizes(Mapping mapping) throws MappingException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Size[] defaultSizes(Mapping mapping) throws MappingException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isXMLElement() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isSame(Object x, Object y) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isEqual(Object x, Object y) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getHashCode(Object x) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getHashCode(Object x, SessionFactoryImplementor factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int compare(Object x, Object y) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isDirty(Object old, Object current, SessionImplementor session) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isDirty(Object oldState, Object currentState, boolean[] checkable, SessionImplementor session) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isModified(Object dbState, Object currentState, boolean[] checkable, SessionImplementor session) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isMutable() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Serializable disassemble(Object value, SessionImplementor session, Object owner) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void beforeAssemble(Serializable cached, SessionImplementor session) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object semiResolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Type getSemiResolvedType(SessionFactoryImplementor factory) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) throws HibernateException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean[] toColumnNullness(Object value, Mapping mapping) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
}
