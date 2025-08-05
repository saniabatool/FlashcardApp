
package com.student.flashcards;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * A professional and aesthetically pleasing Java Swing application for managing and studying flashcards.
 * This version enhances the user interface with a modern look and feel, custom colors, and a cleaner layout.
 * It now includes functionality to create, edit, and delete flashcards.
 */
public class FlashcardApp {

    // --- Data Structures ---

    /**
     * A simple class to represent a single flashcard.
     * It holds the question (front) and answer (back) of the card.
     */
    static class Flashcard {
        private String front;
        private String back;

        public Flashcard(String front, String back) {
            this.front = front;
            this.back = back;
        }

        public String getFront() {
            return front;
        }

        public String getBack() {
            return back;
        }

        public void setFront(String front) {
            this.front = front;
        }

        public void setBack(String back) {
            this.back = back;
        }
        
        @Override
        public String toString() {
            return front + "|" + back;
        }
    }

    // --- GUI Components and State Variables ---

    private JFrame frame;
    private JPanel mainPanel;
    private JLabel cardLabel; // Changed from JTextArea to JLabel for better formatting
    private JButton flipButton;
    private JButton nextButton;
    private JButton prevButton;

    private JTextArea questionInput;
    private JTextArea answerInput;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;

    private List<Flashcard> deck;
    private int currentCardIndex = 0;
    private boolean isFrontVisible = true;
    private Timer animationTimer;
    private int currentFontSize = 0;
    private final int MAX_FONT_SIZE = 36;
    private final int ANIMATION_STEPS = 10;

    // --- Constructor ---

    public FlashcardApp() {
        // Set the Look and Feel to Nimbus for a modern aesthetic
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to the default
        }
        
        // Initialize the GUI
        initializeGUI();
        
        // Initialize the deck with some sample data
        initializeDeck();
        
