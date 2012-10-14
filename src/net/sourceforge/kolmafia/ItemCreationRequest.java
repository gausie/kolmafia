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

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * An extension of <code>KoLRequest</code> designed to handle all the
 * item creation requests.  At the current time, it is only made to
 * handle items which use meat paste and are tradeable in-game.
 */

public class ItemCreationRequest extends KoLRequest implements Comparable
{
	public static final int MEAT_PASTE = 25;
	public static final int MEAT_STACK = 88;
	public static final int DENSE_STACK = 258;

	public static final int SUBCLASS = Integer.MAX_VALUE;

	public static final int NOCREATE = 0;
	public static final int COMBINE = 1;
	public static final int COOK = 2;
	public static final int MIX = 3;
	public static final int SMITH = 4;
	public static final int COOK_REAGENT = 5;
	public static final int COOK_PASTA = 6;
	public static final int MIX_SPECIAL = 7;
	public static final int JEWELRY = 8;
	public static final int STARCHART = 9;
	public static final int PIXEL = 10;
	public static final int ROLLING_PIN = 11;
	public static final int TINKER = 12;
	public static final int SMITH_WEAPON = 13;
	public static final int SMITH_ARMOR = 14;

	private static final AdventureResult OVEN = new AdventureResult( 157, 1 );
	private static final AdventureResult KIT = new AdventureResult( 236, 1 );
	private static final AdventureResult CHEF = new AdventureResult( 438, 1 );
	private static final AdventureResult CLOCKWORK_CHEF = new AdventureResult( 1112, 1 );
	private static final AdventureResult BARTENDER = new AdventureResult( 440, 1 );
	private static final AdventureResult CLOCKWORK_BARTENDER = new AdventureResult( 1111, 1 );

	private int itemID, quantityNeeded, mixingMethod;
	private String name;

	private static final AdventureResult DOUGH = new AdventureResult( 159, 1 );
	private static final AdventureResult FLAT_DOUGH = new AdventureResult( 301, 1 );
	private static final AdventureResult ROLLING = new AdventureResult( 873, 1 );
	private static final AdventureResult UNROLLING = new AdventureResult( 874, 1 );

        private static final AdventureResult [][] DOUGH_DATA =
        {       // input, tool, output
                { DOUGH, ROLLING, FLAT_DOUGH },
                { FLAT_DOUGH, UNROLLING, DOUGH }
        };

	/**
	 * Constructs a new <code>ItemCreationRequest</code> with nothing known
	 * other than the form to use.  This is used by descendant classes to
	 * avoid weird type-casting problems, as it assumes that there is no
	 * known way for the item to be created.
	 *
	 * @param	client	The client to be notified of the item creation
	 * @param	formSource	The form to be used for the item creation
	 */

	protected ItemCreationRequest( KoLmafia client, String formSource, int itemID, int quantityNeeded )
	{	this( client, formSource, itemID, SUBCLASS, quantityNeeded );
	}

	/**
	 * Constructs a new <code>ItemCreationRequest</code> where you create
	 * the given number of items.
	 *
	 * @param	client	The client to be notified of the item creation
	 * @param	formSource	The form to be used for the item creation
	 * @param	itemID	The identifier for the item to be created
	 * @param	mixingMethod	How the item is created
	 * @param	quantityNeeded	How many of this item are needed
	 */

	protected ItemCreationRequest( KoLmafia client, String formSource, int itemID, int mixingMethod, int quantityNeeded )
	{
		super( client, formSource );

		this.itemID = itemID;
		this.mixingMethod = mixingMethod;
		this.quantityNeeded = quantityNeeded;

		if ( client != null )
			addFormField( "pwd", client.getPasswordHash() );

		if ( mixingMethod != SUBCLASS && client != null && !(KoLCharacter.inMuscleSign() && mixingMethod == SMITH) )
			addFormField( "action", "combine" );
		else if ( client != null && KoLCharacter.inMuscleSign() && mixingMethod == SMITH )
			addFormField( "action", "smith" );
	}

	/**
	 * Static method which determines the appropriate subclass
	 * of an ItemCreationRequest to return, based on the idea
	 * that the given AdventureResult is the item to be created.
	 */

