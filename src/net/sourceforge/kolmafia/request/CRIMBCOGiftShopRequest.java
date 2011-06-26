/**
 * Copyright (c) 2005-2011, KoLmafia development team
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
import net.sourceforge.kolmafia.CoinmasterData;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.persistence.CoinmastersDatabase;
import net.sourceforge.kolmafia.request.CoinMasterRequest;
import net.sourceforge.kolmafia.swingui.CoinmastersFrame;

public class CRIMBCOGiftShopRequest
	extends CoinMasterRequest
{
	private static final Pattern TOKEN_PATTERN = Pattern.compile( "You have <b>([\\d,]+)</b> CRIMBCO scrip" );
	public static final AdventureResult CRIMBCO_SCRIP = ItemPool.get( ItemPool.CRIMBCO_SCRIP, 1 );
	public static final CoinmasterData CRIMBCO_GIFT_SHOP =
		new CoinmasterData(
			"CRIMBCO Gift Shop",
			CRIMBCOGiftShopRequest.class,
			"crimbo10.php",
			"CRIMBCO scrip",
			"You don't have any CRIMBCO scrip",
			false,
			CRIMBCOGiftShopRequest.TOKEN_PATTERN,
			CRIMBCOGiftShopRequest.CRIMBCO_SCRIP,
			null,
			"whichitem",
			CoinMasterRequest.ITEMID_PATTERN,
			"howmany",
			CoinMasterRequest.HOWMANY_PATTERN,
			"buygift",
			CoinmastersDatabase.getScripItems(),
			CoinmastersDatabase.scripBuyPrices(),
			null,
			null
			);

	public CRIMBCOGiftShopRequest()
	{
		super( CRIMBCOGiftShopRequest.CRIMBCO_GIFT_SHOP );
	}

	public CRIMBCOGiftShopRequest( final String action )
	{
		super( CRIMBCOGiftShopRequest.CRIMBCO_GIFT_SHOP, action );
	}

	public CRIMBCOGiftShopRequest( final String action, final int itemId, final int quantity )
	{
		super( CRIMBCOGiftShopRequest.CRIMBCO_GIFT_SHOP, action, itemId, quantity );
	}

	public CRIMBCOGiftShopRequest( final String action, final int itemId )
	{
		this( action, itemId, 1 );
	}

	public CRIMBCOGiftShopRequest( final String action, final AdventureResult ar )
	{
		this( action, ar.getItemId(), ar.getCount() );
	}

	public static void parseCRIMBCOGiftShopVisit( final String location, final String responseText )
	{
		if ( !location.startsWith( "crimbo10.php" ) )
		{
			return;
		}

		CoinmasterData data = CRIMBCOGiftShopRequest.CRIMBCO_GIFT_SHOP;
		String action = GenericRequest.getAction( location );
		if ( action == null )
		{
			if ( location.indexOf( "place=giftshop" ) != -1 )
			{
				// Parse current coin balances
				CoinMasterRequest.parseBalance( data, responseText );
				CoinmastersFrame.externalUpdate();
			}

			return;
		}

		if ( !action.equals( "buygift" ) )
		{
			return;
		}

		if ( responseText.indexOf( "You don't have enough" ) != -1 )
		{
			CoinMasterRequest.refundPurchase( data, location );
		}

		CoinMasterRequest.parseBalance( data, responseText );
		CoinmastersFrame.externalUpdate();
	}

	public static final boolean registerRequest( final String urlString )
	{
		// We only claim crimbo10.php?action=buygift
		if ( !urlString.startsWith( "crimbo10.php" ) )
		{
			return false;
		}

		String action = GenericRequest.getAction( urlString );
		if ( action == null )
		{
			return false;
		}

		if ( !action.equals( "buygift" ) )
		{
			return false;
		}

		CoinmasterData data = CRIMBCOGiftShopRequest.CRIMBCO_GIFT_SHOP;
		CoinMasterRequest.buyStuff( data, urlString );
		return true;
	}

	public static String accessible()
	{
		return "The CRIMBCO Gift Shop is not available";
	}
}
