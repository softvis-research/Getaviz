package org.svis.lib.repository.hoster.api

import java.util.List

/**
 * Interface for accessing Repositories
 * 
 * Interface for accessing Repositories from a service like github. 
 */

public interface Hoster {
    def void setLanguage(String language)
    def String getLanguage()
    def List<String> getRepositories(int NOR)
}
