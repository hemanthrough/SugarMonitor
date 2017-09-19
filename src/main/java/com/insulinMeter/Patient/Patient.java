package com.insulinMeter.Patient;

import java.util.HashMap;

import com.insulinMeter.DB.SQliteDB;
import com.insulinMeter.DB.SQliteDB.QueryTyp;
import com.insulinMeter.DB.SQliteDB.TableNames;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart.Data;

public class Patient {

	// singleton variable
	private static Patient singletonVariable;
	int ISF = 50;
	private String name = "prachi";

	private String email = "hemanthrough@gmail.com";

	// graph variables
	private volatile ObservableList<Data<String, Double>> insulinLevel = FXCollections.observableArrayList();

	protected volatile ObservableList<Data<Long, Double>> glucoseLevel = FXCollections.observableArrayList();
	static long timeStamp = System.currentTimeMillis();

	private Double targetGlucose = 100.;

	Double a0 = 121.7;
	// initial sugar level
	public Double g0 = 150.0;
	Double k1 = .0453;
	Double k2 = .0224;
	Double glucogonSensitivity = 4.;
	private Double weight = 75.;

	// property variables
	private final transient StringProperty bslProperty = new SimpleStringProperty();
	private final transient StringProperty previousBslProperty = new SimpleStringProperty();

	// making it uninstantiatable
	private Patient() {

	}

	public Integer getISF() {
		return ISF;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public void setISF(int iSF) {
		ISF = iSF;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ObservableList<Data<Long, Double>> getGlucoseLevel() {
		glucoseLevel = FXCollections.observableArrayList();
		return glucoseLevel;
	}

	public Double getTargetGlucose() {
		return targetGlucose;
	}

	public void setTargetGlucose(Double targetGlucose) {
		this.targetGlucose = targetGlucose;
	}

	public StringProperty getBslProperty() {
		return bslProperty;
	}

	public StringProperty getPreviousBslProperty() {
		return previousBslProperty;
	}

	public Double getGlucogonSensitivity() {
		return glucogonSensitivity;
	}

	public void setGlucogonSensitivity(Double glucogonSensitivity) {
		this.glucogonSensitivity = glucogonSensitivity;
	}

	public Double getA0() {
		return a0;
	}

	public void setA0(Double a0) {
		this.a0 = a0;
	}

	public Double getK1() {
		return k1;
	}

	public void setK1(Double k1) {
		this.k1 = k1;
	}

	public Double getK2() {
		return k2;
	}

	public void setK2(Double k2) {
		this.k2 = k2;
	}

	// initial value of sugar
	// private double sugarLevel = 100;

	// may be used for further calculation
	// private int amtOfInsulinInjected;

	public Double getSugarLevel() {
		return g0;
	}

	/**
	 * setting to the access to only to body
	 * 
	 * @param g0
	 */
	public void setSugarLevelAfterInsulinInjection(Double f) {
		String previousSugarLevel = getSugarLevel().toString();
		getPreviousBslProperty().setValue(previousSugarLevel);
		//getPreviousBslProperty().setValue(getSugarLevel().toString());

		
		/*
		 * if (this.sugarLevel < 3) { this.sugarLevel = 100; }
		 */
		// System.out.println("g0" +this.sugarLevel);
		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						glucoseLevel.add(new Data<Long, Double>(getTimeDelta(), getSugarLevel()));
					}
				});

				return null;
			}
		};
		new Thread(task).start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSugarLevel( getSugarLevel() - (f * ISF));
		String currentSuagrLevel =getSugarLevel().toString(); 
		getBslProperty().set(currentSuagrLevel);
	}

	public void setSugarLevel(Double sugarLevel) {
		this.g0 = sugarLevel;

	}

	public void setSugarLevelAfterGlucogonInjection(Double f) {
		//TODO delete this line
		if (f > 0.00000001) {
			getPreviousBslProperty().set(getSugarLevel().toString());

		}
		/*
		 * if (this.sugarLevel < 3) { this.sugarLevel = 100; }
		 */
		// System.out.println("g0" +this.sugarLevel);
		Task task = new Task<Void>() {
			@Override
			public Void call() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						glucoseLevel.add(new Data<Long, Double>(getTimeDelta(), g0));
					}
				});

				return null;
			}
		};
		new Thread(task).start();
		try{
			Thread.sleep(1000);

		}catch (InterruptedException e) {
			// TODO: handle exception
		}
		this.g0 = g0 + (f * getGlucogonSensitivity());
		getBslProperty().set(g0.toString());
	}

	public Long getTimeDelta() {
		return ((Long) ((System.currentTimeMillis() - timeStamp) / 1000));
	}

	public static Patient getPatient() {
		if (singletonVariable == null) {
			singletonVariable = new Patient();
		}
		return singletonVariable;
	}

	/**
	 * 
	 * @param quantity
	 *            the behavior of the body when insulin is injected
	 */

	// Double g0=90;

	public void injectInsulin(Double quantity) {
		if (quantity < 0.00000001) {
			// logic for decreasing the level after insulin is given
			// setSugarLevel(getSugarLevel() - quantity);
			// this.notifyObs ervers();
			// setting the current insulin inection rate
			// setK2(quantity);
			setSugarLevelAfterInsulinInjection(quantity);
		} else {
			// setK2(0.);
			setSugarLevelAfterInsulinInjection(quantity);
			// insert only when insulin is injected
			HashMap<String, String> hashdelte = new HashMap<>();
			hashdelte.put("SYSDATE", ((Long) System.currentTimeMillis()).toString());
			hashdelte.put("GLUCAGONLEVEL", getSugarLevel().toString());
			hashdelte.put("InsulinInjected", quantity.toString());
			hashdelte.put("GLucoseINjected", "0");
			SQliteDB.getInstance().createTable(TableNames.InsulinData);
			SQliteDB.getInstance().exceuteQuery(hashdelte, QueryTyp.insertIntoTable, TableNames.InsulinData, null);
		}

	}

	public void injectGlucogon(Double glucogonQuantity) {
		if (glucogonQuantity < 0.00000001) {
			setSugarLevelAfterGlucogonInjection(glucogonQuantity);
		} else {
			setSugarLevelAfterGlucogonInjection(glucogonQuantity);
			// insert only when insulin is injected
			HashMap<String, String> hashdelte = new HashMap<>();
			hashdelte.put("SYSDATE", ((Long) System.currentTimeMillis()).toString());
			hashdelte.put("GLUCAGONLEVEL", getSugarLevel().toString());
			hashdelte.put("InsulinInjected", "0");
			hashdelte.put("GLucoseINjected", glucogonQuantity.toString());
			SQliteDB.getInstance().createTable(TableNames.InsulinData);
			SQliteDB.getInstance().exceuteQuery(hashdelte, QueryTyp.insertIntoTable, TableNames.InsulinData, null);
		}
	}

	/**
	 * calculate the g0 after 10 secs
	 */
	Double calculateInsulin() {
		Long delta = getTimeDelta() / 1000;
		Double g1 = (a0 * (k1 / (k2 - k1)) * (Math.exp(-k1 * 10) - Math.exp(-k2 * 10))) + g0;
		// check how does ody react to insulin
		g1 = g1 - g0;
		g0 = g0 - g1;
		System.out.println(delta + " g0 " + g0);
		return Patient.getPatient().getSugarLevel();
	}

}