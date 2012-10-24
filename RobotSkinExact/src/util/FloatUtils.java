package util;

public class FloatUtils {
	private static double tolerance=10e-6;

	public static double getTolerance() {
		return tolerance;
	}

	public static void setTolerance(double tolerance) {
		FloatUtils.tolerance = tolerance;
	}
	
	public static boolean le(double a, double b){
		return a+tolerance < b;
	}

	public static boolean gr(double a, double b){
		return a-tolerance > b;
	}

	public static boolean leq(double a, double b){
		return !gr(a,b);
	}

	public static boolean geq(double a, double b){
		return !le(a,b);
	}

	public static boolean eq(double a, double b){
		return b-a < tolerance && a-b<tolerance;
	}

	public static int roundInt(double a){
		return (int) Math.floor(a + 0.5);
	}
	
	

}
