import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class MotoCare {
    private static final String DATA_FILE = "motocare-data.txt";
    private static final String REPORT_FILE = "motocare-report.csv";
    private static final int DAILY_LIMIT = 30;
    private static final int MAX_PER_SLOT = 2;

    private static final Color BLUE = new Color(13, 110, 253);
    private static final Color BLUE_DARK = new Color(10, 88, 202);
    private static final Color BLUE_LIGHT = new Color(232, 240, 254);
    private static final Color BORDER_GRAY = new Color(222, 226, 230);
    private static final Color LIGHT_BG = new Color(245, 247, 250);
    private static final Color STRIPE_BG = new Color(246, 249, 254);
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color ERROR_RED = new Color(220, 53, 69);
    private static final Color ERROR_RED_LIGHT = new Color(253, 236, 238);
    private static final Color SUCCESS_GREEN = new Color(25, 135, 84);
    private static final Color WARNING_ORANGE = new Color(204, 120, 0);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private static final String[] VEHICLE_KINDS = {"Motorcycle", "Car"};

    // vehicle types
    private static final List<VehicleType> VEHICLE_TYPES = List.of(
            new VehicleType("Motorcycle", "Lower CC", 0.4, 0.5),
            new VehicleType("Motorcycle", "Big Bike", 0.9, 0.8),
            new VehicleType("Car", "Sedan", 1.0, 1.0),
            new VehicleType("Car", "SUV", 1.3, 1.3),
            new VehicleType("Car", "Pickup", 1.4, 1.3),
            new VehicleType("Car", "Sports Car", 1.8, 1.6),
            new VehicleType("Car", "Van", 1.5, 1.4),
            new VehicleType("Car", "Others", 1.0, 1.0)
    );

    // Maintenance services and their base prices (cost and labor).
    private static final List<ServiceItem> SERVICES = List.of(
            new ServiceItem("Oil Change", 600, 200),
            new ServiceItem("Brake Pad Replacement", 1500, 500),
            new ServiceItem("Engine Repair", 3000, 1500),
            new ServiceItem("Tire Service", 1200, 300),
            new ServiceItem("General Vehicle Inspection", 0, 500)
    );

    // Appointment time slots
    private static final List<LocalTime> TIME_SLOTS = new ArrayList<>();

    static {
        for (LocalTime t = LocalTime.of(8, 0);
             !t.isAfter(LocalTime.of(16, 30));
             t = t.plusMinutes(30)) {
            TIME_SLOTS.add(t);
        }
    }

    private static final List<User> users = new ArrayList<>();
    private static final List<Appointment> appointments = new ArrayList<>();

    private static JFrame frame;
    private static User currentUser;

    public static void main(String[] args) {
        loadData();

        SwingUtilities.invokeLater(() -> {
            applyTheme();

            frame = new JFrame("MotoCare");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(960, 640);
            frame.setLocationRelativeTo(null);
            showLoginScreen();
            frame.setVisible(true);
        });
    }

    private static void applyTheme() {
        String[] fontKeys = {
                "Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Table.font", "OptionPane.messageFont",
                "OptionPane.buttonFont"
        };
        for (String key : fontKeys) {
            UIManager.put(key, UI_FONT);
        }

        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_DARK);
    }

    // Login and registration

    private static void showLoginScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JLabel loginMsg = createMessageLabel();
        JLabel registerMsg = createMessageLabel();

        // Login and registration
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        cards.setBackground(Color.WHITE);
        cards.setPreferredSize(new Dimension(400, 540));
        cards.add(buildLoginCard(cardLayout, cards, loginMsg, registerMsg), "login");
        cards.add(buildRegisterCard(cardLayout, cards, loginMsg, registerMsg), "register");

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        center.add(cards);
        root.add(center, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel buildLoginCard(CardLayout cardLayout, JPanel cards,
                                         JLabel loginMsg, JLabel registerMsg) {
        JPanel form = createFormPanel();

        // Greeting
        JLabel greeting = new JLabel("Hello, Welcome to MotoCare", SwingConstants.CENTER);
        greeting.setFont(new Font("Segoe UI", Font.BOLD, 18));
        greeting.setForeground(BLUE);
        addRow(form, greeting, 25);

        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        addRow(form, title, 20);

        addRow(form, createFieldLabel("Username"), 5);
        JTextField usernameField = createTextField("Enter username");
        addRow(form, usernameField, 15);

        addRow(form, createFieldLabel("Password"), 5);
        JPasswordField passwordField = createPasswordField("Password");
        addRow(form, passwordField, 15);

        addRow(form, loginMsg, 8);

        JButton loginButton = createPrimaryButton("Login");
        addRow(form, loginButton, 15);

        addRow(form, createSwitchRow("Doesn't have an account?", "Create Account", () -> {
            setMessage(loginMsg, " ", false);
            setMessage(registerMsg, " ", false);
            cardLayout.show(cards, "register");
        }), 0);

        ActionListener doLogin = e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            User user = findUser(username, password);
            if (user == null) {
                setMessage(loginMsg, "Incorrect username or password.", true);
                return;
            }

            currentUser = user;
            showDashboard();
        };
        loginButton.addActionListener(doLogin);
        usernameField.addActionListener(doLogin);
        passwordField.addActionListener(doLogin);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private static JPanel buildRegisterCard(CardLayout cardLayout, JPanel cards,
                                            JLabel loginMsg, JLabel registerMsg) {
        JPanel form = createFormPanel();

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        addRow(form, title, 20);

        addRow(form, createFieldLabel("Full name"), 5);
        JTextField nameField = createTextField("Enter full name");
        addRow(form, nameField, 12);

        addRow(form, createFieldLabel("Username"), 5);
        JTextField usernameField = createTextField("Choose a username");
        addRow(form, usernameField, 12);

        addRow(form, createFieldLabel("Password"), 5);
        JPasswordField passwordField = createPasswordField("Choose a password");
        addRow(form, passwordField, 12);

        addRow(form, createFieldLabel("Contact number"), 5);
        JTextField contactField = createTextField("Enter contact number");
        addRow(form, contactField, 12);

        addRow(form, registerMsg, 8);

        JButton createButton = createPrimaryButton("Create Account");
        addRow(form, createButton, 15);

        Runnable clearFields = () -> {
            nameField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            contactField.setText("");
        };

        addRow(form, createSwitchRow("Already have an account?", "Login", () -> {
            clearFields.run();
            setMessage(registerMsg, " ", false);
            setMessage(loginMsg, " ", false);
            cardLayout.show(cards, "login");
        }), 0);

        ActionListener doRegister = e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String contact = contactField.getText().trim();

            if (name.isEmpty() || username.isEmpty()
                    || password.isEmpty() || contact.isEmpty()) {
                setMessage(registerMsg, "Please complete all fields.", true);
                return;
            }

            if (findUserByUsername(username) != null) {
                setMessage(registerMsg, "That username is already in use.", true);
                return;
            }

            users.add(new Customer(name, username, password, contact));
            saveData();

            clearFields.run();
            setMessage(registerMsg, " ", false);
            setMessage(loginMsg, "Account created. You can now log in.", false);
            cardLayout.show(cards, "login");
        };
        createButton.addActionListener(doRegister);
        contactField.addActionListener(doRegister); // Press Enter to submit

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    //  Shared UI helpers
    private static JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        return form;
    }

    private static void addRow(JPanel panel, Component component, int bottomGap) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, bottomGap, 0);
        panel.add(component, gbc);
    }

    private static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UI_FONT);
        return label;
    }

    private static JLabel createMessageLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(UI_FONT);
        return label;
    }

    private static void setMessage(JLabel label, String text, boolean isError) {
        label.setForeground(isError ? ERROR_RED : SUCCESS_GREEN);
        label.setText(text);
    }

    private static void styleField(JTextComponent field) {
        field.setPreferredSize(new Dimension(340, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    private static JTextField createTextField(String hint) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        styleField(field);
        return field;
    }

    private static JPasswordField createPasswordField(String hint) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        styleField(field);
        return field;
    }

    private static void addHover(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }

    private static JButton createPrimaryButton(String text) {
        JButton button = createFilledButton(text, 38);
        button.setPreferredSize(new Dimension(340, 38));
        return button;
    }

    private static JButton createFilledButton(String text, int height) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHover(button, BLUE, BLUE_DARK);

        Dimension size = button.getPreferredSize();
        button.setPreferredSize(new Dimension(size.width + 24, height));
        return button;
    }

    private static JButton createOutlineButton(String text, Color accent,
                                               Color hoverBackground, int height) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(Color.WHITE);
        button.setForeground(accent);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(accent));
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHover(button, Color.WHITE, hoverBackground);

        Dimension size = button.getPreferredSize();
        button.setPreferredSize(new Dimension(size.width + 24, height));
        return button;
    }

    private static JButton createSmallButton(String text) {
        return createOutlineButton(text, BLUE, BLUE_LIGHT, 32);
    }

    private static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 4));
        header.setBackground(BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UI_FONT);
        subtitleLabel.setForeground(new Color(220, 232, 255));

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    private static JLabel createHeading(String text) {
        JLabel heading = new JLabel(text);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(BLUE);
        return heading;
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_GRAY);
        table.setSelectionBackground(new Color(207, 226, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(t, value, false, false, row, column);
                setBackground(BLUE);
                setForeground(Color.WHITE);
                setFont(BOLD_FONT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        table.setDefaultRenderer(Object.class,
                new StripedRenderer(SwingConstants.LEFT));
    }

    private static JScrollPane createTableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private static JPanel createSwitchRow(String prompt, String linkText, Runnable action) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setBackground(Color.WHITE);

        JLabel promptLabel = new JLabel(prompt);
        promptLabel.setFont(UI_FONT);

        JLabel link = new JLabel("<html><u>" + linkText + "</u></html>");
        link.setFont(UI_FONT);
        link.setForeground(BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        row.add(promptLabel);
        row.add(link);
        return row;
    }

    private static void paintHint(JTextComponent field, Graphics g, String hint) {
        if (field.getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(150, 150, 150));
            g2.setFont(field.getFont());
            Insets in = field.getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (field.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(hint, in.left, y);
            g2.dispose();
        }
    }

    private static User findUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private static User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    // Dashboards

    private static void showDashboard() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT_BG);

        String role = currentUser instanceof ShopStaff ? "Shop Staff" : "Customer";
        root.add(createHeader(
                "Welcome, " + currentUser.getFullName(),
                role + "   |   " + LocalDate.now().format(DAY_FORMAT)
        ), BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 14));
        buttons.setOpaque(false);

        if (currentUser instanceof ShopStaff) {
            JButton viewButton = createMenuButton("View and manage reservations");
            JButton scheduleButton = createMenuButton("View daily schedule");
            JButton capacityButton = createMenuButton("View daily capacity");
            JButton reportButton = createMenuButton("View report (cost and labor)");

            viewButton.addActionListener(e -> showStaffReservations());
            scheduleButton.addActionListener(e -> showSchedule(LocalDate.now()));
            capacityButton.addActionListener(e -> showCapacity());
            reportButton.addActionListener(e -> showReport());

            buttons.add(viewButton);
            buttons.add(scheduleButton);
            buttons.add(capacityButton);
            buttons.add(reportButton);
        } else {
            JButton servicesButton = createMenuButton("View maintenance services");
            JButton reserveButton = createMenuButton("Make a reservation");
            JButton appointmentsButton = createMenuButton("My appointments");

            servicesButton.addActionListener(e -> showServices());
            reserveButton.addActionListener(e -> makeReservation());
            appointmentsButton.addActionListener(e -> showCustomerAppointments());

            buttons.add(servicesButton);
            buttons.add(reserveButton);
            buttons.add(appointmentsButton);
        }

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(LIGHT_BG);
        center.add(buttons);
        root.add(center, BorderLayout.CENTER);

        JButton logoutButton = createOutlineButton(
                "Log out", ERROR_RED, ERROR_RED_LIGHT, 32);
        logoutButton.setPreferredSize(new Dimension(100, 32));
        logoutButton.addActionListener(e -> {
            currentUser = null;
            showLoginScreen();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setBackground(LIGHT_BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 30));
        bottom.add(logoutButton);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    private static JButton createMenuButton(String text) {
        JButton button = createFilledButton(text, 60);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        button.setPreferredSize(new Dimension(520, 60));
        return button;
    }

    // Vehicle type helpers 
    private static String[] getCategories(String kind) {
        List<String> names = new ArrayList<>();
        for (VehicleType type : VEHICLE_TYPES) {
            if (type.getKind().equals(kind)) {
                names.add(type.getCategory());
            }
        }
        return names.toArray(new String[0]);
    }

    private static VehicleType findVehicleType(String kind, String category) {
        for (VehicleType type : VEHICLE_TYPES) {
            if (type.getKind().equals(kind) && type.getCategory().equals(category)) {
                return type;
            }
        }
        return VEHICLE_TYPES.get(0);
    }

    private static VehicleType getSelectedVehicleType(JComboBox<String> kindBox,
                                                      JComboBox<String> categoryBox) {
        return findVehicleType(
                (String) kindBox.getSelectedItem(),
                (String) categoryBox.getSelectedItem()
        );
    }

    private static void linkVehicleBoxes(JComboBox<String> kindBox,
                                         JComboBox<String> categoryBox,
                                         Runnable onChange) {
        kindBox.addActionListener(e -> {
            categoryBox.setModel(new DefaultComboBoxModel<>(
                    getCategories((String) kindBox.getSelectedItem())));
            onChange.run();
        });
        categoryBox.addActionListener(e -> onChange.run());
    }

    // Services and prices

    private static void showServices() {
        JComboBox<String> kindBox = new JComboBox<>(VEHICLE_KINDS);
        JComboBox<String> categoryBox = new JComboBox<>(getCategories(VEHICLE_KINDS[0]));
        JLabel priceList = new JLabel();

        Runnable updatePrices = () -> {
            VehicleType type = getSelectedVehicleType(kindBox, categoryBox);

            StringBuilder html = new StringBuilder("<html><table cellpadding='3'>");
            html.append("<tr><th align='left'>Service</th>")
                    .append("<th align='right'>Cost</th>")
                    .append("<th align='right'>Labor</th>")
                    .append("<th align='right'>Total</th></tr>");

            for (ServiceItem item : SERVICES) {
                html.append("<tr><td>").append(item.getName()).append("</td>")
                        .append("<td align='right'>").append(money(item.getCostFor(type))).append("</td>")
                        .append("<td align='right'>").append(money(item.getLaborFor(type))).append("</td>")
                        .append("<td align='right'><b>").append(money(item.getTotalFor(type))).append("</b></td></tr>");
            }

            html.append("</table></html>");
            priceList.setText(html.toString());
        };

        linkVehicleBoxes(kindBox, categoryBox, updatePrices);
        updatePrices.run();

        JLabel title = createHeading("Available maintenance services");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        Object[] content = {
                title,
                "Vehicle type:", kindBox,
                "Category:", categoryBox,
                " ", priceList
        };

        JOptionPane.showMessageDialog(
                frame, content, "Maintenance Services", JOptionPane.PLAIN_MESSAGE);
    }

    // Customer reservations

    private static void makeReservation() {
        // Vehicle type: Motorcycle (Lower CC / Big Bike) or Car (Sedan / SUV / ...)
        JComboBox<String> vehicleKindBox = new JComboBox<>(VEHICLE_KINDS);
        JComboBox<String> vehicleTypeBox = new JComboBox<>(getCategories(VEHICLE_KINDS[0]));

        JTextField vehicleField = new JTextField();
        JTextField modelYearField = new JTextField();

        JComboBox<String> serviceBox = new JComboBox<>();
        for (ServiceItem item : SERVICES) {
            serviceBox.addItem(item.getName());
        }

        JLabel estimateLabel = new JLabel();
        estimateLabel.setForeground(BLUE_DARK);
        Runnable updateEstimate = () -> {
            ServiceItem item = SERVICES.get(serviceBox.getSelectedIndex());
            VehicleType type = getSelectedVehicleType(vehicleKindBox, vehicleTypeBox);
            estimateLabel.setText("<html>Cost: " + money(item.getCostFor(type))
                    + "<br>Labor: " + money(item.getLaborFor(type))
                    + "<br><b>Total: " + money(item.getTotalFor(type)) + "</b></html>");
        };
        linkVehicleBoxes(vehicleKindBox, vehicleTypeBox, updateEstimate);
        serviceBox.addActionListener(e -> updateEstimate.run());
        updateEstimate.run();

        JTextField dateField = new JTextField(LocalDate.now().toString());
        JComboBox<String> timeBox = new JComboBox<>();

        // Lists the time slots and marks the ones that are already full
        // for the date typed in the date field.
        Runnable updateTimes = () -> {
            int selected = Math.max(timeBox.getSelectedIndex(), 0);

            LocalDate chosenDate = null;
            try {
                chosenDate = LocalDate.parse(dateField.getText().trim());
            } catch (DateTimeParseException ex) {
                // Date not valid yet: show the plain list of times
            }

            timeBox.removeAllItems();
            for (LocalTime slot : TIME_SLOTS) {
                String label = slot.format(TIME_FORMAT);
                if (chosenDate != null
                        && countReservationsForSlot(chosenDate, slot) >= MAX_PER_SLOT) {
                    label += " (Full)";
                }
                timeBox.addItem(label);
            }
            timeBox.setSelectedIndex(selected);
        };
        dateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTimes.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTimes.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTimes.run();
            }
        });
        updateTimes.run();

        Object[] fields = {
                "Vehicle type:", vehicleKindBox,
                "Category:", vehicleTypeBox,
                "Vehicle model:", vehicleField,
                "Vehicle year:", modelYearField,
                "Service:", serviceBox,
                "Price:", estimateLabel,
                "Appointment date (YYYY-MM-DD):", dateField,
                "Appointment time:", timeBox
        };

        int result = JOptionPane.showConfirmDialog(
                frame, fields, "Make a Reservation",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String vehicle = vehicleField.getText().trim();
        String modelYear = modelYearField.getText().trim();

        if (vehicle.isEmpty() || modelYear.isEmpty()) {
            showMessage("Please provide your vehicle details.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            showMessage("Enter the date in YYYY-MM-DD format.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showMessage("The appointment date cannot be in the past.");
            return;
        }

        LocalTime time = TIME_SLOTS.get(timeBox.getSelectedIndex());
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            showMessage("That time has already passed today. Choose a later time.");
            return;
        }

        // The same date and time cannot be booked by more than MAX_PER_SLOT customers
        if (countReservationsForSlot(date, time) >= MAX_PER_SLOT) {
            showMessage("That time is already full. Please choose another time.");
            return;
        }

        int reservationsForDate = countReservationsForDate(date);
        if (reservationsForDate >= DAILY_LIMIT) {
            showMessage("That date has reached its daily limit of "
                    + DAILY_LIMIT + " reservations.");
            return;
        }

        VehicleType type = getSelectedVehicleType(vehicleKindBox, vehicleTypeBox);
        ServiceItem service = SERVICES.get(serviceBox.getSelectedIndex());
        String vehicleDetails = vehicle + " (" + modelYear + ")";

        // The price is saved with the reservation
        double cost = service.getCostFor(type);
        double labor = service.getLaborFor(type);

        appointments.add(new Appointment(
                currentUser.getUsername(),
                type.getLabel(),
                vehicleDetails,
                service.getName(),
                date,
                time,
                cost,
                labor,
                "Pending"
        ));

        saveData();
        showMessage("Reservation submitted.\n\n"
                + type.getLabel() + ": " + vehicleDetails + "\n"
                + service.getName() + "\n"
                + date + " at " + time.format(TIME_FORMAT) + "\n"
                + "Cost: " + money(cost) + "\n"
                + "Labor: " + money(labor) + "\n"
                + "Total: " + money(cost + labor) + "\n\n"
                + "Status: Pending.");
    }

    private static void showCustomerAppointments() {
        StringBuilder text = new StringBuilder("Your appointments:\n\n");
        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.getCustomerUsername()
                    .equals(currentUser.getUsername())) {
                text.append(appointment.toDisplayString()).append("\n");
                found = true;
            }
        }

        if (!found) {
            text.append("You have no appointments.");
        }

        showMessage(text.toString());
    }

    // Staff functions
    private static void showStaffReservations() {
        if (appointments.isEmpty()) {
            showMessage("There are no reservations yet.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < appointments.size(); i++) {
            text.append(i + 1).append(". ")
                    .append(appointments.get(i).toDisplayString())
                    .append("\n");
        }

        String input = JOptionPane.showInputDialog(
                frame,
                text + "\nEnter a reservation number to update its status "
                        + "(Cancel to return):",
                "Manage Reservations",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            int selected = Integer.parseInt(input.trim()) - 1;
            if (selected < 0 || selected >= appointments.size()) {
                showMessage("That reservation number is not valid.");
                return;
            }

            Appointment appointment = appointments.get(selected);
            String[] statuses = {"Pending", "Approved", "Rejected"};
            String selectedStatus = (String) JOptionPane.showInputDialog(
                    frame,
                    "Choose a new status:",
                    "Update Reservation",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    statuses,
                    appointment.getStatus()
            );

            if (selectedStatus != null) {
                // A rejected reservation gave its slot away, so it can only be
                // reopened if that time is not full again.
                if (appointment.getStatus().equals("Rejected")
                        && !selectedStatus.equals("Rejected")
                        && countReservationsForSlot(
                        appointment.getDate(), appointment.getTime()) >= MAX_PER_SLOT) {
                    showMessage("That time is already full, so this reservation "
                            + "cannot be reopened.");
                    return;
                }

                appointment.setStatus(selectedStatus);
                saveData();
                showMessage("Reservation status updated.");
            }
        } catch (NumberFormatException ex) {
            showMessage("Enter a valid reservation number.");
        }
    }

    private static void showCapacity() {
        StringBuilder text = new StringBuilder(
                "Daily reservation limit: " + DAILY_LIMIT + "\n\n"
        );

        TreeSet<LocalDate> dates = new TreeSet<>();
        for (Appointment appointment : appointments) {
            dates.add(appointment.getDate());
        }

        for (LocalDate date : dates) {
            text.append(date)
                    .append(": ")
                    .append(countReservationsForDate(date))
                    .append("/")
                    .append(DAILY_LIMIT)
                    .append(" reservations\n");
        }

        if (appointments.isEmpty()) {
            text.append("No reservations have been submitted.");
        }

        showMessage(text.toString());
    }

    private static int countReservationsForDate(LocalDate date) {
        int count = 0;
        for (Appointment appointment : appointments) {
            // All submitted reservations count toward the daily limit.
            if (appointment.getDate().equals(date)) {
                count++;
            }
        }
        return count;
    }

    // Number of active (not rejected) reservations on one date and time
    private static int countReservationsForSlot(LocalDate date, LocalTime time) {
        int count = 0;
        for (Appointment appointment : appointments) {
            if (appointment.getDate().equals(date)
                    && appointment.getTime().equals(time)
                    && !appointment.getStatus().equals("Rejected")) {
                count++;
            }
        }
        return count;
    }

    // Daily schedule (staff)

    private static void showSchedule(LocalDate startDate) {
        LocalDate[] selectedDate = {startDate};

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel heading = createHeading("Daily Schedule");

        JButton previousButton = createSmallButton("< Previous day");
        JButton nextButton = createSmallButton("Next day >");
        JButton todayButton = createSmallButton("Today");
        JTextField dateField = new JTextField(10);
        dateField.setPreferredSize(new Dimension(100, 32));
        dateField.setHorizontalAlignment(SwingConstants.CENTER);
        dateField.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        JButton goButton = createFilledButton("Go", 32);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navPanel.setBackground(Color.WHITE);
        navPanel.add(previousButton);
        navPanel.add(dateField);
        navPanel.add(goButton);
        navPanel.add(nextButton);
        navPanel.add(todayButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(heading, BorderLayout.WEST);
        top.add(navPanel, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {
                "Time", "Customer", "Contact", "Vehicle Type",
                "Vehicle", "Service", "Status", "Total"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(7)
                .setCellRenderer(new StripedRenderer(SwingConstants.RIGHT));

        panel.add(createTableScroll(table), BorderLayout.CENTER);

        // Bottom: summary + back button
        JLabel summaryLabel = new JLabel();
        summaryLabel.setFont(BOLD_FONT);

        JButton backButton = createSmallButton("Back");
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.add(backButton);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(Color.WHITE);
        bottom.add(summaryLabel, BorderLayout.NORTH);
        bottom.add(buttonRow, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);

            LocalDate date = selectedDate[0];
            List<Appointment> rows = getAppointmentsForDate(date);

            for (Appointment appointment : rows) {
                model.addRow(new Object[]{
                        appointment.getTime().format(TIME_FORMAT),
                        getCustomerName(appointment.getCustomerUsername()),
                        getCustomerContact(appointment.getCustomerUsername()),
                        appointment.getVehicleType(),
                        appointment.getVehicle(),
                        appointment.getService(),
                        appointment.getStatus(),
                        money(appointment.getTotal())
                });
            }

            dateField.setText(date.toString());

            if (rows.isEmpty()) {
                summaryLabel.setText(date.format(DAY_FORMAT) + ":  no reservations");
            } else {
                summaryLabel.setText(date.format(DAY_FORMAT) + ":  "
                        + rows.size() + " of " + DAILY_LIMIT + " reservations");
            }
        };

        previousButton.addActionListener(e -> {
            selectedDate[0] = selectedDate[0].minusDays(1);
            refresh.run();
        });
        nextButton.addActionListener(e -> {
            selectedDate[0] = selectedDate[0].plusDays(1);
            refresh.run();
        });
        todayButton.addActionListener(e -> {
            selectedDate[0] = LocalDate.now();
            refresh.run();
        });

        ActionListener goToDate = e -> {
            try {
                selectedDate[0] = LocalDate.parse(dateField.getText().trim());
                refresh.run();
            } catch (DateTimeParseException ex) {
                showMessage("Enter the date in YYYY-MM-DD format.");
            }
        };
        goButton.addActionListener(goToDate);
        dateField.addActionListener(goToDate);

        backButton.addActionListener(e -> showDashboard());

        refresh.run();

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    // All reservations on one date
    private static List<Appointment> getAppointmentsForDate(LocalDate date) {
        List<Appointment> rows = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getDate().equals(date)) {
                rows.add(appointment);
            }
        }
        rows.sort(Comparator.comparing(Appointment::getTime));
        return rows;
    }

    // Report (cost and labor)

    private static void showReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel heading = createHeading("Reservation Report");

        String[] filters = {"All", "Pending", "Approved", "Rejected"};
        JComboBox<String> filterBox = new JComboBox<>(filters);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(filterBox);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(heading, BorderLayout.WEST);
        top.add(filterPanel, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {
                "Date", "Time", "Customer", "Vehicle Type", "Vehicle",
                "Service", "Status", "Cost", "Labor", "Total"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        styleTable(table);

        for (int column = 7; column <= 9; column++) {
            table.getColumnModel().getColumn(column)
                    .setCellRenderer(new StripedRenderer(SwingConstants.RIGHT));
        }

        panel.add(createTableScroll(table), BorderLayout.CENTER);

        JLabel totalsLabel = new JLabel();
        totalsLabel.setFont(BOLD_FONT);
        totalsLabel.setForeground(BLUE_DARK);

        JButton exportButton = createFilledButton("Export to CSV", 32);
        JButton backButton = createSmallButton("Back");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.add(exportButton);
        buttonRow.add(backButton);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(Color.WHITE);
        bottom.add(totalsLabel, BorderLayout.NORTH);
        bottom.add(buttonRow, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);

            List<Appointment> rows = getReportRows((String) filterBox.getSelectedItem());
            double totalCost = 0;
            double totalLabor = 0;

            for (Appointment appointment : rows) {
                model.addRow(new Object[]{
                        appointment.getDate(),
                        appointment.getTime().format(TIME_FORMAT),
                        getCustomerName(appointment.getCustomerUsername()),
                        appointment.getVehicleType(),
                        appointment.getVehicle(),
                        appointment.getService(),
                        appointment.getStatus(),
                        money(appointment.getCost()),
                        money(appointment.getLabor()),
                        money(appointment.getTotal())
                });
                totalCost += appointment.getCost();
                totalLabor += appointment.getLabor();
            }

            totalsLabel.setText("Reservations: " + rows.size()
                    + "   |   Cost: " + money(totalCost)
                    + "   |   Labor: " + money(totalLabor)
                    + "   |   Grand total: " + money(totalCost + totalLabor));
        };

        filterBox.addActionListener(e -> refresh.run());
        exportButton.addActionListener(e ->
                exportReport((String) filterBox.getSelectedItem()));
        backButton.addActionListener(e -> showDashboard());

        refresh.run();

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    // Reservations matching the status filter, sorted by date then time
    private static List<Appointment> getReportRows(String statusFilter) {
        List<Appointment> rows = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (statusFilter.equals("All")
                    || appointment.getStatus().equals(statusFilter)) {
                rows.add(appointment);
            }
        }
        rows.sort(Comparator.comparing(Appointment::getDate)
                .thenComparing(Appointment::getTime));
        return rows;
    }

    private static String getCustomerName(String username) {
        User user = findUserByUsername(username);
        return user == null ? username : user.getFullName();
    }

    private static String getCustomerContact(String username) {
        User user = findUserByUsername(username);
        return user == null ? "-" : user.getContact();
    }

    private static void exportReport(String statusFilter) {
        List<Appointment> rows = getReportRows(statusFilter);
        File file = new File(REPORT_FILE);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("MotoCare Reservation Report");
            writer.println("Generated," + LocalDate.now());
            writer.println("Status filter," + statusFilter);
            writer.println();
            writer.println("Date,Time,Customer,Vehicle Type,Vehicle,Service,Status,Cost,Labor,Total");

            double totalCost = 0;
            double totalLabor = 0;

            for (Appointment appointment : rows) {
                writer.println(String.join(",",
                        appointment.getDate().toString(),
                        appointment.getTime().format(TIME_FORMAT),
                        csv(getCustomerName(appointment.getCustomerUsername())),
                        csv(appointment.getVehicleType()),
                        csv(appointment.getVehicle()),
                        csv(appointment.getService()),
                        appointment.getStatus(),
                        plainAmount(appointment.getCost()),
                        plainAmount(appointment.getLabor()),
                        plainAmount(appointment.getTotal())
                ));
                totalCost += appointment.getCost();
                totalLabor += appointment.getLabor();
            }

            writer.println(",,,,,,TOTAL,"
                    + plainAmount(totalCost) + ","
                    + plainAmount(totalLabor) + ","
                    + plainAmount(totalCost + totalLabor));
        } catch (IOException ex) {
            showMessage("Could not save the report: " + ex.getMessage());
            return;
        }

        showMessage("Report saved to:\n" + file.getAbsolutePath());
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String plainAmount(double amount) {
        return String.format(Locale.ENGLISH, "%.2f", amount);
    }

    private static String money(double amount) {
        return String.format(Locale.ENGLISH, "PHP %,.2f", amount);
    }

    private static ServiceItem findService(String name) {
        for (ServiceItem item : SERVICES) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    // File saving 
    private static void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (User user : users) {
                writer.println(String.join("\t",
                        "USER",
                        encode(user.getRole()),
                        encode(user.getFullName()),
                        encode(user.getUsername()),
                        encode(user.getPassword()),
                        encode(user.getContact())
                ));
            }

            for (Appointment appointment : appointments) {
                writer.println(String.join("\t",
                        "APPOINTMENT",
                        encode(appointment.getCustomerUsername()),
                        encode(appointment.getVehicleType()),
                        encode(appointment.getVehicle()),
                        encode(appointment.getService()),
                        appointment.getDate().toString(),
                        appointment.getTime().toString(),
                        Double.toString(appointment.getCost()),
                        Double.toString(appointment.getLabor()),
                        encode(appointment.getStatus())
                ));
            }
        } catch (IOException ex) {
            showMessage("Could not save data: " + ex.getMessage());
        }
    }

    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            // Starter staff account for testing.
            users.add(new ShopStaff(
                    "MotoCare Staff",
                    "staff",
                    "staff123",
                    "0000000000"
            ));
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", -1);

                if (parts.length == 6 && parts[0].equals("USER")) {
                    String role = decode(parts[1]);
                    String name = decode(parts[2]);
                    String username = decode(parts[3]);
                    String password = decode(parts[4]);
                    String contact = decode(parts[5]);

                    if (role.equals("STAFF")) {
                        users.add(new ShopStaff(name, username, password, contact));
                    } else {
                        users.add(new Customer(name, username, password, contact));
                    }

                } else if (parts.length == 10 && parts[0].equals("APPOINTMENT")) {
                    appointments.add(new Appointment(
                            decode(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            decode(parts[4]),
                            LocalDate.parse(parts[5]),
                            LocalTime.parse(parts[6]),
                            Double.parseDouble(parts[7]),
                            Double.parseDouble(parts[8]),
                            decode(parts[9])
                    ));

                } else if (parts.length == 9 && parts[0].equals("APPOINTMENT")) {
                    // Older format (no vehicle type yet)
                    appointments.add(new Appointment(
                            decode(parts[1]),
                            "Not specified",
                            decode(parts[2]),
                            decode(parts[3]),
                            LocalDate.parse(parts[4]),
                            LocalTime.parse(parts[5]),
                            Double.parseDouble(parts[6]),
                            Double.parseDouble(parts[7]),
                            decode(parts[8])
                    ));

                } else if (parts.length == 6 && parts[0].equals("APPOINTMENT")) {
                    // Oldest format (before time and prices existed):
                    // default to 8:00 AM and the base price list.
                    String service = decode(parts[3]);
                    ServiceItem item = findService(service);

                    appointments.add(new Appointment(
                            decode(parts[1]),
                            "Not specified",
                            decode(parts[2]),
                            service,
                            LocalDate.parse(parts[4]),
                            LocalTime.of(8, 0),
                            item == null ? 0 : item.getCost(),
                            item == null ? 0 : item.getLabor(),
                            decode(parts[5])
                    ));
                }
            }
        } catch (IOException | RuntimeException ex) {
            System.err.println("Could not load MotoCare data: " + ex.getMessage());
        }
    }

    private static String encode(String value) {
        return java.util.Base64.getEncoder()
                .encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(
                java.util.Base64.getDecoder().decode(value),
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    private static void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    // Table cell renderer
    private static class StripedRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        public StripedRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column);

            setHorizontalAlignment(alignment);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : STRIPE_BG);
                setForeground(statusColor(String.valueOf(value)));
            }
            return this;
        }

        private Color statusColor(String text) {
            switch (text) {
                case "Approved":
                    return SUCCESS_GREEN;
                case "Rejected":
                    return ERROR_RED;
                case "Pending":
                    return WARNING_ORANGE;
                default:
                    return TEXT_DARK;
            }
        }
    }

    // OOP model classes

    private static abstract class User {
        private final String fullName;
        private final String username;
        private final String password;
        private final String contact;

        protected User(String fullName, String username,
                       String password, String contact) {
            this.fullName = fullName;
            this.username = username;
            this.password = password;
            this.contact = contact;
        }

        public String getFullName() {
            return fullName;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getContact() {
            return contact;
        }

        public abstract String getRole();
    }

    private static class Customer extends User {
        public Customer(String name, String username,
                        String password, String contact) {
            super(name, username, password, contact);
        }

        @Override
        public String getRole() {
            return "CUSTOMER";
        }
    }

    private static class ShopStaff extends User {
        public ShopStaff(String name, String username,
                         String password, String contact) {
            super(name, username, password, contact);
        }

        @Override
        public String getRole() {
            return "STAFF";
        }
    }

    private static class VehicleType {
        private final String kind;
        private final String category;
        private final double costMultiplier;
        private final double laborMultiplier;

        public VehicleType(String kind, String category,
                           double costMultiplier, double laborMultiplier) {
            this.kind = kind;
            this.category = category;
            this.costMultiplier = costMultiplier;
            this.laborMultiplier = laborMultiplier;
        }

        public String getKind() {
            return kind;
        }

        public String getCategory() {
            return category;
        }

        public double getCostMultiplier() {
            return costMultiplier;
        }

        public double getLaborMultiplier() {
            return laborMultiplier;
        }

        public String getLabel() {
            return kind + " - " + category;
        }
    }

    private static class ServiceItem {
        private final String name;
        private final double cost;
        private final double labor;

        public ServiceItem(String name, double cost, double labor) {
            this.name = name;
            this.cost = cost;
            this.labor = labor;
        }

        public String getName() {
            return name;
        }

        // Base prices (standard Sedan)
        public double getCost() {
            return cost;
        }

        public double getLabor() {
            return labor;
        }

        // Prices for a specific vehicle type
        public double getCostFor(VehicleType type) {
            return Math.round(cost * type.getCostMultiplier());
        }

        public double getLaborFor(VehicleType type) {
            return Math.round(labor * type.getLaborMultiplier());
        }

        public double getTotalFor(VehicleType type) {
            return getCostFor(type) + getLaborFor(type);
        }
    }

    private static class Appointment {
        private final String customerUsername;
        private final String vehicleType;
        private final String vehicle;
        private final String service;
        private final LocalDate date;
        private final LocalTime time;
        private final double cost;
        private final double labor;
        private String status;

        public Appointment(String customerUsername, String vehicleType,
                           String vehicle, String service,
                           LocalDate date, LocalTime time,
                           double cost, double labor, String status) {
            this.customerUsername = customerUsername;
            this.vehicleType = vehicleType;
            this.vehicle = vehicle;
            this.service = service;
            this.date = date;
            this.time = time;
            this.cost = cost;
            this.labor = labor;
            this.status = status;
        }

        public String getCustomerUsername() {
            return customerUsername;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getVehicle() {
            return vehicle;
        }

        public String getService() {
            return service;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalTime getTime() {
            return time;
        }

        public double getCost() {
            return cost;
        }

        public double getLabor() {
            return labor;
        }

        public double getTotal() {
            return cost + labor;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String toDisplayString() {
            return date + " " + time.format(TIME_FORMAT)
                    + " | " + service
                    + " | " + vehicleType + ": " + vehicle
                    + " | Total: " + money(getTotal())
                    + " | " + status + " | Customer: " + customerUsername;
        }
    }
}