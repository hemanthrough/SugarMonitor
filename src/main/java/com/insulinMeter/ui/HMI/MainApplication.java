package com.insulinMeter.ui.HMI;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

import javax.swing.Timer;

import com.insulinMeter.Patient.Patient;
import com.insulinMeter.Pump.ClosedInsulinPump;
import com.insulinMeter.Pump.FullyManualPump;
import com.insulinMeter.Pump.InsulinPump;
import com.insulinMeter.Utils.Blink;
import com.insulinMeter.Utils.PasswordDialog;
import com.insulinMeter.Utils.PdfCreator;
import com.insulinMeter.Utils.RandomSugarLevelGenerator;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainApplication extends Application {
	LineChart<Long, Double> li;
	private InsulinPump pump = InsulinPump.getInstance();
	Stage primaryStage;
	private Scene myScene;
	private Pane myPane;

	public static void main(String[] args) {
		InsulinPump.getInstance().setMonioringMode(ClosedInsulinPump.getInstance());
		Timer randomTimer = new javax.swing.Timer(500, new RandomSugarLevelGenerator());
		// randomTimer.start();
		launch(args);

		// new Timer
	}

	public Pane getMyPane() {
		return myPane;
	}

	public void setMyPane(Pane myPane) {
		this.myPane = myPane;
	}

	public Scene getMyScene() {
		return myScene;
	}

	public void setMyScene(Scene myScene) {
		this.myScene = myScene;
	}

	EventHandler<ActionEvent> mainScreenButtonHandler = new EventHandler<ActionEvent>() {

		@Override
		public void handle(ActionEvent event) {
			Button source = (Button) event.getSource();
			if (source != null) {
				// System.out.println(source.getId());
				switch (source.getId()) {
				case "patientNavigation":
					try {
						
						myPane = (Pane) FXMLLoader.load(getClass().getResource("PatientScreen.fxml"));
						myScene = new Scene(myPane);
						primaryStage.setScene(myScene);
						primaryStage.show();
						setBindingPatientView(myScene);
						TextField date = (TextField) myScene.lookup("#Timer");
						date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());
						pump.startMonitoring();

					} catch (IOException e) {
						e.printStackTrace();
					}

					break;
				case "DoctorView":
					try {
						Button souce = (Button) event.getSource();
						//System.out.println(souce.getId());
						myPane = (Pane) FXMLLoader.load(getClass().getResource("first.fxml"));
						myScene = new Scene(myPane);
						primaryStage.setScene(myScene);
						myPane.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
						primaryStage.show();
						setBindings(myScene);
						TextField date = (TextField) myScene.lookup("#Timer");
						date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());
						pump.startMonitoring();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case "BackToScreen":
					try {
						myPane = (Pane) FXMLLoader.load(getClass().getResource("LandingScreen.fxml"));
						myScene = new Scene(myPane);
						primaryStage.setScene(myScene);
						// myPane.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
						primaryStage.show();
						Button patient = (Button) myScene.lookup("#patientNavigation");
						Button doctor = (Button) myScene.lookup("#DoctorView");
						Button battery = (Button) myScene.lookup("#batteryRefil");
						battery.setOnAction(mainScreenButtonHandler);

						patient.setOnAction(mainScreenButtonHandler);

						doctor.setOnAction(mainScreenButtonHandler);
						TextField date = (TextField) myScene.lookup("#Timer");
						date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());
						ProgressBar batteryPercentageBar = (ProgressBar) myScene.lookup("#progressBattery");
						ProgressIndicator batteryPercentageDisplay = (ProgressIndicator) myScene
								.lookup("#piBatteryLevel");

						batteryPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

							@Override
							public void changed(ObservableValue<? extends Number> observable, Number oldValue,
									Number newValue) {
								double progress = newValue == null ? 0 : newValue.doubleValue();
								if (progress < 0.2) {
									batteryPercentageBar.setStyle("-fx-accent:red;");
								} else if (progress < 0.4) {
									batteryPercentageBar.setStyle("-fx-accent:orange;");

								} else if (progress < 0.6) {
									batteryPercentageBar.setStyle("-fx-accent:yellow;");
								} else {
									batteryPercentageBar.setStyle("-fx-accent:green;");
								}
							}
						});
						batteryPercentageBar.progressProperty()
								.bindBidirectional(InsulinPump.getInstance().getBatteryCapacityProperty());

						batteryPercentageDisplay.progressProperty()
								.bindBidirectional(pump.getBatteryCapacityProperty());

					} catch (Exception ec) {
						ec.printStackTrace();
					}
					break;
				case "GoToSettingScreen":
					try {

						// creating the new screen for the setting screen
						Pane settingPane = (Pane) FXMLLoader.load(getClass().getResource("SettingScreen.fxml"));
						Scene settingScene = new Scene(settingPane);
						Stage secondryStage = new Stage();
						secondryStage.setScene(settingScene);
						// myPane.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
						secondryStage.show();
						setBindingSettingScreen(settingScene);

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "ExportPatientHistory":
					DirectoryChooser chooser = new DirectoryChooser();
					chooser.setTitle("JavaFX Projects");
					File selectedDirectory = chooser.showDialog(primaryStage);
					if (selectedDirectory == null) {
						return;
					}
					LocalDate userGivenDate =null;
					try{
						userGivenDate = ((DatePicker) getMyScene().lookup("#DateChoice")).getValue();
					}
					catch(NullPointerException e){
						e.printStackTrace(); 
					}
					String whereClause = null;
					if (userGivenDate != null) {
						whereClause = "where SYSDATE > ";
						whereClause
								.concat(((Long) Timestamp.valueOf(userGivenDate.atStartOfDay()).getTime()).toString());
					}
					new PdfCreator().generatePdfReport(selectedDirectory.toString(), whereClause);
					break;
				case "injectInsulin":
					Double toBeInjectedValue = 0.;
					try {
						toBeInjectedValue = Double
								.parseDouble(((TextField) getMyScene().lookup("#insulinInjectedDisplay")).getText());
					} catch (Exception e) {

					}
					pump.injectInsulin(toBeInjectedValue);
					break;
				case "injectGlucogon":
					toBeInjectedValue = 0.;
					try {
						toBeInjectedValue = Double
								.parseDouble(((TextField) getMyScene().lookup("#glucagonInjectedDisplay")).getText());
					} catch (Exception e) {

					}
					pump.injectGlucogon(toBeInjectedValue);
					break;
				case "insulinAdd":
					InsulinPump.getInstance().setInsulinRemaining(1000.);
					break;
				case "glucagonAdd":
					InsulinPump.getInstance().setGlucogonRemaining(1000.);
					break;
				case "batteryRefil":
					InsulinPump.getInstance().setBatteryLevel(100.);
					break;
				}
			}
		}
	};

	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
		/// code for new window of setting screen
		try {
			// code for landing screen
			myPane = (Pane) FXMLLoader.load(getClass().getResource("LandingScreen.fxml"));
			myScene = new Scene(myPane);
			setMyScene(myScene);
			primaryStage.setScene(myScene);
			// myPane.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
			primaryStage.show();
			// setBindingSettingScreen(myScene);

			Button patient = (Button) myScene.lookup("#patientNavigation");
			Button doctor = (Button) myScene.lookup("#DoctorView");
			Button setting = (Button) myScene.lookup("#GoToSettingScreen");
			Button battery = (Button) myScene.lookup("#batteryRefil");
			patient.setOnAction(mainScreenButtonHandler);

			doctor.setOnAction(mainScreenButtonHandler);

			setting.setOnAction(mainScreenButtonHandler);
			battery.setOnAction(mainScreenButtonHandler);
			TextField date = (TextField) myScene.lookup("#Timer");
			date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());

			ProgressBar batteryPercentageBar = (ProgressBar) myScene.lookup("#progressBattery");
			ProgressIndicator batteryPercentageDisplay = (ProgressIndicator) myScene.lookup("#piBatteryLevel");

			batteryPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					double progress = newValue == null ? 0 : newValue.doubleValue();
					if (progress < 0.2) {
						batteryPercentageBar.setStyle("-fx-accent:red;");
					} else if (progress < 0.4) {
						batteryPercentageBar.setStyle("-fx-accent:orange;");

					} else if (progress < 0.6) {
						batteryPercentageBar.setStyle("-fx-accent:yellow;");
					} else {
						batteryPercentageBar.setStyle("-fx-accent:green;");
					}
				}
			});
			batteryPercentageBar.progressProperty()
					.bindBidirectional(InsulinPump.getInstance().getBatteryCapacityProperty());

			batteryPercentageDisplay.progressProperty().bindBidirectional(pump.getBatteryCapacityProperty());

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	void setBindingSettingScreen(Scene currentView) {
		((TextField) currentView.lookup("#PatientName")).setText(Patient.getPatient().getName());
		((TextField) currentView.lookup("#PatientAddress")).setText("adam opel str");
		;
		((TextField) currentView.lookup("#PatientEmail")).setText(Patient.getPatient().getEmail());
		((TextField)currentView.lookup("#PatientWeight")).setText(Patient.getPatient().getWeight().toString());
		((TextField) currentView.lookup("#PatientICR")).setText("12");
		;
		((TextField) currentView.lookup("#PatientISF")).setText(Patient.getPatient().getISF().toString());
		((TextField) currentView.lookup("#PatientGSF"))
				.setText(Patient.getPatient().getGlucogonSensitivity().toString());
		((TextField) currentView.lookup("#PatientTargetBSL"))
				.setText(Patient.getPatient().getTargetGlucose().toString());
		((TextField) currentView.lookup("#BatteryLevel"))
				.setText(InsulinPump.getInstance().getBatteryLevel().toString());

		Button back = (Button) currentView.lookup("#closeSetting");
		back.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Stage settingStage = (Stage) currentView.getWindow();
				settingStage.close();
			}
		});

		Button exportToPdf = (Button) currentView.lookup("#ExportPatientHistory");
		exportToPdf.setOnAction(mainScreenButtonHandler);

		Button simulate = (Button) currentView.lookup("#SimulatetheDetails");
		simulate.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String name = ((TextField) currentView.lookup("#PatientName")).getText();
				String address = ((TextField) currentView.lookup("#PatientAddress")).getText();
				String email = ((TextField) currentView.lookup("#PatientEmail")).getText();
				Double weight = 70.;
				try {
					weight = Double.parseDouble(((TextField) currentView.lookup("#PatientWeight")).getText());
				} catch (Exception e) {
					//System.out.println("exception in parsing setting deafulf value");
				}
				String icr = ((TextField) currentView.lookup("#PatientICR")).getText();
				Integer isf = Integer.parseInt(((TextField) currentView.lookup("#PatientISF")).getText());
				Double gsf = Double.parseDouble(((TextField) currentView.lookup("#PatientGSF")).getText());
				Double targetSugarLevel = Double
						.parseDouble(((TextField) currentView.lookup("#PatientTargetBSL")).getText());
				Double battery = Double.parseDouble(((TextField) currentView.lookup("#BatteryLevel")).getText());
				Patient.getPatient().setName(name);
				Patient.getPatient().setEmail(email);
				Patient.getPatient().setWeight(weight);
				Patient.getPatient().setISF(isf);
				Patient.getPatient().setGlucogonSensitivity(gsf);
				Patient.getPatient().g0 = targetSugarLevel;
				InsulinPump.getInstance().setBatteryLevel(battery);
			}
		});

		/// option for setting acess mode
		ChoiceBox<String> modeSelectBox = (ChoiceBox<String>) currentView.lookup("#modeProvision");
		modeSelectBox.getItems().addAll("Automatic Mode", "Manual Mode");
		modeSelectBox.setValue("Automatic Mode");
		modeSelectBox.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				//System.out.println(newValue);
				switch (newValue) {
				case "Automatic Mode":
					InsulinPump.getInstance().setManualModeEnabled(false);

					break;
				case "Manual Mode":
					InsulinPump.getInstance().setManualModeEnabled(true);
					break;
				default:
					break;
				}
			}
		});

		// password Screen
		PasswordDialog pd = new PasswordDialog();
		Optional<String> passwordPhrase = pd.showAndWait();
		// closes if there is no input
		if (passwordPhrase.isPresent()) {
			if (!passwordPhrase.get().equals("1234")) {
				Stage settingStage = (Stage) currentView.getWindow();
				settingStage.close();
			}
		} else {
			Stage settingStage = (Stage) currentView.getWindow();
			settingStage.close();
		}
	}

	void setBindings(Scene currentView) {
		li = (LineChart) currentView.lookup("#linePlotBSL");
		// li.getData().clear();
		//System.out.println(li.getData().size());
		TextField insulinDisplay = (TextField) currentView.lookup("#insulinInjectedDisplay");

		TextField glucogonDisplay = (TextField) currentView.lookup("#glucagonInjectedDisplay");

		ProgressBar batteryPercentageBar = (ProgressBar) currentView.lookup("#progressBattery");
		ProgressIndicator batteryPercentageDisplay = (ProgressIndicator) currentView.lookup("#piBatteryLevel");

		batteryPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				double progress = newValue == null ? 0 : newValue.doubleValue();
				if (progress < 0.2) {
					batteryPercentageBar.setStyle("-fx-accent:red;");
				} else if (progress < 0.4) {
					batteryPercentageBar.setStyle("-fx-accent:orange;");

				} else if (progress < 0.6) {
					batteryPercentageBar.setStyle("-fx-accent:yellow;");
				} else {
					batteryPercentageBar.setStyle("-fx-accent:green;");
				}
			}
		});
		if (li.getData()!=null) {
			//li.getData().clear();
			li.requestLayout();
			
		}
		//Patient.getPatient().getGlucoseLevel().so
		XYChart.Series series1 = new XYChart.Series();
		series1.setName("Suger level in body");
		series1.setData(Patient.getPatient().getGlucoseLevel());

		XYChart.Series series2 = new XYChart.Series();
		series2.setName("predcited Gmax");
		series2.setData(pump.getPredictedGmax().sorted());

		XYChart.Series series3 = new XYChart.Series();
		series3.setName("insulin in mL");
		series3.setData(pump.getInsulinInjectedCollection());

		XYChart.Series upperLimit = new XYChart.Series();
		upperLimit.setName("upper Threshold");
		upperLimit.setData(pump.getUpperLimit());

		XYChart.Series lowerLimit = new XYChart.Series();
		lowerLimit.setName("lower Threshold");
		lowerLimit.setData(pump.getLowerLimit());
		XYChart.Series glucogon = new XYChart.Series();
		
		glucogon.setName("Glucogon");
		glucogon.setData(pump.getGluccogonInjected());

		// int i = 0;
		
		li.getData().add(series1);
		li.getData().add(series2);
		li.getData().add(series3);
		li.getData().add(upperLimit);
		li.getData().add(lowerLimit);
		li.getData().add(glucogon);
		
		

		insulinDisplay.textProperty().bindBidirectional(pump.getCalculatedINsulinProperty());
		insulinDisplay.setDisable(true);

		glucogonDisplay.textProperty().bindBidirectional(InsulinPump.getInstance().getGlucogonInjectedProperty());
		glucogonDisplay.setDisable(true);
		batteryPercentageBar.progressProperty()
				.bindBidirectional(InsulinPump.getInstance().getBatteryCapacityProperty());

		batteryPercentageDisplay.progressProperty().bindBidirectional(pump.getBatteryCapacityProperty());

		pump.startMonitoring();

		Button batteryRefil = (Button) currentView.lookup("#batteryRefil");
		batteryRefil.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				InsulinPump.getInstance().setBatteryLevel(100.);
			}
		});
		Button setting = (Button) currentView.lookup("#GoToSettingScreen");
		setting.setOnAction(mainScreenButtonHandler);

		TextField currentBsdMesseage = (TextField) currentView.lookup("#currentBSLDisplay");
		currentBsdMesseage.textProperty().bindBidirectional(Patient.getPatient().getBslProperty());

		TextField previousBsdMesseage = (TextField) currentView.lookup("#previousBSLDisplay");
		previousBsdMesseage.textProperty().bindBidirectional(Patient.getPatient().getPreviousBslProperty());

		TextField date = (TextField) currentView.lookup("#Timer");
		date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());

		Button back = (Button) currentView.lookup("#BackToScreen");
		back.setOnAction(mainScreenButtonHandler);
	}

	void setBindingPatientView(Scene currentView) {
		// adding a ui indicaton of emergency
		AnchorPane alertPlaceHolder = (AnchorPane) currentView.lookup("#alertPlaceHolder");
		Blink control = new Blink();
		
		control.setPrefWidth(80); control.setPrefHeight(80);
		

		control.setBlinking(true);

		alertPlaceHolder.getChildren().add(control);

		alertPlaceHolder.visibleProperty().bind(InsulinPump.getInstance().getAlertBoxProperty());
		//alertPlaceHolder.setVisible(true);
		TextField insulinDisplay = (TextField) currentView.lookup("#insulinInjectedDisplay");
		ProgressBar insulinPercentageBar = (ProgressBar) currentView.lookup("#insulinBankBar");
		ProgressIndicator insulinPercentageDisplay = (ProgressIndicator) currentView.lookup("#insulinIndicate");
		insulinPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				double progress = newValue == null ? 0 : newValue.doubleValue();
				if (progress < 0.2) {
					insulinPercentageBar.setStyle("-fx-accent:red;");
				} else if (progress < 0.4) {
					insulinPercentageBar.setStyle("-fx-accent:orange;");

				} else if (progress < 0.6) {
					insulinPercentageBar.setStyle("-fx-accent:yellow;");
				} else {
					insulinPercentageBar.setStyle("-fx-accent:green;");
				}
			}
		});

		TextField glucogonDisplay = (TextField) currentView.lookup("#glucagonInjectedDisplay");
		ProgressBar glucogonPercentageBar = (ProgressBar) currentView.lookup("#glucagonBankBar");
		ProgressIndicator glucogonPercentageDisplay = (ProgressIndicator) currentView.lookup("#glucagonIndicate");
		glucogonDisplay.setDisable(true);
		glucogonPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				double progress = newValue == null ? 0 : newValue.doubleValue();
				if (progress < 0.2) {
					glucogonPercentageBar.setStyle("-fx-accent:red;");
				} else if (progress < 0.4) {
					glucogonPercentageBar.setStyle("-fx-accent:orange;");

				} else if (progress < 0.6) {
					glucogonPercentageBar.setStyle("-fx-accent:yellow;");
				} else {
					glucogonPercentageBar.setStyle("-fx-accent:green;");
				}
			}
		});

		ProgressBar batteryPercentageBar = (ProgressBar) currentView.lookup("#progressBattery");
		ProgressIndicator batteryPercentageDisplay = (ProgressIndicator) currentView.lookup("#piBatteryLevel");

		batteryPercentageBar.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				double progress = newValue == null ? 0 : newValue.doubleValue();
				if (progress < 0.2) {
					batteryPercentageBar.setStyle("-fx-accent:red;");
				} else if (progress < 0.4) {
					batteryPercentageBar.setStyle("-fx-accent:orange;");

				} else if (progress < 0.6) {
					batteryPercentageBar.setStyle("-fx-accent:yellow;");
				} else {
					batteryPercentageBar.setStyle("-fx-accent:green;");
				}
			}
		});

		insulinDisplay.textProperty().bindBidirectional(pump.getCalculatedINsulinProperty());
		insulinDisplay.setDisable(true);
		insulinPercentageDisplay.progressProperty().bindBidirectional(pump.getInsulinTankCapacityProperty());
		insulinPercentageBar.progressProperty().bindBidirectional(pump.getInsulinTankCapacityProperty());
		// insulinPercentageDisplay.setDisable(true);

		glucogonDisplay.textProperty().bindBidirectional(InsulinPump.getInstance().getGlucogonInjectedProperty());
		glucogonPercentageBar.progressProperty()
				.bindBidirectional(InsulinPump.getInstance().getGlucogonTankCapacityProperty());
		glucogonPercentageDisplay.progressProperty()
				.bindBidirectional(InsulinPump.getInstance().getGlucogonTankCapacityProperty());

		batteryPercentageBar.progressProperty()
				.bindBidirectional(InsulinPump.getInstance().getBatteryCapacityProperty());
		batteryPercentageDisplay.progressProperty().bindBidirectional(pump.getBatteryCapacityProperty());

		pump.startMonitoring();

		// setting the refilling option for the glucose and insulin
		Button insulinRefil = (Button) currentView.lookup("#insulinAdd");
		insulinRefil.setOnAction(mainScreenButtonHandler);
		
		Button glucogonRefil = (Button) currentView.lookup("#glucagonAdd");
		glucogonRefil.setOnAction(mainScreenButtonHandler);

		Button batteryRefil = (Button) currentView.lookup("#batteryRefil");
		batteryRefil.setOnAction(mainScreenButtonHandler);

		TextArea errMesseage = (TextArea) currentView.lookup("#errorDisplay");
		errMesseage.textProperty().bindBidirectional(InsulinPump.getInstance().getErrorMessageProperty());

		TextField currentBsdMesseage = (TextField) currentView.lookup("#currentBSLDisplay");
		currentBsdMesseage.textProperty().bindBidirectional(Patient.getPatient().getBslProperty());

		TextField previousBsdMesseage = (TextField) currentView.lookup("#previousBSLDisplay");
		previousBsdMesseage.textProperty().bindBidirectional(Patient.getPatient().getPreviousBslProperty());

		// settig the property for text field
		TextField date = (TextField) currentView.lookup("#Timer");
		date.textProperty().bindBidirectional(InsulinPump.getInstance().getSystemDateProperty());

		Button back = (Button) currentView.lookup("#BackToScreen");
		back.setOnAction(mainScreenButtonHandler);

		// code for manual mode buttons
		Button insulinInjectButton = (Button) currentView.lookup("#injectInsulin");
		Button glucogonInjectButton = (Button) currentView.lookup("#injectGlucogon");
		// as initially it will be in auto mode
		insulinInjectButton.setVisible(false);
		glucogonInjectButton.setVisible(false);

		ChoiceBox<String> modeSelectBox = (ChoiceBox) currentView.lookup("#modeSelectionBox");
		// null check and check for if the manual mode is enabled
		if (insulinInjectButton != null && glucogonInjectButton != null
				&& InsulinPump.getInstance().isManualModeEnabled()) {
			insulinInjectButton.setOnAction(mainScreenButtonHandler);
			glucogonInjectButton.setOnAction(mainScreenButtonHandler);
			modeSelectBox.getItems().addAll("Automatic Mode", "Manual Mode");
			modeSelectBox.setValue("Automatic Mode");
			modeSelectBox.valueProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					//System.out.println(newValue);
					switch (newValue) {
					case "Automatic Mode":
						insulinDisplay.setDisable(true);
						glucogonDisplay.setDisable(true);
						glucogonDisplay.textProperty()
								.bindBidirectional(InsulinPump.getInstance().getGlucogonInjectedProperty());
						insulinDisplay.textProperty().bindBidirectional(pump.getCalculatedINsulinProperty());
						// kill or clear any resources
						pump.stopMonitoring();
						pump.setMonioringMode(ClosedInsulinPump.getInstance());
						pump.startMonitoring();
						insulinInjectButton.setVisible(false);
						glucogonInjectButton.setVisible(false);

						break;
					case "Manual Mode":

						insulinDisplay.setDisable(false);
						insulinDisplay.textProperty().unbindBidirectional(pump.getCalculatedINsulinProperty());
						glucogonDisplay.textProperty()
								.unbindBidirectional(InsulinPump.getInstance().getGlucogonInjectedProperty());
						glucogonDisplay.setDisable(false);
						// kill or clear any resources
						pump.stopMonitoring();
						pump.setMonioringMode(FullyManualPump.getInstance());
						pump.startMonitoring();
						insulinInjectButton.setVisible(true);
						glucogonInjectButton.setVisible(true);
						break;
					default:
						break;
					}
				}
			});
		} else {
			if (modeSelectBox != null) {
				modeSelectBox.setVisible(false);
			}
		}

	}

}