        // Show the first card
        showCurrentCard();
    }
    
    // Custom JPanel to draw the lined paper background with flowers
    class LinedCardPanel extends JPanel {
        // Updated colors to better match the user's design
        private final Color LINE_COLOR = new Color(175, 205, 175);
        private final Color BACKGROUND_COLOR = new Color(205, 235, 205);

        public LinedCardPanel() {
            setBackground(BACKGROUND_COLOR);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelHeight = getHeight();
            int panelWidth = getWidth();
            
            // Draw horizontal lines
            g2d.setColor(LINE_COLOR);
            for (int y = 50; y < panelHeight - 50; y += 30) {
                g2d.drawLine(50, y, panelWidth - 50, y);
            }
            
            // Draw flowers based on the user's design
            // Top-left flower
            drawFlower(g2d, 50, 50, false); 
            // Top-right flower (just the head)
            drawFlower(g2d, panelWidth - 90, 50, false);
            // Bottom-left flower with stem
            drawFlower(g2d, 50, panelHeight - 90, true);
            // Bottom-right flower with stem
            drawFlower(g2d, panelWidth - 90, panelHeight - 90, true);
        }
        
        private void drawFlower(Graphics2D g2d, int x, int y, boolean withStem) {
            // Updated colors for the flowers
            Color centerColor = new Color(175, 205, 175);
            Color petalColor = Color.WHITE;
            Color stemColor = new Color(120, 160, 120);
            
            // Draw petals (8 petals)
            g2d.setColor(petalColor);
            int petalSize = 25;
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45);
                int petalX = (int) (x + 15 + petalSize * Math.cos(angle));
                int petalY = (int) (y + 15 + petalSize * Math.sin(angle));
                g2d.fillOval(petalX - petalSize / 2, petalY - petalSize / 2, petalSize, petalSize);
            }
            
            // Draw center
            g2d.setColor(centerColor);
            g2d.fillOval(x, y, 30, 30);
            
            // Draw stem if requested
            if (withStem) {
                g2d.setColor(stemColor);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(x + 15, y + 30, x + 15, y + 60);
            }
        }
    }

    /**
     * Sets up the main window and all the GUI components with an enhanced design.
     * This method is now more complex to accommodate the new CRUD and save/load functionality.
     */
    private void initializeGUI() {
        // Create the main frame
        frame = new JFrame("Flashcard App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700); // Increased window size for more features
        frame.setLocationRelativeTo(null); // Center the window on the screen

        // Define the color palette
        Color mainBackgroundColor = new Color(180, 200, 170);
        Color buttonColor = new Color(200, 220, 190);
        Color textColor = new Color(50, 50, 50);
        
        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(mainBackgroundColor);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Create the custom card panel with a light background and the lined effect
        LinedCardPanel cardPanel = new LinedCardPanel();
        
        // Create the label to display the flashcard text
        cardLabel = new JLabel("", SwingConstants.CENTER);
        cardLabel.setFont(new Font("Segoe Script", Font.PLAIN, MAX_FONT_SIZE));
        cardLabel.setForeground(textColor);
        
        // Set the label to be transparent so the background lines are visible
        cardLabel.setOpaque(false);
        
        // Use a JScrollPane for the label to handle long text
        JScrollPane cardScrollPane = new JScrollPane(cardLabel);
        cardScrollPane.setBorder(BorderFactory.createEmptyBorder());
        cardScrollPane.setOpaque(false);
        cardScrollPane.getViewport().setOpaque(false);
        
        // Add the card scroll pane to the center of the card panel
        cardPanel.add(cardScrollPane, BorderLayout.CENTER);
        
        // Add the card panel to the center of the main panel
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // --- Bottom panel for controls and CRUD functionality ---
        JPanel bottomPanel = new JPanel(new BorderLayout(20, 20));
        bottomPanel.setBackground(mainBackgroundColor);

        // Panel for navigation buttons
        JPanel navigationButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        navigationButtonPanel.setBackground(mainBackgroundColor);
        
        // Customize button appearance
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 16);
        
        prevButton = new JButton("Previous Card");
        prevButton.setFont(buttonFont);
        prevButton.setBackground(buttonColor);
        prevButton.setForeground(textColor);
        prevButton.setFocusPainted(false);
        prevButton.setBorderPainted(false);
        prevButton.addActionListener(e -> prevCard());

        flipButton = new JButton("Flip Card");
        flipButton.setFont(buttonFont);
        flipButton.setBackground(buttonColor);
        flipButton.setForeground(textColor);
        flipButton.setFocusPainted(false);
        flipButton.setBorderPainted(false);
        flipButton.addActionListener(e -> flipCard());

        nextButton = new JButton("Next Card");
        nextButton.setFont(buttonFont);
        nextButton.setBackground(buttonColor);
        nextButton.setForeground(textColor);
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.addActionListener(e -> nextCard());
        
        navigationButtonPanel.add(prevButton);
        navigationButtonPanel.add(flipButton);
        navigationButtonPanel.add(nextButton);
        
        bottomPanel.add(navigationButtonPanel, BorderLayout.NORTH);

        // Panel for CRUD controls
        JPanel crudPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        crudPanel.setBackground(mainBackgroundColor);
        
        JPanel questionPanel = new JPanel(new BorderLayout(5, 5));
        questionPanel.setBackground(mainBackgroundColor);
        questionPanel.add(new JLabel("Question:"), BorderLayout.WEST);
        questionInput = new JTextArea(2, 20);
        questionInput.setLineWrap(true);
        questionInput.setWrapStyleWord(true);
        questionPanel.add(new JScrollPane(questionInput), BorderLayout.CENTER);
        
        JPanel answerPanel = new JPanel(new BorderLayout(5, 5));
        answerPanel.setBackground(mainBackgroundColor);
        answerPanel.add(new JLabel("Answer:"), BorderLayout.WEST);
        answerInput = new JTextArea(2, 20);
        answerInput.setLineWrap(true);
        answerInput.setWrapStyleWord(true);
        answerPanel.add(new JScrollPane(answerInput), BorderLayout.CENTER);

        JPanel crudButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        crudButtonPanel.setBackground(mainBackgroundColor);
        
        addButton = new JButton("Add Card");
        addButton.setFont(buttonFont);
        addButton.setBackground(buttonColor);
        addButton.setForeground(textColor);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> addCard());
        
        editButton = new JButton("Edit Card");
        editButton.setFont(buttonFont);
        editButton.setBackground(buttonColor);
        editButton.setForeground(textColor);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.addActionListener(e -> editCard());
        
        deleteButton = new JButton("Delete Card");
        deleteButton.setFont(buttonFont);
        deleteButton.setBackground(buttonColor);
        deleteButton.setForeground(textColor);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(e -> deleteCard());
        
        crudButtonPanel.add(addButton);
        crudButtonPanel.add(editButton);
        crudButtonPanel.add(deleteButton);

        crudPanel.add(questionPanel);
        crudPanel.add(answerPanel);
        crudPanel.add(crudButtonPanel);
        
        bottomPanel.add(crudPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add the main panel to the frame
        frame.add(mainPanel);
        frame.setVisible(true);
    }
    
    /**
     * Populates the deck with sample flashcards.
     */
    private void initializeDeck() {
        deck = new ArrayList<>();
        deck.add(new Flashcard("What is a Java Virtual Machine (JVM)?", "An abstract machine that enables a computer to run Java programs."));
        deck.add(new Flashcard("What is the main method in Java?", "The entry point for any Java program."));
        deck.add(new Flashcard("What is a class in Java?", "A blueprint for creating objects."));
        deck.add(new Flashcard("What is an object in Java?", "An instance of a class."));
        deck.add(new Flashcard("What are the primitive data types in Java?", "byte, short, int, long, float, double, boolean, char."));
        deck.add(new Flashcard("What is a variable?", "A container that holds the value during the Java program execution."));
        deck.add(new Flashcard("What is a constructor in Java?", "A special method used to initialize objects."));
        deck.add(new Flashcard("How do you declare a variable in Java?", "datatype variableName = value;"));
        deck.add(new Flashcard("What is an array in Java?", "A group of like-typed variables referred to by a common name."));
        deck.add(new Flashcard("What does the 'public' keyword mean?", "The member can be accessed from anywhere."));
        
        // Shuffle the deck for a better study experience
        Collections.shuffle(deck);
    }

    /**
     * Displays the current flashcard's front or back, based on the state, with an animation.
     */
    private void showCurrentCard() {
        if (deck.isEmpty()) {
            cardLabel.setText("<html><center>Deck is empty! Add a new card to begin.</center></html>");
            flipButton.setEnabled(false);
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            return;
        } else {
            flipButton.setEnabled(true);
            nextButton.setEnabled(true);
            prevButton.setEnabled(true);
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }

        Flashcard currentCard = deck.get(currentCardIndex);
        String textToShow = isFrontVisible ? currentCard.getFront() : currentCard.getBack();
        
        // The text is wrapped in HTML tags to allow for multi-line centered text in a JLabel
        cardLabel.setText("<html><center>" + textToShow.replace("\n", "<br>") + "</center></html>");
        
        // Stop any existing animation
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        currentFontSize = 0;
        animationTimer = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFontSize < MAX_FONT_SIZE) {
                    currentFontSize += 3;
                    if (currentFontSize > MAX_FONT_SIZE) {
                        currentFontSize = MAX_FONT_SIZE;
                    }
                    cardLabel.setFont(new Font("Segoe Script", Font.PLAIN, currentFontSize));
                } else {
                    ((Timer)e.getSource()).stop();
                }
            }
        });
        animationTimer.start();
        
        // Update input fields to show current card's data for editing
        questionInput.setText(currentCard.getFront());
        answerInput.setText(currentCard.getBack());
    }

    /**
     * Toggles between showing the front and back of the current card.
     */
    private void flipCard() {
        if (deck.isEmpty()) return;
        isFrontVisible = !isFrontVisible;
        showCurrentCard();
    }

    /**
     * Moves to the next card in the deck and resets the view to show the front.
     */
    private void nextCard() {
        if (deck.isEmpty()) return;
        currentCardIndex = (currentCardIndex + 1) % deck.size();
        isFrontVisible = true;
        showCurrentCard();
    }
    
    /**
     * Moves to the previous card in the deck and resets the view to show the front.
     */
    private void prevCard() {
        if (deck.isEmpty()) return;
        currentCardIndex = (currentCardIndex - 1 + deck.size()) % deck.size();
        isFrontVisible = true;
        showCurrentCard();
    }

    /**
     * Adds a new flashcard to the deck.
     */
    private void addCard() {
        String question = questionInput.getText().trim();
        String answer = answerInput.getText().trim();
        
        if (question.isEmpty() || answer.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Question and Answer cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        deck.add(new Flashcard(question, answer));
        currentCardIndex = deck.size() - 1; // Move to the newly added card
        isFrontVisible = true;
        showCurrentCard();
        JOptionPane.showMessageDialog(frame, "Card added successfully!");
        
        // Clear the input fields for the next card
        questionInput.setText("");
        answerInput.setText("");
    }
    
    /**
     * Edits the current flashcard with the new question and answer.
     */
    private void editCard() {
        if (deck.isEmpty()) return;
        
        String question = questionInput.getText().trim();
        String answer = answerInput.getText().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Question and Answer cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Flashcard currentCard = deck.get(currentCardIndex);
        currentCard.setFront(question);
        currentCard.setBack(answer);
        showCurrentCard();
        JOptionPane.showMessageDialog(frame, "Card edited successfully!");
    }

    /**
     * Deletes the current flashcard from the deck.
     */
    private void deleteCard() {
        if (deck.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this card?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            deck.remove(currentCardIndex);
            // Handle the case if the deck becomes empty or if we were at the last card
            if (!deck.isEmpty()) {
                currentCardIndex = Math.max(0, Math.min(currentCardIndex, deck.size() - 1));
            } else {
                currentCardIndex = 0;
            }
            isFrontVisible = true;
            showCurrentCard();
            JOptionPane.showMessageDialog(frame, "Card deleted successfully!");
        }
    }

    /**
     * Main method to run the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Use invokeLater to ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new FlashcardApp());
    }
}