	public static ItemCreationRequest getInstance( KoLmafia client, AdventureResult ar )
	{	return getInstance( client, ar.getItemID(), ar.getCount() );
	}

	/**
	 * Static method which determines the appropriate subclass
	 * of an ItemCreationRequest to return, based on the idea
	 * that the given quantity of the given item is to be created.
	 */

	public static ItemCreationRequest getInstance( KoLmafia client, int itemID, int quantityNeeded )
	{
		if ( itemID == MEAT_PASTE || itemID == MEAT_STACK || itemID == DENSE_STACK )
			return new CombineMeatRequest( client, itemID, quantityNeeded );

		int mixingMethod = ConcoctionsDatabase.getMixingMethod( itemID );

		switch ( mixingMethod )
		{
			case COMBINE:
				return new ItemCreationRequest( client, (client != null && KoLCharacter.inMuscleSign()) ?
					"knoll.php" : "combine.php", itemID, mixingMethod, quantityNeeded );

			case MIX:
			case MIX_SPECIAL:
				return new ItemCreationRequest( client, "cocktail.php", itemID, mixingMethod, quantityNeeded );

			case COOK:
			case COOK_REAGENT:
			case COOK_PASTA:
				return new ItemCreationRequest( client, "cook.php", itemID, mixingMethod, quantityNeeded );

			case SMITH:
				return new ItemCreationRequest( client, (client != null && KoLCharacter.inMuscleSign()) ?
					"knoll.php" : "smith.php", itemID, mixingMethod, quantityNeeded );

			case SMITH_ARMOR:
			case SMITH_WEAPON:
				return new ItemCreationRequest( client, "smith.php", itemID, mixingMethod, quantityNeeded );

			case JEWELRY:
				return new ItemCreationRequest( client, "jewelry.php", itemID, mixingMethod, quantityNeeded );

			case ROLLING_PIN:
				return new ItemCreationRequest( client, "inv_use.php", itemID, mixingMethod, quantityNeeded );

			case STARCHART:
				return new StarChartRequest( client, itemID, quantityNeeded );

			case PIXEL:
				return new PixelRequest( client, itemID, quantityNeeded );

			case TINKER:
				return new TinkerRequest( client, itemID, quantityNeeded );

			default:
				return null;
		}
	}

	public boolean equals( Object o )
	{	return o != null && o instanceof ItemCreationRequest && itemID == ((ItemCreationRequest)o).itemID;
	}

	public int compareTo( Object o )
	{	return o == null ? -1 : this.getName().compareToIgnoreCase( ((ItemCreationRequest)o).getName() );
	}

	/**
	 * Runs the item creation request.  Note that if another item needs
	 * to be created for the request to succeed, this method will fail.
	 */

	public void run()
	{
		if ( !client.permitsContinue() || quantityNeeded <= 0 )
			return;

		switch ( mixingMethod )
		{
			case SUBCLASS:

				super.run();
				break;

			case ROLLING_PIN:

				makeDough();
				break;

			default:

				combineItems();
				break;
		}
	}

	protected void makeDough()
	{
		AdventureResult input = null;
		AdventureResult tool = null;
		AdventureResult output = null;

		// Find the array row and load the
		// correct tool/input/output data.

		for ( int i = 0; i < DOUGH_DATA.length; ++i )
		{
			output = DOUGH_DATA[i][2];
			if ( itemID == output.getItemID() )
			{
				tool = DOUGH_DATA[i][1];
				input = DOUGH_DATA[i][0];
				break;
			}
		}

		if ( tool == null )
		{
			client.cancelRequest();
			updateDisplay( ERROR_STATE, "Can't deduce correct tool to use." );
			return;
		}

		// If we have the correct tool, use it to
		// create the needed dough type.

		if ( tool.getCount( KoLCharacter.getInventory() ) > 0 )
		{
			updateDisplay( DISABLED_STATE, "Using " + tool.getName() + "..." );
			(new ConsumeItemRequest( client, tool )).run();
			return;
		}

		// If we don't have the correct tool, and the
		// person wishes to create more than 10 dough,
		// then notify the person that they should
		// purchase a tool before continuing.

		if ( quantityNeeded >= 10 )
		{
			updateDisplay( ERROR_STATE, "Please purchase a " + tool.getName() + " first." );
			client.cancelRequest();
			return;
		}

		// Without the right tool, we must manipulate
		// the dough by hand.

		String name = output.getName();
		ConsumeItemRequest request = new ConsumeItemRequest( client, input );
		for ( int i = 1; i <= quantityNeeded; ++i )
		{
			updateDisplay( DISABLED_STATE, "Creating " + name + " (" + i + " of " + quantityNeeded + ")..." );
			request.run();
			if ( !client.permitsContinue() )
				return;
		}
	}

