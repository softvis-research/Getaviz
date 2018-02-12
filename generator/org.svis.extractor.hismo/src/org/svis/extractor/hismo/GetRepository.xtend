package org.	svis.extractor.hismo

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.lib.repository.repo.api.Constants
import org.svis.lib.repository.repo.api.impl.RepositoryFactorys
import org.svis.generator.WorkflowComponentWithConfig

import static org.svis.lib.repository.repo.api.Constants.*

class GetRepository extends WorkflowComponentWithConfig {
	
		override invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
			log.info("GetGitRepository has started.")
			Constants::CACHED = true
			val repoFromUrl = RepositoryFactorys.createRepoFromUrl(config.repositoryUrl, config.repositoryType)
			ctx.set("repository", repoFromUrl)
			log.info("GetGitRepository has finished.")
	}
}