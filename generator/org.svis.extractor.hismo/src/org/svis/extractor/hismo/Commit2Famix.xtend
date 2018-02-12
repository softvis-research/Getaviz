package org.svis.extractor.hismo

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.lib.repository.repo.api.Repository
import org.svis.lib.repository.repo.api.exception.BranchNotAvailableException
import org.svis.lib.repository.repo.api.exception.CommitsNotAvailableException
import fr.inria.verveine.extractor.java.VerveineJParser
import java.io.File
import org.svis.generator.WorkflowComponentWithConfig
import java.io.PrintStream
import org.apache.commons.io.output.NullOutputStream
import java.util.List
import org.svis.lib.repository.repo.api.Commit

class Commit2Famix extends WorkflowComponentWithConfig {
	
	override invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Commit2Famix has started.")
		if (!config.recreateFamix) {
			log.info("According to the configuration, no famix models are recreated.")
			return
		}

		val repository =  ctx.get("repository") as Repository

		try {
			val List<Commit> commits = newArrayList 
			if (config.commits.empty) {
				commits.addAll(repository.defaultBranch.getCommits(config.numberOfCommits))
				config.commits = commits.map[commitId.idRepresentation]
			} else {
				config.commits.forEach[id|
					commits.add(repository.defaultBranch.getCommit(id))
				]
			}
			val targetPath = config.outputDirectoryFamix
			
			// use null output stream to prevent VerveineJ from spamming to console			
			val tmp = System.out
  			System.setOut = new PrintStream(new NullOutputStream())
//  			config.commits = commits
  			config.toJSON("output/" + config.repositoryOwner + "_" + config.repositoryName + "/config.json")
			commits.forEach[commit, index|
				val dirFile = new File(targetPath + commit.commitId.idRepresentation)
				if(!dirFile.exists){
					dirFile.mkdir
				}
				val famixModel = targetPath + commit.commitId.idRepresentation + "/" + commit.commitId.idRepresentation + ".famix"
				val sourcePath =  repository.path
				
				if(repository.checkout(commit)) {
					log.info("Checkout commit (" + (index + 1) + "/" + config.commits.size +")")
					log.info("VerveineJ has started.")
					log.info(famixModel)
					if (new File(famixModel).exists) {
						log.info("FamixModel already exist. Skip Commit.")
					} else {
						VerveineJParser::main(#["-o", famixModel, sourcePath])
					}
					log.info("VerveineJ has finished.")
					config.commit = commit.commitId.idRepresentation
					config.language = "java"
					config.commitOrder = index
					val jsonPath = targetPath + commit.commitId.idRepresentation + "/" + "config.json"
					config.toJSON(jsonPath, false)
				} else {
					log.info("Commit (" + (index + 1) + "/" + config.commits.size +") skipped.")
				}
			]
			repository.reset
			System.setOut = tmp
			
		} catch (CommitsNotAvailableException e) {
			log.error(e.message)
		} catch (BranchNotAvailableException e) {
			log.error(e.message)
		}
		log.info("Commit2Famix has finished")
	}
}