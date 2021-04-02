package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TimeSeries {
	public String[] field_names;
	public List<float[]> values;
	
	public TimeSeries(String csvFileName) {
		try (BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName))) {

			String row = csvReader.readLine();
			if(row != null) {
				this.field_names = row.split(",");
				row = csvReader.readLine();
			}

			this.values = new ArrayList<float[]>();
			while (row != null) {
				String[] data = row.split(",");
				float[] row_values = new float[data.length];

				for (int i = 0; i < data.length; i++) {
					row_values[i] = Float.parseFloat(data[i]);
				}

				this.values.add(row_values);

				row = csvReader.readLine();
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
