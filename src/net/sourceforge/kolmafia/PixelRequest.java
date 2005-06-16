/**
 * Copyright (c) 2005, KoLmafia development team
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
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
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

package net.sourceforge.kolmafia;

public class PixelRequest extends ItemCreationRequest
{
	public static final int WHITE_PIXEL = 459;
	public static final int BLACK_PIXEL = 460;
	public static final int RED_PIXEL = 461;
	public static final int GREEN_PIXEL = 462;
	public static final int BLUE_PIXEL = 463;

	private AdventureResult [] ingredientCosts;

	public PixelRequest( KoLmafia client, int itemID, int quantityNeeded )
	{
		super( client, "town_wrong.php", itemID, quantityNeeded );

		ingredientCosts = ConcoctionsDatabase.getIngredients( itemID );
		if ( ingredientCosts != null )
			for ( int i = 0; i < ingredientCosts.length; ++i )
				ingredientCosts[i] = ingredientCosts[i].getNegation();

		addFormField( "action", "makepixel" );
		addFormField( "makewhich", String.valueOf( itemID ) );
	}

	public void run()
	{
		AdventureResult singleCreation = new AdventureResult( getItemID(), 1 );

		for ( int i = 1; i <= getQuantityNeeded(); ++i )
		{
			// Disable controls
			updateDisplay( DISABLED_STATE, "Creating " + getDisplayName() + " (" + i + " of " + getQuantityNeeded() + ")..." );

			// Run the request
			super.run();

			// Account for the results
			client.processResult( singleCreation );
			for ( int j = 0; j < ingredientCosts.length; ++j )
				client.processResult( ingredientCosts[j] );
		}
	}
}
