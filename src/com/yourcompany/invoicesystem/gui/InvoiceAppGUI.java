/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.gui;

// Core Java utilities
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Swing components and utilities
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter; // Added for TableRowSorter

// AWT (Abstract Window Toolkit)
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL; // For loading resources

// Project-specific classes
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;

public class InvoiceAppGUI extends JFrame {

    // DAOs
    private ProductDAO productDAO;
    private InvoiceDAO invoiceDAO;
    private InvoiceItemDAO invoiceItemDAO;

    // Product Table components
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField productSearchField;

    // Current Invoice Table components
    private JTable currentInvoiceTable;
    private DefaultTableModel currentInvoiceTableModel;

    // Control components
    private JSpinner quantitySpinner;
    private JButton addToBillButton;
    private JButton finalizeBillButton;
    private JButton clearBillButton;

    // Summary Panel components
    private JLabel subtotalLabelValue;
    private JTextField discountPercentField;
    private JLabel discountAmountLabelValue;
    private JLabel grandTotalLabelValue;
    private JLabel invoiceDateLabel;

    // In-memory storage and calculations
    private List<Object[]> currentBillItemsData = new ArrayList<>();
    private BigDecimal currentSubtotal = BigDecimal.ZERO;

    // --- UI Styling Constants ---
    private static final Color PRIMARY_COLOR = new Color(60, 70, 200); // Brighter Blue
    private static final Color SECONDARY_COLOR = new Color(245, 248, 255); // Lighter Alice Blue
    private static final Color ACCENT_COLOR = new Color(220, 53, 69); // Bootstrap Danger Red
    private static final Color SUCCESS_COLOR = new Color(25, 135, 84); // Bootstrap Success Green
    private static final Color TEXT_COLOR = new Color(33, 37, 41); // Bootstrap Dark Gray
    private static final Color BORDER_COLOR = new Color(222, 226, 230); // Bootstrap Light Gray

