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
import java.util.StringTokenizer;

public class AutoSellRequest extends SendMessageRequest
{
	private int sellType;

	private int [] prices;
	private int [] limits;

	public static final int AUTOSELL = 1;
	public static final int AUTOMALL = 2;

	public AutoSellRequest( KoLmafia client, AdventureResult item )
	{	this( client, new AdventureResult [] { item }, AUTOSELL );
	}

	public AutoSellRequest( KoLmafia client, AdventureResult item, int price, int limit )
	{	this( client, new AdventureResult [] { item }, new int [] { price }, new int [] { limit }, AUTOMALL );
	}

	public AutoSellRequest( KoLmafia client, Object [] items, int sellType )
	{
		this( client, items, new int[0], new int[0], sellType );
	}

	public AutoSellRequest( KoLmafia client, Object [] items, int [] prices, int [] limits, int sellType )
	{
		super( client, sellType == AUTOSELL ? "sellstuff.php" : "managestore.php", items, 0 );
		addFormField( "pwd", client.getPasswordHash() );

		this.sellType = sellType;
		this.prices = new int[ prices.length ];
		this.limits = new int[ prices.length ];

		if ( sellType == AUTOMALL )
		{
			addFormField( "action", "additem" );

			this.quantityField = "qty";

			for ( int i = 0; i < prices.length; ++i )
			{
				this.prices[i] = prices[i];
				this.limits[i] = limits[i];
			}
		}
	}

	protected void attachItem( AdventureResult item, int index )
	{
		if ( sellType == AUTOMALL )
		{
			addFormField( "item" + index, String.valueOf( item.getItemID() ) );
			addFormField( quantityField + index, String.valueOf( item.getCount() ) );

			if ( prices.length == 0 )
			{
				addFormField( "price" + index, "999999999" );
				addFormField( "limit" + index, "0" );
			}
			else
			{
				addFormField( "price" + index, String.valueOf(
					Math.max( Math.max( TradeableItemDatabase.getPriceByID( item.getItemID() ), 100 ), prices[ index - 1 ] ) ) );
				addFormField( "limit" + index, String.valueOf( limits[ index - 1 ] ) );
			}
		}
		else
		{
			addFormField( "whichitem[]", String.valueOf( item.getItemID() ) );
			addFormField( "action", "sell" );
			addFormField( "type", "quant" );
			addFormField( "howmany", String.valueOf( item.getCount() ) );
		}
	}

	protected int getCapacity()
	{	return sellType == AUTOSELL ? 1 : 11;
	}

	protected void repeat( Object [] attachments )
	{
		int [] prices = new int[ this.prices.length == 0 ? 0 : attachments.length ];
		int [] limits = new int[ this.prices.length == 0 ? 0 : attachments.length ];

		for ( int i = 0; i < prices.length; ++i )
		{
			for ( int j = 0; j < this.attachments.length; ++j )
				if ( attachments[i].equals( this.attachments[j] ) )
				{
					prices[i] = this.prices[i];
					limits[i] = this.limits[i];
				}
		}

		(new AutoSellRequest( client, attachments, limits, prices, sellType )).run();
	}

	/**
	 * Executes the <code>AutoSellRequest</code>.  This will automatically
	 * sell the item for its autosell value and update the client with
	 * the needed information.
	 */

	public void run()
	{
		updateDisplay( DISABLED_STATE, ( sellType == AUTOSELL ) ? "Autoselling items..." : "Placing items in the mall..." );

		super.run();

		// If an error state occurred, return from this
		// request, since there's no content to parse

		if ( isErrorState || responseCode != 200 )
			return;

		// Otherwise, update the client with the information stating that you
		// sold all the items of the given time, and acquired a certain amount
		// of meat from the recipient.

		if ( sellType == AUTOSELL )
		{
			String plainTextResult = responseText.replaceAll( "<.*?>", "" );
			StringTokenizer parsedResults = new StringTokenizer( plainTextResult, " " );

			try
			{
				while ( !parsedResults.nextToken().equals( "for" ) );

				int amount = df.parse( parsedResults.nextToken() ).intValue();
				client.processResult( new AdventureResult( AdventureResult.MEAT, amount ) );
			}
			catch ( Exception e )
			{
				// If an exception is caught, then this is a situation that isn't
				// currently handled by the parser.  Report it to the KoLmafia.getLogStream()
				// and continue on.

				KoLmafia.getLogStream().println( e );
				e.printStackTrace( KoLmafia.getLogStream() );
			}
		}
		else
			StoreManager.update( responseText, false );

		updateDisplay( ENABLED_STATE, "Items sold." );
	}

	protected String getSuccessMessage()
	{	return "";
	}
}
