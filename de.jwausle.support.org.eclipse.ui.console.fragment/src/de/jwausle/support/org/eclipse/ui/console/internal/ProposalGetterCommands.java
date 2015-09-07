package de.jwausle.support.org.eclipse.ui.console.internal;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ProposalGetterCommands implements ProposalGetter {
	private Map<String, String> commandMap = new LinkedHashMap<String, String>();
	private CommandProcessor processor = null;

	public ProposalGetterCommands() {
		this(FrameworkUtil.getBundle(ProposalGetterCommands.class)
				.getBundleContext());
	}

	public ProposalGetterCommands(BundleContext bundleContext) {
		try {
			@SuppressWarnings("rawtypes")
			ServiceTracker serviceTracker = trackOSGiCommands(bundleContext);
			serviceTracker.open();

			@SuppressWarnings("rawtypes")
			ServiceTracker processorTracker = processorTracker(bundleContext);
			processorTracker.open();
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}

	public ICompletionProposal[] getCompletionProposal(String filter,
			CommandWriteCallback writer) {
		ICompletionProposal[] _return;
		if (filter == null)
			_return = QuickAssistant
					.newICompletionProposals(commandMap, writer);
		else if (filter.isEmpty())
			_return = QuickAssistant
					.newICompletionProposals(commandMap, writer);
		else {
			Set<Entry<String, String>> entrySet = commandMap.entrySet();
			Map<String, String> commandMap2 = new LinkedHashMap<String, String>();
			for (Entry<String, String> entry : entrySet) {
				String command = QuickAssistant.commandWithoutScope(entry
						.getKey());
				// System.err.println("===> " + command + "," + filter);
				if (!command.startsWith(filter))
					continue;

				commandMap2.put(entry.getKey(), entry.getValue());
			}
			System.err.println("===> filtered map: " + commandMap2.keySet());
			_return = QuickAssistant.newICompletionProposals(commandMap2,
					writer);
		}
		System.err.printf(
				"===> callback cmd-proposals: %s/%s for filter=´%s´\n",
				_return.length, commandMap.size(), filter.trim());
		return _return;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ServiceTracker trackOSGiCommands(final BundleContext context)
			throws InvalidSyntaxException {
		Filter filter = context.createFilter("(&("
				+ CommandProcessor.COMMAND_SCOPE + "=*)("
				+ CommandProcessor.COMMAND_FUNCTION + "=*))");

		return new ServiceTracker(context, filter, null) {
			@Override
			public Object addingService(ServiceReference reference) {
				Object scope = reference
						.getProperty(CommandProcessor.COMMAND_SCOPE);
				Object function = reference
						.getProperty(CommandProcessor.COMMAND_FUNCTION);
				Map<String, String> commands = new LinkedHashMap<String, String>();

				if (scope != null && function != null) {

					if (function.getClass().isArray()) {
						for (Object f : ((Object[]) function)) {
							String cmd = scope + ":" + f.toString();
							String help = getHelp(cmd);
						
							commands.put(cmd, help);
						}
					} else {
						String cmd = scope + ":" + function.toString();
						String help = getHelp(cmd);
						
						commands.put(cmd, help);
					}
					commandMap.putAll(commands);
					return commands;
				}
				return null;
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				Object scope = reference
						.getProperty(CommandProcessor.COMMAND_SCOPE);
				Object function = reference
						.getProperty(CommandProcessor.COMMAND_FUNCTION);

				if (scope != null && function != null) {
					if (!function.getClass().isArray()) {
						commandMap.remove(scope.toString() + ":"
								+ function.toString());
					} else {
						for (Object func : (Object[]) function) {
							commandMap.remove(scope.toString() + ":"
									+ func.toString());
						}
					}
				}

				super.removedService(reference, service);
			}
		};
	}

	private Thread thread;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ServiceTracker processorTracker(BundleContext context) {
		ServiceTracker t = new ServiceTracker(context,
				CommandProcessor.class.getName(), null) {

			@Override
			public Object addingService(ServiceReference reference) {
				ProposalGetterCommands.this.processor = (CommandProcessor) super
						.addingService(reference);

				helps(context, ProposalGetterCommands.this.processor);
				return ProposalGetterCommands.this.processor;
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				ProposalGetterCommands.this.processor = null;
				if (thread != null) {
					thread.interrupt();
				}
				super.removedService(reference, service);
			}
		};

		return t;
	}

	private void helps(BundleContext context, CommandProcessor processor) {
		Set<Entry<String, String>> entrySet = this.commandMap.entrySet();
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (Entry<String, String> entry : entrySet) {
			String help = null;
			help = help(processor, entry);
			map.put(entry.getKey(), help);
		}
		synchronized (this.commandMap) {
			this.commandMap.putAll(map);
		}

	}

	private String help(CommandProcessor processor, Entry<String, String> entry) {
		String key = entry.getKey();

		return help(processor, key);
	}

	private String help(CommandProcessor processor, String key) {
		String help;
		InputStream in = System.in;
		PrintStream err = System.err;

		Path file = null;
		try {
			file = Files.createTempFile("system", "out");
			PrintStream out = new PrintStream(file.toFile());

			CommandSession session = processor.createSession(in, out, err);
			session.execute("type " + key);
			out.append("\n===== Help ====\n");
			session.execute("help " + key);

			help = new String(Files.readAllBytes(file));

		} catch (Exception e) {
			help = e.getMessage();
		} finally {
			if (file != null) {
				file.toFile().delete();
			}
		}
		return help;
	}

	private String getHelp(String cmd) {
		if (this.processor == null)
			return "no help for " + cmd;
		return help(processor, cmd);
	}
}