    private static final Font HEADING_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_LABEL_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    private static final Font TABLE_HEADER_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private static final Font TABLE_CELL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private static final Font TOTAL_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 16);


    // --- Icon Paths (User needs to provide these images) ---
    // Place these in a folder like 'src/main/resources/icons/' if using Maven,
    // or an 'icons' folder at the project root (and add to classpath).
    private static final String APP_ICON_PATH = "/icons/app_icon_32.png";
    private static final String ADD_ICON_PATH = "/icons/add_to_cart_24.png";
    private static final String SAVE_ICON_PATH = "/icons/save_bill_24.png";
    private static final String CLEAR_ICON_PATH = "/icons/clear_bill_24.png";
    private static final String SEARCH_ICON_PATH = "/icons/search_16.png"; // Smaller for label

    // Tax and payment tracking
    private boolean taxEnabled = false;
    private BigDecimal taxRate = new BigDecimal("20.00");
    private JLabel taxLabelValue;
    
    // User session
    private JLabel currentUserLabel;
    
    // Barcode input field
    private JTextField barcodeField;
    
    public InvoiceAppGUI() {
        productDAO = new ProductDAO();
        invoiceDAO = new InvoiceDAO();
        invoiceItemDAO = new InvoiceItemDAO();
        currentSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        initLookAndFeel();
        
        // Show login dialog
        if (!showLoginDialog()) {
            System.exit(0); // Exit if login cancelled
        }
        
        initFrame();
        initComponents();
        loadProductData();
        checkLowStockProducts();
        applyCustomStylingToDialogs(); // Apply general styling to JOptionPane
    }
    
    private boolean showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);
        return loginDialog.isLoginSuccessful();
    }

    private void initLookAndFeel() {
        try {
            // Using FlatLaf for a modern look - user needs to add this library
            // If not available, Nimbus or System L&F will be used.
            // To use FlatLaf, add the dependency:
            // Maven:
            // <dependency>
            // <groupId>com.formdev</groupId>
            // <artifactId>flatlaf</artifactId>
            // <version>3.x.x</version> // </dependency>
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            System.err.println("FlatLaf not found, falling back to Nimbus/System L&F.");
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        }
    }

    private void initFrame() {
        setTitle("ProBilling - Invoice Management System");
        // Attempt to load application icon
        URL iconURL = getClass().getResource(APP_ICON_PATH);
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        } else {
            System.err.println("Warning: Application icon not found at " + APP_ICON_PATH);
        }

        setSize(1200, 800);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Add menu bar
        setJMenuBar(createMenuBar());
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(238, 238, 238)); // A slightly off-white
        setContentPane(mainPanel);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem newInvoiceItem = new JMenuItem("New Invoice");
        newInvoiceItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newInvoiceItem.addActionListener(e -> clearBill());
        fileMenu.add(newInvoiceItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        JMenuItem viewInvoicesItem = new JMenuItem("Invoice History");
        viewInvoicesItem.setAccelerator(KeyStroke.getKeyStroke("control H"));
        viewInvoicesItem.addActionListener(e -> showInvoiceHistory());
        viewMenu.add(viewInvoicesItem);
        
        JMenuItem viewProductsItem = new JMenuItem("Product Management");
        viewProductsItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
        viewProductsItem.addActionListener(e -> showProductManagement());
        viewMenu.add(viewProductsItem);
        
        JMenuItem returnsItem = new JMenuItem("Returns & Refunds");
        returnsItem.setAccelerator(KeyStroke.getKeyStroke("control T"));
        returnsItem.addActionListener(e -> showReturns());
        viewMenu.add(returnsItem);
        
        menuBar.add(viewMenu);
        
        // Reports Menu
        JMenu reportsMenu = new JMenu("Reports");
        reportsMenu.setMnemonic('R');
        
        JMenuItem reportsItem = new JMenuItem("Generate Reports");
        reportsItem.setAccelerator(KeyStroke.getKeyStroke("control R"));
        reportsItem.addActionListener(e -> showReports());
        reportsMenu.add(reportsItem);
        
        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.setAccelerator(KeyStroke.getKeyStroke("control D"));
        dashboardItem.addActionListener(e -> showDashboard());
        reportsMenu.add(dashboardItem);
        
        menuBar.add(reportsMenu);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem backupItem = new JMenuItem("Backup & Restore");
        backupItem.setAccelerator(KeyStroke.getKeyStroke("control B"));
        backupItem.addActionListener(e -> showBackupRestore());
        toolsMenu.add(backupItem);
        
        menuBar.add(toolsMenu);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void showInvoiceHistory() {
        InvoiceHistoryDialog dialog = new InvoiceHistoryDialog(this);
        dialog.setVisible(true);
    }
    
    private void showProductManagement() {
        ProductManagementDialog dialog = new ProductManagementDialog(this);
        dialog.setVisible(true);
        loadProductData(); // Refresh product list after dialog closes
    }
    
    private void showReports() {
        ReportsDialog dialog = new ReportsDialog(this);
        dialog.setVisible(true);
    }
    
    private void showReturns() {
        ReturnsDialog dialog = new ReturnsDialog(this);
        dialog.setVisible(true);
    }
    
    private void showDashboard() {
        DashboardDialog dialog = new DashboardDialog(this);
        dialog.setVisible(true);
    }
    
    private void showBackupRestore() {
        BackupRestoreDialog dialog = new BackupRestoreDialog(this);
        dialog.setVisible(true);
    }
    
    private void showAbout() {
        String message = "ProBilling - Invoice Management System\n\n" +
                        "Version: 2.0\n" +
                        "Build Date: November 2025\n\n" +
                        "Features:\n" +
                        "• Invoice Creation & Management\n" +
                        "• Inventory Tracking\n" +
                        "• Product Management\n" +
                        "• Sales Reports & Analytics\n" +
                        "• Transaction-safe Operations\n\n" +
                        "© 2025 Your Company";
        
        JOptionPane.showMessageDialog(this, message, "About ProBilling", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void printReceipt(Invoice invoice, int invoiceId, BigDecimal discountPercent,
                             BigDecimal discountAmount, BigDecimal subtotal) {
        try {
            com.yourcompany.invoicesystem.util.ReceiptPrinter printer = 
                new com.yourcompany.invoicesystem.util.ReceiptPrinter();
            
            // Generate receipt text
            String receiptText = printer.generateDetailedReceipt(invoice, discountPercent, 
                                                                discountAmount, subtotal);
            
            // Show preview dialog
            JTextArea textArea = new JTextArea(receiptText);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 11));
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(650, 700));
            
            String[] options = {"Save to File", "Print", "Close"};
            int choice = JOptionPane.showOptionDialog(this,
                scrollPane,
                "Receipt Preview - Invoice #" + invoiceId,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choice == 0) { // Save to File
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new java.io.File("Receipt_" + invoiceId + ".txt"));
                
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    if (printer.saveDetailedReceiptToFile(invoice, discountPercent, 
                                                         discountAmount, subtotal, filePath)) {
                        JOptionPane.showMessageDialog(this,
                            "Receipt saved successfully to:\n" + filePath,
                            "Save Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to save receipt",
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (choice == 1) { // Print
                // Use Java print service
                try {
                    textArea.print();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Print error: " + ex.getMessage(),
                        "Print Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating receipt: " + e.getMessage(),
                "Receipt Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void initComponents() {
        JPanel productSectionPanel = createProductSectionPanel();
        add(productSectionPanel, BorderLayout.WEST);

        JPanel currentInvoicePanel = createCurrentInvoicePanel();
        add(currentInvoicePanel, BorderLayout.CENTER);

        JPanel controlAndSummaryPanel = createControlAndSummaryPanel();
        add(controlAndSummaryPanel, BorderLayout.EAST);

        invoiceDateLabel.setText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
    }

    private JPanel createProductSectionPanel() {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 10));
        sectionPanel.setOpaque(false);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(5,0)); // Use BorderLayout for icon alignment
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0,0,5,0));

        JLabel searchIconLabel = new JLabel();
        URL searchIconURL = getClass().getResource(SEARCH_ICON_PATH);
        if (searchIconURL != null) {
            searchIconLabel.setIcon(new ImageIcon(searchIconURL));
        } else {
            searchIconLabel.setText("S:"); // Fallback text
            System.err.println("Warning: Search icon not found at " + SEARCH_ICON_PATH);
        }
        searchPanel.add(searchIconLabel, BorderLayout.WEST);

        productSearchField = new JTextField();
        productSearchField.setFont(LABEL_FONT);
        productSearchField.setToolTipText("Enter product name or ID to filter");
        productSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterProductTable();
            }
        });
        searchPanel.add(productSearchField, BorderLayout.CENTER);
        sectionPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel tablePanel = createProductPanel();
        sectionPanel.add(tablePanel, BorderLayout.CENTER);

        sectionPanel.setPreferredSize(new Dimension(450, 0)); // Increased width a bit
        return sectionPanel;
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // White background for table panel
        panel.setBorder(createStyledTitledBorder("Available Products"));

        String[] columnNames = {"ID", "Name", "Price (€)", "Stock"};
        productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) return Integer.class;
                if (columnIndex == 2) return BigDecimal.class;
                return String.class;
            }
        };
        productTable = new JTable(productTableModel);
        styleTable(productTable, TABLE_HEADER_FONT, TABLE_CELL_FONT);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(28); // Increased row height
        productTable.getTableHeader().setReorderingAllowed(false);
        productTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCurrentInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createStyledTitledBorder("Current Invoice"));

        invoiceDateLabel = new JLabel("Date: ");
        invoiceDateLabel.setFont(BOLD_LABEL_FONT);
        invoiceDateLabel.setForeground(TEXT_COLOR);
        invoiceDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        invoiceDateLabel.setBorder(new EmptyBorder(8,0,8,10));
        panel.add(invoiceDateLabel, BorderLayout.NORTH);

        String[] columnNames = {"Product Name", "Qty", "Unit Price (€)", "Item Total (€)"};
        currentInvoiceTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        currentInvoiceTable = new JTable(currentInvoiceTableModel);
        styleTable(currentInvoiceTable, TABLE_HEADER_FONT, TABLE_CELL_FONT);
        currentInvoiceTable.setRowHeight(28);
        currentInvoiceTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(currentInvoiceTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createControlAndSummaryPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(createStyledTitledBorder("Order Details & Actions"));
        mainPanel.setPreferredSize(new Dimension(320, 0));

        // --- Controls Panel ---
        JPanel controlsOuterPanel = new JPanel(new BorderLayout());
        controlsOuterPanel.setOpaque(false);
        controlsOuterPanel.setBorder(new EmptyBorder(15,15,15,15));

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbcCtrl = new GridBagConstraints();
        gbcCtrl.fill = GridBagConstraints.HORIZONTAL;
        gbcCtrl.insets = new Insets(8, 5, 8, 5);
        gbcCtrl.weightx = 1.0;

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(LABEL_FONT);
        qtyLabel.setForeground(TEXT_COLOR);
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 0; gbcCtrl.anchor = GridBagConstraints.LINE_START; gbcCtrl.weightx = 0.3;
        controlsPanel.add(qtyLabel, gbcCtrl);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 999, 1);
        quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinner.setFont(LABEL_FONT);
        quantitySpinner.setPreferredSize(new Dimension(80, 30));
        gbcCtrl.gridx = 1; gbcCtrl.gridy = 0; gbcCtrl.anchor = GridBagConstraints.LINE_END; gbcCtrl.weightx = 0.7;
        controlsPanel.add(quantitySpinner, gbcCtrl);

        addToBillButton = createStyledButton("Add to Bill", PRIMARY_COLOR, Color.WHITE, ADD_ICON_PATH);
        addToBillButton.setToolTipText("Add selected product with specified quantity");
        addToBillButton.addActionListener(e -> addItemToBill());
        gbcCtrl.gridx = 0; gbcCtrl.gridy = 1; gbcCtrl.gridwidth = 2; gbcCtrl.insets = new Insets(15, 5, 5, 5);
        controlsPanel.add(addToBillButton, gbcCtrl);

        controlsOuterPanel.add(controlsPanel, BorderLayout.NORTH);
        mainPanel.add(controlsOuterPanel);

        // --- Summary Panel ---
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        GridBagConstraints gbcSum = new GridBagConstraints();
        gbcSum.insets = new Insets(10, 5, 10, 5);
        gbcSum.anchor = GridBagConstraints.LINE_END; gbcSum.weightx = 0.4;

        gbcSum.gridx = 0; gbcSum.gridy = 0;
        summaryPanel.add(createStyledLabel("Subtotal:"), gbcSum);
        gbcSum.gridx = 1; gbcSum.anchor = GridBagConstraints.LINE_START; gbcSum.weightx = 0.6;
        subtotalLabelValue = createStyledValueLabel("€ 0.00");
        summaryPanel.add(subtotalLabelValue, gbcSum);

        gbcSum.gridx = 0; gbcSum.gridy = 1; gbcSum.anchor = GridBagConstraints.LINE_END;
        summaryPanel.add(createStyledLabel("Discount (%):"), gbcSum);
        gbcSum.gridx = 1; gbcSum.anchor = GridBagConstraints.LINE_START;
        discountPercentField = new JTextField("0", 5);
        discountPercentField.setFont(LABEL_FONT);
        discountPercentField.setPreferredSize(new Dimension(70, 30));
        discountPercentField.setHorizontalAlignment(JTextField.RIGHT);
        discountPercentField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { updateTotals(); }
        });
        summaryPanel.add(discountPercentField, gbcSum);

        gbcSum.gridx = 0; gbcSum.gridy = 2; gbcSum.anchor = GridBagConstraints.LINE_END;
        summaryPanel.add(createStyledLabel("Discount Amt:"), gbcSum);
        gbcSum.gridx = 1; gbcSum.anchor = GridBagConstraints.LINE_START;
        discountAmountLabelValue = createStyledValueLabel("€ 0.00");
        summaryPanel.add(discountAmountLabelValue, gbcSum);

        JSeparator separator = new JSeparator();
        gbcSum.gridx = 0; gbcSum.gridy = 3; gbcSum.gridwidth = 2; gbcSum.fill = GridBagConstraints.HORIZONTAL; gbcSum.insets = new Insets(12,0,12,0);
        summaryPanel.add(separator, gbcSum);
        gbcSum.fill = GridBagConstraints.NONE; gbcSum.gridwidth = 1; gbcSum.insets = new Insets(10, 5, 10, 5);


        gbcSum.gridx = 0; gbcSum.gridy = 4; gbcSum.anchor = GridBagConstraints.LINE_END;
        JLabel grandTotalTextLabel = createStyledLabel("Grand Total:");
        grandTotalTextLabel.setFont(TOTAL_FONT);
        summaryPanel.add(grandTotalTextLabel, gbcSum);

        gbcSum.gridx = 1; gbcSum.anchor = GridBagConstraints.LINE_START;
        grandTotalLabelValue = createStyledValueLabel("€ 0.00");
        grandTotalLabelValue.setFont(TOTAL_FONT);
        grandTotalLabelValue.setForeground(SUCCESS_COLOR); // Use success color for grand total
        summaryPanel.add(grandTotalLabelValue, gbcSum);

        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createVerticalGlue());

        // --- Action Buttons Panel ---
        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        clearBillButton = createStyledButton("Clear Bill", new Color(108, 117, 125), Color.WHITE, CLEAR_ICON_PATH); // Bootstrap Secondary
        clearBillButton.setToolTipText("Clear all items from the current bill");
        clearBillButton.addActionListener(e -> clearBill());

        finalizeBillButton = createStyledButton("Finalize & Save", SUCCESS_COLOR, Color.WHITE, SAVE_ICON_PATH);
        finalizeBillButton.setToolTipText("Save the current invoice and update stock");
        finalizeBillButton.addActionListener(e -> finalizeBill());

        actionButtonsPanel.add(clearBillButton);
        actionButtonsPanel.add(finalizeBillButton);
        mainPanel.add(actionButtonsPanel);

        return mainPanel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createStyledValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BOLD_LABEL_FONT);
        label.setForeground(PRIMARY_COLOR);
        return label;
    }

    private TitledBorder createStyledTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR), title); // Top line only
        titledBorder.setTitleFont(HEADING_FONT);
        titledBorder.setTitleColor(TEXT_COLOR); // Darker title
        titledBorder.setTitleJustification(TitledBorder.LEFT);
        titledBorder.setTitlePosition(TitledBorder.TOP);
        // Add padding inside the border title
        return new TitledBorder(titledBorder.getBorder(), title, titledBorder.getTitleJustification(),
                                titledBorder.getTitlePosition(), titledBorder.getTitleFont(), titledBorder.getTitleColor());
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20)); // More padding
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalTextPosition(SwingConstants.RIGHT); // Text to the right of icon
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(8); // Gap between icon and text

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                URL iconURL = getClass().getResource(iconPath);
                if (iconURL != null) {
                    button.setIcon(new ImageIcon(iconURL));
                } else {
                    System.err.println("Warning: Button icon not found at " + iconPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading icon " + iconPath + ": " + e.getMessage());
            }
        }
        return button;
    }

    private void styleTable(JTable table, Font headerFont, Font cellFont) {
        JTableHeader header = table.getTableHeader();
        header.setFont(headerFont);
        header.setBackground(new Color(248, 249, 250)); // Very light gray for header
        header.setForeground(TEXT_COLOR);
        header.setPreferredSize(new Dimension(100, 35));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        header.setBorder(BorderFactory.createMatteBorder(0,0,1,0, BORDER_COLOR)); // Bottom border for header

        table.setFont(cellFont);
        table.setGridColor(new Color(233, 236, 239)); // Lighter grid color
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(cellFont);
                if (isSelected) {
                    setBackground(PRIMARY_COLOR.brighter());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? new Color(252, 252, 253) : Color.WHITE);
                    setForeground(TEXT_COLOR);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8)); // Cell padding
                if (value instanceof Number) {
                    setHorizontalAlignment(JLabel.RIGHT);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                if (column == 1 && value instanceof String && ((String)value).length() > 25) { // Truncate long product names
                    setToolTipText((String)value); // Show full name on hover
                    setText(((String)value).substring(0,22) + "...");
                } else {
                    setToolTipText(null);
                }

                return this;
            }
        });
    }

    private void filterProductTable() {
        String searchText = productSearchField.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        productTable.setRowSorter(sorter);

        if (searchText.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // (?i) for case-insensitive search
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 0, 1)); // Search ID (col 0) and Name (col 1)
        }
    }

    private void addItemToBill() {
        int selectedRowInView = productTable.getSelectedRow();
        if (selectedRowInView == -1) {
            showStyledMessageDialog("Please select a product from the list first.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRowInModel = productTable.convertRowIndexToModel(selectedRowInView);

        int productId = (int) productTableModel.getValueAt(selectedRowInModel, 0);
        String productName = (String) productTableModel.getValueAt(selectedRowInModel, 1);
        BigDecimal unitPrice = (BigDecimal) productTableModel.getValueAt(selectedRowInModel, 2);
        int availableStock = (int) productTableModel.getValueAt(selectedRowInModel, 3);
        int quantity;
        try {
            quantity = (int) quantitySpinner.getValue();
            if (quantity <= 0) {
                showStyledMessageDialog("Please enter a quantity greater than zero.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            showStyledMessageDialog("Invalid quantity value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (quantity > availableStock) {
            showStyledMessageDialog("Insufficient stock for " + productName + ". Only " + availableStock + " remaining.", "Stock Alert", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
        Object[] rowData = {productName, quantity, unitPrice, itemTotal};
        currentInvoiceTableModel.addRow(rowData);

        Object[] billItemData = {productId, productName, quantity, unitPrice, itemTotal, availableStock};
        currentBillItemsData.add(billItemData);

        updateTotals();
        quantitySpinner.setValue(1);
        productSearchField.setText("");
        filterProductTable();
        productSearchField.requestFocusInWindow();
    }

    private void updateTotals() {
        currentSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (Object[] itemData : currentBillItemsData) {
            BigDecimal itemTotal = (BigDecimal) itemData[4];
            currentSubtotal = currentSubtotal.add(itemTotal);
        }
        subtotalLabelValue.setText(String.format("€ %.2f", currentSubtotal));

        BigDecimal discountPercent = BigDecimal.ZERO;
        try {
            String discountText = discountPercentField.getText().trim();
            if (!discountText.isEmpty()) {
                discountPercent = new BigDecimal(discountText);
                if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0) {
                    discountPercent = BigDecimal.ZERO;
                    if (!discountText.equals("0")) { // Avoid warning if user just typed 0
                         showStyledMessageDialog("Discount percentage must be between 0 and 100.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                         discountPercentField.setText("0"); // Reset
                    }
                }
            }
        } catch (NumberFormatException e) {
            discountPercent = BigDecimal.ZERO;
            if (!discountPercentField.getText().trim().isEmpty()) { // Show error only if field wasn't empty
                showStyledMessageDialog("Invalid discount percentage format.", "Format Error", JOptionPane.ERROR_MESSAGE);
                discountPercentField.setText("0"); // Reset
            }
        }

        BigDecimal discountAmount = currentSubtotal.multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        discountAmountLabelValue.setText(String.format("€ %.2f", discountAmount));

        BigDecimal grandTotal = currentSubtotal.subtract(discountAmount);
        grandTotalLabelValue.setText(String.format("€ %.2f", grandTotal));
    }

    private void finalizeBill() {
        if (currentBillItemsData.isEmpty()) {
            showStyledMessageDialog("Cannot finalize an empty bill.", "Empty Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updateTotals(); // Ensure calculations are current
        BigDecimal finalSubtotal = currentSubtotal;
        BigDecimal discountPercentVal;
        try {
            discountPercentVal = new BigDecimal(discountPercentField.getText().trim());
             if (discountPercentVal.compareTo(BigDecimal.ZERO) < 0 || discountPercentVal.compareTo(new BigDecimal("100")) > 0) {
                discountPercentVal = BigDecimal.ZERO; // Correct invalid percentage
            }
        } catch (NumberFormatException e) {
            discountPercentVal = BigDecimal.ZERO;
        }
        BigDecimal finalDiscountAmount = finalSubtotal.multiply(discountPercentVal)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalGrandTotal = finalSubtotal.subtract(finalDiscountAmount);

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDiscountPercentage(discountPercentVal.doubleValue());
        invoice.setTaxAmount(0.0); // Tax calculation can be added later
        invoice.setTotalAmount(finalGrandTotal);
        
        // Create list of invoice items for payment dialog
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for (Object[] itemData : currentBillItemsData) {
            int productId = (int) itemData[0];
            int quantity = (int) itemData[2];
            BigDecimal priceAtSale = (BigDecimal) itemData[3];

            InvoiceItem item = new InvoiceItem();
            item.setProductID(productId);
            item.setQuantity(quantity);
            item.setPriceAtSale(priceAtSale);
            invoiceItems.add(item);
        }
        
        // Open payment dialog
        PaymentDialog paymentDialog = new PaymentDialog(this, invoice, invoiceItems);
        paymentDialog.setVisible(true);
        
        // Check if payment was successful
        if (paymentDialog.isPaymentSuccessful()) {
            Invoice savedInvoice = paymentDialog.getInvoice();
            
            showStyledMessageDialog("Invoice #" + savedInvoice.getInvoiceID() + " saved successfully!\nPayment processed and stock updated.", 
                                  "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            
            // Offer to print receipt
            int printChoice = JOptionPane.showConfirmDialog(this,
                "Would you like to print/save a receipt for this invoice?",
                "Print Receipt",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (printChoice == JOptionPane.YES_OPTION) {
                printReceipt(savedInvoice, savedInvoice.getInvoiceID(), discountPercentVal, finalDiscountAmount, finalSubtotal);
            }
            
            // Clear the bill after successful payment
            clearBill();
            loadProductData(); // Refresh product list to show updated stock
        }
    }
    
    private void finalizeBillOld() {
        if (currentBillItemsData.isEmpty()) {
            showStyledMessageDialog("Cannot finalize an empty bill.", "Empty Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updateTotals(); // Ensure calculations are current
        BigDecimal finalSubtotal = currentSubtotal;
        BigDecimal discountPercentVal;
        try {
            discountPercentVal = new BigDecimal(discountPercentField.getText().trim());
             if (discountPercentVal.compareTo(BigDecimal.ZERO) < 0 || discountPercentVal.compareTo(new BigDecimal("100")) > 0) {
                discountPercentVal = BigDecimal.ZERO; // Correct invalid percentage
            }
        } catch (NumberFormatException e) {
            discountPercentVal = BigDecimal.ZERO;
        }
        BigDecimal finalDiscountAmount = finalSubtotal.multiply(discountPercentVal)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal finalGrandTotal = finalSubtotal.subtract(finalDiscountAmount);

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setTotalAmount(finalGrandTotal);

        int generatedInvoiceId = -1;
        java.sql.Connection conn = null;
        
        try {
            // Start transaction - all operations will be atomic
            conn = com.yourcompany.invoicesystem.util.DBUtil.getConnection(false);
            
            // Step 1: Save invoice header
            generatedInvoiceId = invoiceDAO.saveInvoice(invoice, conn);
            
            // Step 2: Save all invoice items and update stock
            for (Object[] itemData : currentBillItemsData) {
                int productId = (int) itemData[0];
                int quantity = (int) itemData[2];
                BigDecimal priceAtSale = (BigDecimal) itemData[3];

                InvoiceItem item = new InvoiceItem();
                item.setInvoiceID(generatedInvoiceId);
                item.setProductID(productId);
                item.setQuantity(quantity);
                item.setPriceAtSale(priceAtSale);

                // Save invoice item
                invoiceItemDAO.saveInvoiceItem(item, conn);
                
                // Decrease stock (with pessimistic locking)
                productDAO.decreaseProductStock(productId, quantity, conn);
            }
            
            // Step 3: Commit transaction - all or nothing
            conn.commit();
            
            showStyledMessageDialog("Invoice #" + generatedInvoiceId + " saved successfully!\\nAll items processed and stock updated.", 
                                  "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            
            // Offer to print receipt
            int printChoice = JOptionPane.showConfirmDialog(this,
                "Would you like to print/save a receipt for this invoice?",
                "Print Receipt",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (printChoice == JOptionPane.YES_OPTION) {
                printReceipt(invoice, generatedInvoiceId, discountPercentVal, finalDiscountAmount, finalSubtotal);
            }
            
        } catch (java.sql.SQLException e) {
            // Rollback transaction on any error
            if (conn != null) {
                try {
                    conn.rollback();
                    showStyledMessageDialog("Transaction failed and was rolled back.\nError: " + e.getMessage() + 
                                          "\n\nNo changes were made to the database.", 
                                          "Save Failed", JOptionPane.ERROR_MESSAGE);
                } catch (java.sql.SQLException ex) {
                    showStyledMessageDialog("Critical error during rollback: " + ex.getMessage(), 
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            e.printStackTrace();
            return; // Don't clear the bill so user can retry
            
        } catch (Exception e) {
            // Rollback on any other exception
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showStyledMessageDialog("Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
            
        } finally {
            // Always close the connection and restore auto-commit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (java.sql.SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }

        clearBill();
        loadProductData();
    }

    private void clearBill() {
        currentInvoiceTableModel.setRowCount(0);
        currentBillItemsData.clear();
        currentSubtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        discountPercentField.setText("0");
        updateTotals();
        quantitySpinner.setValue(1);
        productTable.clearSelection();
        productSearchField.setText("");
        filterProductTable(); // Reset filter
        invoiceDateLabel.setText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
    }

    private void loadProductData() {
        try {
            productTableModel.setRowCount(0);
            List<Product> products = productDAO.getAllProducts();
            if (products != null) {
                for (Product product : products) {
                    productTableModel.addRow(new Object[]{
                            product.getProductID(),
                            product.getName(),
                            product.getPrice(),
                            product.getStock()
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading product data: " + e.getMessage());
            e.printStackTrace();
            showStyledMessageDialog("Error loading product data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Apply some global styling to JOptionPane dialogs
    private void applyCustomStylingToDialogs() {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE); // For the panel inside JOptionPane
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI Semibold", Font.PLAIN, 13));
        // Button colors might be overridden by L&F, but we can try
        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.focus", new Color(0,0,0,0)); // Remove focus ring if L&F supports
        UIManager.put("Button.select", PRIMARY_COLOR.darker());
    }


    private void showStyledMessageDialog(String message, String title, int messageType) {
        // Temporary styling for this specific dialog call.
        // For more robust JOptionPane styling, consider creating a custom dialog
        // or using libraries that offer more control.
        // The applyCustomStylingToDialogs() method sets some global defaults.
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    private void lookupProductByBarcode() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            return;
        }
        
        // Search for product by barcode in the product table
        boolean found = false;
        for (int i = 0; i < productTableModel.getRowCount(); i++) {
            String productId = productTableModel.getValueAt(i, 0).toString();
            String productName = productTableModel.getValueAt(i, 1).toString().toLowerCase();
            
            if (productId.equals(barcode) || productName.contains(barcode.toLowerCase())) {
                productTable.setRowSelectionInterval(i, i);
                productTable.scrollRectToVisible(productTable.getCellRect(i, 0, true));
                addItemToBill();
                found = true;
                break;
            }
        }
        
        if (!found) {
            showStyledMessageDialog("Product not found for barcode: " + barcode, 
                                  "Not Found", JOptionPane.WARNING_MESSAGE);
        }
        
        barcodeField.setText("");
        barcodeField.requestFocus();
    }
    
    private void checkLowStockProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            List<String> lowStockItems = new ArrayList<>();
            
            for (Product product : products) {
                if (product.getStock() <= 10 && product.getStock() > 0) {
                    lowStockItems.add(product.getName() + " (Stock: " + product.getStock() + ")");
                } else if (product.getStock() == 0) {
                    lowStockItems.add(product.getName() + " (OUT OF STOCK)");
                }
            }
            
            if (!lowStockItems.isEmpty() && com.yourcompany.invoicesystem.util.SessionManager.getInstance().isManager()) {
                StringBuilder message = new StringBuilder("Low Stock Alert:\n\n");
                for (String item : lowStockItems) {
                    message.append("• ").append(item).append("\n");
                }
                message.append("\nPlease restock these items.");
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, message.toString(), 
                                                "Low Stock Alert", 
                                                JOptionPane.WARNING_MESSAGE);
                });
            }
        } catch (Exception e) {
            com.yourcompany.invoicesystem.util.Logger.error("Error checking low stock", e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InvoiceAppGUI app = new InvoiceAppGUI();
            app.setVisible(true);
        });
    }
}

