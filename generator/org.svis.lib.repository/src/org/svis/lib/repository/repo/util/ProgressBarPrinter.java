package org.svis.lib.repository.repo.util;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.svis.lib.repository.repo.api.Constants;

public class ProgressBarPrinter {

	private static NumberFormat percentInstance = DecimalFormat.getPercentInstance(Locale.ENGLISH);
	
	static {
		percentInstance.setMaximumFractionDigits(1);
	}
	
	private long latestRevision;
	private PrintStream out;

	public ProgressBarPrinter() {
		this(100);
	}
	
	public ProgressBarPrinter(long latestRevision) {
		this(latestRevision, Constants.messageOutputStream);
	}
	
	public ProgressBarPrinter(long latestRevision, PrintStream out) {
		this.latestRevision = latestRevision;
		this.out = out;
		printProgressBar(0L);
	}

	public void printProgressBar(long current) {
		double l =  current * 1.0 / latestRevision;
		printProgressBar(l);
	}
	
	private void printProgressBar(double l) {
		String percentage = percentInstance.format(l);
		out.print(createProgressBar((int)(l * 100d)) + "   " + percentage + "     \r");
		
	}

	String createProgressBar(int bars){
		StringBuilder builder = new StringBuilder(102);
		builder.append('|');
		for(int i = 0; i < 100; i++){
			if(i < bars){
				builder.append("=");
			} else {
				builder.append(" ");
			}
		}
		builder.append("|");
		return builder.toString();
	}

}
