package test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

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
			System.out.print(text);
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
			System.out.print(val);
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

	public class UploadAndAnalyzeCommand extends Command{

		public UploadAndAnalyzeCommand() {
			super("upload anomalies and analyze results");
		}

		@Override
		public void execute() {			
			List<Anomaly> actualAnomalies = this.UploadAnomalies();
			List<Anomaly> detectedAnomalies = this.JoinReports(sharedState.reports);

			double P = actualAnomalies.size();
			double N = sharedState.testTs.getColumn(0).length - sharedState.reports.size();

			double FP = 0, TP = 0;

			for (Anomaly detected : detectedAnomalies) {
				boolean overlap = actualAnomalies.stream().anyMatch(a -> 
					a.startTimeStep <= detected.endTimeStep && detected.startTimeStep <= a.endTimeStep
				);

				if (overlap) {
					TP++;
				} else {
					FP++;
				}
			}

			dio.write("True Positive Rate: " + (double)((int)((TP / P)*1000))/1000 + "\n");
			dio.write("False Positive Rate: " + (double)((int)((FP / N)*1000))/1000 + "\n");
		}

		private List<Anomaly> UploadAnomalies() {
			dio.write("Please upload your local anomalies file.\n");
			
			List<Anomaly> anomalies = new ArrayList<>();
			String line = dio.readText();
			if (line.equals("")) {
				line = dio.readText();	
			}

			while (!line.equals("done")) {
				long start = Long.parseLong(line.split(",")[0]);
				long end = Long.parseLong(line.split(",")[1]);
				anomalies.add(new Anomaly(start, end, ""));

				line = dio.readText();
			}

			dio.write("Upload complete.\n");
			return anomalies;
		}

		private List<Anomaly> JoinReports(List<AnomalyReport> reports) {
			List<Anomaly> anomalies = new ArrayList<>();
			for (int i = 0; i < reports.size(); i++) {
				AnomalyReport currReport = reports.get(i);
				long start = currReport.timeStep;
				long end = start;

				for (int j = i + 1; j < reports.size(); j++) {
					AnomalyReport nextReport = reports.get(j);
					if (
						currReport.timeStep + 1 == nextReport.timeStep &&
						currReport.description.equals(nextReport.description)
					) {
						i = j;
						end = nextReport.timeStep;
						currReport = nextReport;
					} else {
						break;
					}
				}

				anomalies.add(new Anomaly(start, end, currReport.description));
			}

			return anomalies;
		}
	}

	private class Anomaly { 
		public final long startTimeStep;
		public final long endTimeStep;
		public final String description;

		public Anomaly(long start, long end, String description) {
			this.startTimeStep = start;
			this.endTimeStep = end;
			this.description = description;
		}
	}

	public class ExitCommand extends Command{

		public ExitCommand() {
			super("exit");
		}

		@Override
		public void execute() {
			// implement
		}
	}
}