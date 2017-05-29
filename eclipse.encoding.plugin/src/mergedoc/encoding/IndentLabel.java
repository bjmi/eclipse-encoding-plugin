package mergedoc.encoding;

import static mergedoc.encoding.IndentChars.SPACE;
import static mergedoc.encoding.IndentChars.TAB;
import static mergedoc.encoding.Langs.formatLabel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

import mergedoc.encoding.EncodingPreferenceInitializer.PreferenceKey;
import mergedoc.encoding.document.ActiveDocument;

/**
 * TODO michael Line separetor label shown in the status bar.
 *
 * @author Shinji Kashihara
 */
public class IndentLabel implements PreferenceKey {

    private final ActiveDocumentAgent agent;
    private final CLabel label;
    private Menu popupMenu;
    private List<IndentItem> indentItemList;

    private static class IndentItem {
        public String value;
        public String desc;

        public IndentItem(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }

    public IndentLabel(ActiveDocumentAgent agent, Composite statusBar, int widthHint) {
        this.agent = agent;
        label = new CLabel(statusBar, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.widthHint = widthHint;
        label.setLayoutData(gridData);
    }

    public void initMenu() {

        ActiveDocument doc = agent.getDocument();
        String indentChar = doc.getIndentChar();
        if (indentChar == null) {
            label.setText(null);
            label.setMenu(null);
            label.setImage(null);
            label.setToolTipText(null);
            return;
        }

        label.setText(indentChar);
        if (indentItemList == null) {
            indentItemList = new ArrayList<IndentItem>();
            indentItemList.add(new IndentItem(SPACE, "(' ', 0x20, Space)"));
            indentItemList.add(new IndentItem(TAB, "('\\t', 0x09, Tab)"));
        }

        if (IndentChars.isMixed(indentChar)) {
            label.setImage(Activator.getImage("warn"));
        } else {
            label.setImage(null);
        }

        if (popupMenu != null && !popupMenu.isDisposed()) {
            label.setMenu(popupMenu);
            return;
        }
        popupMenu = new Menu(label);
        label.setMenu(popupMenu);

        // Add the menu items dynamically.
        popupMenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {

                ActiveDocument doc = agent.getDocument();
                doc.warnDirtyMessage(agent.isDocumentDirty() && doc.canConvertContent());

                // Remove existing menu items.
                for (MenuItem item : popupMenu.getItems()) {
                    item.dispose();
                }

                createShortcutMenu();
                createSelectionMenu();
            }
        });
    }

    private void createShortcutMenu() {

        final ActiveDocument doc = agent.getDocument();

        // Workspace Preferences
        {
            MenuItem menuItem = new MenuItem(popupMenu, SWT.NONE);
            menuItem.setText(formatLabel("Workspace Preferences...", IndentChars.ofWorkspace()));
            menuItem.setImage(Activator.getImage("workspace"));
            menuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
                                    "org.eclipse.ui.preferencePages.GeneralTextEditor", null, null).open();
                }
            });
        }

        new MenuItem(popupMenu, SWT.SEPARATOR);
    }

    private void createSelectionMenu() {

        final ActiveDocument doc = agent.getDocument();

        for (final IndentItem indentItem : indentItemList) {
            final MenuItem menuItem = new MenuItem(popupMenu, SWT.RADIO);
            menuItem.setText(indentItem.value + " " + indentItem.desc);
            menuItem.setEnabled(false);
            if (indentItem.value.equals(doc.getIndentChar())) {
                menuItem.setSelection(true);
            }
            menuItem.setImage(Activator.getImage(indentItem.value));
        }
    }

}
