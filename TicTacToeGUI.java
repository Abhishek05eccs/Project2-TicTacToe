import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;

public class TicTacToeGUI extends JFrame {
    private TicTacToeLogic logic;
    private TexturedPanel boardPanel;
    private CustomButton[][] buttons;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel roundLabel;
    private JButton undoButton, restartRoundButton, replayButton, closeButton, modeToggleButton, saveButton, loadButton;
    private boolean darkMode = false;

    // Colors matching a cosmic teal-to-green theme
    private Color bgStart = new Color(10, 50, 60); // Deep teal
    private Color bgEnd = new Color(20, 80, 40); // Dark green
    private Color boardStart = new Color(20, 60, 70); // Slightly lighter teal
    private Color boardEnd = new Color(30, 90, 50); // Slightly lighter green
    private Color fgColor = Color.WHITE;
    private Color xColor = new Color(180, 255, 200); // Light teal
    private Color oColor = new Color(255, 200, 180); // Light coral
    private Color winHighlight = new Color(100, 255, 150); // Light teal-green

    // Inner class for textured panel with animated symbols and particles
    private class TexturedPanel extends JPanel {
        private final boolean showSymbols;
        private BufferedImage texture;
        private float twinklePhase;
        private Symbol[] symbols;
        private List<Particle> particles = new ArrayList<>();

        private class Symbol {
            float x, y;
            float vx, vy;
            String value;
            float phaseOffset;

            Symbol(String value, float x, float y, float vx, float vy, float phaseOffset) {
                this.value = value;
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.phaseOffset = phaseOffset;
            }
        }

        public TexturedPanel(boolean showSymbols, int width, int height) {
            this.showSymbols = showSymbols;
            setOpaque(true);
            generateTexture(width, height);
            if (showSymbols) {
                Random rand = new Random();
                String[] symbolValues = {"X", "O", "?", "★", "✦"};
                symbols = new Symbol[Math.min(20, 5 * width / 500)];
                for (int i = 0; i < symbols.length; i++) {
                    float x = rand.nextInt(width);
                    float y = rand.nextInt(height);
                    float angle = rand.nextFloat() * 2 * (float) Math.PI;
                    float speed = 0.5f + rand.nextFloat() * 1.0f;
                    symbols[i] = new Symbol(
                        symbolValues[rand.nextInt(symbolValues.length)],
                        x, y,
                        (float) Math.cos(angle) * speed,
                        (float) Math.sin(angle) * speed,
                        rand.nextFloat() * 2 * (float) Math.PI
                    );
                }
            }
            new Timer(50, e -> {
                twinklePhase = (twinklePhase + 0.05f) % (2 * (float) Math.PI);
                if (showSymbols) {
                    updateSymbols();
                }
                updateParticles();
                repaint();
            }).start();
        }

        private void generateTexture(int w, int h) {
            texture = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = texture.createGraphics();
            Random rand = new Random();

            GradientPaint cosmicGradient = new GradientPaint(
                0, 0, bgStart,
                0, h, bgEnd
            );
            g2d.setPaint(cosmicGradient);
            g2d.fillRect(0, 0, w, h);

            for (int i = 0; i < 50; i++) {
                int x = rand.nextInt(w);
                int y = rand.nextInt(h);
                int size = rand.nextInt(2) + 1;
                g2d.setColor(new Color(255, 255, 255, 80 + rand.nextInt(80)));
                g2d.fillOval(x, y, size, size);
            }
            g2d.dispose();
        }

        private void updateSymbols() {
            int w = getWidth();
            int h = getHeight();
            for (Symbol symbol : symbols) {
                symbol.x += symbol.vx;
                symbol.y += symbol.vy;
                if (symbol.x < 0 || symbol.x > w) {
                    symbol.vx = -symbol.vx;
                }
                if (symbol.y < 0 || symbol.y > h) {
                    symbol.vy = -symbol.vy;
                }
                symbol.x = Math.max(0, Math.min(w, symbol.x));
                symbol.y = Math.max(0, Math.min(h, symbol.y));
            }
        }

