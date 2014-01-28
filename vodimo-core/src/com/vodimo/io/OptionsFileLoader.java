package com.vodimo.io;

import com.vodimo.core.util.VodimoDataBase;

public abstract class OptionsFileLoader {

	protected VodimoDataBase db;
	protected IOptionsReader reader;
	
	public OptionsFileLoader(VodimoDataBase db, IOptionsReader reader) {
		this.db = db;
		this.reader = reader;
	}
	
	public abstract void load() throws Exception;
		
}
