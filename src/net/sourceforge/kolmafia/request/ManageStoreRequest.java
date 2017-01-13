/**
 * Copyright (c) 2005-2016, KoLmafia development team
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

package net.sourceforge.kolmafia.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.AdventureResult;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;

import net.sourceforge.kolmafia.objectpool.ItemPool;

import net.sourceforge.kolmafia.persistence.ItemDatabase;

import net.sourceforge.kolmafia.request.MallPurchaseRequest;

import net.sourceforge.kolmafia.session.ResultProcessor;
import net.sourceforge.kolmafia.session.StoreManager;

import net.sourceforge.kolmafia.utilities.StringUtilities;

public class ManageStoreRequest
	extends GenericRequest
{
	private static Pattern ITEMID_PATTERN = Pattern.compile( "itemid=(h)?(\\d+)" );
	private static Pattern PRICE_PATTERN = Pattern.compile( "price=(\\d+)?" );
	private static Pattern QUANTITY_PATTERN = Pattern.compile( "quantity=(\\d+|\\*|)" );
	private static Pattern QTY_PATTERN = Pattern.compile( "qty=(\\d+)" );
	private static Pattern LIMIT_PATTERN = Pattern.compile( "limit=(\\d+)?" );

	// (2) breath mints stocked for 999,999,999 meat each.
	private static Pattern STOCKED_PATTERN = Pattern.compile( "\\(([\\d,]+)\\) (.*?) stocked for ([\\d,]+) meat each( \\(([\\d,]+)/day\\))?" );

	// <tr class="deets" rel="618679857" after="6"><td valign="center"><img src="https://s3.amazonaws.com/images.kingdomofloathing.com/itemimages/cocostraw.gif"></td><td valign="center"><b>slip 'n' slide</b></td><td valign="center" align="center">1,081</td valign="center"><td align="center"><span class="tohide">230</span><input type="text" class="hideit price" rel="230" style="width:80px" name="price[681]" value="230" /></td><td valign="center" align="center"><span class="tohide">&infin;</span><input type="text" class="hideit lim" style="width:24px" name="limit[681]" value="0" /><input type="submit" value="Save" class="button hideit pricejax" style="font-size: 8pt"/></td><td align="right" valign="center">[<a href="#" class="update">update</a>][<a href="/backoffice.php?pwd=90ef7aca1d45123f7abe567b758c5b89&iid=681&action=prices" class="prices">prices</a>]<span class="tohide">[<a class="take" href="backoffice.php?qty=1&pwd=90ef7aca1d45123f7abe567b758c5b89&action=removeitem&itemid=681">take&nbsp;1</a>][<a class="take" href="backoffice.php?qty=1081&pwd=90ef7aca1d45123f7abe567b758c5b89&action=removeitem&itemid=681">take&nbsp;&infin;</a>]</span><span class="hideit" style="font-size: .9em">  <span class="setp">min&nbsp;price:&nbsp;230</span><br /><span class="setp">cheapest: 230</span></span></td></tr>

	private static Pattern INVENTORY_ROW_PATTERN = Pattern.compile( "<tr class=\"deets\".*?</tr>" );
	private static Pattern INVENTORY_PATTERN = Pattern.compile( ".*?>([\\d,]+<).*name=\"price\\[(.*?)\\]\" value=\"(.*?)\".*name=\"limit\\[.*?\\]\" value=\"(.*?)\"" );

	private static enum RequestType
	{
		ITEM_ADDITION,
		ITEM_REMOVAL,
		PRICE_UPDATE,
		REFRESH,
		VIEW_STORE_LOG,
	}

	private final RequestType requestType;

	// For action=removeitem
	private AdventureResult item;

	// For action=additem
	private AdventureResult[] items;
	private int[] prices, limits;
	private boolean storage;

	public ManageStoreRequest()
	{
		super( "manageprices.php" );
		this.requestType = RequestType.REFRESH;
	}

	public ManageStoreRequest( final boolean isStoreLog )
	{
		super( "backoffice.php" );
		this.addFormField( "which", "3" );
		this.requestType =  RequestType.VIEW_STORE_LOG;
	}

	public ManageStoreRequest( final int itemId, int qty )
	{
		super( "backoffice.php" );
		this.addFormField( "itemid", String.valueOf( itemId ) );
		this.addFormField( "action", "removeitem" );

		// Cannot ask for more to be removed than are really in the store
		qty = Math.min( qty, StoreManager.shopAmount( itemId ) );
		if ( qty > 1 )
		{
			AdventureResult item = ItemPool.get( itemId );
			if ( KoLConstants.profitableList.contains( item ) )
			{
				KoLConstants.profitableList.remove( item );
			}
		}

		this.addFormField( "qty", String.valueOf( qty ) );
		this.addFormField( "ajax", "1" );

		this.requestType = RequestType.ITEM_REMOVAL;
		this.item = ItemPool.get( itemId, qty );
	}

	public ManageStoreRequest( final AdventureResult[] items, boolean storage )
	{
		this( items, null, null, storage );
	}

	public ManageStoreRequest( final AdventureResult[] items, final int[] prices, final int[] limits, boolean storage )
	{
		super( "backoffice.php" );
		this.addFormField( "action", "additem" );
		this.addFormField( "ajax", "1" );

		this.requestType = RequestType.ITEM_ADDITION;
		this.items = items;
		this.prices = prices;
		this.limits = limits;
		this.storage = storage;
	}

	public ManageStoreRequest( final int[] itemId, final int[] prices, final int[] limits )
	{
		super( "backoffice.php" );
		this.addFormField( "action", "updateinv" );
		this.addFormField( "ajax", "1" );

		this.requestType = RequestType.PRICE_UPDATE;
		for ( int i = 0; i < itemId.length; ++i )
		{
			if ( prices[ i ] == 0 )
			{
				continue;
			}
			if ( prices[ i ] == StoreManager.getPrice( i ) &&
			     limits[ i ] == StoreManager.getLimit( i ) )
			{
				continue;
			}
			this.addFormField( "price[" + itemId[ i ] + "]", String.valueOf( Math.max(
				prices[ i ], Math.max( ItemDatabase.getPriceById( itemId[ i ] ), 100 ) ) ) );
			if ( limits[ i ] != 0 )
			{
				this.addFormField( "limit[" + itemId[ i ] + "]", String.valueOf( limits[ i ] ) );
			}
		}
	}

	@Override
	protected boolean retryOnTimeout()
	{
		return true;
	}

	@Override
	public void run()
	{
		switch ( this.requestType )
		{
		case ITEM_ADDITION:
			this.addItems();
			break;

		case ITEM_REMOVAL:
			this.removeItem();
			break;

		case PRICE_UPDATE:
			this.priceUpdate();
			break;

		case REFRESH:
			this.managePrices();
			break;

		case VIEW_STORE_LOG:
			this.viewStoreLogs();
			break;
		}
	}

	private void addItems()
	{
		for ( int i = 0; KoLmafia.permitsContinue() && i < this.items.length; ++i )
		{
			// backoffice.php?itemid=h362&price=180&quantity=1&limit=&pwd&action=additem&ajax=1
			AdventureResult item = this.items[ i ];
			String name = item.getName();

			this.addFormField( "itemid", ( this.storage ? "h" : "" ) + String.valueOf( item.getItemId() ) );
			this.addFormField( "price", ( this.prices == null ? "" : "" + this.prices[ i ] ) );
			this.addFormField( "quantity", String.valueOf( item.getCount() ) );
			this.addFormField( "limit", ( this.limits == null ? "" : "" + this.limits[ i ] ) );

			KoLmafia.updateDisplay( "Adding " + name + " to store..." );
			super.run();
			KoLmafia.updateDisplay( item.getCount() + " " + name + " added to your store." );
		}
	}

	private void removeItem()
	{
		String name = this.item.getName();

		KoLmafia.updateDisplay( "Removing " + name + " from store..." );
		super.run();
		KoLmafia.updateDisplay( this.item.getCount() + " " + name + " removed from your store." );
	}

	private void managePrices()
	{
		KoLmafia.updateDisplay( "Requesting store inventory..." );

		super.run();

		if ( this.responseText != null )
		{
			StoreManager.update( this.responseText, true );
		}

		KoLmafia.updateDisplay( "Store inventory request complete." );
	}

	private void viewStoreLogs()
	{
		KoLmafia.updateDisplay( "Examining store logs..." );

		super.run();

		if ( this.responseText != null )
		{
			StoreManager.parseLog( this.responseText );
		}

		KoLmafia.updateDisplay( "Store purchase logs retrieved." );
	}

	private void priceUpdate()
	{
		KoLmafia.updateDisplay( "Updating store prices..." );

		super.run();

		KoLmafia.updateDisplay( "Store prices updated." );
	}

	@Override
	public void processResults()
	{
		ManageStoreRequest.parseResponse( this.getURLString(), this.responseText );
	}

	public static final void parseResponse( final String urlString, final String responseText )
	{
		if ( !urlString.startsWith( "backoffice.php" ) )
		{
			return;
		}

		String action = GenericRequest.getAction( urlString );
		if ( action == null )
		{
			return;
		}

		if ( action.equals( "additem" ) )
		{
			// (2) breath mints stocked for 999,999,999 meat each.
			Matcher stockedMatcher = ManageStoreRequest.STOCKED_PATTERN.matcher( responseText );
			if ( !stockedMatcher.find() )
			{
				return;
			}

			int quantity = StringUtilities.parseInt( stockedMatcher.group( 1 ) );
			int price = StringUtilities.parseInt( stockedMatcher.group( 3 ) );
			int limit = stockedMatcher.group( 4 ) == null ? 0 : StringUtilities.parseInt( stockedMatcher.group( 5 ) );

			// backoffice.php?itemid=h362&price=180&quantity=1&limit=&pwd&action=additem&ajax=1
			// backoffice.php?itemid=362&price=180&quantity=1&limit=&pwd&action=additem&ajax=1

			// get the item ID - and whether it is from Hagnk's - from the URL submitted.
			// ignore price, quantity, and limit, since the response told us those

			Matcher itemMatcher = ManageStoreRequest.ITEMID_PATTERN.matcher( urlString );
			if ( !itemMatcher.find() )
			{
				return;
			}

			boolean storage = itemMatcher.group( 1 ) != null;
			int itemId = StringUtilities.parseInt( itemMatcher.group( 2 ) );

			AdventureResult item = ItemPool.get( itemId, -quantity );
			if ( storage)
			{
				AdventureResult.addResultToList( KoLConstants.storage, item );
			}
			else
			{
				ResultProcessor.processItem( itemId, -quantity );
			}

			StoreManager.addItem( itemId, quantity, price, limit );

			return;
		}

		if ( action.equals( "removeitem" ) )
		{
			// backoffice.php?qty=1&pwd&action=removeitem&itemid=362&ajax=1

			AdventureResult item = MallPurchaseRequest.processItemFromMall( responseText );
			if ( item != null )
			{
				StoreManager.removeItem( item.getItemId(), item.getCount() );
			}

			return;
		}

		if ( action.equals( "updateinv" ) )
		{
			// backoffice.php?action=updateinv&price[682]=230&price[684]=230&price[797]=230&price[679]=230&price[680]=230&price[681]=230
			//
			// With ajax:
			// <table><tr><td>a little sump'm sump'm updated  (price: 231, limit: 0)<!-- U:{"116230030":{"price":231,"lim":0}} --></td></tr></table>
			//
			// That is nice and succinct and tells you only the items which changed price
			//
			// Without ajax, we can also get the quantity

			if ( !urlString.contains( "ajax=1" ) )
			{
				Matcher rowMatcher = ManageStoreRequest.INVENTORY_ROW_PATTERN.matcher( responseText );
				while ( rowMatcher.find() )
				{
					Matcher matcher = ManageStoreRequest.INVENTORY_PATTERN.matcher( rowMatcher.group( 0 ) );
					if ( matcher.find() )
					{
						int itemId = StringUtilities.parseInt( matcher.group( 2 ) );
						int count = StringUtilities.parseInt( matcher.group( 1 ) );
						int price = StringUtilities.parseInt( matcher.group( 3 ) );
						int limit = StringUtilities.parseInt( matcher.group( 4 ) );
						StoreManager.updateItem( itemId, count, price, limit );
					}
				}
			}
			else
			{
				StoreManager.updateSomePrices( responseText );
			}

			StoreManager.calculatePotentialEarnings();

			return;
		}
	}

	public static boolean registerRequest( final String urlString )
	{
		if ( !urlString.startsWith( "backoffice.php" ) )
		{
			return false;
		}

		String action = GenericRequest.getAction( urlString );
		if ( action == null )
		{
			return false;
		}

		if ( action.equals( "additem" ) )
		{
			// backoffice.php?itemid=h362&price=180&quantity=1&limit=&pwd&action=additem&ajax=1
			// backoffice.php?itemid=362&price=180&quantity=1&limit=&pwd&action=additem&ajax=1

			// get the item ID - and whether it is from Hagnk's - from the URL submitted.
			Matcher itemMatcher = ManageStoreRequest.ITEMID_PATTERN.matcher( urlString );
			if ( !itemMatcher.find() )
			{
				return false;
			}
			boolean storage = itemMatcher.group( 1 ) != null;
			int itemId = StringUtilities.parseInt( itemMatcher.group( 2 ) );

			Matcher quantityMatcher = ManageStoreRequest.QUANTITY_PATTERN.matcher( urlString );
			if ( !quantityMatcher.find() )
			{
				return false;
			}
			String quantityString = quantityMatcher.group( 1 );
			String quantity =
				quantityString.equals( "" ) ? "1" :
				quantityString.equals( "*" ) ? "all" :
				quantityString;

			Matcher priceMatcher = ManageStoreRequest.PRICE_PATTERN.matcher( urlString );
			if ( !priceMatcher.find() )
			{
				return false;
			}
			int price = priceMatcher.group( 1 ) == null ? 999999999 : StringUtilities.parseInt( priceMatcher.group( 1 ) );

			Matcher limitMatcher = ManageStoreRequest.LIMIT_PATTERN.matcher( urlString );
			if ( !limitMatcher.find() )
			{
				return false;
			}
			int limit = limitMatcher.group( 1 ) == null ? 0 : StringUtilities.parseInt( limitMatcher.group( 1 ) );

			StringBuilder buffer = new StringBuilder();

			buffer.append( "Adding " );
			buffer.append( quantity );
			buffer.append( " " );
			buffer.append( ItemDatabase.getItemName( itemId ) );
			buffer.append( " to store from " );
			buffer.append( storage ? "storage" : "inventory" );
			buffer.append( " for " );
			buffer.append( KoLConstants.COMMA_FORMAT.format( price ) );
			buffer.append( " Meat" );
			if ( limit > 0 )
			{
				buffer.append( ", limited to " );
				buffer.append( String.valueOf( limit ) );
				buffer.append( "/day" );
			}
			else
			{
				buffer.append( " with no limit" );
			}

			RequestLogger.updateSessionLog( buffer.toString() );

			return true;
		}

		if ( action.equals( "removeitem" ) )
		{
			// backoffice.php?qty=1&pwd&action=removeitem&itemid=362&ajax=1

			Matcher itemMatcher = ManageStoreRequest.ITEMID_PATTERN.matcher( urlString );
			if ( !itemMatcher.find() )
			{
				return false;
			}
			int itemId = StringUtilities.parseInt( itemMatcher.group( 2 ) );

			Matcher qtyMatcher = ManageStoreRequest.QTY_PATTERN.matcher( urlString );
			if ( !qtyMatcher.find() )
			{
				return false;
			}
			int qty = StringUtilities.parseInt( qtyMatcher.group( 1 ) );

			StringBuilder buffer = new StringBuilder();

			buffer.append( "Removing " );
			buffer.append( qty );
			buffer.append( " " );
			buffer.append( ItemDatabase.getItemName( itemId ) );
			buffer.append( " from store" );

			RequestLogger.updateSessionLog( buffer.toString() );

			return true;
		}

		return false;
	}
}
