package ca.ucalgary.codesets.models;


import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.GenericVisitor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;

import ca.ucalgary.codesets.views.ElementLabelProvider;

//uses eclipse's ast parser to compute the set of all references from a
//given element.
public class MembersOfType extends GenericVisitor {
	TypeDeclaration type;
	NodeSet set;
	ElementLabelProvider labelProvider = new ElementLabelProvider();

	public void search(ASTNode node) {		
		TypeDeclaration type = (TypeDeclaration) ASTHelper.getAncestorByType(node, ASTNode.TYPE_DECLARATION);
		if (type == null)
			return;
		set = new NodeSet(labelProvider.getFullText(ASTHelper.getJavaElement(type)), "Members of Type");

		if (NodeSetManager.instance().containsSet(set))
			return;
		type.accept(this);

		if (set.size() != 0)
			NodeSetManager.instance().addSet(set);
	}

	protected boolean visitNode(ASTNode node) {
		return true;
	}

	public boolean visit(MethodDeclaration node) {
		set.add(node);
		return visitNode(node);
	}

	public boolean visit(FieldDeclaration node) {
		set.add(node);
		return visitNode(node);
	}

}