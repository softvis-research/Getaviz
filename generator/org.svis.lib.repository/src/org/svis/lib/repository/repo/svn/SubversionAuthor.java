package org.svis.lib.repository.repo.svn;

import org.svis.lib.repository.repo.api.Author;

public class SubversionAuthor implements Author {
	
	private String author;

	public SubversionAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getName() {
		return author;
	}

	@Override
	public String getEmailAddress() {
		return null;
	}
}