	protected void makeIngredients()
	{
		AdventureResult [] ingredients = ConcoctionsDatabase.getIngredients( itemID );

		for ( int i = 0; i < ingredients.length; ++i )
		{
			if ( !client.permitsContinue() )
				return;

			// First, calculate the multiplier that's needed
			// for this ingredient to avoid not making enough
			// intermediate ingredients and getting an error.

			int multiplier = 0;
			for ( int j = 0; j < ingredients.length; ++j )
				if ( ingredients[i].getItemID() == ingredients[j].getItemID() )
					multiplier += ingredients[i].getCount();

			// Then, make enough of the ingredient in order
			// to proceed with the concoction.

			makeIngredient( ingredients[i], multiplier );
		}

		// If this is a combining request, you will need
		// to make meat paste as well.

		if ( mixingMethod == COMBINE && !KoLCharacter.inMuscleSign() )
			makeIngredient( new AdventureResult( MEAT_PASTE, quantityNeeded ), 1 );
	}

	/**
	 * Helper routine which actually does the item combination.
	 */

	private void combineItems()
	{
		// First, make all the required ingredients for
		// this concoction.

		makeIngredients();

		// If the request has been cancelled midway, be
		// sure to return from here.

		if ( !client.permitsContinue() )
			return;

		// Now that the ingredients have been created, you can actually
		// do the request!

		AdventureResult [] ingredients = ConcoctionsDatabase.getIngredients( itemID );
		for ( int i = 0; i < ingredients.length; ++i )
			addFormField( "item" + (i+1), String.valueOf( ingredients[i].getItemID() ) );

		addFormField( "quantity", String.valueOf( quantityNeeded ) );

		// Auto-create chef or bartender if one doesn't
		// exist and the user has opted to repair.

		if ( !autoRepairBoxServant() )
			client.cancelRequest();

		// If the request has been cancelled midway, be
		// sure to return from here.

		if ( !client.permitsContinue() )
			return;

		updateDisplay( DISABLED_STATE, "Creating " + toString() + "..." );

		super.run();

		// If an error state occurred, return from this
		// request, since there's no content to parse

		if ( isErrorState || responseCode != 200 )
			return;

		// Check to make sure that the item creation did not fail.

		if ( responseText.indexOf( "You don't have enough" ) != -1 )
		{
			client.cancelRequest();
			updateDisplay( ERROR_STATE, "You're missing ingredients." );
			return;
		}

		if ( responseText.indexOf( "You don't have that many adventures left" ) != -1 )
		{
			client.cancelRequest();
			updateDisplay( ERROR_STATE, "You don't have enough adventures." );
			return;
		}

		// Arbitrary results can happen - just throw the
		// entire string to the results parser and let
		// it figure out what was actually gained; note
		// that this is potentially inaccurate, as you
		// may get your initial creation attempt back.

		AdventureResult createdItem = new AdventureResult( itemID, 0 );
		int beforeQuantity = createdItem.getCount( KoLCharacter.getInventory() );

		processResults( responseText );

		// Figure out how many items were created
		String itemName = TradeableItemDatabase.getItemName( itemID );

		int createdQuantity = createdItem.getCount( KoLCharacter.getInventory() ) - beforeQuantity;

		if ( createdQuantity > 0 )
		{
			// Because an explosion might have occurred, the
			// quantity that has changed might not be accurate.
			// Therefore, update with the actual value.

			for ( int i = 0; i < ingredients.length; ++i )
				client.processResult( new AdventureResult( ingredients[i].getItemID(), 0 - createdQuantity ) );

			// Reduce adventures and use meat paste

			switch ( mixingMethod )
			{
			case COMBINE:
				if ( !KoLCharacter.inMuscleSign() )
					client.processResult( new AdventureResult( MEAT_PASTE, 0 - createdQuantity ) );
				break;

			case SMITH:
				if ( !KoLCharacter.inMuscleSign() )
					client.processResult( new AdventureResult( AdventureResult.ADV, 0 - createdQuantity ) );
				break;

			case SMITH_ARMOR:
			case SMITH_WEAPON:
				client.processResult( new AdventureResult( AdventureResult.ADV, 0 - createdQuantity ) );
				break;

			case JEWELRY:
				client.processResult( new AdventureResult( AdventureResult.ADV, 0 - (3 * createdQuantity) ) );
				break;

			case COOK:
			case COOK_REAGENT:
			case COOK_PASTA:
				if ( !KoLCharacter.hasChef() )
					client.processResult( new AdventureResult( AdventureResult.ADV, 0 - createdQuantity ) );
				break;

			case MIX:
			case MIX_SPECIAL:
				if ( !KoLCharacter.hasBartender() )
					client.processResult( new AdventureResult( AdventureResult.ADV, 0 - createdQuantity ) );
				break;
			}
		}

		// Check to see if box-servant was overworked and exploded.

		if ( responseText.indexOf( "Smoke" ) != -1 )
		{
			ItemCreationRequest leftOver = ItemCreationRequest.getInstance( client, itemID, quantityNeeded - createdQuantity );

			switch ( mixingMethod )
			{
			case COOK:
			case COOK_REAGENT:
			case COOK_PASTA:
				KoLCharacter.setChef( false );
				leftOver.run();
				break;

			case MIX:
			case MIX_SPECIAL:
				KoLCharacter.setBartender( false );
				leftOver.run();
				break;
			}
		}
	}

