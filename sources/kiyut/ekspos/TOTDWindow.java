package kiyut.ekspos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import kiyut.ekspos.prefs.GeneralPreferences;


/** Thought of the day Frame
 *
 * @author  tonny
 */
public class TOTDWindow extends JFrame implements ActionListener {
    private JPanel                  contentPane;
    private JPanel                  centerPane;
    private JPanel                  bottomPane;
    private JButton                 closeBtn;
    private JButton                 nextBtn;
    private JButton                 prevBtn;
    private JCheckBox               showTipCkb;
    private JTextArea               textArea;
    private ArrayList<TOTDText>     texts;
    private int                     index;  // index for ArrayList text
    private GeneralPreferences      prefs;
    
    /**
     * Creates new TOTDWindow
     */
    public TOTDWindow() {
        //loadConfig();
        loadText();
        initObject();
        setAlwaysOnTop(true);
        //setVisible(true);
    }
    
    private void initObject() {
        setTitle("Thought of the day");
        setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/kiyut/ekspos/totd16.png")));
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //setSize(new Dimension(300,300));
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        
        centerPane = new JPanel();
        bottomPane = new JPanel();
        
        centerPane.setPreferredSize(new Dimension(225,175));
        
        Color tempColor = javax.swing.UIManager.getDefaults().getColor("TextArea.background");
        Color backColor = new Color(tempColor.getRed(),tempColor.getGreen(),tempColor.getBlue(),tempColor.getAlpha());
        
        textArea = new JTextArea("Thought of the day");
        textArea.setBackground(backColor);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createTitledBorder("Thought of the day"));
        textArea.setEditable(false);
        centerPane.setLayout(new BorderLayout());
        centerPane.add(textArea,BorderLayout.CENTER);
        
        prefs = GeneralPreferences.getInstance();
        showTipCkb = new JCheckBox("Show next time");
        showTipCkb.setSelected(prefs.getBoolean(GeneralPreferences.TOTD_KEY));
        centerPane.add(showTipCkb,BorderLayout.SOUTH);
        
        closeBtn = new JButton("Close");
        prevBtn = new JButton("Previous");
        nextBtn = new JButton("Next");
        bottomPane.add(prevBtn);
        bottomPane.add(nextBtn);
        bottomPane.add(closeBtn);
        
        closeBtn.setActionCommand("close");
        prevBtn.setActionCommand("previous");
        nextBtn.setActionCommand("next");
        
        closeBtn.addActionListener(this);
        prevBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        
        contentPane.add(centerPane,BorderLayout.CENTER);
        contentPane.add(bottomPane,BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(closeBtn);
        
        showText();
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("close")) {
            closeForm();
        } else if (e.getActionCommand().equalsIgnoreCase("next")) {
            showNext();
        } else if (e.getActionCommand().equalsIgnoreCase("previous")) {
            showPrevious();
        }
        
    }
    
    private void loadText() {
        index = 0;
        texts = new ArrayList<TOTDText>();
        
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = getClass().getResourceAsStream("/kiyut/ekspos/totd.txt");
            reader = new BufferedReader(new InputStreamReader(is));
            String str;
            while ((str=reader.readLine()) != null) {
                TOTDText totdText = new TOTDText();
                int indexOf = str.indexOf(',');
                try {
                    totdText.setAuthor(str.substring(0,indexOf).trim());
                    totdText.setText(str.substring(indexOf+1,str.length()).trim());
                    texts.add(totdText);
                } catch (Exception ex) {
                    System.err.println("-----TOTD text in wrong format-----");
                    System.err.println(str);
                    continue;
                }
            }
        } catch (Exception ex) {
            System.err.println("Unable to load totd.txt.");
            //System.exit(1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {}
                reader = null;
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) { }
                is = null;
            }
        }
        Collections.shuffle(texts);
    }
    
    private void showNext() {
        if (index < texts.size()-1) {
            index++;
        } else {
            index = 0;
        }
        showText();
    }
    
    private void showPrevious() {
        if (index > 0) {
            index--;
        } else {
            index = texts.size() - 1;
        }
        showText();
    }
    
    private void showText() {
        try {
            // clear the text
            Document doc = textArea.getDocument();
            doc.remove(0,doc.getLength());
        } catch (Exception e) {
            // do nothing
        }
        
        // insert new text
        TOTDText totdText = texts.get(index);
        textArea.insert(totdText.getText() + "\n\n-" + totdText.getAuthor(),0);
    }
    
    private void closeForm() {
        prefs.putBoolean(GeneralPreferences.TOTD_KEY, showTipCkb.isSelected());
        prefs.save();
        this.dispatchEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
    }
    
    
    public class TOTDText {
        String author;
        String text;
        
        public TOTDText() {
            
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getAuthor() {
            return this.author;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getText() {
            return this.text;
        }
    }
}
