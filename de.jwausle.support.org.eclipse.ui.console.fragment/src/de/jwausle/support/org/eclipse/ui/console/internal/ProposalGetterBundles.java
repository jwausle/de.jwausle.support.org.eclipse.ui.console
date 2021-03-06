package de.jwausle.support.org.eclipse.ui.console.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.BundleTracker;

public class ProposalGetterBundles implements ProposalGetter {
	private final Logger log = Logger.getLogger(ProposalGetterBundles.class);
	private Map<String, String> bundleMap = Collections
			.synchronizedMap(new LinkedHashMap<String, String>());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ProposalGetterBundles(BundleContext bundleContext) {
		BundleContext context = FrameworkUtil.getBundle(
				ProposalGetterBundles.class).getBundleContext();
		new BundleTracker(context, Bundle.RESOLVED, null) {

			@Override
			public Object addingBundle(Bundle bundle, BundleEvent event) {
				Object addingBundle = super.addingBundle(bundle, event);
				Dictionary<String, String> headers = bundle.getHeaders();

				String headersString = ProposalGetterBundles.toString(headers);
				bundleMap.put(bundle.getSymbolicName(), headersString);

				return addingBundle;
			}

			@Override
			public void remove(Bundle bundle) {
				bundleMap.remove(bundle.getSymbolicName());
			}

		}.open();
	}

	public static String toString(Dictionary<String, String> headers) {
		StringBuffer buffer = new StringBuffer();
		Enumeration<String> keys = headers.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = headers.get(key);
			buffer.append(key + " : " + value + "\n");
		}
		return buffer.toString();
	}

	public ICompletionProposal[] getCompletionProposal(String filter,
			CommandWriteCallback writer) {
		ICompletionProposal[] _return;
		if (filter == null)
			_return = QuickAssistant.newICompletionProposals(bundleMap,
					writer);
		else if (filter.trim().isEmpty())
			_return = QuickAssistant.newICompletionProposals(bundleMap,
					writer);
		else {
			Map<String, String> map = new LinkedHashMap<String, String>();
			Set<Entry<String, String>> entrySet = bundleMap.entrySet();
			for (Entry<String, String> entry : entrySet) {
				boolean contains = entry.getKey().startsWith(filter.trim());
				if (!contains)
					continue;

				map.put(entry.getKey(), entry.getValue());
			}
			log.info("===> filtered map: " + map.keySet());
			_return = QuickAssistant
					.newICompletionProposals(map, writer);
		}
		log.info("===> callback bundle-proposals: %s/%s for filter=´%s´\n", _return.length,bundleMap.size(), filter.trim());
		return _return;
	}

}
