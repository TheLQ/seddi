package org.thelq.se.dbimport.gui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author Leon
 */
@Slf4j
public class GUILogAppender extends AppenderBase<ILoggingEvent> {
	protected final GUI gui;
	protected boolean inited = false;
	protected final LinkedList<ILoggingEvent> initMessageQueue = new LinkedList<ILoggingEvent>();
	protected JTextPane loggerText;
	protected StyledDocument loggerStyle;
	protected SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss a");
	protected PatternLayout messageLayout;

	public GUILogAppender(GUI gui) {
		this.gui = gui;

		//Grab the context from root logger
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		setContext(rootLogger.getLoggerContext());

		messageLayout = new PatternLayout();
		messageLayout.setContext(getContext());
		messageLayout.setPattern("%logger{36} %message%n");
		messageLayout.start();

		//Init
		start();
		rootLogger.addAppender(this);
		log.debug("Added GUILogAppender, waiting for init");
	}

	public void init() {
		//Configure logger
		loggerText = gui.getLoggerText();
		loggerStyle = loggerText.getStyledDocument();
		loggerStyle.addStyle("Normal", null);
		StyleConstants.setForeground(loggerStyle.addStyle("Class", null), Color.blue);
		StyleConstants.setForeground(loggerStyle.addStyle("Error", null), Color.red);
		StyleConstants.setItalic(loggerStyle.addStyle("Thread", null), true);
		StyleConstants.setItalic(loggerStyle.addStyle("Level", null), true);

		log.debug("Inited GUILogAppender, processing any saved messages");
		synchronized (initMessageQueue) {
			inited = true;
			while (!initMessageQueue.isEmpty())
				append(initMessageQueue.poll());
		}
	}

	@Override
	protected void append(final ILoggingEvent event) {
		if (!inited)
			synchronized (initMessageQueue) {
				if (!inited) {
					initMessageQueue.add(event);
					return;
				}
			}
		//Color the message properly
		final Style msgStyle;
		if (event.getLevel().isGreaterOrEqual(Level.WARN))
			msgStyle = loggerStyle.getStyle("Error");
		else
			msgStyle = loggerStyle.getStyle("Normal");

		final String longContainer = MDC.get("longContainer");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					runInsert();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			protected void runInsert() throws BadLocationException {
				int prevLength = loggerStyle.getLength();
				String[] messageArray = StringUtils.split(messageLayout.doLayout(event).trim(), " ", 2);
				loggerStyle.insertString(loggerStyle.getLength(), "[" + dateFormatter.format(event.getTimeStamp()) + "]", loggerStyle.getStyle("Normal")); //time
				//doc.insertString(doc.getLength(), "["+event.getThreadName()+"] ", doc.getStyle("Thread")); //thread name
				if (StringUtils.isNotBlank(longContainer))
					loggerStyle.insertString(loggerStyle.getLength(), longContainer, loggerStyle.getStyle("Level")); //Container name
				loggerStyle.insertString(loggerStyle.getLength(), event.getLevel().toString() + " ", loggerStyle.getStyle("Level")); //Logging level
				loggerStyle.insertString(loggerStyle.getLength(), messageArray[0] + " ", loggerStyle.getStyle("Class"));
				loggerStyle.insertString(loggerStyle.getLength(), messageArray[1], msgStyle);
				loggerStyle.insertString(loggerStyle.getLength(), "\n", loggerStyle.getStyle("Normal"));

				//Only autoscroll if the scrollbar is at the bottom
				//JScrollBar scrollBar = scroll.getVerticalScrollBar();
				//if (scrollBar.getVisibleAmount() != scrollBar.getMaximum() && scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum())
				loggerText.setCaretPosition(prevLength);
			}
		});
	}
}
