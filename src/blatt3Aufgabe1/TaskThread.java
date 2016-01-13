package blatt3Aufgabe1;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import rm.requestResponse.*;

public class TaskThread implements Runnable {

	private Component communication;
	private Message msg;
	public static int counter;
	private Counter c;
	public static long processingTime;
	public static long receiveTime;
	public static long waitingTime;

	public TaskThread(Component communication, Message msg, Counter c,
			long receiveTime) {
		this.c = c;
		this.communication = communication;
		this.msg = msg;
		TaskThread.receiveTime = receiveTime;
		c.incrementCounter();
	}

	private final static Logger LOGGER = Logger.getLogger(TaskThread.class
			.getName());

	@SuppressWarnings("unchecked")
	public void run() {
		// TODO Auto-generated method stub
		
		LOGGER.finer("Sending ...");
		try {
			Long request = (Long) msg.getContent();
			
			JSONObject answer = new JSONObject();
			answer.put("isPrime", new Boolean(primeService(request.longValue())));
			answer.put("processingTime", new Long(processingTime));
			answer.put("waitingTime", new Long(waitingTime));

			// sende an Client zurück
			communication.send(new Message("localhost", 0, new String(JSONValue.toJSONString(answer))),
					msg.getPort(), true);

			c.decrementCounter();

		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.fine("message sent.");

		}

	}

	private boolean primeService(long number) {
		waitingTime = System.currentTimeMillis() - receiveTime;
		long timeStart = System.currentTimeMillis();
		for (long i = 2; i < Math.sqrt(number) + 1; i++) {
			if (number % i == 0) {
				processingTime = System.currentTimeMillis() - timeStart;
				return false;
			}
		}
		processingTime = System.currentTimeMillis() - timeStart;
		return true;
	}

}
