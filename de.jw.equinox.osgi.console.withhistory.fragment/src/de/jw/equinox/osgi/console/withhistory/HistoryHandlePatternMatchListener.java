package de.jw.equinox.osgi.console.withhistory;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public final class HistoryHandlePatternMatchListener implements
		IPatternMatchListener {
	private static final String OSGI_COMMAND_LINE_PATTERN = "osgi>.*\n[^\n]";
	private final HistoryHandle ioConsoleWithHistoryHandle;
	private IOConsole ioConsole;

	public HistoryHandlePatternMatchListener(
			IOConsole console, HistoryHandle ioConsoleWithHistoryHandle) {
		this.ioConsoleWithHistoryHandle = ioConsoleWithHistoryHandle;
		this.ioConsole= console;
	}

	public void matchFound(PatternMatchEvent event) {
		IDocument document = ioConsole.getDocument();
		
		String command = safeGetNextPatternMatchFromDocument(event, document);
		
		if ((command == null) || command.isEmpty()) {
			return;
		}
		if (!ioConsoleWithHistoryHandle.data
				.equalLastCommand(command)) {
			ioConsoleWithHistoryHandle.data.add(command);
		}
		ioConsoleWithHistoryHandle.data.clearSessionStack();
		System.out.println("enter:  " + ioConsoleWithHistoryHandle.data);
	}

	private String safeGetNextPatternMatchFromDocument(PatternMatchEvent event, IDocument document) {
		int lineOffset = event.getOffset();
		int lineLength = event.getLength();
		String command = "";
		try {
			command = document.get(lineOffset, lineLength);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		int indexOf = command.lastIndexOf("osgi>");
		if(indexOf == -1)
			return "";
		int indexOf2 = command.indexOf('\n', indexOf);
		if(indexOf2 == -1)
			return "";
		
		command = command.substring(indexOf + /*osgi>*/5, indexOf2);
		return command;
	}

	public void disconnect() {

	}

	public void connect(TextConsole console) {

	}

	public String getPattern() {
		return OSGI_COMMAND_LINE_PATTERN;
	}

	public String getLineQualifier() {
		return OSGI_COMMAND_LINE_PATTERN;
	}

	public int getCompilerFlags() {
		return 0;
	}
}