        private void updateParticles() {
            particles.removeIf(p -> p.lifetime <= 0);
            for (Particle p : particles) {
                p.update();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2d.drawImage(texture, 0, 0, w, h, null, null);

            if (showSymbols && symbols != null) {
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                for (Symbol symbol : symbols) {
                    int alpha = (int) (100 + 50 * Math.sin(twinklePhase + symbol.phaseOffset));
                    g2d.setColor(new Color(180, 255, 200, alpha));
                    g2d.drawString(symbol.value, symbol.x, symbol.y);
                }
            }

            for (Particle p : particles) {
                p.render(g2d);
            }

            GradientPaint glow = new GradientPaint(
                0, 0, new Color(255, 255, 255, 20),
                0, h, new Color(100, 255, 150, 50)
            );
            g2d.setPaint(glow);
            g2d.fillRect(0, 0, w, h);
        }
    }

    // Particle class for effects
    private class Particle {
        float x, y;
        float vx, vy;
        int lifetime;
        Color color;

        Particle(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            Random rand = new Random();
            float angle = rand.nextFloat() * 2 * (float) Math.PI;
            float speed = 1.0f + rand.nextFloat() * 2.0f;
            this.vx = (float) Math.cos(angle) * speed;
            this.vy = (float) Math.sin(angle) * speed;
            this.lifetime = 20 + rand.nextInt(20);
            this.color = color;
        }

        void update() {
            x += vx;
            y += vy;
            lifetime--;
            vx *= 0.95f;
            vy *= 0.95f;
        }

