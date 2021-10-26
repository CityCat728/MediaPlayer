import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;


public class Player extends Application
{
	//The default setting of file open
    private File mediaFile=new File("video.mp4");
    private Media media = new Media(mediaFile.toURI().toString());
    private MediaPlayer player=new MediaPlayer(media);   
    private MediaView viewPoint;
    //Navbar like,offer to user that take action in open file or exit
    private MenuBar menuBar; 
    private Menu fileMenu;
    private MenuItem openFile,exit;
    //Optimize the volume,if the default volume is not suitable
    private Slider volumeSlider;
    private Text volume;  
    private Text currentTime,slash,fullTime;
    //Two button,one combine the play and pause function,another is to reset the video
    private Button playPauseBtn;
    private Button resetBtn;
    //Build up a slide bar like object,offering to user
    private ProgressBar progressBar;
    private BorderPane mainView; 
    private BorderPane bottomView;   
    //Layout of some objects
    private HBox playPause; 
    private HBox mediaPanel,volumeAndSlider;  
    private HBox progressPanel;   
    private VBox bottomPanel;  


  //Transform the length of time in type from double to String
    public String transform(double secondDouble)
    {
        int hours=(int)secondDouble/3600;
        int minutes=(int)(secondDouble/60-hours*60);
        int seconds=(int)(secondDouble-hours*3600-minutes*60);
        
        String hourStr,minuteStr,secondStr;
        
        if(hours<10)
            hourStr="0"+hours;
        else
            hourStr=""+hours;
        
        
        if(minutes<10)
            minuteStr="0"+minutes;
        else
            minuteStr=""+minutes;
        
        
        if(seconds<10)
        	secondStr="0"+seconds;
        else
            secondStr=""+seconds;
               

        return hourStr+":"+minuteStr+":"+secondStr;
    }

    
    
    
    @Override
    public void start(Stage primaryStage)
    {
    	//Set Menu
    	
    	menuBar=new MenuBar();
        
        fileMenu=new Menu("Options");
      
        openFile=new MenuItem("Open File");
        FileChooser fileChooser=new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP4 Video", "*.mp4"),
                new FileChooser.ExtensionFilter("MP3 Music", "*.mp3"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        openFile.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(new Stage());
            //Whenever the user have choose a file that belongs to mp3,mp4
            if (file != null) {
            	//Initialization,make sure there's no such set already exist
                player.dispose();
                media = new Media(file.toURI().toString());
       
                player = new MediaPlayer(media); 
                viewPoint.setMediaPlayer(player);
                
                //Initialize the media player
                //Whenever the media play is in the end
                player.setOnEndOfMedia(()->
                {
                    player.seek(player.getStartTime());
                    progressBar.setProgress(0);
                    currentTime.setText(transform(player.getStartTime().toSeconds()));
                    playPauseBtn.setText("Play");
                    player.pause();
                });
                //Whenever the player is open up,ready to play the video
                player.setOnReady(()->
                {
                    progressBar.setProgress(0);
                    volumeSlider.setValue(30);
                    playPauseBtn.setText("Pause");
                    player.play();
                    currentTime.setText("00:00:00");
                    fullTime.setText(transform(player.getStopTime().toSeconds()));
                });
                //Updating the current time and progress bar
                player.currentTimeProperty().addListener(ov->
                {
                    currentTime.setText(transform(player.getCurrentTime().toSeconds()));
                    progressBar.setProgress((player.getCurrentTime().toMillis()-player.getStartTime().toMillis())/player.getCycleDuration().toMillis());
                });
                
                
            }
        });

        //The option for the purpose of closing the window
        exit=new MenuItem("Exit");
        exit.setOnAction(ex->
        {
            Platform.exit();
            System.exit(0);
        });
        
        fileMenu.getItems().addAll(openFile,exit); 
        menuBar.getMenus().addAll(fileMenu);  
        
        
        
        
        //Set the media view
        mediaPanel=new HBox();
        mediaPanel.setAlignment(Pos.CENTER);
        viewPoint=new MediaView(player);
        mediaPanel.getChildren().add(viewPoint);
        mediaPanel.setStyle("-fx-background-color: black"); 
        //Determine whether the left mouse button click the window
        viewPoint.setOnMouseClicked(ex->  
        {
        	if(ex.getButton()==MouseButton.PRIMARY)
        	{	
        		//Also,the text in the button will change whenever the user click the button play/pause
        		//The playing status take the corresponding action as well
        		if (playPauseBtn.getText().equals("Play")) 
            	{
                    playPauseBtn.setText("Pause");
                    player.play();
                } 
        		
        		else 
            	{
                    playPauseBtn.setText("Play");
                    player.pause();
                }
        	}
        	
        });
        
        
        
        
        
        //Set Progress bar
        progressPanel=new HBox(5);
        progressPanel.setPadding(new Insets(5,5,5,3));

