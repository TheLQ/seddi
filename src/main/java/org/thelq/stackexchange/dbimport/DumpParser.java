/**
 * Copyright (C) 2013 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Unified StackExchange Data Dump Importer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, softwar
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thelq.stackexchange.dbimport;

import com.ctc.wstx.cfg.ErrorConsts;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.hibernate.type.Type;
import org.thelq.stackexchange.dbimport.sources.DumpEntry;

/**
 *
 * @author Leon
 */
@Slf4j
public class DumpParser {
	protected static int BATCH_SIZE = 5000;
	public static final XMLInputFactory2 XML_FACTORY;

	static {
		XML_FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();
		XML_FACTORY.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		XML_FACTORY.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		XML_FACTORY.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		XML_FACTORY.configureForSpeed();
	}
	protected XMLStreamReader2 xmlReader;
	@Getter
	protected String root;
	@Getter
	protected int parsedCount;
	@Getter
	protected boolean endOfFile = false;
	@Setter
	protected Map<String, Type> properties;
	protected SimpleDateFormat dateFormatterLong = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected SimpleDateFormat dateFormatterShort = new SimpleDateFormat("yyyy-MM-dd");
	@Getter
	@Setter
	protected boolean enabled = true;
	protected final DumpEntry dumpEntry;
	@Setter
	protected DatabaseWriter databaseWriter;

	public DumpParser(DumpEntry dumpEntry) {
		try {
			this.dumpEntry = dumpEntry;

			//Initialize the reader
			this.xmlReader = (XMLStreamReader2) XML_FACTORY.createXMLStreamReader(dumpEntry.getInput());

			//Get root element
			xmlReader.next();
			this.root = xmlReader.getLocalName();
		} catch (XMLStreamException ex) {
			throw new RuntimeException("Cannot initially load file " + dumpEntry.getLocation(), ex);
		}
	}

	public void parseNextEntry() {
		try {
			if (!enabled)
				throw new RuntimeException("Parser is disabled");
			else if (endOfFile)
				throw new RuntimeException("Reached end of file, cannot continue");
			else if (parsedCount == 0)
				log.info("Starting parsing {}", dumpEntry.getName());
			int eventType = xmlReader.nextTag();
			String curElement = xmlReader.getName().toString();
			//System.out.println("Current element: " + curElement);
			if (curElement.equals(getRoot())) {
				//Were done, shutdown this parser
				endOfFile = true;
				log.info("Done parsing {}, parsed {} enteries", dumpEntry.getName(), parsedCount);
				return;
			} else if (eventType != XMLEvent.START_ELEMENT)
				throw new RuntimeException("Unexpected event " + ErrorConsts.tokenTypeDesc(eventType));

			//Build attributes map
			log.debug("Parsing entry {}", parsedCount);
			Map<String, Object> attributesMap = ArrayMap.create(xmlReader.getAttributeCount());
			for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
				String attributeName = xmlReader.getAttributeLocalName(i);
				Type attributeType = properties.get(attributeName);
				if (attributeType == null)
					throw new RuntimeException("Unknown column " + attributeName + " at " + xmlReader.getLocation());
				Class attributeTypeClass = attributeType.getReturnedClass();
				String attributeValueRaw = xmlReader.getAttributeValue(i);

				//Attempt to convert to number if nessesary
				Object attributeValue;
				if (attributeTypeClass == Date.class) {
					log.debug("Converting {} to a date", attributeValueRaw);
					if (attributeValueRaw.length() < 11)
						attributeValue = dateFormatterShort.parse(attributeValueRaw);
					else
						attributeValue = dateFormatterLong.parse(attributeValueRaw);
				} else if (attributeTypeClass == Byte.class) {
					log.debug("Converting {} to a byte", attributeValueRaw);
					attributeValue = Byte.parseByte(attributeValueRaw);
				} else if (attributeTypeClass != String.class) {
					log.debug("Converting {} to class {}", attributeValueRaw, attributeTypeClass);
					if (attributeValueRaw.isEmpty())
						attributeValue = 0;
					else
						attributeValue = NumberUtils.createNumber(attributeValueRaw);
				} else
					attributeValue = attributeValueRaw;

				attributesMap.put(attributeName, attributeValue);
			}

			//Advance to END_ELEMENT, skipping the attributes and other stuff
			while (xmlReader.next() != XMLEvent.END_ELEMENT) {
			}

			parsedCount++;
			databaseWriter.insertData(attributesMap);
		} catch (Exception e) {
			throw new RuntimeException("Cannot parse entry in " + dumpEntry.getLocation()
					+ " #" + (parsedCount + 1) + " at " + xmlReader.getLocation(), e);
		}
	}

	public void close() {
		try {
			xmlReader.close();
			xmlReader.closeCompletely();
		} catch (Exception e) {
			throw new RuntimeException("Cannot close xmlReader for " + getRoot(), e);
		}
	}
}
