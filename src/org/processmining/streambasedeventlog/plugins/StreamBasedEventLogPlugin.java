package org.processmining.streambasedeventlog.plugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.eventstream.connections.XSEventXSAuthorXSStreamConnectionImpl;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.dialogs.XSEventStreamConnectionDialogImpl;
import org.processmining.eventstream.models.XSEventAuthor;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stream.connections.XSAuthorXSStreamConnectionImpl;
import org.processmining.streambasedeventlog.algorithms.TrieBasedEventCollectorImpl;
import org.processmining.streambasedeventlog.help.StreamBasedEventLogHelp;
import org.processmining.streambasedeventlog.models.EventPayload;
import org.processmining.streambasedeventlog.models.XSEventStreamToXLogReader;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventLogParametersImpl;
import org.processmining.streambasedeventlog.parameters.StreamBasedEventStorageParametersImpl;
import org.processmining.streambasedeventstorage.algorithms.XSEventStoreSlidingWindowImpl;
import org.processmining.streambasedeventstorage.parameters.XSEventStoreSlidingWindowParametersImpl;

@Plugin(name = "Store Event Stream as Event Log(s)", parameterLabels = { "Event Stream",
		"Parameters" }, returnLabels = { "Event Log Generator" }, returnTypes = {
				XSEventStreamToXLogReader.class }, help = StreamBasedEventLogHelp.TEXT)
public class StreamBasedEventLogPlugin {

	@PluginVariant(variantLabel = "Store Event Stream as Event Log(s), stream / parameters", requiredParameterLabels = {
			0, 1 })
	public XSEventStreamToXLogReader<?> run(PluginContext context, XSEventStream stream,
			StreamBasedEventLogParametersImpl parameters) {
		XSEventStreamToXLogReader<?> algorithm = null;
		switch (parameters.getStorageStrategy()) {
			case TRIE :
				XSEventStoreSlidingWindowParametersImpl swpar = new XSEventStoreSlidingWindowParametersImpl();
				swpar.setSize(parameters.getSlidingWindowSize());
				algorithm = new TrieBasedEventCollectorImpl<EventPayload, StreamBasedEventStorageParametersImpl>(
						parameters, new EventPayload.FactoryNaiveImpl(), new XSEventStoreSlidingWindowImpl("", swpar));
				break;
		}

		algorithm.start();
		stream.connect(algorithm);
		return algorithm;
	}

	@PluginVariant(variantLabel = "Store Event Stream as Event Log(s), stream", requiredParameterLabels = { 0 })
	public XSEventStreamToXLogReader<?> runDefault(PluginContext context, XSEventStream stream) {
		StreamBasedEventLogParametersImpl parameters = new StreamBasedEventLogParametersImpl();
		return run(context, stream, parameters);
	}

	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "Sebastiaan J. van Zelst", email = "s.j.v.zelst@tue.nl")
	@PluginVariant(variantLabel = "Store Event Stream as Event Log(s), UI", requiredParameterLabels = { 0 })
	public XSEventStreamToXLogReader<?> runUI(UIPluginContext context, XSEventStream stream) {
		return run(context, stream, new StreamBasedEventLogParametersImpl());
	}

	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "Sebastiaan J. van Zelst", email = "s.j.v.zelst@tue.nl")
	@PluginVariant(variantLabel = "Store Event Stream as Event Log(s), UI", requiredParameterLabels = { 0 })
	public XSEventStreamToXLogReader<?> runUI(UIPluginContext context, XSEventAuthor author) {
		List<XSEventStream> availableStreamsOfAuthor = new ArrayList<>();
		try {
			for (Connection c : context.getConnectionManager()
					.getConnections(XSEventXSAuthorXSStreamConnectionImpl.class, context, author)) {
				XSEventStream stream = c.getObjectWithRole(XSAuthorXSStreamConnectionImpl.KEY_STREAM);
				availableStreamsOfAuthor.add(stream);
			}
			if (!availableStreamsOfAuthor.isEmpty()) {
				XSEventStreamConnectionDialogImpl dialog = new XSEventStreamConnectionDialogImpl(
						availableStreamsOfAuthor);
				if (context.showWizard("Select Stream", true, true, dialog).equals(InteractionResult.FINISHED)) {
					return run(context, dialog.getSelectedStream(), new StreamBasedEventLogParametersImpl());
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		context.getFutureResult(0).cancel(true);
		return null;
	}
}
