package org.svis.lib.repository.repo.util;

import static org.svis.lib.repository.repo.api.Constants.TEMP_DIR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

public class TmpDirCreator {

	private String dirPath;

	public TmpDirCreator(String dirPath) {
		this.dirPath = dirPath;
	}
	
	public File getLocalTempDir() {
		File returnable = new File(TEMP_DIR.getAbsolutePath() + "/" + DigestUtils.md5Hex(dirPath));
		return returnable;
	}
	
	public void writeIdFileToTempDir(){
		File file = new File(getLocalTempDir() + "/id.txt");
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new PrintWriter(file));
			bw.write(dirPath);
		} catch (IOException e){
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(bw);
		}
	}
	
	
}
