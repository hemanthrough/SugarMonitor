package com.insulinMeter.Pump;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.insulinMeter.Mail.MailNAlertUtils;
import com.insulinMeter.Patient.Patient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart.Data;
import javafx.util.Duration;

public class InsulinPump {

	// parameters
	private final Integer tankCapcity = 1000;

	// private final Integer

	private Double calculatedINsulin = 10.;

	private Double insulinRemaining = 1000.;

	private Double calculatedGlucogon = 0.;
	private Double glucogonRemaining = 1000.;

	private Long dateString = 0L;

	// instance variables
	private static MonitoringBehavior monioringMode;
	private static InsulinPump singletonInstance;
	private Double batteryLevel = 10.;
	private int refreshrate = 5000;
	
	//TODO set this to false after testing
	private boolean isManualModeEnabled = true;

	// property variables
	private final transient StringProperty insulinInjectedProperty = new SimpleStringProperty();
	private final transient DoubleProperty insulinTankCapacityProperty = new SimpleDoubleProperty();
	private final transient DoubleProperty batteryCapacityProperty = new SimpleDoubleProperty();
	private final transient StringProperty errorMessageProperty = new SimpleStringProperty();
	private final transient StringProperty glucogonInjectedProperty = new SimpleStringProperty();
	private final transient DoubleProperty glucogonTankCapacityProperty = new SimpleDoubleProperty();
	private final transient StringProperty systemDateProperty = new SimpleStringProperty();
	private final transient BooleanProperty alertBoxProperty = new SimpleBooleanProperty(true);



	// log the predicted glucose
	ObservableList<Data<Long, Double>> predictedGmax = FXCollections.observableArrayList();
	// log the insulin injected in the body
	ObservableList<Data<Long, Double>> lowerLimit = FXCollections.observableArrayList();
	// lower limit of blood glucose
	ObservableList<Data<Long, Double>> upperLimit = FXCollections.observableArrayList();
	// upper Limit of glucose level
	ObservableList<Data<Long, Double>> insulinInjected = FXCollections.observableArrayList();

	ObservableList<Data<Long, Double>> gluccogonInjected = FXCollections.observableArrayList();

	private InsulinPump() {

	}

