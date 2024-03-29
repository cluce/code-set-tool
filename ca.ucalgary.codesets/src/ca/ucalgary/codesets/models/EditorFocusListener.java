package ca.ucalgary.codesets.models;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import ca.ucalgary.codesets.views.ElementLabelProvider;

//this class listens to editor selection events and in response to events
//creates three kinds of code sets: (1) the navigation history set, (2) the 
//references to set, and (3) the references from set
public class EditorFocusListener implements ISelectionChangedListener, IPartListener {
	JavaEditor editor;
	ISelectionProvider selectionProvider;
	IJavaElement last;
	int lastCaret;

	// retruns a suitable name for the given element
	String name(IJavaElement element) {
		return new ElementLabelProvider().getText(element);
	}

	public void register(JavaEditor part) {
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

	public void unregister() {
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
		
		if ((element != null) && !(element.equals(last))) {
			// We don't want to include package declarations in our sets, so ignore them
			boolean isPackageDeclaration = (element instanceof IPackageDeclaration);
			if (!isPackageDeclaration) {
				last = element;
			}
		}
	}

	boolean isDeclaration(IJavaElement element) {
		return element != null && (element.getElementType() == IJavaElement.FIELD ||
			element.getElementType() == IJavaElement.METHOD ||
			element.getElementType() == IJavaElement.TYPE);
	}
	
	IJavaElement computeSelection() {
		ISourceViewer sourceViewer = editor.getViewer();
		if (sourceViewer == null) return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null) return null;

		int caret = getCaretPosition(sourceViewer, styledText);
		this.lastCaret = caret;

		IWorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
		ICompilationUnit unit = manager.getWorkingCopy(editor.getEditorInput());
		if (unit == null) return null;

		try {
			IJavaElement element = getElementAt(unit, caret, false);
			
			// we want to ignore package decls and the like
			if (element != null && element.getElementType() == IJavaElement.PACKAGE_DECLARATION)
				return null;
			if (element != null && element.getElementType() == IJavaElement.IMPORT_DECLARATION)
				return null;
			
			if(element == null)
				return null;
			while (element.getElementType() == IJavaElement.TYPE &&
					element.getParent().getElementType() != IJavaElement.COMPILATION_UNIT)
				element = element.getParent();
						
			// this loop gets past methods declared in anonymous classes
			while (element != null && 
					element.getParent().getElementType() == IJavaElement.TYPE &&
					((IType)element.getParent()).isAnonymous()) {
				IJavaElement next = element.getParent().getAncestor(IJavaElement.METHOD);
				if (next == null)
					next = element.getParent().getAncestor(IJavaElement.FIELD);
				
				element =  next;
			}
			
			ASTNode node = ASTHelper.getNodeAtPosition(unit, caret);
			
			NodeSetManager.instance().setFocus(element, node);
			new ReferenceFromSearch().search(element, node);
			new ReferenceToSearch().search(element);
			new MembersOfType().search(element, node);
			
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
			register((JavaEditor)part);
		}
	}
	
	// Autogenerated required methods of IPartListener
	public void partBroughtToTop(IWorkbenchPart part) {
	}
	public void partClosed(IWorkbenchPart part) {
	}
	public void partDeactivated(IWorkbenchPart part) {
	}
	public void partOpened(IWorkbenchPart part) {
	}
}
