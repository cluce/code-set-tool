package ca.ucalgary.codesets.sets;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.TableViewer;

import ca.ucalgary.codesets.EditorModifiedListener;
import ca.ucalgary.codesets.ElementLabelProvider;
import ca.ucalgary.codesets.listeners.*;

public class EditorChangeSet extends CodeSet {

	public EditorChangeSet () {
		super();
		listener = new EditorModifiedListener(this);
	}
	
	@Override
	public void activate() {
		PartListener.addListener(listener);
	}

	@Override
	public void deactivate() {
		PartListener.removeListener(listener);
	}
}
