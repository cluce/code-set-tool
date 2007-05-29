package ca.ucalgary.codesets.sets;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.mylar.monitor.core.InteractionEvent;

import ca.ucalgary.codesets.ElementLabelProvider;
import ca.ucalgary.codesets.listeners.*;

public class HistorySet extends CodeSet {

	public HistorySet () {
		super();
		listener = new EditorFocusListener(this);
	}
	
	@Override
	public void activate() {
		super.activate();
//		PartListener.addListener(listener);
		InteractionListener.addListener(InteractionEvent.Kind.SELECTION, listener);
	}

	@Override
	public void deactivate() {
		super.deactivate();
//		PartListener.removeListener(listener);
	}
}
