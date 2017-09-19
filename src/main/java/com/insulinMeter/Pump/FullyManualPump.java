package com.insulinMeter.Pump;

import java.awt.event.ActionEvent;

import javax.swing.Timer;

import com.insulinMeter.DB.SQliteDB;
import com.insulinMeter.Mail.MailNAlertUtils;
import com.insulinMeter.Patient.Patient;

public class FullyManualPump extends MonitoringBehavior {

	Timer timer;
	
	private static FullyManualPump instance;
	
	private FullyManualPump() {
		
	}
	
	public static FullyManualPump getInstance(){
		if(instance == null){
			instance = new FullyManualPump();
		}
		return instance;
	}

	@Override
	public void startMonitoring() {
		timer = new Timer(InsulinPump.getInstance().getRefreshrate(), this);
		timer.start();
		InsulinPump.getInstance().setErrorMessage("switched to manual mode",false);
	}

	@Override
	public void checkGlucose() {
		// monitors and alerts users but doest take any action
		Double currentSugarLevel = calulateGmax();
		if (140 < currentSugarLevel) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail("sugar level too high immediate attention required current sugar Level is "
					+ Patient.getPatient().getSugarLevel());
		} else if (Patient.getPatient().getSugarLevel() < 70) {
			MailNAlertUtils mailUtils = new MailNAlertUtils();
			mailUtils.sendMail("sugar level too low immediate attention required current sugar Level is "
					+ Patient.getPatient().getSugarLevel());
		}
		InsulinPump.getInstance().setCalculatedINsulin(0.);
		Patient.getPatient().injectInsulin(.0);
		InsulinPump.getInstance().setCalculatedGlucogon(0.);
		Patient.getPatient().injectGlucogon(.0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		checkGlucose();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		// timer.stop();
	}

	@Override
	public void injectInsulin(Double quantity) {
		// checks if after insulinInjection leads to safe level
		Double currentSugarLevel = Patient.getPatient().g0;
		Double glucoseAfterDosage = currentSugarLevel - quantity * Patient.getPatient().getISF();
		// send out message if injection leads to hypoGlycemia
		if (glucoseAfterDosage < 70) {
			InsulinPump.getInstance().setErrorMessage("Alert the dosage is not recommendeed",true);
			InsulinPump.getInstance().setCalculatedINsulin(.0);
		} else {
			InsulinPump.getInstance().setCalculatedINsulin(quantity);
			Patient.getPatient().injectInsulin(InsulinPump.getInstance().getCalculatedINsulin());

		}

		updateGraph(InsulinPump.getInstance().getCalculatedINsulin(),
				InsulinPump.getInstance().getCalculatedGlucogon());
		reduceBattery();
	}

	void updateGraph(Double insulin, Double glucogon) {

		InsulinPump.getInstance().updateGraph(0., insulin, glucogon);
	}

	@Override
	public void stopMonitoring() {
		timer.stop();
	}

	@Override
	public void injectGlucogon(Double quantity) {
		// checks if insulin is injected within 20mins
		if (SQliteDB.getInstance().getLastGlucoseInjectedTime() == null) {
			// checks if after insulinInjection leads to safe level
			Double currentSugarLevel = Patient.getPatient().g0;
			Double glucoseAfterDosage = currentSugarLevel + quantity * Patient.getPatient().getGlucogonSensitivity();
			// check if after injection doesnt lead to hyper glysemia
			if (glucoseAfterDosage > 100) {
				Patient.getPatient().injectGlucogon(0.);
			} else {
				// setting the max sugar level to 2 units
				Double glucoseToBeInjected = Math.min(2, quantity);
				InsulinPump.getInstance().setCalculatedGlucogon(glucoseToBeInjected);
				Patient.getPatient().injectGlucogon(glucoseToBeInjected);
			}
		} else {
			InsulinPump.getInstance().setErrorMessage("Glucogon should be injected after sometime",true);
		}
	}

}
