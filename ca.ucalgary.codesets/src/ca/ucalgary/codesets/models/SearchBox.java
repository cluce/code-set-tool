package ca.ucalgary.codesets.models;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.search.ui.text.TextSearchQueryProvider.TextSearchInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import ca.ucalgary.codesets.controllers.Logger;

public class SearchBox extends Composite{
	private final Text text = new Text(this, SWT.SINGLE | SWT.BORDER);
	private Button button = new Button(this,SWT.PUSH);
	private String name = null;

	public SearchBox(Composite parent) {
		super(parent,SWT.INHERIT_DEFAULT);
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		this.setLayout( grid);
		GridData data = new GridData();
		data.widthHint = 100;
		text.setLayoutData(data);
		addTextBox();
		addSearchButton();
	}

	//Adds a button called search to the Composite
	//When the button is pressed the listener calls search().
	private void addSearchButton() {
		button.setText("Search");
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				getTextBoxText();
			}
		});	
	}
	
	//Method for retrieving the text that's in the text box when either the search button was clicked, or 
	//return was applied while typing
	private void getTextBoxText() {
		name = text.getText();
		if(name!=null)
			name = name.trim();
		button.setText("Search");  //positive feedback... hopefully
		Logger.instance().addEvent("Searched For:"+'\t'+name);
		search();
	}

	

	public FileTextSearchScope createTextSearchScope() { 
		String[] string = {"*.java"};
		return FileTextSearchScope.newWorkspaceScope(string, false);
	}


	private static class TextSearchPageInput extends TextSearchInput {
		private final String fSearchText;
		private final boolean fIsCaseSensitive;
		private final boolean fIsRegEx;
		private final FileTextSearchScope fScope;

		public TextSearchPageInput(String searchText, boolean isCaseSensitive, boolean isRegEx, FileTextSearchScope scope) {
			fSearchText= searchText;
			fIsCaseSensitive= isCaseSensitive;
			fIsRegEx= isRegEx;
			fScope= scope;
		}

		public String getSearchText() {
			return fSearchText;
		}

		public boolean isCaseSensitiveSearch() {
			return fIsCaseSensitive;
		}

		public boolean isRegExSearch() {
			return fIsRegEx;
		}

		public FileTextSearchScope getScope() {
			return fScope;
		}
	}

	private ISearchQuery newQuery() throws CoreException { 
		TextSearchPageInput input= new TextSearchPageInput(name, true, false, createTextSearchScope());
		return TextSearchQueryProvider.getPreferred().createQuery(input);
	}

	private void search() {		
		if(!name.equals("Enter Search") && name != null  && !name.equals("")) {
			final NodeSet searchSet = new NodeSet(name,"search");
			if(!NodeSetManager.instance.containsSet(searchSet)){
				try {
					ISearchQuery query = newQuery(); //Creates a query for the given text
					query.getSearchResult().addListener(new ISearchResultListener() {
						public void searchResultChanged(SearchResultEvent e) {
							if(e instanceof MatchEvent){
								Match[] matches = ((MatchEvent)e).getMatches();
								for (Match m:matches) {
									FileMatch fm = (FileMatch)m; //Returns IFiles, and create a compilation unit for each one
									ICompilationUnit unit = JavaCore.createCompilationUnitFrom(fm.getFile());
									ASTNode node = ASTHelper.getNodeAtPosition(unit, m.getOffset());
									searchSet.add(node);
								}	
								NodeSetManager.instance.addSet(searchSet);
								
								text.setText("Enter Search");
							}
						}
					});
					query.run(new NullProgressMonitor());
				} catch (IllegalArgumentException e2) {
					e2.printStackTrace();
				} catch (CoreException e2) {
					e2.printStackTrace();
				}
			}
		}	
		text.setText("Enter Search");
		
	}

	//Adds a text box to the composite. 
	//When the text box is clicked, if it still has the original text in it 
	//It deletes it and makes it an empty text box
	private void addTextBox() {
		text.setText("Enter Search");
		text.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				if(text.getText().equals("Enter Search"))
					text.setText("");
			}
		});
		
		text.addListener(SWT.KeyDown, new Listener(){
			public void handleEvent(Event event) {
				if(event.character == '\r'){   // When the user hits enter in the text box, this event happens
					getTextBoxText();
				}
			}
		});
	}	
}
