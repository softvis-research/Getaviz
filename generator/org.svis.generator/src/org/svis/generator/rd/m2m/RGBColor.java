package org.svis.generator.rd.m2m;

public class RGBColor {
	private final double lowerBound =0;
	private final double upperBound =255;

	public RGBColor() {
		super();
		this.r =0;
		this.g =0;
		this.b =0;
	}

	public RGBColor(double r, double g, double b) {
		super();
		this.r = checkRanges(r);
		this.g = checkRanges(g);
		this.b = checkRanges(b);
	}
	private double r;
	private double g;
	private double b;
	
	private double checkRanges(double value){
		if(value>upperBound){
			return upperBound;
		}else if(value<lowerBound){
			return lowerBound;
		}else{
			return value;
		}
	}

	public double r() {
		return r;
	}

	public void r(double r) {
		this.r = checkRanges(r);
	}

	public double g() {
		return g;
	}

	public void g(double g) {
		this.g = checkRanges(g);
	}

	public double b() {
		return b;
	}

	public void b(double b) {
		this.b = checkRanges(b);
	}

	public String asPercentage() {
		//modified for X3D: RGB%
		double r_mod = r/255;
		double g_mod = g/255;
		double b_mod = b/255;
		return (r_mod+ " " +g_mod+ " " +b_mod);
	}
}
