package org.svis.generator.hismo.s2m

import java.util.Set
import org.apache.commons.logging.LogFactory
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.svis.xtext.hismo.HISMOClassHistory
import org.svis.xtext.hismo.HISMOClassVersion
import org.svis.xtext.hismo.HISMONamespaceHistory
import org.svis.xtext.hismo.HismoDocument
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot
import org.apache.commons.beanutils.BeanComparator
import org.svis.generator.famix.Famix2Famix
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import com.datastax.driver.core.Session
import org.svis.xtext.hismo.impl.HismoFactoryImpl
import org.svis.xtext.hismo.HISMONamespaceVersion
import org.svis.generator.hismo.HismoUtils
import com.datastax.driver.core.querybuilder.QueryBuilder
import org.svis.xtext.hismo.HISMOIssue

class Cassandra2Hismo extends WorkflowComponentWithModelSlot {
	val hismoFactory = new HismoFactoryImpl()
	var HismoDocument hismoDocument
	val log = LogFactory::getLog(class)
	val famix = new Famix2Famix
	extension HismoUtils hismoutil = new HismoUtils
	val nameComparator = new BeanComparator("name")
	var Session session
	
	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		log.info("Cassandra2Hismo has started")
		
		val client = new CassandraConnector();
    	client.connect("127.0.0.1", 9042, "mrt" );
    	session = client.getSession();
    	
    	var hismoRoot = hismoFactory.createHismoRoot
		hismoDocument = hismoFactory.createHismoDocument
		hismoRoot.hismoDocument = hismoDocument

		val Set<HISMONamespaceHistory> namespaceHistories = newTreeSet(nameComparator)
    	val Set<HISMONamespaceVersion> namespaceVersions = newTreeSet(nameComparator)
    	val Set<HISMOClassHistory> classHistories = newTreeSet(nameComparator)
    	val Set<HISMOClassVersion> classVersions = newTreeSet(nameComparator)
    	
    	val query = "SELECT * FROM commits"
    	    	    
        session.execute(query).forEach[result|
            val filename = result.getString("filename")
            if(filename.endsWith(".java")) {
            	val packagename = result.getString("package_name")
            	val commitdate = result.getDate("commit_date").toString
            	val commitid = result.getString("commit_id")
            	val project = result.getString("project")
            	val addedLines = result.getInt("added_lines")
            	val removedLines = result.getInt("removed_lines")
            	//val id = result.getUUID("id").toString
            	
            	val realPackagename = project + "." + packagename
            	
            	val fqn = realPackagename + "." + filename
            	
            //	if(project == "project_a") {
       	
	            	val statement = QueryBuilder.select.all
	        			.from("issues")
	        			.allowFiltering
	        			.where(QueryBuilder::eq("filename", filename))
	        			.and(QueryBuilder::eq("project", project))
	        			.and(QueryBuilder::eq("package_name", packagename))
	        		
	        		var closedIncidents = 0;
	        		var openIncidents = 0;
	        		var openSecurityIncidents = 0;
	        		var closedSecurityIncidents = 0;
	        		var issueIds = newHashSet
	        		for(issueResult : session.execute(statement)) {
	            		val old =  issueResult.getBool("old")
	            		val security_relevant = issueResult.getBool("security_relevant")
	            		val id = issueResult.getString("issue_id")
	            		var newIssue = issueIds.add(id)
	            		if(newIssue){
	            			hismoDocument.elements += toIssue(id, !old, security_relevant)
	            		}
	            		if(old) {
	            			if(security_relevant) {
	            				closedSecurityIncidents++
	            			} else {
	            				closedIncidents++	
	            			}
	            		} else {
	            			if(security_relevant) {
	            				openSecurityIncidents++            				
	            			} else {
	            				openIncidents++
	            			}
	            		}
	            	}
	            	
	            	var projectHistory = namespaceHistories.findFirst[value == project]
	            	if(projectHistory === null) {
	            		projectHistory = toNamespaceHistory(project)
	            		namespaceHistories += projectHistory
	            	}
	            	var projectVersion = namespaceVersions.findFirst[value == project && commitId == commitid]
	            	if(projectVersion === null) {
	            		projectVersion = toNamespaceVersion(projectHistory, commitdate, commitid)
	            		namespaceVersions += projectVersion
	            	}
	            	var namespaceHistory = namespaceHistories.findFirst[value == realPackagename]
	            	if(namespaceHistory === null) {
	            		namespaceHistory = toNamespaceHistory(realPackagename)
	            		namespaceHistories += namespaceHistory
	            	}
	            	var namespaceVersion = namespaceVersions.findFirst[value == realPackagename]
	            	if(namespaceVersion === null) {
	            		namespaceVersion = toNamespaceVersion(namespaceHistory, commitdate, commitid)
	            		namespaceVersions += namespaceVersion
	            	}
	            	var classHistory = classHistories.findFirst[value == fqn]
	            	if(classHistory === null) {
	            		classHistory = toClassHistory(namespaceHistory, fqn, realPackagename, commitdate, closedIncidents, closedSecurityIncidents, openIncidents, openSecurityIncidents)
	            		classHistories += classHistory
	            	}
	            	var classVersion = classVersions.findFirst[value == packagename && commitId == commitid]
	            	if(classVersion === null) {
	            		classVersion = toClassVersion(classHistory, fqn, commitid, commitdate, addedLines - removedLines)
	            		(classVersion.parentHistory.ref as HISMOClassHistory).issues += issueIds
	            		classVersions += classVersion
	            	}
	            }
           // }
        ]
        
