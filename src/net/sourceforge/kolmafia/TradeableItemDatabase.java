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

import java.io.BufferedReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A static class which retrieves all the tradeable items available in
 * the Kingdom of Loathing and allows the client to do item look-ups.
 * The item list being used is a parsed and resorted list found on
 * Ohayou's Kingdom of Loathing website.  In order to decrease server
 * load, this item list is stored within the JAR archive.
 */

public class TradeableItemDatabase extends KoLDatabase
{
	public static final int ITEM_COUNT = 1500;

	private static String [] itemByID = new String[ ITEM_COUNT ];
	private static int [] consumptionID = new int[ ITEM_COUNT ];
	private static int [] priceByID = new int[ ITEM_COUNT ];
	private static String [] descByID = new String[ ITEM_COUNT ];

	private static Map itemByName = new TreeMap();

	static
	{
		// This begins by opening up the data file and preparing
		// a buffered reader; once this is done, every line is
		// examined and double-referenced: once in the name-lookup,
		// and again in the ID lookup.

		BufferedReader reader = getReader( "tradeitems.dat" );

		String [] data;
		int itemID;

		while ( (data = readData( reader )) != null )
		{
			if ( data.length == 4 )
			{
				itemID = Integer.parseInt( data[0] );

				consumptionID[ itemID ] = Integer.parseInt( data[2] );
				priceByID[ itemID ] = Integer.parseInt( data[3] );
				itemByID[ itemID ] = getDisplayName( data[1] );
				itemByName.put( getCanonicalName( data[1] ), new Integer( itemID ) );
			}
		}

		// Next, retrieve the description IDs using the data
		// table present in MaxDemian's database.

		reader = getReader( "itemdescs.dat" );

		while ( (data = readData( reader )) != null )
		{
			boolean isDescriptionID = true;
			if ( data.length >= 2 && data[1].length() > 0 )
			{
				isDescriptionID = true;
				for ( int i = 0; i < data[1].length() && isDescriptionID; ++i )
					if ( !Character.isDigit( data[1].charAt(i) ) )
						isDescriptionID = false;

				if ( isDescriptionID )
					descByID[ Integer.parseInt( data[0].trim() ) ] = data[1];
			}
		}
	}

	/**
	 * Temporarily adds an item to the item database.  This
	 * is used whenever KoLmafia encounters an unknown item
	 * in the mall or in the player's inventory.
	 */

	public static void registerItem( int itemID, String itemName )
	{
		client.getLogStream().println( "New item: \"" + itemName + "\" (" + itemID + ")" );

		consumptionID[ itemID ] = 0;
		priceByID[ itemID ] = 0;
		itemByID[ itemID ] = itemName;

		itemByName.put( itemName.toLowerCase(), new Integer( itemID ) );
	}

	/**
	 * Returns the ID number for an item, given its name.
	 * @param	itemName	The name of the item to lookup
	 * @return	The ID number of the corresponding item
	 */

