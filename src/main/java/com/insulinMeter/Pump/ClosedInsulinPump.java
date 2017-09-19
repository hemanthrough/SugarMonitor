package com.insulinMeter.Pump;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.Timer;

import com.insulinMeter.DB.SQliteDB;
import com.insulinMeter.DB.SQliteDB.QueryTyp;
import com.insulinMeter.DB.SQliteDB.TableNames;
import com.insulinMeter.Mail.MailNAlertUtils;
import com.insulinMeter.Patient.Patient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart.Data;

public class ClosedInsulinPump extends MonitoringBehavior {

	private static ClosedInsulinPump instance;

	private ClosedInsulinPump() {

	}

	public static ClosedInsulinPump getInstance() {
		if (instance == null) {
			instance = new ClosedInsulinPump();
		}
		return instance;
	}

	Timer timer;
	// Double predictedGlucoseLevel;

	/**
	 * check the glucoseLevl and injects if it exceeds threshold level
	 */
	@Override
	public void checkGlucose() {
		injectInsulin(calulateGmax());
		// return 0.;
		reduceBattery();
	}

	/***
	 * 
	 * @return predicts max value of insulin using the max value alg
	 */

	@Override
	public void startMonitoring() {
		timer = new Timer(InsulinPump.getInstance().getRefreshrate(), this);
		timer.start();
		// predictedGmax = FXCollections.observableArrayList();
		InsulinPump.getInstance().setErrorMessage("switched to Auto mode", false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (timer != null) {
			checkGlucose();
			InsulinPump.getInstance().setDateString(System.currentTimeMillis());
		}
	}

	/***
	 * added logic for calculating the insulin and subsequently injecting the
	 * insulin
	 */
	@Override
	public void injectInsulin(Double predictedGlucoseLevel) {
		if (predictedGlucoseLevel > 140) {
			// logic for sending mail
			if (predictedGlucoseLevel > 200) {
				MailNAlertUtils mailUtils = new MailNAlertUtils();
				mailUtils.sendMail("sugar level too high immediate attention required current sugar Level is "
						+ Patient.getPatient().getSugarLevel());
			}
			// 100 is our target sugar level
			Double insulinDelta = predictedGlucoseLevel - 100;
			// check if the sugar is under control
			if (insulinDelta > 0) {
				// getting the isf value
				int ISF = Patient.getPatient().getISF();
				// check for max value of insulin injected

				// InsulinPump.getInstance().setCalculatedINsulin();
				// setting the max injectable insulin 2 units
				InsulinPump.getInstance().setCalculatedINsulin(Math.min(insulinDelta / ISF, .5));
				Patient.getPatient().injectInsulin(InsulinPump.getInstance().getCalculatedINsulin());

			} else {

				InsulinPump.getInstance().setCalculatedINsulin(0.);
				Patient.getPatient().injectInsulin(.0);
				InsulinPump.getInstance().setCalculatedGlucogon(0.);
				Patient.getPatient().injectGlucogon(.0);
			}

		} else {
			checkBloodGlucoseLevel();
			InsulinPump.getInstance().setCalculatedINsulin(0.);
			Patient.getPatient().injectInsulin(0.);
		}
		// updating the graph
		updateGraph(predictedGlucoseLevel, InsulinPump.getInstance().getCalculatedINsulin(),
				InsulinPump.getInstance().getCalculatedGlucogon());
		InsulinPump.getInstance().setCalculatedGlucogon(0.);
		Patient.getPatient().injectGlucogon(.0);
	}

	private void checkBloodGlucoseLevel() {
		Double currentGlucoseLevel = Patient.getPatient().getSugarLevel();
		injectGlucogon(currentGlucoseLevel);

	}

	private void updateGraph(Double predictedGlucoseLevel, Double calculatedINsulin, Double calculateGlucogon) {
		InsulinPump.getInstance().updateGraph(predictedGlucoseLevel, calculatedINsulin, calculateGlucogon);
	}

	@Override
	public void stopMonitoring() {
		timer.stop();
		timer.removeActionListener(this);
		timer = null;
	}

	@Override
	public void injectGlucogon(Double currentGlucoseLevel) {
		if (currentGlucoseLevel < 70) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail(
					"Blood glucose level too low if left unattended may lead to severe consequences \n patient sugar level "
							+ Patient.getPatient().getSugarLevel());
			// checks if insulin is injected within 20mins
			if (SQliteDB.getInstance().getLastGlucoseInjectedTime() == null) {
				// calculates only if it is not injected
				Double glucoseDelta = 100 - currentGlucoseLevel;
				Double GlucogonSenstivity = Patient.getPatient().getGlucogonSensitivity();
				Double glucoseToBeInjected = Math.min(2, glucoseDelta / GlucogonSenstivity);
				InsulinPump.getInstance().setCalculatedGlucogon(glucoseToBeInjected);
				Patient.getPatient().injectGlucogon(glucoseToBeInjected);
			} else {
				InsulinPump.getInstance().setCalculatedINsulin(0.);
				Patient.getPatient().injectInsulin(0.);
				InsulinPump.getInstance().setCalculatedGlucogon(0.);
				Patient.getPatient().injectGlucogon(.0);
			}
		} else {
			InsulinPump.getInstance().setCalculatedINsulin(0.);
			Patient.getPatient().injectInsulin(0.);
			InsulinPump.getInstance().setCalculatedGlucogon(0.);
			Patient.getPatient().injectGlucogon(.0);
		}
	}

}
