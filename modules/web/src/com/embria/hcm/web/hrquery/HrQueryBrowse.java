package com.embria.hcm.web.hrquery;

import com.embria.hcm.entity.HrQuery;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class HrQueryBrowse extends AbstractLookup {

    /**
     * The {@link CollectionDatasource} instance that loads a list of {@link HrQuery} records
     * to be displayed in {@link HrQueryBrowse#hrQueriesTable} on the left
     */
    @Inject
    private CollectionDatasource<HrQuery, UUID> hrQueriesDs;

    /**
     * The {@link Datasource} instance that contains an instance of the selected entity
     * in {@link HrQueryBrowse#hrQueriesDs}
     * <p/> Containing instance is loaded in {@link CollectionDatasource#addItemChangeListener}
     * with the view, specified in the XML screen descriptor.
     * The listener is set in the {@link HrQueryBrowse#init(Map)} method
     */
    @Inject
    private Datasource<HrQuery> hrQueryDs;

    /**
     * The {@link Table} instance, containing a list of {@link HrQuery} records,
     * loaded via {@link HrQueryBrowse#hrQueriesDs}
     */
    @Inject
    private Table<HrQuery> hrQueriesTable;

    /**
     * The {@link BoxLayout} instance that contains components on the left side
     * of {@link SplitPanel}
     */
    @Inject
    private BoxLayout lookupBox;

    /**
     * The {@link BoxLayout} instance that contains buttons to invoke Save or Cancel actions in edit mode
     */
    @Inject
    private BoxLayout actionsPane;

    /**
     * The {@link FieldGroup} instance that is linked to {@link HrQueryBrowse#hrQueryDs}
     * and shows fields of the selected {@link HrQuery} record
     */
    @Inject
    private FieldGroup fieldGroup;

    /**
     * The {@link RemoveAction} instance, related to {@link HrQueryBrowse#hrQueriesTable}
     */
    @Named("hrQueriesTable.remove")
    private RemoveAction hrQueriesTableRemove;

    @Inject
    private DataSupplier dataSupplier;

    /**
     * {@link Boolean} value, indicating if a new instance of {@link HrQuery} is being created
     */
    private boolean creating;

    @Override
    public void init(Map<String, Object> params) {

        /*
         * Adding {@link com.haulmont.cuba.gui.data.Datasource.ItemChangeListener} to {@link hrQueriesDs}
         * The listener reloads the selected record with the specified view and sets it to {@link hrQueryDs}
         */
        hrQueriesDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                HrQuery reloadedItem = dataSupplier.reload(e.getDs().getItem(), hrQueryDs.getView());
                hrQueryDs.setItem(reloadedItem);
            }
        });

        /*
         * Adding {@link CreateAction} to {@link hrQueriesTable}
         * The listener removes selection in {@link hrQueriesTable}, sets a newly created item to {@link hrQueryDs}
         * and enables controls for record editing
         */
        hrQueriesTable.addAction(new CreateAction(hrQueriesTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                hrQueriesTable.setSelected(Collections.emptyList());
                hrQueryDs.setItem((HrQuery) newItem);
                refreshOptionsForLookupFields();
                enableEditControls(true);
            }
        });

        /*
         * Adding {@link EditAction} to {@link hrQueriesTable}
         * The listener enables controls for record editing
         */
        hrQueriesTable.addAction(new EditAction(hrQueriesTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                if (hrQueriesTable.getSelected().size() == 1) {
                    refreshOptionsForLookupFields();
                    enableEditControls(false);
                }
            }
        });

        /*
         * Setting {@link RemoveAction#afterRemoveHandler} for {@link hrQueriesTableRemove}
         * to reset record, contained in {@link hrQueryDs}
         */
        hrQueriesTableRemove.setAfterRemoveHandler(removedItems -> hrQueryDs.setItem(null));

        disableEditControls();
    }

    private void refreshOptionsForLookupFields() {
        for (Component component : fieldGroup.getOwnComponents()) {
            if (component instanceof LookupField) {
                CollectionDatasource optionsDatasource = ((LookupField) component).getOptionsDatasource();
                if (optionsDatasource != null) {
                    optionsDatasource.refresh();
                }
            }
        }
    }

    /**
     * Method that is invoked by clicking Ok button after editing an existing or creating a new record
     */
    public void save() {
        if (!validate(Collections.singletonList(fieldGroup))) {
            return;
        }
        getDsContext().commit();

        HrQuery editedItem = hrQueryDs.getItem();
        if (creating) {
            hrQueriesDs.includeItem(editedItem);
        } else {
            hrQueriesDs.updateItem(editedItem);
        }
        hrQueriesTable.setSelected(editedItem);

        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Cancel button, discards changes and disables controls for record editing
     */
    public void cancel() {
        HrQuery selectedItem = hrQueriesDs.getItem();
        if (selectedItem != null) {
            HrQuery reloadedItem = dataSupplier.reload(selectedItem, hrQueryDs.getView());
            hrQueriesDs.setItem(reloadedItem);
        } else {
            hrQueryDs.setItem(null);
        }

        disableEditControls();
    }

    /**
     * Enabling controls for record editing
     * @param creating indicates if a new instance of {@link HrQuery} is being created
     */
    private void enableEditControls(boolean creating) {
        this.creating = creating;
        initEditComponents(true);
        fieldGroup.requestFocus();
    }

    /**
     * Disabling editing controls
     */
    private void disableEditControls() {
        initEditComponents(false);
        hrQueriesTable.requestFocus();
    }

    /**
     * Initiating edit controls, depending on if they should be enabled/disabled
     * @param enabled if true - enables editing controls and disables controls on the left side of the splitter
     *                if false - visa versa
     */
    private void initEditComponents(boolean enabled) {
        fieldGroup.setEditable(enabled);
        actionsPane.setVisible(enabled);
        lookupBox.setEnabled(!enabled);
    }
}