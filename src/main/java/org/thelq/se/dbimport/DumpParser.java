package org.thelq.se.dbimport;

import com.ctc.wstx.cfg.ErrorConsts;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import lombok.Getter;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 *
 * @author Leon
 */
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
	protected File file;
	@Getter
	protected String root;
	protected int parsedCount;
	@Getter
	protected boolean endOfFile = false;

	public DumpParser(File file) throws XMLStreamException {
		this.file = file;

		//Initialize the reader
		this.xmlReader = (XMLStreamReader2) xmlFactory.createXMLStreamReader(file);

		//Get root element
		xmlReader.next();
		this.root = xmlReader.getLocalName();
	}

	public List<Map<String, String>> parseNextBatch(List<String> properties) {
		List<Map<String, String>> entries = new ArrayList();
		for (int i = 0; i < BATCH_SIZE; i++) {
			Map<String, String> newEntry = parseNextEntry(properties);
			if (newEntry == null)
				//Were done
				break;
			entries.add(newEntry);
		}
		return entries;
	}

	public Map<String, String> parseNextEntry(List<String> properties) {
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
			Map<String, String> attributesMap = new HashMap();
			for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
				String normalName = Utils.getCaseInsensitive(properties, xmlReader.getAttributeLocalName(i));
				attributesMap.put(normalName, xmlReader.getAttributeValue(i));
			}

			//Advance to END_ELEMENT, skipping the attributes and other stuff
			while (xmlReader.next() != XMLEvent.END_ELEMENT) {
			}

			parsedCount++;
			return attributesMap;
		} catch (Exception e) {
			throw new RuntimeException("Cannot parse entry #" + (parsedCount + 1), e);
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
