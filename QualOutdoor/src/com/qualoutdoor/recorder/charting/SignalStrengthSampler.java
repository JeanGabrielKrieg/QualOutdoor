package com.qualoutdoor.recorder.charting;

import java.util.Observable;
import java.util.Observer;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

public class SignalStrengthSampler implements Runnable {

	public class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public synchronized void onSignalStrengthsChanged(
				SignalStrength signalStrength) {
			mySignalStrength = signalStrength;
		}
	}

	/**
	 * Encapsulates management of the observers watching this datasource for
	 * update events:
	 */
	class MyObservable extends Observable {
		@Override
		public void notifyObservers() {
			setChanged();
			super.notifyObservers();
		}
	}

	private SignalStrength mySignalStrength;
	private int sampling_rate = 500;
	private MyObservable notifier;
	private MyPhoneStateListener listener;

	public SignalStrengthSampler(int samplingRate) {
		this.sampling_rate = samplingRate;
		this.notifier = new MyObservable();
		this.listener = new MyPhoneStateListener();
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(sampling_rate); // Define the sampling rate
				this.notifier.notifyObservers();
			}
		} catch (InterruptedException exc) {
			Log.d("Sampler", "Thread interrupted");
		}
	}

	public synchronized SignalStrength getSignalStrength() {
		return mySignalStrength;
	}

	public PhoneStateListener getPhoneStateListener() {
		return listener;
	}

	public void addObserver(Observer observer) {
		notifier.addObserver(observer);
	}

	public void removeObserver(Observer observer) {
		notifier.deleteObserver(observer);
	}

}
