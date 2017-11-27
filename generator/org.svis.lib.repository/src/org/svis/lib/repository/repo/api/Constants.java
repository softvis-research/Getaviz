package org.svis.lib.repository.repo.api;

import java.io.File;
import java.io.PrintStream;

/**
 * Created by Dan Häberlein
 */
public class Constants {
    private static final String GIT_DEFAULT_BRANCH_PROPNAME = "git.default_branch";
    private static final String SVN_DEFAULT_BRANCH_PROPNAME = "svn.default_branch";

    public static final String TAGS = "tags";
    public static final String BRANCHES = "branches";
	public static final String TRUNK = "trunk";
    
	public static final String HTTP_SCHEME = "http";
	public static final String HTTPS_SCHEME = "https";
	public static final String SVN_SCHEME = "svn";
	public static final String SVN_PLUS_SSH_SCHEME = "svn+ssh";
	public static final String FILE_SCHEME = "file";
	public static final String GIT_SCHEME = "git";
	
	public static char SEPARATOR = getFromSystemProperties("separator", ";").charAt(0);
	public static boolean CACHED = getFromSystemProperties("cached", "false").equals("true");
	
    public static String GIT_DEFAULT_BRANCH = getFromSystemProperties(GIT_DEFAULT_BRANCH_PROPNAME, "master");// "master");
    public static String SVN_DEFAULT_BRANCH = getFromSystemProperties(SVN_DEFAULT_BRANCH_PROPNAME, "trunk");
    
    public static long ABORT_THRESHOLD = Long.parseLong(getFromSystemProperties("svn.sec_kill_after_no_cache_progress", "60")) * 1000L;
    
    public static File TEMP_DIR = new File("Temp" + "/versioning");
    
    public static PrintStream messageOutputStream = System.out;

    private static String getFromSystemProperties(String propertyName, String defaultValue){
        String property = System.getProperty(propertyName);
        return property == null ? defaultValue : property;
    }
}
