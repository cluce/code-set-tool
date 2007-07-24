package ca.ucalgary.codesets.models;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import ca.ucalgary.codesets.views.ElementLabelProvider;

// this class listens to editor selection events and in response to events
// creates three kinds of code sets: (1) the navigation history set, (2) the 
// references to set, and (3) the references from set
public class EditorFocusListener implements ISelectionChangedListener, IPartListener {
	JavaEditor editor;
	ISelectionProvider selectionProvider;
	IJavaElement last;
	
	public EditorFocusListener(IEditorPart part) {
		if (part instanceof JavaEditor)
			register((JavaEditor)part);
	}
	
	// retruns a suitable name for the given element
	String name(IJavaElement element) {
		return new ElementLabelProvider().getText(element);
	}
	
	void register(JavaEditor part) {
		unregister();
		editor = part;
		selectionProvider = editor.getSelectionProvider();
		
		if (selectionProvider != null) {
			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else {
				selectionProvider.addSelectionChangedListener(this);
			}
		}
	}
	
	void unregister() {
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
		
		selectionProvider = null;
		editor = null;
	}
	
	/**
	 * Entry point for handling each change event.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		IJavaElement element = computeSelection(); 
		if (element != null && ! element.equals(last) && !(element instanceof PackageDeclaration)) {
//			System.out.println(element);
			last = element;
			
			try {
				String s = ((ISourceReference)element).getSource();
//				System.out.println(s);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			CodeSetManager.instance().setFocus((ISourceReference)element);
			new ReferenceToSearch().search(element, name(element));
			new ReferenceFromSearch().search(element, name(element));
			new RefOfFile().search(element);
		}
	}
	
	IJavaElement computeSelection() {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null) return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null) return null;

		int caret = getCaretPosition(sourceViewer, styledText);

		IWorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
		ICompilationUnit unit = manager.getWorkingCopy(editor.getEditorInput());
		if (unit == null) return null;
		
		try {
			IJavaElement element = getElementAt(unit, caret, false);
			if (!(element instanceof ISourceReference))
				return null;
			return element;
		} catch (JavaModelException x) {
			if (!x.isDoesNotExist())
				JavaPlugin.log(x.getStatus());
		}
		
		return null;
	}
	
	protected static IJavaElement getElementAt(ICompilationUnit unit, int offset,
			boolean reconcile) throws JavaModelException {
		if (reconcile) {
			synchronized (unit) {
				unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
			}
			return unit.getElementAt(offset);
		} else if (unit.isConsistent())
			return unit.getElementAt(offset);
		
		return null;
	}

	int getCaretPosition(ISourceViewer sourceViewer, StyledText styledText) {
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			return extension.widgetOffset2ModelOffset(styledText
					.getCaretOffset());
		} else {
			int offset = sourceViewer.getVisibleRegion().getOffset();
			return offset + styledText.getCaretOffset();
		}
	}

	public void partActivated(IWorkbenchPart part) {
		if (part instanceof JavaEditor) {
			System.out.println("Selection Changed " + (JavaEditor)part);
			register((JavaEditor)part);
		}
	}
	public void partBroughtToTop(IWorkbenchPart part) {
	}
	public void partClosed(IWorkbenchPart part) {
	}
	public void partDeactivated(IWorkbenchPart part) {
	}
	public void partOpened(IWorkbenchPart part) {
	}
}
