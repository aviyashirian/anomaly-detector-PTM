package test;


public class StatLib {
	// simple average
	public static float avg(float[] x) {
		float sum = 0;

		for (float f : x) {
			sum += f;
		}

		return sum / x.length;
	}

	// returns the variance of X and Y
	public static float var(float[] x) {
		float avg = StatLib.avg(x);

		float squaredDiff = 0;
		for (float f : x)
			squaredDiff += (f - avg) * (f - avg);

		return squaredDiff / x.length;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float avg_x = StatLib.avg(x);
		float avg_y = StatLib.avg(y);
		int n = x.length;

		float diff_sum = 0;
		for(int i=0; i<n; i++)
			diff_sum += (x[i] - avg_x) * (y[i] - avg_y);

		return diff_sum / n;
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		double dev_x = Math.sqrt(StatLib.var(x));
		double dev_y = Math.sqrt(StatLib.var(y));

		return StatLib.cov(x, y) / (float)(dev_x * dev_y);
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float[] x = new float[points.length];
		float[] y = new float[points.length];
		for (int i = 0; i < points.length; i++) {
			x[i] = points[i].x;
			y[i] = points[i].y;
		}

		float a = StatLib.cov(x, y) / StatLib.var(x);
		float b = StatLib.avg(y) - a * StatLib.avg(x);
		return new Line(a, b);
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		return 0;
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		return 0;
	}
	
}
