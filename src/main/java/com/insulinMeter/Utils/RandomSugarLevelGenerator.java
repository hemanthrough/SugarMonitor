package com.insulinMeter.Utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.insulinMeter.Patient.Patient;

public class RandomSugarLevelGenerator implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Patient.getPatient().setSugarLevel(Math.random()*220);
		System.out.println("setting sugar");
	}
	

	

}
