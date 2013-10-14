package com.vodimo.io;

import java.io.Reader;

import com.vodimo.core.util.VodimoDataBase;

public interface IOptionsReader {
	
	public void read(Reader reader, VodimoDataBase db) throws Exception;
	
}
