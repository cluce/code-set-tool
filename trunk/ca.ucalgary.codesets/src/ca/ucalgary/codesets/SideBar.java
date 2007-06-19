package ca.ucalgary.codesets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.HyperlinkListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.widgets.*;

import ca.ucalgary.codesets.listeners.InteractionListener;
import ca.ucalgary.codesets.listeners.LinkExpansionListener;
import ca.ucalgary.codesets.listeners.LinkListener;
import ca.ucalgary.codesets.listeners.SetListener;
import ca.ucalgary.codesets.sets.CodeSet;
import ca.ucalgary.codesets.views.CodeSetView;

public class SideBar implements SetListener {

	Color backgroundColor = null; //new Color(null,245,245,250);
	HashMap<Object,Form> forms;

	CodeSetView theView;

	Composite container;
	FormToolkit toolkit;
	GridLayout mainLayout;
	GridLayout subLayout;

	public SideBar (Composite parent, CodeSetView theView, CodeSet history, CodeSet edit) {

		initialize(parent);
		this.theView = theView;
		createSet(history);
		createSet(edit);
		InteractionListener.getReferenceFrom().changeListener(this);
		InteractionListener.getReferenceTo().changeListener(this);
		createSet(InteractionListener.getReferenceFrom());
		createSet(InteractionListener.getReferenceTo());

	}

	public void initialize (Composite parent) {

		forms = new HashMap<Object,Form>();

		mainLayout = new GridLayout();
		mainLayout.verticalSpacing = 20;
		mainLayout.marginLeft = 0;
		mainLayout.numColumns = 1;

		subLayout = new GridLayout();
		subLayout.verticalSpacing = 0;
		subLayout.marginLeft = 0;
		subLayout.numColumns = 1;

		container = new Composite(parent, SWT.NONE);
		container.setLayout(mainLayout);
		container.setBackground(backgroundColor);

		toolkit = new FormToolkit(container.getDisplay());
		toolkit.setBackground(backgroundColor);
	}

	public void createSet (Object set) {

		Form setpanel = toolkit.createForm(container);
		forms.put(set, setpanel);
		int hashCode = set.hashCode();

		setpanel.setBackground(backgroundColor);
		setpanel.getBody().setLayout(subLayout);

		createContents(set,setpanel);
	}

	public void createContents (Object set, Form setpanel) {	
		if (set instanceof CodeSet) {
			CodeSet codeSet = (CodeSet) set;

			Hyperlink link = toolkit.createHyperlink(setpanel.getBody(), codeSet.getName(), SWT.NONE);
			link.addHyperlinkListener(new LinkListener(theView));
			link.setHref(codeSet);
		}
		else if (set instanceof ResultSet) {
			ArrayList<CodeSet> results = new ArrayList<CodeSet>();

			ResultSet resultSet = (ResultSet) set;

			setpanel.setText(resultSet.getName());
			if (resultSet.size() > 3)
				if (resultSet.displayAll())
					results.addAll(resultSet.subList(0, resultSet.size()));
				else
					results.addAll(resultSet.subList(0, 3));
			else
				results.addAll(resultSet.subList(0, resultSet.size()));
			for (CodeSet c:results) {
				Hyperlink link = toolkit.createHyperlink(setpanel.getBody(), c.getName(), SWT.NONE);
				link.addHyperlinkListener(new LinkListener(theView));
				link.setHref(c);
			}
			if (resultSet.size() > 3) {
				Hyperlink link;
				if (resultSet.displayAll())
					link = toolkit.createHyperlink(setpanel.getBody(), "^", SWT.NONE);
				else
					link = toolkit.createHyperlink(setpanel.getBody(), "...", SWT.NONE);
				link.setHref(resultSet);
				link.addHyperlinkListener(new LinkExpansionListener());
			}
		}
	}


	public void refresh(Object s) {
		Form form = forms.get(s);

		if (form != null) {
			for (Control c: form.getBody().getChildren()) {
				c.dispose();
			}
			createContents(s,form);
			form.layout();
			form.getParent().layout();
		}
	}
}