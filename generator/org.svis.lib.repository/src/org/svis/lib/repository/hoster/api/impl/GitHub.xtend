package org.svis.lib.repository.hoster.api.impl

import java.util.List
import org.eclipse.egit.github.core.SearchRepository
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService
import com.google.common.collect.ImmutableList
import org.eclipse.xtend.lib.annotations.Accessors
import java.text.SimpleDateFormat
import java.util.Calendar
import org.json.simple.JSONObject
import java.io.FileWriter
import java.io.File
import org.svis.lib.repository.hoster.api.Hoster
import org.svis.lib.repository.repo.api.impl.RepositoryFactorys.RepositoryType

public class GitHub implements Hoster {
    @Accessors String language = "java"
    @Accessors int numberOfCommits = 100
    var List<SearchRepository> repos
    val static sortingOptions = ImmutableList.of("stars", "forks")//, "updated")
    val client = new GitHubClient()
    
   	/**
     * The used token only has access to public repositories.
     * If you would like to do something else with it, create your own token.
     * 
     * @see https://github.com/settings/tokens/new
     */
    
    new(String language, int numberOfCommits) {
     	 
        client.OAuth2Token = "6b5eefe7865a6744c40b91de1cc7cacec7de9a4b"
        
        this.language = language
        this.numberOfCommits = numberOfCommits 
        
        val dirFile = new File("output/")
		if(!dirFile.exists) {
			dirFile.mkdir
		}
    }
    
    override getRepositories(int NOR) {
    	val result = newLinkedList
    	sortingOptions.forEach[sort|
    		result += getRepositories(NOR/sortingOptions.size, sort)
    	]
    	return result
    	}
    	
    def getRepositories(int NOR, String sort) {
    	val result = newLinkedList
    	val repositoryService = new RepositoryService(client)
    	val pageSize = 100 // defined by egit github

    	for(page : 0..< (NOR/pageSize + 1)) {
         	val map = newHashMap
        	map.put("language", language)
        	map.put("sort", sort)
//        	map.put("order", order) // causes trouble for unknown reason
        	repos = repositoryService.searchRepositories(map,page)
        	
    	   	for (var i = 0 ; (i < repos.size) && (i < (NOR - page * 100)); i++) {
    	   		val repo = repos.get(i)
    	   		
    	   		val name = repo.owner + "_" + repo.name
    	   		
    	   		val repoDir = new File("output/" + name)
    	   		if(!repoDir.exists) {
    	   			repoDir.mkdir
    	   		}
    	   		
        		val json = new JSONObject()
				json.put("url", repo.url)
				json.put("name", repo.name)
				json.put("owner", repo.owner)
				json.put("type", RepositoryType::GIT.name)
				json.put("startDate", "2016-06-01 00:00:00")
				json.put("endDate",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar::instance.time))
				json.put("numberOfCommits", numberOfCommits)
				
				val path = repoDir + "/config.json"
				if (!new File(path).exists) {
					val file = new FileWriter(path)
					file.write(json.toJSONString)
					file.flush
					file.close
				}
				result += name
	    	}
         }
         return result
    }
}
