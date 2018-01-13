package org.svis.lib.repository.repo.api.impl;

import org.svis.lib.repository.repo.api.VersionedFile;

public class ByteArrayVersionedFile implements VersionedFile {

	private byte[] returnable;

	public ByteArrayVersionedFile(byte[] returnable) {
		this.returnable = returnable;
	}
	
	@Override
	public byte[] getContent() {
		return returnable;
	}

}
