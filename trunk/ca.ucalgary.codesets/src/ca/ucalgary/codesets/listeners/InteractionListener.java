package ca.ucalgary.codesets.listeners;

import java.util.ArrayList;
import java.util.HashMap;

//import org.eclipse.mylar.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.mylar.monitor.core.IInteractionEventListener;
import org.eclipse.mylar.monitor.core.InteractionEvent;
import org.eclipse.mylar.monitor.ui.MylarMonitorUiPlugin;
import org.eclipse.mylar.context.core.*;

public class InteractionListener implements IInteractionEventListener {

	private static HashMap<InteractionEvent.Kind,ArrayList<CodeSetListener>> listenerKinds;

	/**
	 * Creates a new InteractionListener.  The interaction Listener is like an event bus
	 * which allows several listeners to listen to different kinds of events
	 *
	 */
	public InteractionListener() {
		initialize();
		startMonitoring ();
	}

	/**
	 * Initializes the InteractionListeners listenerKinds Arrays 
	 *
	 */
	private void initialize () {
		listenerKinds = new HashMap<InteractionEvent.Kind, ArrayList<CodeSetListener>>();
		for(InteractionEvent.Kind kind: InteractionEvent.Kind.values()) {
			listenerKinds.put(kind,new ArrayList<CodeSetListener>());
		}
	}

	/**
	 * Is notified when an interaction occurs.  This method then determines the JavaElement which
	 * participated in the event, and notifies any interested parties
	 */
	public void interactionObserved(InteractionEvent event) {
		AbstractContextStructureBridge bridge = ContextCorePlugin.getDefault().getStructureBridge(event.getStructureKind());
		Object theObject = bridge.getObjectForHandle(event.getStructureHandle());
		System.out.println(event.getKind() + ": " + event.getDate());
		if (theObject instanceof IJavaElement) {
			IJavaElement element = (IJavaElement) theObject;
			for (CodeSetListener listener: listenerKinds.get(event.getKind())) {
				listener.eventOccured(element);
			}
		}
	}

	/**
	 * Tells this class to start listening to the Mylar Monitor to keep track of events that occur
	 * and notify any interested parties
	 */
	public void startMonitoring () {
		MylarMonitorUiPlugin.getDefault().addInteractionListener(this);
	}

	/**
	 * Stops listening to the Mylar Monitor
	 */
	public void stopMonitoring () {
		MylarMonitorUiPlugin.getDefault().removeInteractionListener(this);
	}

	/**
	 * Adds a specific CodeSetListener to a specific InteractionEvent.Kind 
	 * @param kind
	 * @param listener
	 */
	public static void addListener(InteractionEvent.Kind kind, CodeSetListener listener) {
		ArrayList<CodeSetListener> listeners = listenerKinds.get(kind);
		listeners.add(listener);
	}

	/**
	 * Adds a specific CodeSetListener to All InteractionEvent.Kind
	 * @param listener
	 */
	public static void addListener(CodeSetListener listener) {
		for (InteractionEvent.Kind kind:InteractionEvent.Kind.values()) {
			addListener(kind, listener);
		}
	}

	/**
	 * Removes a specific CodeSetListener from All InteractionEvent.Kind
	 * @param listener
	 */
	public static void removeListener (CodeSetListener listener) {
		for (ArrayList<CodeSetListener> listeners: listenerKinds.values()) {
			listeners.remove(listener);
		}
	}
}
