package org.svis.generator.plant.m2t;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;

public class FileManager implements IWorkflowComponent {

	private String trash;
	private String source;
	private String destiny;

	@Override
	public void invoke(IWorkflowContext arg0) {
		// TODO Auto-generated method stub
//		System.out.println("source:" + source);
//		System.out.println("destiny:" + destiny);

		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		s = s.replace(trash, "");
//		System.out.println("real source: " + s + source);
//		System.out.println("real destiny: " + s + destiny);

		// create folder if not exist:
		File f = new File(s + destiny);
//		System.out.println(f.getParent());
		new File(f.getParent()).mkdirs();

		// copy texture from source to desteny:
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(s + source);
			os = new FileOutputStream(s + destiny);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} catch (Exception e) {
		} finally {
			try {
				if(is != null)
				is.close();
				if(os != null)
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void postInvoke() {
		// TODO Auto-generated method stub
	}

	@Override
	public void preInvoke() {
		// TODO Auto-generated method stub
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestiny() {
		return destiny;
	}

	public void setDestiny(String destiny) {
		this.destiny = destiny;
	}

	public String getTrash() {
		return trash;
	}

	public void setTrash(String trash) {
		this.trash = trash;
	}
}
