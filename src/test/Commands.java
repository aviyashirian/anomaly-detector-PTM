package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		public String readText();
		public void write(String text);
		public float readVal();
		public void write(float val);

		// you may add default methods here
	}

	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}
	
	public class StandardIO implements DefaultIO {
		@Override
		public String readText() {
			Scanner scan = new Scanner(System.in);
			String line = scan.nextLine();
			scan.close();
			return line;
		}

		@Override
		public void write(String text) {
			System.out.println(text);
		}

		@Override
		public float readVal() {
			Scanner myScan = new Scanner(System.in);
			float in = myScan.nextInt();
			myScan.close();
			return in;
		}

		@Override
		public void write(float val) {
			System.out.println(val);
		}
		
		public void readCsv(PrintWriter out) throws IOException {
			String line = dio.readText();
			if (line == ""){
				line = dio.readText();
			}
			while(!line.contains("done")){
				out.write(line + "\n");
				line = dio.readText();
			}
		}
	}


	
	
	
	// the shared state of all commands
	private class SharedState{
		public TimeSeries trainTs;
		public TimeSeries testTs;
		public float threshold = (float) 0.9;
		public SimpleAnomalyDetector detector = new SimpleAnomalyDetector();
		public List<AnomalyReport> reports;
		public StandardIO sio = new StandardIO();
	}
	
	private  SharedState sharedState=new SharedState();

	
	// Command abstract class
	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}
	
	public class UploadCsvCommand extends Command{

		public UploadCsvCommand() {
			super("upload a time series csv file");
		}

		@Override
		public void execute() {
			try {
				dio.write("Please upload your local train CSV file.\n");
				PrintWriter train = new PrintWriter(new FileWriter("anomalyTrain.csv"));
				sharedState.sio.readCsv(train);
				train.close();
				sharedState.trainTs = new TimeSeries("anomalyTrain.csv");
				dio.write("Upload complete.\n");
				
				dio.write("Please upload your local test CSV file.\n");
				PrintWriter test = new PrintWriter( new FileWriter("anomalyTest.csv"));
				sharedState.sio.readCsv(test);
				test.close();
				sharedState.testTs = new TimeSeries("anomalyTest.csv");
				dio.write("Upload complete.\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}		
	}
	
	public class AlgorithmSettingCommand extends Command{

		public AlgorithmSettingCommand() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
		    dio.write("The current correlation threshold is " + sharedState.threshold + "\n");
		    dio.write("Type a new threshold\n");
			float inputThreshold = dio.readVal();

			while (inputThreshold < 0 || inputThreshold > 1) {
				dio.write("please choose a value between 0 and 1.");
				inputThreshold = dio.readVal();
			}

			sharedState.threshold = inputThreshold;
		}
	}

	public class DetectAnomaliesCommand extends Command{

		public DetectAnomaliesCommand() {
			super("detect anomalies");
		}

		@Override
		public void execute() {
			sharedState.detector.learnNormal(sharedState.trainTs);
			sharedState.reports = sharedState.detector.detect(sharedState.testTs);
			dio.write("anomaly detection complete.\n");
		}
	}

	public class DisplayResultsCommand extends Command{

		public DisplayResultsCommand() {
			super("display results");
		}

		@Override
		public void execute() {
			for (AnomalyReport report : sharedState.reports){
				dio.write(report.timeStep + "\t" + report.description + "\n");
			}
			dio.write("Done.\n");
		}
	}
}