	private boolean autoRepairBoxServant()
	{
		// If the request has been cancelled midway, be
		// sure to return from here.

		if ( !client.permitsContinue() )
			return false;

		boolean noServantNeeded = getProperty( "createWithoutBoxServants" ).equals( "true" );

		switch ( mixingMethod )
		{
			case COOK:
			case COOK_REAGENT:
			case COOK_PASTA:

				if ( KoLCharacter.hasChef() || ( noServantNeeded && KoLCharacter.getInventory().contains( OVEN ) ) )
					return true;
				break;

			case MIX:
			case MIX_SPECIAL:

				if ( KoLCharacter.hasBartender() || ( noServantNeeded && KoLCharacter.getInventory().contains( KIT ) ) )
					return true;
				break;

			default:
				return true;
		}

		// No currently installed servant and player insists on using
		// one.	 Check to see if they want to autorepair

		if ( getProperty( "autoRepairBoxes" ).equals( "false" ) )
		{
			updateDisplay( ERROR_STATE, "Box servant explosion!" );
			return false;
		}

		// If they do want to auto-repair, make sure that
		// the appropriate item is available in their inventory

		boolean useClockwork = getProperty( "useClockworkBoxes" ).equals( "true" );

		switch ( mixingMethod )
		{
			case COOK:
			case COOK_REAGENT:
			case COOK_PASTA:

				return useBoxServant( CHEF ) || ( useClockwork && useBoxServant( CLOCKWORK_CHEF ) );

			case MIX:
			case MIX_SPECIAL:

				return useBoxServant( BARTENDER ) || ( useClockwork && useBoxServant( CLOCKWORK_BARTENDER ) );
		}

		return false;
	}