        void render(Graphics2D g2d) {
            int alpha = (int) (255 * (lifetime / 40.0f));
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha))));
            g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
        }
    }

    // Custom button for the game board
    private class CustomButton extends JButton {
        private String symbol = "";
        private float glowPhase = 0;
        private Color symbolColor = fgColor;

        public CustomButton() {
            setContentAreaFilled(false);
            setFont(new Font("Arial", Font.BOLD, 40));
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
            repaint();
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbolColor(Color color) {
            this.symbolColor = color;
            repaint();
        }

        public void setGlowPhase(float phase) {
            this.glowPhase = phase;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isEnabled()) {
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
            }

            if (!symbol.isEmpty()) {
                g2d.setFont(getFont());
                g2d.setColor(symbolColor);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (w - fm.stringWidth(symbol)) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(symbol, x, y);

                if (glowPhase > 0) {
                    float glow = (float) Math.sin(glowPhase) * 0.5f + 0.5f;
                    g2d.setColor(new Color(255, 255, 255, (int) (glow * 50)));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(2, 2, w - 4, h - 4, 15, 15);
                }
            }

            g2d.dispose();
        }
    }

    // Rounded border for buttons
    private class RoundedBorder implements Border {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(100, 100, 120));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    public TicTacToeGUI() {
        logic = new TicTacToeLogic();
        showConfigDialog();
        int panelWidth = 500;
        int panelHeight = 450;

        TexturedPanel contentPane = new TexturedPanel(false, panelWidth, panelHeight);
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        setTitle("🎲 Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(panelWidth, panelHeight);
        setLocationRelativeTo(null);

        initComponents();
        showRules();
        applyTheme();
        setVisible(true);
    }

    private void showConfigDialog() {
        JDialog configDialog = new JDialog(this, "Game Settings", true);
        configDialog.setUndecorated(true);
        configDialog.setSize(350, 250);
        configDialog.setLocationRelativeTo(this);

        TexturedPanel contentPane = new TexturedPanel(false, 350, 250);
        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        configDialog.setContentPane(contentPane);

        JLabel titleLabel = new JLabel("Tic-Tac-Toe Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(fgColor);
        contentPane.add(titleLabel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel boardSizeLabel = new JLabel("Board Size:");
        boardSizeLabel.setForeground(fgColor);
        boardSizeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner boardSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 3, 10, 1));
        boardSizeSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        boardSizeSpinner.setBackground(new Color(50, 50, 50));
        boardSizeSpinner.setForeground(fgColor);
        ((JSpinner.DefaultEditor) boardSizeSpinner.getEditor()).getTextField().setBackground(new Color(50, 50, 50));
        ((JSpinner.DefaultEditor) boardSizeSpinner.getEditor()).getTextField().setForeground(fgColor);

        JLabel roundsLabel = new JLabel("Max Rounds:");
        roundsLabel.setForeground(fgColor);
        roundsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner roundsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        roundsSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        roundsSpinner.setBackground(new Color(50, 50, 50));
        roundsSpinner.setForeground(fgColor);
        ((JSpinner.DefaultEditor) roundsSpinner.getEditor()).getTextField().setBackground(new Color(50, 50, 50));
        ((JSpinner.DefaultEditor) roundsSpinner.getEditor()).getTextField().setForeground(fgColor);

        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setForeground(fgColor);
        difficultyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JComboBox<String> difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyCombo.setBackground(new Color(50, 50, 50));
        difficultyCombo.setForeground(fgColor);

        settingsPanel.add(boardSizeLabel);
        settingsPanel.add(boardSizeSpinner);
        settingsPanel.add(roundsLabel);
        settingsPanel.add(roundsSpinner);
        settingsPanel.add(difficultyLabel);
        settingsPanel.add(difficultyCombo);

        contentPane.add(settingsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(50, 50, 50));
        startButton.setForeground(fgColor);
        startButton.setBorder(new RoundedBorder(10));
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> {
            logic.init((Integer) boardSizeSpinner.getValue());
            logic.setMaxRounds((Integer) roundsSpinner.getValue());
            logic.setDifficultyLevel(difficultyCombo.getSelectedIndex() + 1);
            configDialog.dispose();
        });
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startButton.setBackground(new Color(80, 80, 80));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                startButton.setBackground(new Color(50, 50, 50));
            }
        });
        buttonPanel.add(startButton);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        configDialog.setVisible(true);
    }

    private void showRules() {
        JDialog rulesDialog = new JDialog(this, "Tic-Tac-Toe Rules", true);
        rulesDialog.setSize(600, 400);
        rulesDialog.setLocationRelativeTo(this);
        rulesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        TexturedPanel rulesPanel = new TexturedPanel(false, 600, 400);
        rulesPanel.setLayout(new BorderLayout());
        JTextPane rulesText = new JTextPane();
        rulesText.setContentType("text/html");
        rulesText.setEditable(false);
        rulesText.setOpaque(false);
        rulesText.setFont(new Font("Arial", Font.PLAIN, 14));
        String difficultyText = logic.getDifficultyLevel() == 1 ? "Easy" : logic.getDifficultyLevel() == 2 ? "Medium" : "Hard";
        String rules = "<html>" +
                "<h1 style='color: white; text-align: center;'>Welcome to Tic-Tac-Toe!</h1>" +
                "<h2 style='color: orange;'>Objective</h2>" +
                "<p style='color: white;'>Place " + logic.getBoardSize() + " of your symbols ('X' or 'O') in a row, column, or diagonal to win a round.</p>" +
                "<h2 style='color: orange;'>Rules</h2>" +
                "<ul style='color: white;'>" +
                "<li>Playing on a " + logic.getBoardSize() + "x" + logic.getBoardSize() + " board with " + logic.getMaxRounds() + " rounds.</li>" +
                "<li>You are " + logic.getPlayerName() + " ('" + logic.getPlayerSymbol() + "') vs. " + logic.getOpponentName() + " ('" + logic.getComputerSymbol() + "', " + difficultyText + " difficulty).</li>" +
                "<li>Click an empty cell to place your symbol.</li>" +
                "<li>The game ends after " + logic.getMaxRounds() + " rounds or when you choose to close.</li>" +
                "<li>Features: Undo moves, save/load game, restart round, toggle cosmic/light theme.</li>" +
                "</ul>" +
                "<h2 style='color: orange;'>Controls</h2>" +
                "<ul style='color: white;'>" +
                "<li><b>Undo (Ctrl+Z)</b>: Undo last move.</li>" +
                "<li><b>Restart Round (Ctrl+R)</b>: Restart the current round.</li>" +
                "<li><b>Save (Ctrl+S)</b>: Save game state.</li>" +
                "<li><b>Load (Ctrl+L)</b>: Load saved game.</li>" +
                "<li><b>Replay (Ctrl+N)</b>: Start a new game.</li>" +
                "<li><b>Close (Ctrl+Q)</b>: Exit the game.</li>" +
                "</ul>" +
                "<p style='color: cyan; text-align: center;'>Enjoy the cosmic Tic-Tac-Toe experience!</p>" +
                "</html>";
        rulesText.setText(rules);
        rulesText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(rulesText);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        JButton okButton = new JButton("Start Game");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(new Color(50, 50, 50));
        okButton.setForeground(Color.WHITE);
        okButton.addActionListener(e -> rulesDialog.dispose());

        rulesPanel.add(scrollPane, BorderLayout.CENTER);
        rulesPanel.add(okButton, BorderLayout.SOUTH);
        rulesDialog.add(rulesPanel);
        rulesDialog.setVisible(true);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setOpaque(false);
        roundLabel = new JLabel("Round: 1 / " + logic.getMaxRounds());
        roundLabel.setFont(new Font("Arial", Font.BOLD, 16));
        roundLabel.setForeground(fgColor);
        topPanel.add(roundLabel, BorderLayout.WEST);

        scoreLabel = new JLabel(getScoreText());
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(fgColor);
        topPanel.add(scoreLabel, BorderLayout.CENTER);

        modeToggleButton = new JButton("🌙 Cosmic Mode");
        modeToggleButton.setToolTipText("Toggle between cosmic and light mode");
        modeToggleButton.setMnemonic(KeyEvent.VK_D);
        modeToggleButton.setFont(new Font("Arial", Font.PLAIN, 14));
        modeToggleButton.setBackground(new Color(50, 50, 50));
        modeToggleButton.setForeground(fgColor);
        modeToggleButton.addActionListener(e -> toggleMode());
        topPanel.add(modeToggleButton, BorderLayout.EAST);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        int cellSize = Math.min(100, 400 / logic.getBoardSize());
        boardPanel = new TexturedPanel(true, cellSize * logic.getBoardSize(), cellSize * logic.getBoardSize());
        boardPanel.setLayout(new GridLayout(logic.getBoardSize(), logic.getBoardSize(), 8, 8));
        boardPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        buttons = new CustomButton[logic.getBoardSize()][logic.getBoardSize()];
        for (int i = 0; i < logic.getBoardSize(); i++) {
            for (int j = 0; j < logic.getBoardSize(); j++) {
                CustomButton btn = new CustomButton();
                btn.setFocusable(false);
                btn.setBackground(new Color(50, 50, 50, 200));
                btn.setOpaque(true);
                btn.setBorder(new RoundedBorder(15));
                btn.setToolTipText("Cell " + (i + 1) + "," + (j + 1));
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (btn.getSymbol().isEmpty() && !logic.isGameOver()) {
                            btn.setBackground(new Color(80, 80, 80, 200));
                        }
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (btn.getSymbol().isEmpty() && !logic.isGameOver()) {
                            btn.setBackground(new Color(50, 50, 50, 200));
                        }
                    }
                });
                int r = i, c = j;
                btn.addActionListener(e -> onCellClicked(r, c));
                buttons[i][j] = btn;
                boardPanel.add(btn);
            }
        }
        JScrollPane scrollPane = new JScrollPane(boardPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setOpaque(false);

        statusLabel = new JLabel("Your turn (" + logic.getPlayerSymbol() + ")");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        statusLabel.setForeground(fgColor);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(statusLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setOpaque(false);
        undoButton = new JButton("↩️ Undo");
        undoButton.setToolTipText("Undo last move (Ctrl+Z)");
        undoButton.setMnemonic(KeyEvent.VK_Z);
        undoButton.setFont(new Font("Arial", Font.PLAIN, 14));
        undoButton.setBackground(new Color(50, 50, 50));
        undoButton.setForeground(fgColor);
        undoButton.addActionListener(e -> undoMove());
        btnPanel.add(undoButton);

        restartRoundButton = new JButton("🔁 Restart Round");
        restartRoundButton.setToolTipText("Restart current round (Ctrl+R)");
        restartRoundButton.setMnemonic(KeyEvent.VK_R);
        restartRoundButton.setFont(new Font("Arial", Font.PLAIN, 14));
        restartRoundButton.setBackground(new Color(50, 50, 50));
        restartRoundButton.setForeground(fgColor);
        restartRoundButton.addActionListener(e -> restartRound());
        btnPanel.add(restartRoundButton);

        saveButton = new JButton("💾 Save");
        saveButton.setToolTipText("Save game state (Ctrl+S)");
        saveButton.setMnemonic(KeyEvent.VK_S);
        saveButton.setFont(new Font("Arial", Font.PLAIN, 14));
        saveButton.setBackground(new Color(50, 50, 50));
        saveButton.setForeground(fgColor);
        saveButton.addActionListener(e -> saveGame());
        btnPanel.add(saveButton);

        loadButton = new JButton("📂 Load");
        loadButton.setToolTipText("Load game state (Ctrl+L)");
        loadButton.setMnemonic(KeyEvent.VK_L);
        loadButton.setFont(new Font("Arial", Font.PLAIN, 14));
        loadButton.setBackground(new Color(50, 50, 50));
        loadButton.setForeground(fgColor);
        loadButton.addActionListener(e -> loadGame());
        btnPanel.add(loadButton);

        replayButton = new JButton("🔄 Replay");
        replayButton.setToolTipText("Start a new game (Ctrl+N)");
        replayButton.setMnemonic(KeyEvent.VK_N);
        replayButton.setFont(new Font("Arial", Font.PLAIN, 14));
        replayButton.setBackground(new Color(50, 50, 50));
        replayButton.setForeground(fgColor);
        replayButton.setVisible(false);
        replayButton.addActionListener(e -> replayGame());
        btnPanel.add(replayButton);

        closeButton = new JButton("❌ Close");
        closeButton.setToolTipText("Close the game (Ctrl+Q)");
        closeButton.setMnemonic(KeyEvent.VK_Q);
        closeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        closeButton.setBackground(new Color(50, 50, 50));
        closeButton.setForeground(fgColor);
        closeButton.setVisible(false);
        closeButton.addActionListener(e -> System.exit(0));
        btnPanel.add(closeButton);

        bottomPanel.add(btnPanel);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private void onCellClicked(int row, int col) {
        if (logic.isGameOver()) {
            return;
        }

        String symbol = logic.isPlayerTurn() ? logic.getPlayerSymbol() : logic.getOpponentSymbol();
        if (!logic.makeMove(row, col, symbol)) {
            return;
        }

        buttons[row][col].setSymbol(symbol);
        addParticleEffect(row, col);
        animateButton(buttons[row][col]);
        updateButtons(false);

        if (logic.checkWinner(symbol)) {
            logic.updateScore(symbol);
            String winner = logic.isPlayerTurn() ? logic.getPlayerName() : logic.getOpponentName();
            updateAfterRound(winner + " wins!");
            return;
        }

        if (logic.isBoardFull()) {
            logic.updateScore("");
            updateAfterRound("Draw!");
            return;
        }

        logic.switchTurn();
        updateStatusLabel();
        if (logic.isVsComputer() && !logic.isPlayerTurn()) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Computer is thinking...");
                Timer timer = new Timer(500, e -> computerMove());
                timer.setRepeats(false);
                timer.start();
            });
        }
    }

    private void computerMove() {
        if (logic.isGameOver() || logic.isPlayerTurn()) {
            return;
        }

        logic.computerMove();
        for (int i = 0; i < logic.getBoardSize(); i++) {
            for (int j = 0; j < logic.getBoardSize(); j++) {
                if (!buttons[i][j].getSymbol().equals(logic.getMark(i, j))) {
                    buttons[i][j].setSymbol(logic.getMark(i, j));
                    addParticleEffect(i, j);
                    animateButton(buttons[i][j]);
                }
            }
        }

        if (logic.checkWinner(logic.getComputerSymbol())) {
            logic.updateScore(logic.getComputerSymbol());
            updateAfterRound("Computer wins!");
            return;
        }

        if (logic.isBoardFull()) {
            logic.updateScore("");
            updateAfterRound("Draw!");
            return;
        }

        logic.switchTurn();
        updateStatusLabel();
    }

    private void updateButtons(boolean fullUpdate) {
        int size = logic.getBoardSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (fullUpdate || !buttons[i][j].getSymbol().equals(logic.getMark(i, j))) {
                    buttons[i][j].setSymbol(logic.getMark(i, j));
                    buttons[i][j].setSymbolColor(logic.getMark(i, j).equals("X") ? xColor : oColor);
                    if (!fullUpdate) {
                        animateButton(buttons[i][j]);
                    }
                }
            }
        }
    }

    private void updateStatusLabel() {
        if (logic.isGameOver()) {
            return;
        }

        if (logic.isPlayerTurn()) {
            statusLabel.setText(logic.getPlayerName() + "'s turn (" + logic.getPlayerSymbol() + ")");
        } else {
            if (logic.isVsComputer()) {
                statusLabel.setText("Computer's turn (" + logic.getComputerSymbol() + ")");
            } else {
                statusLabel.setText(logic.getOpponentName() + "'s turn (" + logic.getOpponentSymbol() + ")");
            }
        }
    }

    private void updateAfterRound(String message) {
        statusLabel.setText(message);
        scoreLabel.setText(getScoreText());
        resetBoardUI();
        if (!message.equals("Draw!")) {
            highlightWinningLine(message);
        }

        if (logic.getCurrentRound() >= logic.getMaxRounds()) {
            endGame();
        } else {
            logic.nextRound();
            roundLabel.setText("Round: " + logic.getCurrentRound() + " / " + logic.getMaxRounds());
            resetBoardUI();
            updateStatusLabel();
        }
    }

    private void highlightWinningLine(String message) {
        String symbol = message.contains(logic.getPlayerName()) ? logic.getPlayerSymbol() : (logic.isVsComputer() ? logic.getComputerSymbol() : logic.getOpponentSymbol());
        Timer glowTimer = new Timer(100, null);
        final float[] glowPhase = {0};
        glowTimer.addActionListener(e -> {
            glowPhase[0] += 0.1f;
            if (glowPhase[0] > 2 * Math.PI) glowPhase[0] -= 2 * Math.PI;
            boolean updated = false;

            for (int i = 0; i < logic.getBoardSize(); i++) {
                boolean win = true;
                for (int j = 0; j < logic.getBoardSize(); j++) {
                    if (!logic.getMark(i, j).equals(symbol)) {
                        win = false;
                        break;
                    }
                }
                if (win) {
                    for (int j = 0; j < logic.getBoardSize(); j++) {
                        buttons[i][j].setGlowPhase(glowPhase[0]);
                        buttons[i][j].setBackground(winHighlight);
                    }
                    updated = true;
                }
            }

            if (!updated) {
                for (int j = 0; j < logic.getBoardSize(); j++) {
                    boolean win = true;
                    for (int i = 0; i < logic.getBoardSize(); i++) {
                        if (!logic.getMark(i, j).equals(symbol)) {
                            win = false;
                            break;
                        }
                    }
                    if (win) {
                        for (int i = 0; i < logic.getBoardSize(); i++) {
                            buttons[i][j].setGlowPhase(glowPhase[0]);
                            buttons[i][j].setBackground(winHighlight);
                        }
                        updated = true;
                    }
                }
            }

            if (!updated) {
                boolean diag1 = true;
                for (int i = 0; i < logic.getBoardSize(); i++) {
                    if (!logic.getMark(i, i).equals(symbol)) {
                        diag1 = false;
                        break;
                    }
                }
                if (diag1) {
                    for (int i = 0; i < logic.getBoardSize(); i++) {
                        buttons[i][i].setGlowPhase(glowPhase[0]);
                        buttons[i][i].setBackground(winHighlight);
                    }
                    updated = true;
                }
            }

            if (!updated) {
                boolean diag2 = true;
                for (int i = 0; i < logic.getBoardSize(); i++) {
                    if (!logic.getMark(i, logic.getBoardSize() - i - 1).equals(symbol)) {
                        diag2 = false;
                        break;
                    }
                }
                if (diag2) {
                    for (int i = 0; i < logic.getBoardSize(); i++) {
                        buttons[i][logic.getBoardSize() - i - 1].setGlowPhase(glowPhase[0]);
                        buttons[i][logic.getBoardSize() - i - 1].setBackground(winHighlight);
                    }
                }
            }

            if (glowPhase[0] > 4 * Math.PI) glowTimer.stop();
            repaint();
        });
        glowTimer.start();
    }

    private void resetBoardUI() {
        for (int i = 0; i < logic.getBoardSize(); i++) {
            for (int j = 0; j < logic.getBoardSize(); j++) {
                buttons[i][j].setSymbol("");
                buttons[i][j].setBackground(new Color(50, 50, 50, 200));
                buttons[i][j].setGlowPhase(0);
            }
        }
    }

    private void endGame() {
        statusLabel.setText("Game Over!");
        replayButton.setVisible(true);
        closeButton.setVisible(true);
        for (CustomButton[] row : buttons) {
            for (CustomButton btn : row) {
                btn.setEnabled(false);
            }
        }
    }

    private void undoMove() {
        if (logic.undoLastMove()) {
            updateButtons(true);
            updateStatusLabel();
        }
    }

    private void restartRound() {
        logic.restartRound();
        resetBoardUI();
        updateStatusLabel();
        roundLabel.setText("Round: " + logic.getCurrentRound() + " / " + logic.getMaxRounds());
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                logic.saveGame(fileChooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Game saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                logic = TicTacToeLogic.loadGame(fileChooser.getSelectedFile().getAbsolutePath());
                dispose();
                SwingUtilities.invokeLater(() -> new TicTacToeGUI().setLogic(logic));
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error loading game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void replayGame() {
        logic.replayGame();
        dispose();
        SwingUtilities.invokeLater(() -> new TicTacToeGUI());
    }

    private void setLogic(TicTacToeLogic logic) {
        this.logic = logic;
        int panelWidth = 500;
        int panelHeight = 450;

        TexturedPanel contentPane = new TexturedPanel(false, panelWidth, panelHeight);
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        setTitle("🎲 Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(panelWidth, panelHeight);
        setLocationRelativeTo(null);

        initComponents();
        updateButtons(true);
        updateStatusLabel();
        roundLabel.setText("Round: " + logic.getCurrentRound() + " / " + logic.getMaxRounds());
        applyTheme();
        setVisible(true);
    }

    private void toggleMode() {
        darkMode = !darkMode;
        modeToggleButton.setText(darkMode ? "☀️ Light Mode" : "🌙 Cosmic Mode");
        applyTheme();
    }

    private void applyTheme() {
        if (darkMode) {
            bgStart = new Color(10, 50, 60);
            bgEnd = new Color(20, 80, 40);
            boardStart = new Color(20, 60, 70);
            boardEnd = new Color(30, 90, 50);
            fgColor = Color.WHITE;
        } else {
            bgStart = new Color(220, 240, 240);
            bgEnd = new Color(200, 220, 200);
            boardStart = new Color(200, 220, 220);
            boardEnd = new Color(180, 200, 180);
            fgColor = Color.BLACK;
        }

        getContentPane().setBackground(bgStart);
        roundLabel.setForeground(fgColor);
        scoreLabel.setForeground(fgColor);
        statusLabel.setForeground(fgColor);
        modeToggleButton.setForeground(fgColor);
        modeToggleButton.setBackground(new Color(50, 50, 50));
        undoButton.setForeground(fgColor);
        undoButton.setBackground(new Color(50, 50, 50));
        restartRoundButton.setForeground(fgColor);
        restartRoundButton.setBackground(new Color(50, 50, 50));
        saveButton.setForeground(fgColor);
        saveButton.setBackground(new Color(50, 50, 50));
        loadButton.setForeground(fgColor);
        loadButton.setBackground(new Color(50, 50, 50));
        replayButton.setForeground(fgColor);
        replayButton.setBackground(new Color(50, 50, 50));
        closeButton.setForeground(fgColor);
        closeButton.setBackground(new Color(50, 50, 50));

        for (int i = 0; i < logic.getBoardSize(); i++) {
            for (int j = 0; j < logic.getBoardSize(); j++) {
                buttons[i][j].setSymbolColor(logic.getMark(i, j).equals("X") ? xColor : oColor);
                buttons[i][j].repaint();
            }
        }

        repaint();
    }

    private void addParticleEffect(int row, int col) {
        Point buttonPos = buttons[row][col].getLocation();
        int centerX = buttonPos.x + buttons[row][col].getWidth() / 2;
        int centerY = buttonPos.y + buttons[row][col].getHeight() / 2;
        Color color = logic.getMark(row, col).equals("X") ? xColor : oColor;
        for (int i = 0; i < 6; i++) {
            ((TexturedPanel) boardPanel).particles.add(new Particle(centerX, centerY, color));
        }
    }

    private void animateButton(CustomButton button) {
        Timer timer = new Timer(50, null);
        final float[] phase = {0};
        timer.addActionListener(e -> {
            phase[0] += 0.2f;
            if (phase[0] > 2 * Math.PI) {
                timer.stop();
                button.setGlowPhase(0);
            } else {
                button.setGlowPhase(phase[0]);
            }
            button.repaint();
        });
        timer.start();
    }

    private String getScoreText() {
        return logic.getPlayerName() + ": " + logic.getPlayerScore() + " | " +
        logic.getOpponentName() + ": " + logic.getOpponentScore() + " | Draw: " + logic.getDrawCount();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeGUI());
    }
}