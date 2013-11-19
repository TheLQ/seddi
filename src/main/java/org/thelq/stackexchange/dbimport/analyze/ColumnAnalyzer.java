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
package org.thelq.stackexchange.dbimport.analyze;

import com.ctc.wstx.cfg.ErrorConsts;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.thelq.stackexchange.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Slf4j
public class ColumnAnalyzer {
	protected static void analyze(String path, InputStream input) throws XMLStreamException {
		log.info("Started parsing file " + path);
		Map<String, ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		int maxColumnLength = 0;
		//Preconditions.checkArgument(archive.isFile(), "Must specify a file");
		//Preconditions.checkArgument(archive.getName().endsWith(".7z"), "Must specify an archive");

		XMLStreamReader2 xmlReader = (XMLStreamReader2) DumpParser.XML_FACTORY.createXMLStreamReader(input);

		//Get root element
		xmlReader.next();
		String root = xmlReader.getLocalName();

		int parsedCount = 0;
		while (true) {
			int eventType = xmlReader.nextTag();
			String curElement = xmlReader.getName().toString();
			if (curElement.equals(root)) {
				//Done, print info
				StringBuilder logBuilder = new StringBuilder();
				logBuilder.append("Finished parsing ")
						.append(parsedCount)
						.append(" rows")
						.append(SystemUtils.LINE_SEPARATOR);
				for (Map.Entry<String, ColumnInfo> curColumnInfoEntry : columnInfoMap.entrySet())
					logBuilder.append("   ")
							.append(StringUtils.rightPad(curColumnInfoEntry.getKey(), maxColumnLength + 2))
							.append(StringUtils.rightPad(curColumnInfoEntry.getValue().type.toString(), 9))
							.append(curColumnInfoEntry.getValue().maxSize)
							.append(SystemUtils.LINE_SEPARATOR);
				log.info(logBuilder.toString().trim());
				xmlReader.close();
				return;
			} else if (eventType != XMLEvent.START_ELEMENT)
				throw new RuntimeException("Unexpected event " + ErrorConsts.tokenTypeDesc(eventType));

			for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
				String columnName = xmlReader.getAttributeLocalName(i);
				String attributeValueRaw = xmlReader.getAttributeValue(i);

				//Load columnInfo
				ColumnInfo columnInfo = columnInfoMap.get(columnName);
				if (columnInfo == null) {
					columnInfoMap.put(columnName, columnInfo = new ColumnInfo());
					if (columnName.length() > maxColumnLength)
						maxColumnLength = columnName.length();
				}

				if (columnInfo.type == ColumnType.INTEGER)
					try {
						int attributeValueInt = Integer.parseInt(attributeValueRaw);
						if (columnInfo.maxSize < attributeValueInt)
							columnInfo.maxSize = attributeValueInt;
					} catch (NumberFormatException e) {
						//Nope, its a string
						columnInfo.type = ColumnType.STRING;
					}
				if (columnInfo.type == ColumnType.STRING) {
					int attributeValueLength = attributeValueRaw.length();
					if (columnInfo.maxSize < attributeValueLength)
						columnInfo.maxSize = attributeValueLength;
				}
			}

			//Advance to END_ELEMENT, skipping the attributes and other stuff
			while (xmlReader.next() != XMLEvent.END_ELEMENT) {
			}

			parsedCount++;
		}
	}

	protected static class ColumnInfo {
		protected ColumnType type = ColumnType.INTEGER;
		protected int maxSize = 0;

		@Override
		public String toString() {
			return type + "\t" + maxSize;
		}
	}

	protected static enum ColumnType {
		STRING,
		INTEGER
	}

	public static void main(String[] args) throws IOException, XMLStreamException {
		String archivePath = "C:\\Users\\Leon\\Downloads\\Stack Exchange Data Dump - Jun 2013\\Content\\android.stackexchange.com.7z";
		final SevenZFile archive = new SevenZFile(new File(archivePath));
		InputStream archiveWrappedInputStream = new InputStream() {
			@Override
			public int read() throws IOException {
				return archive.read();
			}

			@Override
			public int read(byte[] b) throws IOException {
				return archive.read(b);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return archive.read(b, off, len);
			}
		};
		for (SevenZArchiveEntry curEntry = archive.getNextEntry(); (curEntry = archive.getNextEntry()) != null;)
			if (curEntry.getName().endsWith(".xml"))
				analyze(archivePath + "\\" + curEntry.getName(), archiveWrappedInputStream);
	}
}
