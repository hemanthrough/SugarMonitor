package com.insulinMeter.Mail;

import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.SynthesizerProperties;

import com.insulinMeter.Patient.Patient;
import com.insulinMeter.Pump.InsulinPump;

public class MailNAlertUtils {

	Synthesizer synthesizer;
	// for avoiding repetaion of multiple audio
	static ReentrantLock audioLock = new ReentrantLock();

	public void sendMail(String args) {
		final String username = "insulinmonitor@gmail.com";
		final String password = "frauas@2016";
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		// SmtpAuthenticator smtp = new SmtpAuthenticator();
		InsulinPump.getInstance().setErrorMessage("Alert " + args, true);
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			// Message message = new
			// MimeMessage(Session.getDefaultInstance(props, smtp));
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("hemanthrough@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("hemanthrough@gmail.com"));
			message.setSubject("Attention patient in critical state");
			message.setText("Dear USer," + "\n\n This mail is regarding/from the insulin pump of Mrs/mr!"
					+ Patient.getPatient().getName()
					+ "\n the following parameter are critical in the said patient kindly contact him as soon as possible"
					+ args);
			// message.se
			Thread mailThread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {

						Transport.send(message);

					} catch (MessagingException e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			});
			//check if there is pending alert
			if (!audioLock.isLocked()) {
				System.out.println(audioLock.isLocked());
				alarm(args);
			}

			mailThread.start();
			// System.out.println("Done");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public synchronized void alarm(String speechText) {
		try {
			

			if (synthesizer == null) {
				System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
				SynthesizerModeDesc voiceModesynthesizer = new SynthesizerModeDesc(Locale.US);
				Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
				synthesizer = Central.createSynthesizer(voiceModesynthesizer);
				synthesizer.allocate();
				synthesizer.resume();
				SynthesizerProperties props = (SynthesizerProperties) synthesizer.getEngineProperties();
				Float speakingRate = 100f;
				props.setSpeakingRate(speakingRate);
				voiceModesynthesizer = null;
			}
			Thread voiceThread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						audioLock.lock();
						synthesizer.speakPlainText(speechText, null);
						
						synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
						if (audioLock.isLocked()) {
							audioLock.unlock();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						if (audioLock.isLocked()) {
							audioLock.unlock();
						}
					}
				}
			});
			voiceThread.start();

		} catch (EngineException e) {
			e.printStackTrace();
		} catch (AudioException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
}
