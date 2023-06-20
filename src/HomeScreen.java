import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

public class Main {
private JFrame frame;
	private JTextField textFieldSearch;
	private Map<String, Slang> baseSlangs = new HashMap<String, Slang>();
	
	private static String SLANGS_RAW_FILE_PATH = "slang_raw.txt";
	private static String SLANGS_FILE_PATH = "slang.txt";
	private static String HISTORIES_FILE_PATH = "histories.txt";
	private static String DATA_PATH = "data/";
	private static int NUMBER_OF_QUIZ_ANSWER = 4;
	static final ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
	private DefaultListModel<String> listMode = new DefaultListModel<String>();
	private DefaultListModel<String> historiesMode = new DefaultListModel<String>();
	private JButton btnReset = new JButton("Reset");
	private JLabel lblHistories = new JLabel("");
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HomeScreen window = new HomeScreen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    }
    public Main() {
		setupData();
		initialize();
	}
	
	private void setupData() {
		writeDownLocalFiles(false);
		baseSlangs.clear();
		baseSlangs = readSlangsData(SLANGS_FILE_PATH);
	}
	
	private void writeDownLocalFiles(boolean isReset) {
		try {
			
			File f = new File(SLANGS_FILE_PATH);
			if (!f.exists() || isReset) {
				f.createNewFile();
				File histories = new File(HISTORIES_FILE_PATH);
				histories.createNewFile();
				
				FileWriter fw = new FileWriter(SLANGS_FILE_PATH, false);
				InputStream bin = loader.getResourceAsStream(SLANGS_RAW_FILE_PATH);
				BufferedReader reader = new BufferedReader(new InputStreamReader(bin, "utf8"));
				while (reader.ready()) {
					String line = reader.readLine();
					fw.write(line);
					fw.write("\n");
				}
				
				reader.close();
				bin.close();
				fw.close();
				
				System.out.println("writeDownLocalFiles from " + loader.getResource(SLANGS_RAW_FILE_PATH).getFile() + " to " + SLANGS_FILE_PATH);
			}
			
		} catch(Exception e ) {
			e.printStackTrace();
		}
	}
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 666, 608);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblWord = new JLabel("New label");
		lblWord.setBounds(34, 32, 367, 16);
		frame.getContentPane().add(lblWord);
		
		textFieldSearch = new JTextField();
		textFieldSearch.setBounds(34, 131, 244, 36);
		frame.getContentPane().add(textFieldSearch);
		textFieldSearch.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(278, 136, 85, 29);
		btnSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
			
		});
		frame.getContentPane().add(btnSearch);
		
		lblHistories.setText(readHistoriesData());
		JScrollPane historiesScrollPane = new JScrollPane();
		historiesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		historiesScrollPane.setBounds(34, 60, 609, 59);
		historiesScrollPane.setViewportView(lblHistories);
		frame.getContentPane().add(historiesScrollPane);
		
		historiesScrollPane.setViewportView(lblHistories);
		
		JButton btnAdd = new JButton("New Word");
		btnAdd.setBounds(526, 27, 117, 29);
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Hashtable<String, String> result = showCreateDialog(frame);
				String slag = result.get("slag");
				String mean = result.get("mean");
				if (!slag.isEmpty() && !mean.isEmpty()) {
					if (baseSlangs.containsKey(slag)) {
						showDialog(frame, "Warning", "This word is existed!");
					} else {
						Slang s = new Slang();
						s.setMeaning(mean);
						s.setSlag(slag);
						listMode.addElement(s.toString());
						baseSlangs.put(slag, s);
						
						writeData(SLANGS_FILE_PATH, slag + "`" + mean, true);
						showDialog(frame, "Message", "This word is saved!");
					}
				}
			}
			
		});
		frame.getContentPane().add(btnAdd);
		
		btnReset.setBounds(26, 540, 117, 29);
		btnReset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				writeDownLocalFiles(true);
				setupData();
				resetResultList();
				lblHistories.setText(readHistoriesData());
			}
			
		});
		frame.getContentPane().add(btnReset);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setEnabled(false);
		btnDelete.setBounds(575, 137, 68, 26);
		frame.getContentPane().add(btnDelete);
		
		JButton btnEdit = new JButton("Edit");
		btnEdit.setEnabled(false);
		btnEdit.setBounds(508, 136, 68, 29);
		frame.getContentPane().add(btnEdit);
		
		JList<String> listResult = new JList<String>(listMode);
		listResult.setVisibleRowCount(30);
		listResult.addListSelectionListener((ListSelectionListener) new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					String selected = listResult.getSelectedValue();
	                if (selected != null) {
	                	btnEdit.setEnabled(true);
	                	btnDelete.setEnabled(true);
	                }
	            }
			}
			
		});
		JScrollPane scrollPane = new JScrollPane(listResult);
		scrollPane.setBounds(34, 188, 609, 349);
		frame.getContentPane().add(scrollPane);
		
		btnEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = listResult.getSelectedValue().toString();
				String[] data = selected.split(": ");
            
            	if (data.length > 1) {
            		String oldSlag = data[0];
            		String oldMean = data[1];
            		Hashtable<String, String> result = showEditDialog(frame, oldSlag, oldMean);
            		String slag = result.get("slag");
     				String mean = result.get("mean");
     				boolean isUpdatableSlag = !baseSlangs.containsKey(slag) && !slag.contentEquals(oldSlag);
     				boolean isChanged = (!oldMean.contentEquals(mean) && oldSlag.contentEquals(slag)) || (!oldMean.contentEquals(mean) && isUpdatableSlag) || (oldMean.contentEquals(mean) && isUpdatableSlag);
     				
            		if (isChanged) {
            			int updateIndex = listMode.indexOf(selected);
            			listMode.set(updateIndex, slag + ": " + mean);
            			
            			updateInFile(SLANGS_FILE_PATH, oldSlag + "`" + oldMean, slag + "`" + mean);
            			setupData();
            		}
            	}
			}
			
		});
		
		btnDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selected = listResult.getSelectedValue().toString();
				String[] data = selected.split(": ");
            
            	if (data.length > 1) {
            		// show confirm dialog
            		int result = showConfirmDialog("Really", "Do you really want to delete it?");
            		if (result == 0) {
            			String oldSlag = data[0];
                		String oldMean = data[1];
                		int updateIndex = listMode.indexOf(selected);
            			listMode.remove(updateIndex);
            			baseSlangs.remove(oldSlag);
            			
            			updateInFile(SLANGS_FILE_PATH, oldSlag + "`" + oldMean + "\n", "");
            		}
            	}
			}
			
		});
		
		setupViews(lblWord);
		
		JButton btnClear = new JButton("Clear");
		btnClear.setBounds(356, 136, 68, 29);
		btnClear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textFieldSearch.setText("");
				resetResultList();
			}
			
		});
		frame.getContentPane().add(btnClear);
		
		JButton btnQuiz1 = new JButton("Quiz 1");
		btnQuiz1.setBounds(413, 540, 117, 29);
		btnQuiz1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSlagQuiz();
			}
			
		});
		frame.getContentPane().add(btnQuiz1);
		
		JButton btnQuiz2 = new JButton("Quiz 2");
		btnQuiz2.setBounds(526, 540, 117, 29);
		btnQuiz2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showDefinitionQuiz();
				
			}
			
		});
		frame.getContentPane().add(btnQuiz2);
	}
	private Hashtable<String, String> showCreateDialog(JFrame frame) {
	    Hashtable<String, String> newSlang = new Hashtable<String, String>();

	    JPanel panel = new JPanel(new BorderLayout(5, 5));

	    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
	    label.add(new JLabel("Slag", SwingConstants.RIGHT));
	    label.add(new JLabel("Meaning", SwingConstants.RIGHT));
	    panel.add(label, BorderLayout.WEST);

	    JPanel controls = new JPanel(new GridLayout(0, 1, 10, 2));
	    JTextField slag = new JTextField();
	    controls.add(slag);
	    JTextField mean = new JTextField();
	    controls.add(mean);
	    panel.add(controls, BorderLayout.CENTER);

	    JOptionPane.showMessageDialog(frame, panel, "New Slangword", JOptionPane.PLAIN_MESSAGE);

	    newSlang.put("slag", slag.getText());
	    newSlang.put("mean", mean.getText());
	    return newSlang;
	}
	private void showDialog(JFrame frame, String title, String content) {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
	    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
	    label.add(new JLabel(content, SwingConstants.RIGHT));
	    panel.add(label, BorderLayout.WEST);
		JOptionPane.showMessageDialog(frame, panel, title, JOptionPane.PLAIN_MESSAGE);
	}
		private ArrayList<Slang> randomQuizOptions() {
		Object[] keys = baseSlangs.keySet().toArray();
		ArrayList<Slang> slangOptions = new ArrayList<Slang>();
		
		while (slangOptions.size() < NUMBER_OF_QUIZ_ANSWER) {
			int randomIndex = new Random().nextInt(keys.length);
			Slang randomSlang = baseSlangs.get(keys[randomIndex]);
			if (!slangOptions.contains(randomSlang)) {
				slangOptions.add(randomSlang);
			}
		}
}