        //Display the length of time,including the video have been played and the full length
        currentTime=new Text("--:--:--");
        fullTime=new Text("--:--:--");
        slash=new Text("/");
        progressBar=new ProgressBar(0);
        progressPanel.getChildren().addAll(progressBar,currentTime,slash,fullTime);
        //User may make use of the bar at the bottom, jump to the part which user would like to watch
        progressBar.setOnMouseClicked(ev->
        {
            double x=ev.getX();
            double progressPercent=x/progressBar.getWidth();
            player.seek(player.getCycleDuration().multiply(progressPercent));
            currentTime.setText(transform(player.getCycleDuration().multiply(progressPercent).toSeconds()));
            progressBar.setProgress(progressPercent);
        });
        
        
        
        //Set the layout and operation on the button at the bottom
        playPauseBtn=new Button("Play");
        playPauseBtn.setPrefSize(75,25);
        playPauseBtn.setOnAction(event ->
        {
        	//Besides mouse action in window,the button also offer to user
            if (playPauseBtn.getText().equals("Play")) {
                playPauseBtn.setText("Pause");
                player.play();
            } else {
                playPauseBtn.setText("Play");
                player.pause();
            }

        });
        //If there is any snippet be skipped,click the button to reset the video
        resetBtn=new Button("Reset");
        resetBtn.setPrefSize(75,25);
        resetBtn.setOnAction(event -> {
            player.seek(player.getStartTime());
            progressBar.setProgress(0);
       
            playPauseBtn.setText("Play");
            player.pause();
        });
        
     
        bottomView=new BorderPane();
        //Set the layout of button group
        playPause=new HBox(15);
        playPause.setAlignment(Pos.CENTER);
        playPause.setPadding(new Insets(0,5,5,5));
        playPause.getChildren().addAll(playPauseBtn,resetBtn);  
        
        //Set Volume
        volume=new Text("Volume:");
        volumeSlider=new Slider();
        volumeSlider.setPrefWidth(50);
        volumeSlider.valueProperty().addListener(ov->
        {
            //the text display in volume
            if(volumeSlider.getValue()!=0)
            {
                volume.setText("Volume:");
            }
            else
            {
                volume.setText("Muse");
            }
            
            player.setVolume(volumeSlider.getValue()/100);
        });
       
        volume.setOnMouseClicked(ov->
        {
        	//Value of volume is 0,equals to muse
            if("Vol:".equals(volume.getText()))
            {
                volume.setText("Mute");
                volumeSlider.setValue(0);
            }
            else
            {	//Default volume is 30%,if there is no adjustment
                volume.setText("Vol:");
                volumeSlider.setValue(30);
            }
        });
        
        //Set the layout of volume bar
        volumeAndSlider=new HBox(5);
        volumeAndSlider.setPadding(new Insets(5,5,5,5));
        volumeAndSlider.setAlignment(Pos.CENTER_RIGHT);
        volumeAndSlider.getChildren().addAll(volume,volumeSlider);
        
        //Set the layout of button play/pause and volume bar
        bottomView.setCenter(playPause);
        bottomView.setRight(volumeAndSlider);
       
        bottomPanel=new VBox();
        bottomPanel.setStyle("-fx-background-color: white"); 
        bottomPanel.getChildren().addAll(progressPanel,bottomView);
        
        
        //Set the main view
        mainView=new BorderPane();
        mainView.setStyle("-fx-background-color: black");
        mainView.setTop(menuBar);
        mainView.setCenter(mediaPanel);
        mainView.setBottom(bottomPanel);

        volumeSlider.setPrefWidth(mainView.getWidth()/7);
        mainView.widthProperty().addListener(ov->
        {
            viewPoint.setFitWidth(mainView.getWidth());
            progressBar.setPrefWidth(mainView.getWidth());
            volumeSlider.setPrefWidth(mainView.getWidth()/7);
        });
        mainView.heightProperty().addListener(ov->
                viewPoint.setFitHeight(mainView.getHeight()-90));
        
        
        
        //Initialize the media player
        player.setOnEndOfMedia(()->
        {
          	player.seek(player.getStartTime());
          	progressBar.setProgress(0);
          	currentTime.setText(transform(player.getStartTime().toSeconds()));
          	playPauseBtn.setText("Play");
          	player.pause();
      	});
      //Whenever the player is open up,ready to play the video
        player.setOnReady(()->
      	{
          	progressBar.setProgress(0);
          	volumeSlider.setValue(30);
          	playPauseBtn.setText("Pause");
          	player.play();
          	currentTime.setText("00:00:00");
          	fullTime.setText(transform(player.getStopTime().toSeconds()));
      	});
        //Updating the current time and progress bar
      	player.currentTimeProperty().addListener(ov->
      	{
          currentTime.setText(transform(player.getCurrentTime().toSeconds()));
          progressBar.setProgress((player.getCurrentTime().toMillis()-player.getStartTime().toMillis())/player.getCycleDuration().toMillis());
        });
        player.play();

        //Set the window view,including size,title
        Scene scene=new Scene(mainView,800,600);
        primaryStage.setTitle("MediaPlayer");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(ex->
        {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    

    public static void main(String[] args)
    {
        launch(args);
    }

}