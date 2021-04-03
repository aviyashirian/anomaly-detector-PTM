package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	private List<CorrelatedFeatures> correlated_features = new ArrayList<CorrelatedFeatures>();

	@Override
	public void learnNormal(TimeSeries ts) {
		int n_fields = ts.getFieldNames().size();

		for (int f1 = 0; f1 < n_fields - 1; f1++) {
			int f2 = f1 + 1; 
			float best_pearson = 0;

			for (int i = f1 + 1; i < n_fields; i++) {
				float pearson = Math.abs(StatLib.pearson(ts.getColumn(f1), ts.getColumn(i)));
				if (pearson > best_pearson) {
					best_pearson = pearson;
					f2 = i;
				}
			}

			if (best_pearson >= 0.9) {
				Point[] points = ts.getPoints(f1, f2);
				Line line = StatLib.linear_reg(points);

				this.correlated_features.add(
					new CorrelatedFeatures(
						ts.getFieldNames().get(f1), 
						ts.getFieldNames().get(f2), 
						best_pearson, 
						line,
						this.calcThreshold(points, line)
					)
				);
			}
		}
	}

	private float calcThreshold(Point[] pts,Line l){
		float max_dev=0;
		for (Point point : pts) {
			float dev = StatLib.dev(point, l);
			if(dev > max_dev)
				max_dev=dev;
		}
		return max_dev;
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		return null;
	}
	
	public List<CorrelatedFeatures> getNormalModel(){
		return this.correlated_features;
	}
}
