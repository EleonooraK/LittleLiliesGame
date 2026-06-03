import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

public class GameApp extends Application {
    private GameLogic logic = new GameLogic();

    private Label dialogueLabel;
    private Label locationLabel;
    private Label promptLabel;
    private Label errorLabel;
    private ImageView playerDot;
    private ImageView mapImageView;
    private Scene menuScene;

    private MediaPlayer videoPlayer, audioPlayer;
    private MediaPlayer gameAudioPlayer;
    private int selectionIndex = 0;
    private final Label playOption = new Label("  START GAME  ");
    private final Label collectedOption = new Label("  ENDINGS COLLECTED  ");
    private final Label quitOption = new Label("  QUIT GAME  ");

    private boolean isShowingCommands = false;
    private final List<String> storyScriptQueue = new ArrayList<>();
    private int currentQueueIndex = 0;
    private boolean textAnimationRunning = false;
    private Timeline typewriterTimeline;
    private String cleanActiveLineText = "";

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #2e0b3d;");

        MediaView backgroundVideo = new MediaView();
        try {
            URL videoUrl = getClass().getResource("/start_screen.mp4");
            URL audioUrl = getClass().getResource("/Start_screen_audio.mp3");

            if (videoUrl != null) {
                videoPlayer = new MediaPlayer(new Media(videoUrl.toExternalForm()));
                videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                videoPlayer.setMute(true);
                videoPlayer.play();
                backgroundVideo.setMediaPlayer(videoPlayer);
                backgroundVideo.fitWidthProperty().bind(root.widthProperty());
                backgroundVideo.fitHeightProperty().bind(root.heightProperty());
            }
            if (audioUrl != null) {
                audioPlayer = new MediaPlayer(new Media(audioUrl.toExternalForm()));
                audioPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                audioPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Media fallbacks: " + e.getMessage());
        }

        Label title = new Label("『 LITTLE LILIES 』");
        title.setStyle("-fx-font-size: 48px; -fx-text-fill: #e0b0ff; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        updateMenuStyles();

        VBox menuUI = new VBox(20, title, playOption, collectedOption, quitOption);
        menuUI.setAlignment(Pos.CENTER);
        root.getChildren().addAll(backgroundVideo, menuUI);

        // Assigned to the global field variable
        menuScene = new Scene(root, 960, 540);
        menuScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) {
                selectionIndex--;
                if (selectionIndex < 0) selectionIndex = 2;
                updateMenuStyles();
            }
            else if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) {
                selectionIndex++;
                if (selectionIndex > 2) selectionIndex = 0;
                updateMenuStyles();
            }
            else if (e.getCode() == KeyCode.ENTER) {
                if (selectionIndex == 0) {
                    if (videoPlayer != null) videoPlayer.stop();
                    if (audioPlayer != null) audioPlayer.stop();
                    switchToGame(primaryStage);
                } else if (selectionIndex == 1) {
                    showEndingsPopup(primaryStage);
                } else {
                    primaryStage.close();
                }
            }
        });

        playOption.setOnMouseEntered(e -> { selectionIndex = 0; updateMenuStyles(); });
        playOption.setOnMouseClicked(e -> handleMenuSelection(primaryStage));

        collectedOption.setOnMouseEntered(e -> { selectionIndex = 1; updateMenuStyles(); });
        collectedOption.setOnMouseClicked(e -> handleMenuSelection(primaryStage));

        quitOption.setOnMouseEntered(e -> { selectionIndex = 2; updateMenuStyles(); });
        quitOption.setOnMouseClicked(e -> handleMenuSelection(primaryStage));

        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void handleMenuSelection(Stage stage) {
        if (selectionIndex == 0) {
            if (videoPlayer != null) videoPlayer.stop();
            if (audioPlayer != null) audioPlayer.stop();
            switchToGame(stage);
        } else if (selectionIndex == 1) {
            showEndingsPopup(stage);
        } else {
            stage.close();
        }
    }

    private void switchToGame(Stage stage) {
        try {
            URL gameAudioUrl = getClass().getResource("/Game_audio.mp3");            if (gameAudioUrl != null) {
                gameAudioPlayer = new MediaPlayer(new Media(gameAudioUrl.toExternalForm()));
                gameAudioPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                gameAudioPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Media fallbacks: " + e.getMessage());
        }
        
        VBox gameRoot = new VBox();
        gameRoot.setStyle("-fx-background-color: #1c0526;");

        StackPane mapContainer = new StackPane();
        mapContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(mapContainer, Priority.ALWAYS);

        try {
            mapImageView = new ImageView(new Image(getClass().getResourceAsStream("/map_hedge.PNG")));        } catch (Exception e) {
            System.err.println("Error loading base map: " + e.getMessage());
            mapImageView = new ImageView();
        }
        mapImageView.fitWidthProperty().bind(gameRoot.widthProperty());
        mapImageView.fitHeightProperty().bind(gameRoot.heightProperty().multiply(0.66));
        mapImageView.setPreserveRatio(true);

        try{
            Image player = new Image(getClass().getResourceAsStream("/Lily.png"));
            playerDot = new ImageView(player);

        }catch (Exception e) {
            System.err.println("Media fallbacks: " + e.getMessage());
        }

        mapContainer.getChildren().addAll(mapImageView, playerDot);

        VBox dialogueBox = new VBox(3);
        dialogueBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-padding: 15; -fx-border-color: #7d5a8a; -fx-border-width: 2 0 0 0;");
        dialogueBox.prefHeightProperty().bind(gameRoot.heightProperty().multiply(0.34));
        dialogueBox.maxWidthProperty().bind(gameRoot.widthProperty());
        dialogueBox.setAlignment(Pos.TOP_LEFT);

        locationLabel = new Label("LOCATION: FOUNTAIN");
        locationLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");

        dialogueLabel = new Label();
        dialogueLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        dialogueLabel.setWrapText(true);

        promptLabel = new Label();
        promptLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        VBox.setMargin(promptLabel, new Insets(5, 0, 0, 0));

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff4a4a; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");

        dialogueBox.getChildren().addAll(locationLabel, dialogueLabel, promptLabel, errorLabel);        gameRoot.getChildren().addAll(mapContainer, dialogueBox);
        Scene gameScene = new Scene(gameRoot, 960, 540);

        updateVisualCoordinates();

        enqueueScriptLines(new String[]{
                "STORY: You wake up. It's raining. There's nobody around.",
                "STORY: \"I have to get out. I know her secret. She's using all of us. I can't stay here.\"",
        });

        gameScene.setOnKeyPressed(e -> {
            errorLabel.setText("");
            if (dialogueLabel.getText().startsWith("== INVENTORY ==")) {
                if (e.getCode() == KeyCode.ENTER) {
                    renderCompactCommands();
                }
                return;
            }

            if (isShowingCommands && e.getCode() == KeyCode.ENTER) {
                dialogueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
                dialogueLabel.setText("== COMMAND INTERFACE ==\n [W,A,S,D / Arrows] Move Character Dot\n [I] View Inventory Items");
                promptLabel.setText("[Press ENTER for area actions...]");

                isShowingCommands = false;
                return;
            }

            if (isShowingCommands && e.getCode().isDigitKey()) {
                String keyText = e.getText();

                if (dialogueLabel.getText().contains("== ESCAPE THE GARDEN ==")) {
                    if (keyText.equals("1")) {
                        isShowingCommands = false;
                        // Evaluates coins and triggers the dynamic ending string sequence
                        int coins = logic.getPlayer().getCoinsThrown();
                        if (logic.getPlayer().hasItem("gas mask") && coins >= 3) {
                            try { java.nio.file.Files.write(java.nio.file.Paths.get("achievements.txt"), java.util.Arrays.asList("ENDING 5: Feline Gratitude"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch(Exception ex){}
                            enqueueScriptLines(new String[]{
                                    "STORY: You turn the gate key and shove the heavy iron bars outward.",
                                    "STORY: Behind you, a dark purple cloud begins to erupt from the cellar ventilation structures.",
                                    "STORY: You tighten the straps of the cat's gas mask over your face, breathing steadily through the filter.",
                                    "STORY:.",
                                    "==== ENDING: Feline Gratitude.  and lived to tell the tale. ===="
                            });
                        }
                        if (coins == 0) {
                            try { java.nio.file.Files.write(java.nio.file.Paths.get("achievements.txt"), java.util.Arrays.asList("ENDING 1: Hymn Of Selfishness"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch(Exception ex){}
                            enqueueScriptLines(new String[]{"==== ENDING: You get what you give. Sometimes it's about more than winning. ===="});
                        } else if (coins == 1) {
                            try { java.nio.file.Files.write(java.nio.file.Paths.get("achievements.txt"), java.util.Arrays.asList("ENDING 2: Echoes Of Wisdom"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch(Exception ex){}
                            enqueueScriptLines(new String[]{"==== ENDING: The path of selfishness is often the least meaningful. ===="});
                        } else {
                            try { java.nio.file.Files.write(java.nio.file.Paths.get("achievements.txt"), java.util.Arrays.asList("ENDING 3: What Could Have Been"), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND); } catch(Exception ex){}
                            enqueueScriptLines(new String[]{"==== ENDING: You could have done so much more. ===="});
                        }
                    }
                    else if (keyText.equals("2")) {

                        renderCompactCommands();
                    }
                    return;
                }

                try {
                    int selectedIdx = Integer.parseInt(keyText) - 1;
                    String[] currentOptions = logic.getLookChoices();

                    if (selectedIdx < 0 || selectedIdx >= currentOptions.length) {
                        throw new IllegalArgumentException("Incorrect option! There's no such choice in the menu: " + keyText);
                    }

                    isShowingCommands = false;
                    enqueueScriptLines(logic.executeMenuAction(selectedIdx));
                    return;

                } catch (IllegalArgumentException ex) {
                    errorLabel.setText("[ERROR] " + ex.getMessage());
                    return;
                }
            }

            if (!isShowingCommands) {
                if (e.getCode() == KeyCode.ENTER) {
                    if (textAnimationRunning) {
                        if (typewriterTimeline != null) typewriterTimeline.stop();
                        textAnimationRunning = false;
                        String cutOffPrintText = cleanActiveLineText.replace("STORY: ", "").replace("CAT: ", "").replace("ENDING: ", "");
                        dialogueLabel.setText(cutOffPrintText);
                        promptLabel.setText("[Press ENTER to proceed...]");
                    } else {
                        advanceScriptQueue();
                    }
                }
                if (e.getCode() != KeyCode.ENTER && e.getCode().isDigitKey()) return;
            }

            String direction = "";
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) direction = "north";
            else if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) direction = "south";
            else if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) direction = "west";
            else if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) direction = "east";

            if (!direction.isEmpty()) {
                isShowingCommands = false;
                String moveResponse = logic.move(direction);

                updateVisualCoordinates();
                checkAndSwapMapAssets();

                if (moveResponse.equals("GATE_LOCKED")) {
                    enqueueScriptLines(new String[]{
                            "STORY: You stand before the great iron gate.",
                            "STORY: The gate is locked. You need a key to open it."
                    });
                }
                else if (moveResponse.equals("CELLAR_UNLOCKED")) {
                    enqueueScriptLines(new String[]{
                            "STORY: You have a rusty key. You open the cellar door.",
                            "STORY: The hinges creak loudly in the silence."
                    });
                }
                else if (moveResponse.equals("GATE_CHOICE")) {
                    isShowingCommands = true;
                    dialogueLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
                    dialogueLabel.setText("== ESCAPE THE GARDEN ==\nYou have the key to the gate. Do you want to open it?\n\n [1] Yes, open the gate and escape.\n [2] No, stay in the garden.");
                    promptLabel.setText("[Press 1 or 2 to choose...]");
                }
                else if (moveResponse.equals("HEDGE_CUT_SUCCESS")) {
                    enqueueScriptLines(new String[]{
                            "STORY: You cut a clean opening through the tangled hedge!",
                            "STORY: The path is now open.",
                    });
                }
                else if (moveResponse.equals("ROOM_CHANGED")) {
                    Room currentRoomObj = logic.getCurrentRoom();

                    java.util.Random rand = new java.util.Random();
                    String singleLine = currentRoomObj.getRandomText(rand);

                    enqueueScriptLines(new String[]{ singleLine });
                }
            }
            else if (e.getCode() == KeyCode.I) {
                isShowingCommands = true;
                List<Item> currentItems = logic.getPlayer().getInventory();
                dialogueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
                promptLabel.setText("");

                if (currentItems.isEmpty()) {
                    dialogueLabel.setText("== INVENTORY ==\nYour inventory is empty.");
                } else {
                    StringBuilder sb = new StringBuilder("== INVENTORY ==\n");
                    for (Item item : currentItems) {
                        sb.append("- ").append(item.getName().toUpperCase()).append("\n");
                    }
                    dialogueLabel.setText(sb.toString());
                }
                promptLabel.setText("[Press ENTER to exit inventory...]");
            }
        });

        stage.setScene(gameScene);
    }

    private void enqueueScriptLines(String[] lines) {
        if (lines == null || lines.length == 0) return;
        storyScriptQueue.clear();
        Collections.addAll(storyScriptQueue, lines);
        currentQueueIndex = 0;
        isShowingCommands = false;
        renderActiveQueueLine();
    }

    private void advanceScriptQueue() {
        if (cleanActiveLineText.contains("====")) {
            if (videoPlayer != null) videoPlayer.play();
            if (audioPlayer != null) audioPlayer.play();

            logic = new GameLogic();
            Stage currentStage = (Stage) dialogueLabel.getScene().getWindow();

            currentStage.setScene(menuScene);
            return;
        }

        currentQueueIndex++;
        if (currentQueueIndex < storyScriptQueue.size()) {
            renderActiveQueueLine();
        } else {
            renderCompactCommands();
        }
    }

    private void renderActiveQueueLine() {
        String fullRawLine = storyScriptQueue.get(currentQueueIndex);
        cleanActiveLineText = fullRawLine;
        promptLabel.setText("");

        if (fullRawLine.contains("====")) {
            dialogueLabel.setStyle("-fx-text-fill: #9d4edd; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
        else if (fullRawLine.startsWith("\"") || fullRawLine.startsWith("CAT:") || (logic.getCurrentRoom().getName().equals("INSIDE SHED") && !fullRawLine.startsWith("You"))) {
            dialogueLabel.setStyle("-fx-text-fill: #ffdf00; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
        else {
            dialogueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        }

        String printableLineText = fullRawLine.replace("STORY: ", "").replace("CAT: ", "").replace("ENDING: ", "");
        runTypewriterAnimation(printableLineText);
    }

    private void runTypewriterAnimation(String targetText) {
        textAnimationRunning = true;
        dialogueLabel.setText("");
        promptLabel.setText("");

        typewriterTimeline = new Timeline();
        for (int i = 0; i < targetText.length(); i++) {
            final int charIdx = i;
            KeyFrame frame = new KeyFrame(Duration.millis(15 * i), event -> {
                dialogueLabel.setText(dialogueLabel.getText() + targetText.charAt(charIdx));
            });
            typewriterTimeline.getKeyFrames().add(frame);
        }

        typewriterTimeline.setOnFinished(event -> {
            textAnimationRunning = false;
            promptLabel.setText("[Press ENTER to proceed...]");

            if (cleanActiveLineText.contains("====") && currentQueueIndex == storyScriptQueue.size() - 1) {
                promptLabel.setText("[GAME OVER - Press ENTER to return to title screen]");
            }
        });
        typewriterTimeline.play();
    }

    private void renderCompactCommands() {
        isShowingCommands = true;
        dialogueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        promptLabel.setText("");

        String[] subchoices = logic.getLookChoices();
        if (subchoices.length == 0) {
            dialogueLabel.setText("== COMMAND INTERFACE ==\n [W,A,S,D / Arrows] Move Character Dot\n [I] View Inventory Items");
            promptLabel.setText("[Press ENTER to see commands...]");
        } else {
            StringBuilder sb = new StringBuilder("== AREA ACTIONS ==\n");
            for (int i = 0; i < subchoices.length; i++) {
                sb.append(" [").append(i + 1).append("] ").append(subchoices[i]).append("\n");
            }
            dialogueLabel.setText(sb.toString());
            promptLabel.setText("[Press ENTER to see commands...]");
        }
    }

    private void updateVisualCoordinates() {
        Room current = logic.getCurrentRoom();
        locationLabel.setText("LOCATION: " + current.getName());
        playerDot.setTranslateX(current.getX());
        playerDot.setTranslateY(current.getY());

        String colorTheme = current.getColorTheme();
        String roomName = current.getName();

        if (roomName.equals("CELLAR DOORS")) {
            locationLabel.setStyle("-fx-text-fill: #555555; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (roomName.equals("GATE")) {
            locationLabel.setStyle("-fx-text-fill: #8b5a2b; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (colorTheme.equals("fountainBlue")) {
            locationLabel.setStyle("-fx-text-fill: #a0c4ff; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (colorTheme.equals("cellarRock")) {
            locationLabel.setStyle("-fx-text-fill: #b0b3b8; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (colorTheme.equals("graveyardGrey")) {
            locationLabel.setStyle("-fx-text-fill: #70e000; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (colorTheme.equals("shedBrown")) {
            locationLabel.setStyle("-fx-text-fill: #d4a373; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            locationLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    private void checkAndSwapMapAssets() {
        try {
            if (logic.isHedgeCut()) {
                mapImageView.setImage(new Image(getClass().getResourceAsStream("/map_no_hedge.PNG")));
            } else {
                mapImageView.setImage(new Image(getClass().getResourceAsStream("/map_hedge.PNG")));
            }
        } catch (Exception e) {
            System.err.println("Map swap resource error: " + e.getMessage());
        }
    }

    private void updateMenuStyles() {
        String normal = "-fx-font-size: 24px; -fx-text-fill: #b599c2; -fx-font-family: 'Courier New'; -fx-padding: 10;";
        String active = "-fx-font-size: 24px; -fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-background-color: rgba(74, 30, 93, 0.8); -fx-background-radius: 5; -fx-padding: 10;";

        playOption.setStyle(selectionIndex == 0 ? active : normal);
        collectedOption.setStyle(selectionIndex == 1 ? active : normal);
        quitOption.setStyle(selectionIndex == 2 ? active : normal);
    }

    private void showEndingsPopup(Stage ownerStage) {
        Stage popup = new Stage();
        popup.initOwner(ownerStage);
        popup.setTitle("Unlocked Endings");

        VBox layout = new VBox(15);
        layout.setStyle("-fx-background-color: #12031a; -fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("=== UNLOCKED ENDINGS ===");
        header.setStyle("-fx-text-fill: #e0b0ff; -fx-font-family: 'Courier New'; -fx-font-size: 18px; -fx-font-weight: bold;");
        layout.getChildren().add(header);

        // Read achieved files safely from the project directory
        Set<String> unlocked = new HashSet<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("achievements.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                unlocked.add(line.trim());
            }
        } catch (Exception e) {
            // No endings saved to the text file yet
        }

        // Perfect 1-to-5 sequential listing matching your newly customized file string writes
        String[] allEndings = {
                "ENDING 1: Hymn Of Selfishness",
                "ENDING 2: Echoes Of Wisdom",
                "ENDING 3: What Could Have Been",
                "ENDING 4: Tyranny Overthrown",
                "ENDING 5: Feline Gratitude"
        };

        for (String ending : allEndings) {
            Label row = new Label();
            if (unlocked.contains(ending)) {
                row.setText("[X] " + ending);
                row.setStyle("-fx-text-fill: #70e000; -fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-font-weight: bold;");
            } else {
                row.setText("[ ] Locked Ending");
                row.setStyle("-fx-text-fill: #555555; -fx-font-family: 'Courier New'; -fx-font-size: 14px;");
            }
            layout.getChildren().add(row);
        }

        Label closePrompt = new Label("[Press ESC to go back]");
        closePrompt.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        VBox.setMargin(closePrompt, new Insets(10, 0, 0, 0));
        layout.getChildren().add(closePrompt);

        Scene popupScene = new Scene(layout, 400, 280);
        popupScene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) popup.close();
        });

        popup.setScene(popupScene);
        popup.show();
    }

    public static void main(String[] args) { launch(args); }
}
