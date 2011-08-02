/*
 * MenuFactory.java
 *
 * Created on October 25, 2004, 12:12 PM
 */

package kiyut.swing.util;

import java.util.*;

import javax.swing.*;

/** Factory Method for create JMenuBar using ResourceBundle.
 * <strong>Note:</strong> a lot of idea adapted from Apache Batik
 *
 * @author Kiyut
 */
public class MenuFactory {
    private final static String TYPE_MENU   = "MENU";
    private final static String TYPE_ITEM   = "ITEM";
    private final static String TYPE_RADIO  = "RADIO";
    private final static String TYPE_CHECK  = "CHECK";
    private final static String SEPARATOR   = "-";
    
    private final static String MENU_TYPE   = "MenuType";
    private final static String MENU_ICON   = "MenuIcon";
    private final static String ACTION      = "Action";
    private final static String SELECTED    = "Selected";
    private final static String ENABLED     = "Enabled";
    
    protected ResourceBundle res;
    protected Map actionMap;
    
    /** Creates a new instance of MenuFactory
     * @param bundle the ResourceBundle
     * @param actionMap map contain action with actionCommand as Key
     */
    public MenuFactory(ResourceBundle bundle, Map actionMap) {
        this.res= bundle;
        this.actionMap = actionMap;
    }
    
    /** Configure Action Map based on ResourceBundle
     * @param res ResourceBundle
     * @param actionMap Map of Actions
     */
    public static void configureActionMap(ResourceBundle res,Map actionMap) {
        Iterator it = actionMap.keySet().iterator();
        while (it.hasNext()) {
            String name = (String)it.next();
            Action action = (Action)actionMap.get(name);
            
            name = name + ".";
            
            //System.out.println(action.getClass().getName() + " " + name);
            
            // text
            try {
                action.putValue(Action.NAME,res.getString(name + Action.NAME));
            } catch (MissingResourceException e) {}
            
            
            // icon
            try {
                //System.out.println(name + Action.SMALL_ICON);
                String url = res.getString(name + Action.SMALL_ICON);
                ImageIcon icon = new ImageIcon(action.getClass().getResource(url));
                action.putValue(Action.SMALL_ICON,icon);
            } catch (MissingResourceException e) { }
            
            // tooltip
            try {
                action.putValue(Action.SHORT_DESCRIPTION,res.getString(name + Action.SHORT_DESCRIPTION));
            } catch (MissingResourceException e) {}
            
            // Mnemonic
            try {
                String str = res.getString(name + Action.MNEMONIC_KEY);
                if (str.length() == 1) {
                    action.putValue(Action.MNEMONIC_KEY, new Integer(str.charAt(0)));
                }
            } catch (MissingResourceException e) {}
            
            // Accelerator
            try {
                String str = res.getString(name + Action.ACCELERATOR_KEY);
                KeyStroke ks = KeyStroke.getKeyStroke(str);
                if (ks != null) {
                    action.putValue(Action.ACCELERATOR_KEY,ks);
                }
            } catch (MissingResourceException e) {}
        }
    }
    
    /** Creates JMenuBar
     * @param res the ResourceBundle
     * @param actionMap map contain action with actionCommand as Key
     * @return JMenuBar
     */
    public static JMenuBar createMenuBar(ResourceBundle res, Map actionMap) {
        MenuFactory factory = new MenuFactory(res,actionMap);
        JMenuBar menuBar = factory.createMenuBar();
        return menuBar;
    }
    
    /** Creates JPopupMenu
     * @param res the ResourceBundle
     * @param actionMap map contain action with actionCommand as Key
     * @param name the property key in the ResourceBundle
     * @return JMenuBar
     */
    public static JPopupMenu createPopupMenu(ResourceBundle res, Map actionMap, String name) {
        MenuFactory factory = new MenuFactory(res,actionMap);
        JPopupMenu popupMenu = factory.createPopupMenu(name);
        return popupMenu;
    }
    
    /** Return JMenuBar
     * @return JMenuBar
     */
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        String menu = res.getString("MenuBar");
        List menuList = convertToList(menu);
        Iterator it   = menuList.iterator();
        
        while (it.hasNext()) {
            menuBar.add(createMenuComponent((String)it.next()));
        }
        
