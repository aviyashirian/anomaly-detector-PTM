package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<>();

		commands.add(c.new UploadCsvCommand());
		commands.add(c.new AlgorithmSettingCommand());
		commands.add(c.new DetectAnomaliesCommand());
		commands.add(c.new DisplayResultsCommand());
		commands.add(c.new UploadAndAnalyzeCommand());
		commands.add(c.new ExitCommand());
	}
	
	public void start() {
		this.printCommands();

		float selection = Float.parseFloat(dio.readText());

		while (selection > 0 && selection <= this.commands.size() && this.commands.get((int)selection - 1).description != "exit") {
			try {
				this.commands.get((int)selection - 1).execute();
			} catch (Exception e) {
				this.dio.write("Error in input, please try again");
			}

			this.printCommands();
			selection = dio.readVal();
		}
	}

	private void printCommands() {
		dio.write("Welcome to the Anomaly Detection Server.\nPlease choose an option:\n");

		for (int i = 0; i < this.commands.size(); i++) {
			dio.write(i+1 + ". " + this.commands.get(i).description + "\n");
		}
	}
}
