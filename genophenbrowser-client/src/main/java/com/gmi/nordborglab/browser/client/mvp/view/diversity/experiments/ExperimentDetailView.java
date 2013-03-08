package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.gmi.nordborglab.browser.client.editors.ExperimentDisplayEditor;
import com.gmi.nordborglab.browser.client.editors.ExperimentEditEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.ExperimentDetailUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.experiments.ExperimentDetailPresenter.State;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.tools.GWASViewerPresenter;
import com.gmi.nordborglab.browser.client.resources.MainResources;
import com.gmi.nordborglab.browser.shared.proxy.AccessControlEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.AclExperimentEntryProxy;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.gmi.nordborglab.browser.shared.proxy.PermissionProxy;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ExperimentDetailView extends ViewWithUiHandlers<ExperimentDetailUiHandlers> implements
		ExperimentDetailPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, ExperimentDetailView> {
	}
	
	public interface ExperimentEditDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentEditEditor> {}
	public interface ExperimentDisplayDriver extends RequestFactoryEditorDriver<ExperimentProxy, ExperimentDisplayEditor> {}
	
	@UiField ExperimentEditEditor experimentEditEditor;
	@UiField ExperimentDisplayEditor experimentDisplayEditor;
	@UiField ToggleButton edit;
	@UiField ToggleButton save;
	@UiField Anchor cancel;
	@UiField Anchor delete;
    @UiField
    ToggleButton share;
    private final ExperimentEditDriver experimentEditDriver;
	private final ExperimentDisplayDriver experimentDisplayDriver;
	private State state = State.DISPLAYING;
	private final MainResources resources;
    private Modal permissionPopUp = new Modal(true);

	@Inject
	public ExperimentDetailView(final Binder binder,
			final ExperimentEditDriver experimentEditDriver,
			final ExperimentDisplayDriver experimentDisplayDriver,
			final MainResources resources) {
		this.resources = resources;
		widget = binder.createAndBindUi(this);
		this.experimentEditDriver = experimentEditDriver;
		this.experimentDisplayDriver = experimentDisplayDriver;
		this.experimentDisplayDriver.initialize(experimentDisplayEditor);
		this.experimentEditDriver.initialize(experimentEditEditor);
        permissionPopUp.setBackdrop(BackdropType.STATIC);
        permissionPopUp.setTitle("Permissions");
        permissionPopUp.setMaxHeigth("700px");
        permissionPopUp.setCloseVisible(false);
        permissionPopUp.setKeyboard(false);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public ExperimentEditDriver getExperimentEditDriver() {
		return experimentEditDriver;
	}

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == ExperimentDetailPresenter.TYPE_SetPermissionContent) {
            permissionPopUp.add(content);
        }
        else {
            super.setInSlot(slot, content);
        }
    }



	@Override
	public void setState(State state,int permission) {
		this.state = state;
		experimentDisplayEditor.setVisible(state == State.DISPLAYING);
		experimentEditEditor.setVisible((state == State.EDITING || state == State.SAVING) && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		edit.setVisible(state == State.DISPLAYING && 
				(permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		save.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		cancel.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
		delete.setVisible(state == State.EDITING && (permission & AccessControlEntryProxy.EDIT) == AccessControlEntryProxy.EDIT);
        share.setVisible((permission & AccessControlEntryProxy.ADMINISTRATION) == AccessControlEntryProxy.ADMINISTRATION);
	}

	@Override
	public State getState() {
		return state;
	}

    @Override
    public void showPermissionPanel(boolean show) {
        if (show)
            permissionPopUp.show();
        else
            permissionPopUp.hide();
    }

    @Override
	public ExperimentDisplayDriver getExperimentDisplayDriver() {
		return experimentDisplayDriver;
	}
	
	@UiHandler("edit")
	public void onEdit(ClickEvent e) {
		if (state == State.DISPLAYING) {
			getUiHandlers().onEdit();
		}
	}
	
	@UiHandler("delete")
	public void onDelete(ClickEvent e) {
		if (state == State.EDITING) {
			if (Window.confirm("Do you really want to delete the Experiment?")) 
				getUiHandlers().onDelete();
		}
	}
	
	@UiHandler("save") 
	public void onSave(ClickEvent e) {
		if (state == State.EDITING) {
			getUiHandlers().onSave();
		}
	}
	
	@UiHandler("cancel") 
	public void onCancel(ClickEvent e) {
		if (state == State.EDITING) {
			getUiHandlers().onCancel();
		}
	}

    @UiHandler("share")
    public void onClickShareBtn(ClickEvent e) {
        getUiHandlers().onShare();
    }
	
}
