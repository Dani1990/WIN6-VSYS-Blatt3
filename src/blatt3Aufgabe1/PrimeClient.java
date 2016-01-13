package blatt3Aufgabe1;

import java.io.BufferedReader;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import rm.requestResponse.*;

public class PrimeClient {
	private static final String HOSTNAME = "localhost";
	private static final int PORT = 1234;
	private static final long INITIAL_VALUE = (long) 1e17;
	private static final long COUNT = 20;
	private static final String CLIENT_NAME = PrimeClient.class.getName();

	static Random randomGen = new Random();

	JSONParser parser = new JSONParser();

	private Component communication;
	String hostname;
	int port;
	long initialValue, count;
	static String requestMode;
	int portClient;

	// variables for avg times
	long processingTimeSum;
	long waitingTimeSum;
	long communicationTimeSum;
	int requestCount;

	public PrimeClient(String hostname, int port, long initialValue, long count) {
		this.hostname = hostname;
		this.port = port;
		this.initialValue = initialValue;
		this.count = count;

		portClient = (randomGen.nextInt(1599) + 4000);

	}

	public void run() throws ClassNotFoundException, IOException {
		communication = new Component();

		for (long i = initialValue; i < initialValue + count; i++) {
			processNumber(i);
		}
	}

	public void processNumber(long value) throws IOException,
			ClassNotFoundException {
		System.out.println("Client-Port: " + portClient);
		long sendTime = System.currentTimeMillis();
		System.out.println("sendTime: " + sendTime);
		communication.send(new Message(hostname, portClient, new Long(value)),
				port, false);
		String answer = (String) communication.receive(portClient, true, true)
				.getContent();
		long answerTime = System.currentTimeMillis();
		System.out.println("answerTime: " + answerTime);

		// Parse and process answer + Sysout
		try {
			requestCount++;

			Object obj = parser.parse(answer);
			JSONObject obj2 = (JSONObject) obj;

			boolean isPrime = (boolean) obj2.get("isPrime");
			long processingTime = (long) obj2.get("processingTime");
			processingTimeSum += processingTime;
			long processingTimeAvg = processingTimeSum / requestCount;
			long waitingTime = (long) obj2.get("waitingTime");
			waitingTimeSum += waitingTime;
			long waitingTimeAvg = waitingTimeSum / requestCount;
			long communicationTime = answerTime - sendTime - waitingTime
					- processingTime;
			communicationTime = (communicationTime < 0 ? -communicationTime : communicationTime);
			System.out.println("a: " + answerTime + ", s: " + sendTime + ", w: " + waitingTime + ", p: " + processingTime);
			communicationTimeSum += communicationTime;
			long communicationTimeAvg = communicationTimeSum / requestCount;

			System.out.println(value + ": "
					+ (isPrime ? "prime	" : "not prime	") + "	| p: "
					+ processingTime + " (" + processingTimeAvg + ") ms"
					+ " | w: " + waitingTime + " (" + waitingTimeAvg + ") ms"
					+ " | c: " + communicationTime + " ("
					+ communicationTimeAvg + ") ms");

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException,
			ClassNotFoundException {
		String hostname = HOSTNAME;
		int port = PORT;
		long initialValue = INITIAL_VALUE;
		long count = COUNT;

		boolean doExit = false;

		String input;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));

		System.out.println("Welcome to " + CLIENT_NAME + "\n");

		while (!doExit) {
			System.out.print("Server hostname [" + hostname + "] > ");
			input = reader.readLine();
			if (!input.equals(""))
				hostname = input;

			System.out.print("Server port [" + port + "] > ");
			input = reader.readLine();
			if (!input.equals(""))
				port = Integer.parseInt(input);

			// Request Mode added to menu
			System.out.println("Request mode [SYNCHRONIZED/NEBENLÄUFIG]");

			System.out.println("Prime search initial value [" + initialValue
					+ "] > ");
			input = reader.readLine();
			if (!input.equals(""))
				initialValue = Integer.parseInt(input);

			System.out.print("Prime search count [" + count + "] > ");
			input = reader.readLine();
			if (!input.equals(""))
				count = Integer.parseInt(input);

			new PrimeClient(hostname, port, initialValue, count).run();

			System.out.println("Exit [n]> ");
			input = reader.readLine();
			if (input.equals("y") || input.equals("j"))
				doExit = true;
		}
	}
}