	public static final int getItemID( String itemName )
	{
		if ( itemName == null )
			return -1;

		if ( itemName.equals( "ice-cold beer (Schlitz)" ) )
			return 41;

		if ( itemName.equals( "ice-cold beer (Willer)" ) )
			return 81;

		String canonicalName = getCanonicalName( itemName );
		Object itemID = itemByName.get( canonicalName );

		// If the name, as-is, exists in the item database,
		// then go ahead and return the item ID.

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// If this is the pluralized version of chewing
		// gum, then return the ID for chewing gum.

		if ( canonicalName.equals( "chewing gums on strings" ) )
			return SewerRequest.GUM.getItemID();

		// If it's a pluralized form of something that
		// ends with "y", then return the appropriate
		// item ID for the "y" version.

		itemID = itemByName.get( canonicalName.replaceFirst( "ies ", "y " ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// If it's a pluralized form of something that
		// ends with "o", then return the appropriate
		// item ID for the "o" version.

		itemID = itemByName.get( canonicalName.replaceFirst( "oes ", "o " ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// If it's a standard pluralized forms, then
		// return the appropriate item ID.

		itemID = itemByName.get( canonicalName.replaceFirst( "([A-Za-z])s ", "$1 " ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// If it's a cactus, then go ahead and return
		// the appropriate cactus-type ID.

		itemID = itemByName.get( canonicalName.replaceFirst( "cacti", "cactus" ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// Check for plurals occurring at the end of
		// the item name.  This includes all of the
		// versions indicated above.

		if ( canonicalName.endsWith( "es" ) )
			itemID = itemByName.get( canonicalName.substring( 0, canonicalName.length() - 2 ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		if ( canonicalName.endsWith( "s" ) )
			itemID = itemByName.get( canonicalName.substring( 0, canonicalName.length() - 1 ) );

		if ( itemID != null )
			return ((Integer)itemID).intValue();

		// All tests failing, there is no item that
		// exists with the given name.

		return -1;
	}

	/**
	 * Returns the price for the item with the given ID.
	 * @return	The price associated with the item
	 */

	public static final int getPriceByID( int itemID )
	{	return itemID < 0 ? 0 : priceByID[ itemID ];
	}

	/**
	 * Returns the name for an item, given its ID number.
	 * @param	itemID	The ID number of the item to lookup
	 * @return	The name of the corresponding item
	 */

	public static final String getItemName( int itemID )
	{
		return itemID < 0 || itemID > ITEM_COUNT ? null :
			itemID == 41 ? "ice-cold beer (Schlitz)" : itemID == 81 ? "ice-cold beer (Willer)" : itemByID[ itemID ];
	}

	/**
	 * Returns a list of all items which contain the given
	 * substring.  This is useful for people who are doing
	 * lookups on items.
	 */

	public static final List getMatchingNames( String substring )
	{
		List substringList = new ArrayList();
		String searchString = substring.toLowerCase().replaceAll( "\"", "" );

		if ( substring.indexOf( "\"" ) != -1 )
		{
			if ( TradeableItemDatabase.contains( searchString ) )
				substringList.add( getDisplayName( searchString ) );
		}
		else
		{
			String currentItemName;

			Iterator completeItems = itemByName.keySet().iterator();
			while ( completeItems.hasNext() )
			{
				currentItemName = (String) completeItems.next();
				if ( currentItemName.indexOf( searchString ) != -1 )
					substringList.add( getItemName( getItemID( currentItemName ) ) );
			}
		}

		return substringList;
	}

	/**
	 * Returns whether or not an item with a given name
	 * exists in the database; this is useful in the
	 * event that an item is encountered which is not
	 * tradeable (and hence, should not be displayed).
	 *
	 * @return	<code>true</code> if the item is in the database
	 */

	public static final boolean contains( String itemName )
	{	return getItemID( itemName ) != -1;
	}

	/**
	 * Returns whether or not the item with the given name
	 * is usable (this includes edibility).
	 *
	 * @return	<code>true</code> if the item is usable
	 */

	public static final boolean isUsable( String itemName )
	{
		int itemID = getItemID( itemName );
		return itemID == -1 ? false : consumptionID[ itemID ] != ConsumeItemRequest.NO_CONSUME &&
			consumptionID[ itemID ] < ConsumeItemRequest.EQUIP_FAMILIAR;
	}

	/**
	 * Returns the kind of consumption associated with the
	 * item with the given name.
	 *
	 * @return	The consumption associated with the item
	 */

	public static final int getConsumptionType( String itemName )
	{
		int itemID = getItemID( itemName );
		return itemID == -1 ? ConsumeItemRequest.NO_CONSUME : consumptionID[ itemID ];
	}

	/**
	 * Returns the item description ID used by the given
	 * item, given its item ID.
	 *
	 * @return	The description ID associated with the item
	 */

	public static final String getDescriptionID( int itemID )
	{	return itemID == -1 || itemID >= ITEM_COUNT ? "" : descByID[ itemID ];
	}
}
