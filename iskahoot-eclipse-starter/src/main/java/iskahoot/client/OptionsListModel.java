package iskahoot.client;

import javax.swing.*;
import java.util.List;

public class OptionsListModel extends AbstractListModel<String> {
    private java.util.List<String> options = java.util.Collections.emptyList();
    public void setOptions(List<String> opts) {
        int oldSize = options.size();
        options = (opts != null) ? opts : java.util.Collections.emptyList();
        if (oldSize > 0) fireIntervalRemoved(this, 0, oldSize - 1);
        if (!options.isEmpty()) fireIntervalAdded(this, 0, options.size() - 1);
    }
    @Override public int getSize() { return options.size(); }
    @Override public String getElementAt(int index) { return options.get(index); }
}