	private boolean useBoxServant( AdventureResult servant )
	{
		// First, check to see if a box servant is available
		// for usage, either normally, or through some form
		// of creation.  This can be done by consulting the
		// creation table.

		ItemCreationRequest creationRequest = ItemCreationRequest.getInstance( client, servant );

		// If it turns out that you can create the servant,
		// it is available in one of three locations - already
		// in the inventory, inside of the closet, or ready
		// for creation.  Issue the appropriate checks.

		if ( !KoLCharacter.getInventory().contains( servant ) )
		{
			if ( getProperty( "useClosetForCreation" ).equals( "true" ) && KoLCharacter.getCloset().contains( servant ) )
			{
				updateDisplay( DISABLED_STATE, "Retrieving " + servant.getName() + " from closet..." );
				AdventureResult [] servantArray = { servant };
				(new ItemStorageRequest( client, ItemStorageRequest.CLOSET_TO_INVENTORY, servantArray )).run();
			}
			else if ( ConcoctionsDatabase.getConcoctions().contains( creationRequest ) )
			{
				creationRequest.run();
			}
			else
			{
				updateDisplay( ERROR_STATE, "Could not auto-repair " + servant.getName() + "." );
				return false;
			}
		}

		// Once you hit this point, you're guaranteed to
		// have the servant in your inventory, so attempt
		// to repair the box servant.

		updateDisplay( DISABLED_STATE, "Repairing " + servant.getName() + "..." );
		(new ConsumeItemRequest( client, servant )).run();
		return true;
	}

	/**
	 * Helper routine which makes more of the given ingredient, if it
	 * is needed.
	 *
	 * @param	ingredient	The ingredient to make
	 * @param	multiplier	The multiplier on quantity needed
	 */

	protected void makeIngredient( AdventureResult ingredient, int multiplier )
	{
		int actualQuantityNeeded = (quantityNeeded * multiplier) - ingredient.getCount( KoLCharacter.getInventory() );

		// In order to minimize server overload by making exact quantities,
		// the client will attempt to overcompensate by making more meat
		// paste than is necessary.

		if ( actualQuantityNeeded > 0 )
		{
			// Now, if you are to retrieve an item from the closet, this is where
			// that retrieval would be done.

			if ( getProperty( "useClosetForCreation" ).equals( "true" ) )
			{
				int closetCount = ingredient.getCount( KoLCharacter.getCloset() );

				if ( closetCount != 0 )
				{
					AdventureResult [] retrieval = new AdventureResult[1];
					retrieval[0] = ingredient.getInstance( Math.min( actualQuantityNeeded, closetCount ) );

					updateDisplay( DISABLED_STATE, "Retrieving " + retrieval[0].toString() + " from closet..." );
					(new ItemStorageRequest( client, ItemStorageRequest.CLOSET_TO_INVENTORY, retrieval )).run();
					actualQuantityNeeded -= retrieval[0].getCount();
				}
			}

			// If more items are still needed, then attempt to create the desired
			// item (since that's really the only way).

			if ( actualQuantityNeeded > 0 )
				ItemCreationRequest.getInstance( client, ingredient.getItemID(), actualQuantityNeeded ).run();
		}
	}

	/**
	 * Returns the item ID for the item created by this request.
	 * @return	The item ID of the item being created
	 */

	public int getItemID()
	{	return itemID;
	}

	/**
	 * Returns the name of the item created by this request.
	 * @return	The name of the item being created
	 */

	public String getName()
	{	return TradeableItemDatabase.getItemName( itemID );
	}

	/**
	 * Returns the quantity of items to be created by this request
	 * if it were to run right now.
	 */

	public int getQuantityNeeded()
	{	return quantityNeeded;
	}

	/**
	 * Sets the quantity of items to be created by this request.
	 * This method is used whenever the original quantity intended
	 * by the request changes.
	 */

	public void setQuantityNeeded( int quantityNeeded )
	{
		this.quantityNeeded = quantityNeeded;
	}

	/**
	 * Returns the string form of this item creation request.
	 * This displays the item name, and the amount that will
	 * be created by this request.
	 *
	 * @return	The string form of this request
	 */

	public String toString()
	{	return getName() + " (" + quantityNeeded + ")";
	}

	/**
	 * Special method which simplifies the constant use of indexOf and
	 * count retrieval.  This makes intent more transparent.
	 */

	public int getCount( List list )
	{
		int index = list.indexOf( this );
		return index == -1 ? 0 : ((ItemCreationRequest)list.get( index )).getQuantityNeeded();
	}
}
