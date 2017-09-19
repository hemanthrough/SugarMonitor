package com.insulinMeter.Pump;

import java.awt.event.ActionListener;

import com.insulinMeter.Patient.Patient;

public abstract class MonitoringBehavior implements ActionListener {



	public void plotGraph() {

	}

	/**
	 * calcualte insulin based on math model
	 */
	abstract public void checkGlucose();
	// change the monitoring behavior
	abstract public void startMonitoring();

	abstract public void injectInsulin(Double quantity);
	
	abstract public void injectGlucogon(Double quantity);

	protected final double calulateGmax() {
		// getting predicts params from the patient's instance
		Patient patientInstance = Patient.getPatient();
		Double k1 = patientInstance.getK1();
		Double k2 = patientInstance.getK2();
		// for predciting using the arbitarry value as the k2 will be 0 when
		// patent is in normal stage
		if (k2 < 0.00000001) {
			k2 = .0224;
		}

		Double a0 = patientInstance.getA0();
		Double g0 = patientInstance.getSugarLevel();
		Double lnOfK1K2 = (Math.log(k1) - Math.log(k2)) / (k1 - k2);
		// since this value is independent of the time function we can essily
		// predict max value with current
		// parameters
		Double gPredicted = (a0 * (k1 / (k2 - k1)) * (Math.exp(-k1 * 10) - Math.exp(-k2 * 10))) + g0;
		// (((a0* k1)/(k2-k1))*(Math.exp(-k1* lnOfK1K2)- Math.exp(-k2*
		// lnOfK1K2))) + g0;

		return gPredicted;
	}

	
	abstract public void stopMonitoring();
	
	protected void reduceBattery(){
		InsulinPump.getInstance().setBatteryLevel(InsulinPump.getInstance().getBatteryLevel()-.05);
		
	}

}