	public static InsulinPump getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new InsulinPump();
		}
		return singletonInstance;
	}

	public boolean isManualModeEnabled() {
		return isManualModeEnabled;
	}

	public void setManualModeEnabled(boolean isManualModeEnabled) {
		this.isManualModeEnabled = isManualModeEnabled;
	}

	public void setDateString(Long dateLong) {
		this.dateString = dateLong;
		Date currentDate = new Date(dateLong);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		getSystemDateProperty().set(sdf.format(currentDate));
	}
	
	public BooleanProperty getAlertBoxProperty() {
		return alertBoxProperty;
	}

	public Double getGlucogonRemaining() {
		return glucogonRemaining;
	}

	public void setGlucogonRemaining(Double glucogonRemaining) {
		this.glucogonRemaining = glucogonRemaining;
		Double glucogonPercentage = getGlucogonRemaining() / this.tankCapcity;
		getGlucogonTankCapacityProperty().set(glucogonPercentage);
		if (glucogonPercentage < .06) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail("glucogon capacity in the pump is too low please alert the user");
		}
	}

	public Double getCalculatedGlucogon() {
		return calculatedGlucogon;
	}

	public void setCalculatedGlucogon(Double calculatedGlucogon) {
		this.calculatedGlucogon = calculatedGlucogon;
		Double glucogonRmaining = getGlucogonRemaining() - calculatedGlucogon;
		setGlucogonRemaining(glucogonRmaining);
		getGlucogonInjectedProperty().set(this.calculatedGlucogon.toString());

	}

	public int getRefreshrate() {
		return refreshrate;
	}

	public Double getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(Double batteryLevel) {
		this.batteryLevel = Math.min(100, batteryLevel);
		getBatteryCapacityProperty().set(this.batteryLevel / 100);
		// System.out.println(this.batteryLevel);
		// TODO alert the user of battery
		if (getBatteryLevel() < 6) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail("Battery Level too low please alert the user");
		}
	}

	public StringProperty getSystemDateProperty() {
		return systemDateProperty;
	}

	public StringProperty getGlucogonInjectedProperty() {
		return glucogonInjectedProperty;
	}

	public DoubleProperty getGlucogonTankCapacityProperty() {
		return glucogonTankCapacityProperty;
	}

	public StringProperty getErrorMessageProperty() {
		return errorMessageProperty;
	}

	public DoubleProperty getBatteryCapacityProperty() {
		return batteryCapacityProperty;
	}

	public ObservableList<Data<Long, Double>> getGluccogonInjected() {
		return gluccogonInjected;
	}

	public Double getInsulinRemaining() {
		return insulinRemaining;
	}

	public void setInsulinRemaining(Double insulinTankcapacilty) {
		this.insulinRemaining = insulinTankcapacilty;
		Double insulinPercentage = getInsulinRemaining() / this.tankCapcity;
		getInsulinTankCapacityProperty().set(insulinPercentage);
		// alert the user
		if (insulinPercentage < .06) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail("Insulin capacity in the pump is too low please alert the user");
		}
		// System.out.println("percentage "+insulinPercentage);
	}

	protected void setCalculatedINsulin(Double calculatedINsulin) {
		this.calculatedINsulin = calculatedINsulin;
		Double insulinRmaining = getInsulinRemaining() - calculatedINsulin;
		setInsulinRemaining(insulinRmaining);
		getCalculatedINsulinProperty().set(calculatedINsulin.toString());
	}

	protected Double getCalculatedINsulin() {
		return this.calculatedINsulin;
	}

	public DoubleProperty getInsulinTankCapacityProperty() {
		return insulinTankCapacityProperty;
	}

	public StringProperty getCalculatedINsulinProperty() {
		return insulinInjectedProperty;
	}

	public ObservableList<Data<Long, Double>> getPredictedGmax() {
		// predictedGmax= FXCollections.observableArrayList();
		return predictedGmax;
	}

	public ObservableList<Data<Long, Double>> getInsulinInjectedCollection() {
		// insulinInjected = FXCollections.observableArrayList();
		return insulinInjected;
	}

	private InsulinPump(MonitoringBehavior mode) {
		// TODO Auto-generated constructor stub
		monioringMode = mode;
		monioringMode.startMonitoring();
	}

	public void setMonioringMode(MonitoringBehavior monioringMode) {
		// changing monitoring status
		this.monioringMode = monioringMode;
		monioringMode.startMonitoring();
	}

	private static MonitoringBehavior getMonioringMode() {
		// check monitoring status
		return monioringMode;
	}

	public ObservableList<Data<Long, Double>> getLowerLimit() {
		return lowerLimit;
	}

	public ObservableList<Data<Long, Double>> getUpperLimit() {
		return upperLimit;
	}

	public void setErrorMessage(String message,Boolean alertUser) {
		//setting the blink button
		getAlertBoxProperty().set(alertUser);
		//the alert will stay for 25 seconds and go away
		if (alertUser) {
			Timeline timeline = new Timeline(
					new KeyFrame(Duration.seconds(30), ae -> getAlertBoxProperty().set(false)));
			timeline.play();
		}
		getErrorMessageProperty().set(message);
	}

	public void calculateInsulin() {
		monioringMode.checkGlucose();
	}

	public void injectInsulin(Double quantity) {
		monioringMode.injectInsulin(quantity);
	}

	/**
	 * logs the value to the graph
	 * 
	 * @param gPredictedValue
	 */
	public void updateGraph(Double gPredictedValue, Double insulin, Double glucogon) {

		Task clTask = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// System.out.println("gpredicted " + glucogon);
						getPredictedGmax()
								.add(new Data<Long, Double>(Patient.getPatient().getTimeDelta(), gPredictedValue));
						getInsulinInjectedCollection()
								.add(new Data<Long, Double>(Patient.getPatient().getTimeDelta(), insulin * 100));
						getUpperLimit().add(new Data<Long, Double>(Patient.getPatient().getTimeDelta(),
								Patient.getPatient().getTargetGlucose()));
						getLowerLimit().add(new Data<Long, Double>(Patient.getPatient().getTimeDelta(), 50.));
						getGluccogonInjected()
								.add(new Data<Long, Double>(Patient.getPatient().getTimeDelta(), glucogon * 10));
					}
				});

				return null;
			}
		};
		new Thread(clTask).start();
	}

	public void stopMonitoring() {
		// TODO Auto-generated method stub
		monioringMode.stopMonitoring();
	}

	public void startMonitoring() {
		monioringMode.startMonitoring();
	}

	public void injectGlucogon(Double toBeInjectedValue) {
		// TODO Auto-generated method stub
		monioringMode.injectGlucogon(toBeInjectedValue);
	}

}
