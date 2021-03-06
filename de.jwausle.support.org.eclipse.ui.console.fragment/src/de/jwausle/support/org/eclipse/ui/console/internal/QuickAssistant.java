package de.jwausle.support.org.eclipse.ui.console.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

public class QuickAssistant implements ProposalGetter {
	public static String commandWithoutScope(String key) {
		if (!key.contains(":"))
			return key;

		return key.substring(key.indexOf(':') + 1);
	}

	public static ICompletionProposal[] newICompletionProposals(
			Map<String, String> commandMap, final CommandWriteCallback writer) {
		List<ICompletionProposal> list = new LinkedList<ICompletionProposal>();
		Set<Entry<String, String>> entrySet = commandMap.entrySet();
		for (final Entry<String, String> entry : entrySet) {
			list.add(new QuickAssistantCompletionProposal(writer, entry));
		}
		Collections.sort(list, new Comparator<ICompletionProposal>() {

			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				String displayString = o1.getDisplayString();
				String displayString2 = o2.getDisplayString();
				return displayString.compareTo(displayString2);
			}
		});
		return list.toArray(new ICompletionProposal[list.size()]);
	}
	private boolean visible = false;
	
	private QuickAssistAssistant assistant = null;
	private ProposalGetter getter = null;
	private String filter = null;
	private CommandWriteCallback writer = null;

	public QuickAssistant() {
		assistant = new QuickAssistAssistant();
		IQuickAssistProcessor processor = new IQuickAssistProcessor() {

			public String getErrorMessage() {
				return "ERROR on "
						+ QuickAssistant.class.getSimpleName();
			}

			public ICompletionProposal[] computeQuickAssistProposals(
					IQuickAssistInvocationContext invocationContext) {
				QuickAssistant assistant = QuickAssistant.this;
				ICompletionProposal[] completionProposal = assistant
						.getCompletionProposal(filter, writer);
				return completionProposal;
			}

			public boolean canFix(Annotation annotation) {
				return false;
			}

			public boolean canAssist(
					IQuickAssistInvocationContext invocationContext) {
				return false;
			}
		};
		assistant.setQuickAssistProcessor(processor);
		AbstractReusableInformationControlCreator creator = new AbstractReusableInformationControlCreator() {

			protected IInformationControl doCreateInformationControl(
					Shell parent) {
				return new DefaultInformationControl(parent);
			}
		};
		assistant.setInformationControlCreator(creator);
	}

	public SourceViewerConfiguration get() {
		return new SourceViewerConfiguration() {

			@Override
			public IQuickAssistAssistant getQuickAssistAssistant(
					ISourceViewer sourceViewer) {
				return assistant;
			}
		};
	}

	public void show(ProposalGetter proposalGetter, String filter, CommandWriteCallback commandWriter) {
		this.filter = filter;
		this.getter = proposalGetter;
		this.writer = commandWriter;
		this.assistant.showPossibleQuickAssists();
		this.visible = true;
	}

	public ICompletionProposal[] getCompletionProposal(String filter,
			CommandWriteCallback writer) {
		return this.getter.getCompletionProposal(filter, writer);
	}

	public boolean isVisible() {
		return visible;
	}
	
	private void setVisible(boolean newVisible){
		this.visible = newVisible;
	}

	public void hide() {
		setVisible(false);
	}
}
