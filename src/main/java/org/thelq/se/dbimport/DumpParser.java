package org.thelq.se.dbimport;

import com.ctc.wstx.cfg.ErrorConsts;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

/**
 *
 * @author Leon
 */
@Slf4j
public class DumpParser {
	protected static int BATCH_SIZE = 5000;
	protected static XMLInputFactory2 xmlFactory;

	static {
		xmlFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
		xmlFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		xmlFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlFactory.configureForSpeed();
	}
	protected XMLStreamReader2 xmlReader;
	@Getter
	protected File file;
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
	protected List<String> errors = new ArrayList();

	public DumpParser(File file) throws XMLStreamException {
		this.file = file;

		//Initialize the reader
		this.xmlReader = (XMLStreamReader2) xmlFactory.createXMLStreamReader(file);

		//Get root element
		xmlReader.next();
		this.root = xmlReader.getLocalName();
	}

	public List<Map<String, Object>> parseNextBatch() {
		List<Map<String, Object>> entries = new ArrayList();
		for (int i = 0; i < BATCH_SIZE; i++) {
			Map<String, Object> newEntry = parseNextEntry();
			if (newEntry == null)
				//Were done
				break;
			entries.add(newEntry);
		}
		return entries;
	}

	public Map<String, Object> parseNextEntry() {
		try {
			int eventType = xmlReader.nextTag();
			String curElement = xmlReader.getName().toString();
			//System.out.println("Current element: " + curElement);
			if (curElement.equals(getRoot())) {
				//Were done, shutdown this parser
				System.out.println(parsedCount + " Recieved end");
				close();
				return null;
			} else if (eventType != XMLEvent.START_ELEMENT)
				throw new RuntimeException("Unexpected event " + ErrorConsts.tokenTypeDesc(eventType)
						+ " at " + xmlReader.getLocation().toString());

			//Build attributes map
			Map<String, Object> attributesMap = new HashMap();
			for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
				String normalName = Utils.getCaseInsensitive(properties.keySet(), xmlReader.getAttributeLocalName(i));
				Type attributeType = properties.get(normalName);
				if (attributeType == null) {
					errors.add("Unknown column " + xmlReader.getAttributeLocalName(i)
							+ " at " + xmlReader.getLocation().toString());
					continue;
				}
				Class attributeTypeClass = attributeType.getReturnedClass();
				String attributeValueRaw = xmlReader.getAttributeValue(i);

				//Attempt to convert to number if nessesary
				Object attributeValue;
				if (attributeTypeClass == Date.class) {
					log.debug("Attempting to parse " + attributeValueRaw + " as a date");
					if (attributeValueRaw.length() < 11)
						attributeValue = dateFormatterShort.parse(attributeValueRaw);
					else
						attributeValue = dateFormatterLong.parse(attributeValueRaw);
				} else if (attributeTypeClass == Byte.class) {
					log.debug("Converting " + attributeValueRaw + " to a byte");
					attributeValue = Byte.parseByte(attributeValueRaw);
				} else if (attributeTypeClass != String.class) {
					log.debug("Converting " + attributeValueRaw + " ( " + xmlReader.getAttributeLocalName(i) + " to class " + attributeTypeClass);
					if (attributeValueRaw.isEmpty())
						attributeValue = 0;
					else
						attributeValue = NumberUtils.createNumber(attributeValueRaw);
				} else
					attributeValue = attributeValueRaw;

				attributesMap.put(normalName, attributeValue);
			}

			//Advance to END_ELEMENT, skipping the attributes and other stuff
			while (xmlReader.next() != XMLEvent.END_ELEMENT) {
			}

			parsedCount++;
			return attributesMap;
		} catch (Exception e) {
			throw new RuntimeException("Cannot parse entry #" + (parsedCount + 1)
					+ " at " + xmlReader.getLocation(), e);
		}
	}

	protected void close() {
		try {
			endOfFile = true;
			xmlReader.close();
			xmlReader.closeCompletely();
		} catch (XMLStreamException e) {
			throw new RuntimeException("Cannot close xmlReader for " + getRoot(), e);
		}
	}
}
