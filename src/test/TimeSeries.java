package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSeries {
	private List<String> fields;
	private Map<String, List<Float>> time_steps;
	
	public TimeSeries(String csvFileName) {
		this.fields = new ArrayList<>();
		this.time_steps = new HashMap<>();

		try (BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName))) {

			String row = csvReader.readLine();
			if(row != null) {
				for (String field : row.split(",")) {
					this.fields.add(field);
					this.time_steps.put(field, new ArrayList<Float>());
				}
			}

			while ((row = csvReader.readLine()) != null) {
				String[] row_values = row.split(",");
				for (int i = 0; i < row_values.length; i++) {
					this.time_steps.get(this.fields.get(i)).add(Float.parseFloat(row_values[i]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getFieldNames() {
		return this.fields;
	}

	public float[] getColumn(int index) {
		return this.getColumn(this.fields.get(index));
	}

	public float[] getColumn(String feature) {
		List<Float> list = this.time_steps.get(feature);
		float[] arr = new float[list.size()];
		for(int i = 0; i < list.size(); i++) arr[i] = list.get(i);

		return arr;
	}

	public Point[] getPoints(int feature1, int feature2) {
		float[] x = this.getColumn(feature1);
		float[] y = this.getColumn(feature2);
		Point[] points = new Point[x.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(x[i], y[i]);
		}

		return points;
	}
}