        return menuBar;
    }
    
    /** Return JPopupMenu
     * @param name property Key
     * @return JPopupMenu
     */
    public JPopupMenu createPopupMenu(String name) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        String menu = res.getString(name);
        List menuList = convertToList(menu);
        Iterator it   = menuList.iterator();
        
        while (it.hasNext()) {
            popupMenu.add(createMenuComponent((String)it.next()));
        }
        
        return popupMenu;
    }
    
    protected List convertToList(String str) {
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }
    
    protected JComponent createMenuComponent(String name) {
        if (name.equals(SEPARATOR)) {
            return new JSeparator();
        }
        
        JComponent comp;
        String type = res.getString(name + "." + MENU_TYPE);
        if (type.equalsIgnoreCase(TYPE_MENU)) {
            comp = createJMenu(name);
        } else if (type.equalsIgnoreCase(TYPE_ITEM)) {
            comp = createJMenuItem(name);
        } else if (type.equalsIgnoreCase(TYPE_CHECK)) {
            comp = createJCheckBoxMenuItem(name);
        /*} else if (type.equals(TYPE_RADIO)) {
            comp = createJRadioButtonMenuItem(name);
            buttonGroup.add((AbstractButton)item);
         */
        } else {
            throw new MissingResourceException("Malformed resource " + name+MENU_TYPE,res.getClass().getName(),name+MENU_TYPE);
        }
        return comp;
    }
    
    protected JMenu createJMenu(String name) {
        JMenu menu = new JMenu();
        configureMenuItem(menu, name);
        
        String str = null;
        try {
            str = res.getString(name);
        } catch (MissingResourceException e) { }
        
        if (str == null) { return menu; }
        
        List menuList = convertToList(str);
        Iterator it = menuList.iterator();
        
        while (it.hasNext()) {
            menu.add(createMenuComponent((String)it.next()));
        }
        
        return menu;
    }
    
    protected JMenuItem createJMenuItem(String name) {
        JMenuItem menuItem = new JMenuItem();
        configureMenuItem(menuItem, name);
        return menuItem;
    }
    
    protected JCheckBoxMenuItem createJCheckBoxMenuItem(String name) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
        configureMenuItem(menuItem, name);
        
        return menuItem;
    }
    
    /** Configure MenuItem. If action is exist, it will using action property instead.
     * @param item JMenuItem
     * @param name name
     */
    protected void configureMenuItem(JMenuItem item, String name) {
        Action action = null;
        name = name + ".";
        
        //System.out.println(getClass().getName() + " " + name);
        
        // find the action
        try {
            String actionStr = res.getString(name + ACTION);
            action = (Action)actionMap.get(actionStr);
            if (action != null) {
                item.setAction(action);
            } else {
                throw new NullPointerException("Missing actionListener " + name + ACTION);
            }
        } catch (MissingResourceException e) {}
        
        // text
        try {
            if (action == null) {
                item.setText(res.getString(name + Action.NAME));
            } 
            
        } catch (MissingResourceException e) { }
        
        // Menu Icon
        // this is special Menu have different Icon(16) instead of Action.SmallIcon(24)
        try {
            //System.out.println(name + MENU_ICON);
            String url = res.getString(name + MENU_ICON);
            ImageIcon icon = new ImageIcon(getClass().getResource(url));
            item.setIcon(icon);
        } catch (MissingResourceException e) { }
        
        // Mnemonic
        try {
            String str = res.getString(name + Action.MNEMONIC_KEY);
            if (str.length() == 1) {
                if (action == null) {
                    item.setMnemonic(str.charAt(0));
                } 
            }
        } catch (MissingResourceException e) {}
        
        // Accelerator
        try {
            String str = res.getString(name + Action.ACCELERATOR_KEY);
            KeyStroke ks = KeyStroke.getKeyStroke(str);
            if (ks != null) {
                if (action == null) {
                    item.setAccelerator(ks);
                }
            }
        } catch (MissingResourceException e) {}
    }
    
    /** Return MenuItem for the specified param
     * @param menuElements to be searched
     * @param actionCommand ActionCommand for the menu
     * @return MenuItem
     */
    public static JMenuItem getMenuItem(MenuElement[] menuElements, String actionCommand) {
        return searchMenuItem(menuElements,actionCommand);
    }
    
    /** Recursive search based on specific ActionCommand
     */
    private static JMenuItem searchMenuItem(MenuElement[] menuElements , String actionCommand) {
        JMenuItem menuItem = null;
        for (int i=0; i<menuElements.length; i++) {
            MenuElement menuElement = menuElements[i];
            if (menuElement instanceof JMenu || menuElement instanceof JPopupMenu) {
                if (menuElement instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) menuElement;
                    if (actionCommand.equals(item.getActionCommand())) {
                        menuItem = item;
                        break;
                    }
                }
                JMenuItem item = searchMenuItem(menuElement.getSubElements(),actionCommand);
                if (item != null) {
                    menuItem = item;
                    break;
                }
            } else if (menuElement instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) menuElement;
                if (actionCommand.equals(item.getActionCommand())) {
                    menuItem = item;
                    break;
                }
            }
        }
        return menuItem;
    }
}
