package com.gmi.nordborglab.browser.client.ui;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.gmi.nordborglab.browser.client.resources.Icons;
import com.gmi.nordborglab.browser.client.place.NameTokens;
import com.gmi.nordborglab.browser.client.ui.SearchSuggestOracle.SearchSuggestion;
import com.gmi.nordborglab.browser.shared.proxy.SearchItemProxy.SUB_CATEGORY;
import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SearchSuggestDisplay extends SuggestionDisplay {

    private final PopupPanel suggestionPopup;
    private final HTMLPanel content;
    // private Collection<? extends Suggestion> suggestions;
    private ImmutableBiMap<Element, Suggestion> suggestionToUiMap;
    private Element table;
    private Suggestion selectedSuggestion = null;
    private SuggestionCallback callback;
    private int selectedSuggesionIndex = -1;
    private SuggestBox suggestBox;

    public SearchSuggestDisplay() {


        suggestionPopup = createPopup();
        content = new HTMLPanel("");
        suggestionPopup.setWidget(content);
        content.addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (callback == null)
                    return;
                Element target = event.getNativeEvent().getEventTarget().cast();
                if (target.getTagName().toLowerCase().equals("em"))
                    target = target.getParentElement();
                if (target.getTagName().toLowerCase().equals(SpanElement.TAG.toLowerCase()))
                    target = target.getParentElement();
                Suggestion suggestion = suggestionToUiMap.get(target);
                if (suggestion == null)
                    return;
                callback.onSuggestionSelected(suggestion);
            }
        }, ClickEvent.getType());
    }

    private PopupPanel createPopup() {
        PopupPanel popupPanel = new PopupPanel(true, false);
        //noinspection GWTStyleCheck
        popupPanel.setStyleName("searchBoxPopup");
        popupPanel.setPreviewingAllNativeEvents(true);
        return popupPanel;
    }

    @Override
    protected Suggestion getCurrentSelection() {
        if (!isSuggestionListShowing()) {
            return null;
        }
        return selectedSuggestion;
    }

    @Override
    protected void hideSuggestions() {
        suggestionPopup.hide();

    }

    @Override
    protected void moveSelectionDown() {
        String test = "test";

    }

    @Override
    protected void moveSelectionUp() {
        String test = "test";

    }

    @Override
    protected void showSuggestions(SuggestBox suggestBox,
                                   Collection<? extends Suggestion> suggestions,
                                   boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
                                   SuggestionCallback callback) {
        this.callback = callback;
        this.suggestBox = suggestBox;
        if (suggestionPopup.isAttached()) {
            suggestionPopup.hide();
        }
        createPopupContent(groupSuggestions(suggestions));
        suggestionPopup.showRelativeTo(suggestBox);
    }

    private ImmutableListMultimap<SUB_CATEGORY, ? extends Suggestion> groupSuggestions(Collection<? extends Suggestion> suggestions) {

        ImmutableListMultimap<SUB_CATEGORY, ? extends Suggestion> groupedSuggestions = Multimaps.index(suggestions,
                new Function<Suggestion, SUB_CATEGORY>() {

                    @Override
                    @Nullable
                    public SUB_CATEGORY apply(@Nullable Suggestion input) {
                        return ((SearchSuggestion) input).getCategory();
                    }
                });
        return groupedSuggestions;
    }

    private void createPopupContent(ImmutableListMultimap<SUB_CATEGORY, ? extends Suggestion> groupedSuggestions) {
        content.clear();
        content.getElement().setInnerHTML("");
        if (table != null)
            table.removeFromParent();
        table = DOM.createTable();
        Element body = DOM.createTBody();
        table.appendChild(body);
        int i = 0;
        if (groupedSuggestions == null || groupedSuggestions.size() == 0) {
            ParagraphElement title = Document.get().createPElement();
            title.setClassName("searchBoxPopup_nohits");
            title.setInnerText("Nothing found ...");
            content.getElement().appendChild(title);
        } else {
            Iterator<SUB_CATEGORY> keyIterator = groupedSuggestions.asMap()
                    .keySet().iterator();
            Element tr = null;
            ImmutableBiMap.Builder<Element, Suggestion> builder = ImmutableBiMap.builder();

            while (keyIterator.hasNext()) {
                SUB_CATEGORY key = keyIterator.next();
                Collection<? extends Suggestion> suggestions = groupedSuggestions
                        .get(key);
                tr = DOM.createTR();
                if (i % 2 == 0) {
                    tr.addClassName("alt");
                }
                Element th = DOM.createTH();
                HeadingElement h3 = Document.get().createHElement(3);
                String categoryText = key.toString();
                if (NameTokens.subCategory2Token.containsKey(key)) {
                    categoryText = "<a href=\"/#" + NameTokens.subCategory2Token.get(key) + "?query=" + suggestBox.getText() + "\">" + categoryText + "</a>";
                }
                if (Icons.subCategory2Icon.containsKey(key)) {
                    categoryText = "<i class=\"" + Icons.subCategory2Icon.get(key) + "\" style=\"margin-right:5px;\"></i>" + categoryText;
                }
                h3.setInnerHTML(categoryText);
                th.appendChild(h3);
                h3 = Document.get().createHElement(3);
                h3.setInnerText(suggestions.size() + "/" + ((SearchSuggestion) Iterables.get(suggestions, 0)).getSearchFacetPage().getTotal());
                th.appendChild(h3);
                tr.appendChild(th);
                Element td = DOM.createTD();
                tr.appendChild(td);
                UListElement ul = Document.get().createULElement();
                for (Suggestion suggestion : suggestions) {

                    LIElement li = Document.get().createLIElement();
                    SpanElement span = Document.get().createSpanElement();
                    span.setInnerHTML(suggestion.getDisplayString());
                    li.appendChild(span);
                    ul.appendChild(li);
                    builder.put(li, suggestion);
                }
                td.appendChild(ul);
                body.appendChild(tr);
                i = i + 1;
            }
            suggestionToUiMap = builder.build();
        }
        TableCellElement searchTd = Document.get().createTDElement();
        Element searchIcon = DOM.createElement("i");
        Element arrowIcon = DOM.createElement("i");
        arrowIcon.setClassName("icon-circle-arrow-right");
        searchIcon.setClassName("icon-search");
        SpanElement spanElement = Document.get().createSpanElement();
        spanElement.setClassName("searchBoxPopup_term");
        spanElement.appendChild(searchIcon);
        spanElement.appendChild(Document.get().createTextNode("Search for '" + suggestBox.getText() + "'"));
        spanElement.appendChild(arrowIcon);
        searchTd.appendChild(spanElement);
        TableRowElement searchRow = Document.get().createTRElement();
        if (i % 2 == 0)
            searchRow.addClassName("alt");
        searchRow.appendChild(Document.get().createTHElement());
        searchRow.appendChild(searchTd);
        body.appendChild(searchRow);
        content.getElement().appendChild(table);
    }

    public boolean isSuggestionListShowing() {
        return suggestionPopup.isShowing();
    }

}
