/**
 * Copyright (c) 2005-2018, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia" nor the names of its contributors may
 *      be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia.textui;

import org.luaj.vm2.LuaError;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.utilities.CharacterEntities;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.textui.RuntimeLibrary;
import java.io.BufferedReader;
import java.io.File;

import net.sourceforge.kolmafia.utilities.FileUtilities;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.LuaValue;

public class LuaInterpreter
{
  public static final LuaValue execute( final String script ) {
    Globals globals = JsePlatform.standardGlobals();
    globals.STDOUT = RequestLogger.INSTANCE;

    globals.set( "kol", CoerceJavaToLua.coerce( LuaRuntimeLibrary.class ) );

    LuaValue chunk = globals.load( script );

    try
    {
      return chunk.call();
    }
    catch ( LuaError e )
    {
			String message = CharacterEntities.escape( e.getMessage() );
			KoLmafia.updateDisplay( MafiaState.ERROR, message );
			return LuaValue.NONE;
    }
  }

  public static final LuaValue execute( final File scriptFile ) {
    BufferedReader reader = FileUtilities.getReader( scriptFile );
    String line;
    StringBuilder script = new StringBuilder();

    while ( (line = FileUtilities.readLine( reader )) != null )
    {
      script.append( line );
      script.append( KoLConstants.LINE_BREAK );
    }

    return LuaInterpreter.execute( script.toString() );
  }
}
