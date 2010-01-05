/**
 * Copyright 2009 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://www.threecrickets.com/
 */

package com.threecrickets.scripturian.internal;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.threecrickets.scripturian.Document;
import com.threecrickets.scripturian.ScriptletParsingHelper;
import com.threecrickets.scripturian.Scripturian;
import com.threecrickets.scripturian.exception.DocumentCompilationException;
import com.threecrickets.scripturian.exception.DocumentInitializationException;

/**
 * @author Tal Liron
 * @see Document
 */
public class DocumentSegment
{
	//
	// Construction
	//

	public DocumentSegment( String text, boolean isScriptlet, String scriptEngineName )
	{
		this.text = text;
		this.isScriptlet = isScriptlet;
		this.scriptEngineName = scriptEngineName;
	}

	//
	// Attributes
	//

	public String text;

	public CompiledScript compiledScript;

	public final boolean isScriptlet;

	public final String scriptEngineName;

	public void resolve( Document document, ScriptEngineManager scriptEngineManager, boolean allowCompilation ) throws DocumentInitializationException
	{
		ScriptEngine scriptEngine = scriptEngineManager.getEngineByName( scriptEngineName );
		if( scriptEngine == null )
			throw DocumentInitializationException.scriptEngineNotFound( document.getName(), scriptEngineName );

		ScriptletParsingHelper scriptletParsingHelper = Scripturian.scriptletParsingHelpers.get( scriptEngineName );
		if( scriptletParsingHelper == null )
			throw DocumentInitializationException.scriptletParsingHelperNotFound( document.getName(), scriptEngineName );

		// Add header
		String header = scriptletParsingHelper.getScriptletHeader( document, scriptEngine );
		if( header != null )
			text = header + text;

		// Add footer
		String footer = scriptletParsingHelper.getScriptletFooter( document, scriptEngine );
		if( footer != null )
			text += footer;

		if( allowCompilation && ( scriptEngine instanceof Compilable ) && scriptletParsingHelper.isCompilable() )
		{
			try
			{
				compiledScript = ( (Compilable) scriptEngine ).compile( text );
			}
			catch( ScriptException x )
			{
				throw new DocumentCompilationException( document.getName(), "Compilation error in " + scriptEngineName, x );
			}
		}
	}
}