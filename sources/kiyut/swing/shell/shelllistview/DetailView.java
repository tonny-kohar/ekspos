package kiyut.swing.shell.shelllistview;

import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;



/** <code>DetailView</code> is a view for <code>ShellListView</code> which display
 * the <code>ShellListViewModel</code> in a table like view.
 * It has 4 column: name, size, type, modified
 *
 * @author  tonny
 */
public class DetailView extends JTable implements ViewComponent {
    private boolean doNotEdit = false;
    
    /** Constructs a <code>DetailView</code> using the given data model
     * @param dataModel data model for this component
     */
    public DetailView(ShellListViewModel dataModel) {
        super(dataModel);
                
        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(true);
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onHeaderMouseClicked(e);
            }
        });
        
        // set column
        //TableColumnModel columnModel = getColumnModel();
        
        columnModel.getColumn(0).setPreferredWidth(200);
        columnModel.getColumn(1).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(120);
        
        columnModel.getColumn(0).setCellRenderer(new DetailNameCellRenderer(dataModel.getFileSystemView()));
        columnModel.getColumn(1).setCellRenderer(new DetailSizeCellRenderer());
        columnModel.getColumn(3).setCellRenderer(new DetailModifiedCellRenderer());
        
        // set column editor
        DetailNameCellEditor newCellEditor = new DetailNameCellEditor();
        columnModel.getColumn(0).setCellEditor(newCellEditor);
    }
    
    /** {@inheritDoc} */
    public ShellListViewModel getViewModel() {
        return (ShellListViewModel)getModel();
    }
    
    /** Overrides to provide only name column is editable 
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        boolean b = false;
        //ShellListViewModel dataModel = (ShellListViewModel)getModel();
        //if ((col == 0) && (dataModel.getState() == ShellListViewModel.EDIT)) {
        if ((col == 0)  && (doNotEdit == false)) {
            b = true;
        }
        doNotEdit = false;
        return b;
    }
    
    /** Overriden to handle mouse Double Click traverse instead of Edit
     * {@inheritDoc} */
    @Override
    protected void processMouseEvent(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            doNotEdit = true;
        }
        super.processMouseEvent(evt);
    }
    
    @Override
    public void editingCanceled(ChangeEvent evt) {
        ShellListViewModel model = (ShellListViewModel)getModel();
        model.setState(ShellListViewModel.BROWSE);
        super.editingCanceled(evt);
        //removeEditor();
    }
    
    @Override
    public void editingStopped(ChangeEvent evt) {
        ShellListViewModel modle = (ShellListViewModel)getModel();
        modle.setState(ShellListViewModel.BROWSE);
        super.editingStopped(evt);
        
    }
    
    ////////////////////////////////
    // Event Handler
    ///////////////////////////////
    
    
    /** Invoked when the mouse button has been clicked (pressed and released) on the header
     * @param evt a <code>MouseEvent</code> object
     */
    private void onHeaderMouseClicked(MouseEvent evt) {
        TableColumnModel colModel = getColumnModel();
        int viewColumnIndex = colModel.getColumnIndexAtX(evt.getX());
        int column = convertColumnIndexToModel(viewColumnIndex);
        //System.err.println("table header click column:" + column);
        
        if (evt.getClickCount() == 1 && column != -1) {
            ShellListViewModel model = (ShellListViewModel)getModel();
            model.setSortBy(column);
            model.refresh();
        }
    }
}