        client.close
        hismoDocument.elements.filter(HISMONamespaceHistory).toList.forEach[createParent(it.value)]
        hismoDocument.elements.filter(HISMOClassHistory).toList.forEach[history|
        	val uniqueIssues = history.issues.toSet
        	history.issues.clear
        	history.issues += uniqueIssues
        ]
        updateParents
        setParentVersion
		
		val resource = new ResourceImpl()
		var hismoList = newArrayList
		hismoList += hismoRoot
		resource.contents += hismoList
		ctx.set("hismo", hismoList)
		ctx.set("metadata", resource)
			
		log.info("Cassandra2Hismo has finished.")
	}
	
	def void createParent(String fqn) {
		val index = fqn.lastIndexOf(".")
		if(index < 0) return
		val parentFQN = fqn.substring(0, index)
		var parentHistory = hismoDocument.elements.filter(HISMONamespaceHistory).findFirst[value == parentFQN]
		if(parentHistory === null) {
			createParent(parentFQN)
			parentHistory = toNamespaceHistory(parentFQN)
			toNamespaceVersion(parentHistory, "foobar", "0")
		}
	}
	
	def void updateParents() {
		hismoDocument.elements.filter(HISMONamespaceHistory).forEach[namespaceHistory|
			val index = namespaceHistory.value.lastIndexOf(".")
			if(index < 0) return
			val parentFQN = namespaceHistory.value.substring(0, index)
			var parentHistory = hismoDocument.elements.filter(HISMONamespaceHistory).findFirst[value == parentFQN]
			namespaceHistory.containingNamespaceHistory = parentHistory.createReference
		]
	}
	
	def void setParentVersion() {
		hismoDocument.elements.filter(HISMOClassVersion).forEach[classVersion|
			val history = classVersion.parentHistory.ref as HISMOClassHistory
			classVersion.container = (history.containingNamespaceHistory.ref as HISMONamespaceHistory)
			.namespaceVersions.map[ref as HISMONamespaceVersion].last.createReference
		]
		hismoDocument.elements.filter(HISMONamespaceVersion).forEach[namespaceVersion|
			val history = namespaceVersion.parentHistory.ref as HISMONamespaceHistory
			if(history.containingNamespaceHistory !== null) {
				namespaceVersion.container = (history.containingNamespaceHistory.ref as HISMONamespaceHistory)
				.namespaceVersions.map[ref as HISMONamespaceVersion].last.createReference
			}
		]
	}
	
	def HISMOIssue toIssue(String id, boolean open, boolean security) {
		val issue = hismoFactory.createHISMOIssue 
		issue.value = id
		issue.open = open.toString
		issue.security = security.toString
		return issue
	}
	
	def HISMONamespaceHistory toNamespaceHistory(String fqn) {
		val namespaceHistory = hismoFactory.createHISMONamespaceHistory 
		namespaceHistory.value = fqn
		namespaceHistory.name = famix.createID(fqn)
		hismoDocument.elements += namespaceHistory
		return namespaceHistory
	}
	
	def HISMONamespaceVersion toNamespaceVersion(HISMONamespaceHistory history, String commit, String commitid) {
		val namespaceVersion = hismoFactory.createHISMONamespaceVersion
		namespaceVersion.timestamp = commit
		namespaceVersion.commitId = commitid
		namespaceVersion.name = history.value
		namespaceVersion.id = famix.createID(namespaceVersion.name)
		namespaceVersion.value =  history.value
		namespaceVersion.parentHistory = history.createReference
		
		history.namespaceVersions += namespaceVersion.createReference

		hismoDocument.elements += namespaceVersion

		return namespaceVersion
	}
	
	def private HISMOClassHistory toClassHistory(HISMONamespaceHistory history, String fqn, String packagename, String commitdate, 
		int closedIncidents, int closedSecurityIncidents, int openIncidents, int openSecurityIncidents) {
		val classHistory = hismoFactory.createHISMOClassHistory
		classHistory.value = fqn
		classHistory.name = famix.createID(fqn + commitdate)
		
		history.classHistories += classHistory.createReference
		classHistory.containingNamespaceHistory = history.createReference
		history.classHistories += classHistory.createReference
		
		switch (openIncidents) {
			case openIncidents < 0: classHistory.avgNumberOfOpenIncidents = 0
			default: classHistory.avgNumberOfOpenIncidents = openIncidents
		}
		
		switch (closedIncidents) {
			case closedIncidents < 0: classHistory.avgNumberOfClosedIncidents = 0
			default: classHistory.avgNumberOfClosedIncidents = closedIncidents
		}
		
		classHistory.numberOfOpenSecurityIncidents = openSecurityIncidents
		classHistory.numberOfClosedSecurityIncidents = closedSecurityIncidents

		hismoDocument.elements += classHistory
		
		return classHistory
	}
	
	def private HISMOClassVersion toClassVersion(HISMOClassHistory history, String fqn, String commitid, String commitdate, int numberOfStatements) {
		val classVersion =  hismoFactory.createHISMOClassVersion
		classVersion.timestamp = commitdate
		classVersion.commitId = commitid
		classVersion.name = fqn
		classVersion.id = famix.createID(fqn)
		classVersion.value = fqn
		classVersion.evolutionNumberOfStatements = numberOfStatements
		classVersion.parentHistory = history.createReference
		
		history.classVersions += classVersion.createReference

		hismoDocument.elements += classVersion
		
		return classVersion
	}	
}