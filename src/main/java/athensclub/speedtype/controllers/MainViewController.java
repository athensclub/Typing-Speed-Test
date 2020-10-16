package athensclub.speedtype.controllers;

import athensclub.speedtype.Loader;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MainViewController implements Initializable {

    @FXML
    private StyleClassedTextArea inputText;

    @FXML
    private StyleClassedTextArea displayText;

    @FXML
    private Label statInfo;

    private SimpleIntegerProperty remainingTime;

    private SimpleIntegerProperty correctCharacters;

    private SimpleBooleanProperty currentWordCorrect;

    private SimpleStringProperty currentWord;

    private SimpleIntegerProperty currentWordIndex;

    private SimpleIntegerProperty timeTakenOnLastWord;

    private boolean over;

    private Timer timer;

    private TimerTask currentTimer;

    private Instant lastWordStartTime;

    private ObservableList<String> current;

    private LinkedList<String> typed;

    private String[] words;

    private Random random;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        words = Loader.loadWords();
        typed = new LinkedList<>();
        current = FXCollections.observableArrayList();
        random = new Random();
        timer = new Timer("Typing Speed Test Timer");
        timeTakenOnLastWord = new SimpleIntegerProperty(0);
        remainingTime = new SimpleIntegerProperty(60);
        SimpleStringProperty textOfInput = new SimpleStringProperty("");
        currentWord = new SimpleStringProperty("");
        correctCharacters = new SimpleIntegerProperty(0);
        currentWordCorrect = new SimpleBooleanProperty(false);
        SimpleIntegerProperty cpm = new SimpleIntegerProperty(0);
        SimpleIntegerProperty wpm = new SimpleIntegerProperty(0);
        correctCharacters = new SimpleIntegerProperty(0);
        currentWordIndex = new SimpleIntegerProperty(0);
        over = false;
        reset();

        textOfInput.bind(inputText.textProperty());
        currentWord.bind(Bindings.stringValueAt(current, currentWordIndex));
        currentWordCorrect.bind(currentWord.isEqualTo(textOfInput));
        NumberBinding timePassed = Bindings.subtract(60, remainingTime).add(timeTakenOnLastWord);
        cpm.bind(correctCharacters.multiply(60).divide(
                Bindings.when(timePassed.isEqualTo(0))
                        .then(1)
                        .otherwise(timePassed)));
        wpm.bind(cpm.divide(5));
        statInfo.textProperty()
                .bind(Bindings.concat("Correct CPM: ", cpm, " WPM: ", wpm,
                        " time left: ", remainingTime, "s"));

        remainingTime.addListener((prop, old, val) -> {
            over = remainingTime.get() == 0;
            if (over) {
                currentTimer.cancel();
                currentTimer = null;
                lastWordStartTime = Instant.now();
            }
        });

        //use event filter because event handler will get the inputText that is empty from backspacing the
        //last character
        inputText.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.BACK_SPACE &&
                    currentWordIndex.get() > 0 &&
                    inputText.getText().isEmpty() &&
                    !over) {
                back();
            }
        });

        inputText.textProperty().addListener((prop, old, val) -> {
            if(inputText.isDisabled())
                return; // don't check correctness when the text is 'Press restart to try again'

            if (!over && currentTimer == null) {
                currentTimer = new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> remainingTime.set(remainingTime.get() - 1));
                    }
                };
                timer.scheduleAtFixedRate(currentTimer, 1000, 1000);
            }

            if (val.contains(" ")) {
                String[] parts = val.split(" ");
                String last = parts.length == 0 ? "" : parts[0];
                typed.add(last); // become previous part
                addCpmIfCorrect(last);
                highlightCorrectness(last);

                if (!over) {
                    Platform.runLater(() -> {
                        currentWordIndex.set(currentWordIndex.get() + 1);
                        inputText.replace(0, inputText.getText().length(), parts.length > 1 ? parts[1] : "", "");
                        // highlightCurrentWord will already be called by this exact handler
                    });
                } else {
                    Instant now = Instant.now();
                    Platform.runLater(() -> {
                        timeTakenOnLastWord.set((int) Duration.between(lastWordStartTime, now).getSeconds());
                        inputText.setDisable(true); // make disable before replacing so it is checkable
                        inputText.replace(0, inputText.getText().length(), "Press restart to try again.", "");
                    });
                }
            } else {
                highlightCurrentWord();
            }


        });
    }

    @FXML
    public void onRestartPressed() {
        reset();
    }

    /**
     * Add the current word length to the CPM, if the input matches with the current word.
     *
     * @param input the input to check.
     */
    private void addCpmIfCorrect(String input) {
        String correct = currentWord.get();
        if (input.equals(correct))
            Platform.runLater(() -> correctCharacters
                    .set(correctCharacters.get() + currentWord.length().get() + 1)); // count the spaces
    }

    /**
     * Make the current word green if the user correctly typed the entire word, otherwise make the
     * current word red.
     *
     * @param input the text to be compared with the correct word.
     */
    private void highlightCorrectness(String input) {
        int idx = stringIndexOf(currentWordIndex.get());
        String correct = currentWord.get();
        if (input.equals(correct))
            Platform.runLater(() ->
                    displayText.setStyle(idx, idx + correct.length(), List.of("green-text")));
        else
            Platform.runLater(() ->
                    displayText.setStyle(idx, idx + correct.length(), List.of("red-text")));

    }

    /**
     * Make the current word has current-word style class and highlight the correctly typed character.
     * Also bring the caret to the current word and request to follow it.
     */
    private void highlightCurrentWord() {
        int idx = stringIndexOf(currentWordIndex.get());
        String correct = currentWord.get();
        String input = inputText.getText();
        for (int i = 0; i < input.length() && i < correct.length(); i++) {
            final int temp = i;
            if (correct.charAt(i) == input.charAt(i))
                Platform.runLater(() ->
                        displayText.setStyle(idx + temp, idx + temp + 1, List.of("current-word", "green-text")));
            else
                Platform.runLater(() ->
                        displayText.setStyle(idx + temp, idx + temp + 1, List.of("current-word", "red-text")));
        }
        if (input.length() < correct.length())
            Platform.runLater(() ->
                    displayText.setStyle(idx + input.length(), idx + correct.length(), List.of("current-word")));
        else if (input.length() > correct.length())
            Platform.runLater(() ->
                    displayText.setStyle(idx, idx + correct.length(), List.of("current-word", "red-text")));
        Platform.runLater(() -> {
            displayText.displaceCaret(idx);
            displayText.requestFollowCaret();
        });
    }

    /**
     * Go back to the previous word.
     */
    private void back() {
        int idx = stringIndexOf(currentWordIndex.get());
        String word = currentWord.get();
        String last = typed.removeLast();
        Platform.runLater(() -> {
            displayText.clearStyle(idx, idx + word.length());
            currentWordIndex.set(currentWordIndex.get() - 1);
            inputText.replace(0, inputText.getText().length(), last, "");
            if (currentWordCorrect.get())
                correctCharacters.set(correctCharacters.get() - currentWord.length().get() - 1); // count the spaces
            highlightCurrentWord();
        });
    }

    /**
     * Get the index of the beginning character of the word in a string of joined words (spacing included,
     * the one being displayed).
     *
     * @param wordIndex the index of the word in the list of words.
     * @return the index of the beginning of the word in a string of joined words.
     */
    private int stringIndexOf(int wordIndex) {
        int idx = 0;
        for (int i = 0; i < wordIndex; i++)
            idx += current.get(i).length() + 1; // +1 is including space
        return idx;
    }

    /**
     * Reset by doing the following:
     * <ul>
     *     <li>Randomize new set of words.</li>
     *     <li>Make the current word the first word.</li>
     *     <li>Clear all the typed word.</li>
     *     <li>Display the new words.</li>
     *     <li>Reset the timer.</li>
     *     <li>Enable the input text.</li>
     *     <li>Number of correctly typed characters.</li>
     * </ul>
     */
    private void reset() {
        List<String> randomWords = random
                .ints(1000, 0, words.length)
                .mapToObj(i -> words[i])
                .collect(Collectors.toList());
        typed.clear();
        over = false;
        if (currentTimer != null)
            currentTimer.cancel();
        currentTimer = null;
        Platform.runLater(() -> {
            inputText.clear();
            timeTakenOnLastWord.set(0);
            correctCharacters.set(0);
            currentWordIndex.set(0);
            current.clear();
            current.addAll(randomWords);
            displayText.replace(0, displayText.getText().length(), String.join(" ", current), "");
            inputText.setDisable(false);
            remainingTime.set(60);
            highlightCurrentWord();
        });
    }

}
