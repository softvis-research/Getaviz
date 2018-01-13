package org.svis.lib.repository.repo.git.util;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.svis.lib.repository.repo.util.ProgressBarPrinter;

public class GitProgressMonitor extends BatchingProgressMonitor implements ProgressMonitor {

	private ProgressBarPrinter progressBarPrinter;
	private AtomicLong totalRecievedObjects = new AtomicLong(0);
	private AtomicLong totalResolvedDeltas = new AtomicLong(0);

	public GitProgressMonitor() {
		progressBarPrinter = new ProgressBarPrinter(200L);
	}
	
	@Override
	protected void onUpdate(String taskName, int workCurr) {}

	@Override
	protected void onEndTask(String taskName, int workCurr) {}

	@Override
	protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
		refreshTaskState(taskName, percentDone); 
	}

	private void refreshTaskState(String taskName, int percentDone) {
		if(taskName.equals("Receiving objects")){
			totalRecievedObjects.set(percentDone);
			progressBarPrinter.printProgressBar(getTotalPercentage());
		} else if(taskName.equals("Resolving deltas")){
			totalResolvedDeltas.set(percentDone);
			progressBarPrinter.printProgressBar(getTotalPercentage());
		}
	}

	private synchronized long getTotalPercentage() {
		return totalRecievedObjects.get() + totalResolvedDeltas.get();
	}

	@Override
	protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
		refreshTaskState(taskName, percentDone);
	}
	
}
