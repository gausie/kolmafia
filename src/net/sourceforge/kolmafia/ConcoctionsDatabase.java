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
import java.io.BufferedReader;
import javax.swing.SwingUtilities;
import net.java.dev.spellcast.utilities.SortedListModel;

/**
 * A static class which retrieves all the concoctions available in
 * the Kingdom of Loathing.  This class technically uses up a lot
 * more memory than it needs to because it creates an array storing
 * all possible item combinations, but that's okay!  Because it's
 * only temporary.  Then again, this is supposedly true of all the
 * flow-control using exceptions, but that hasn't been changed.
 */

public class ConcoctionsDatabase extends KoLDatabase
{
	public static final SortedListModel concoctionsList = new SortedListModel();

	private static ConcoctionArray concoctions = new ConcoctionArray();
	private static SortedListModelArray knownUses = new SortedListModelArray();

	public static final int METHOD_COUNT = ItemCreationRequest.METHOD_COUNT;
	private static boolean [] PERMIT_METHOD = new boolean[ METHOD_COUNT ];
	private static int [] ADVENTURE_USAGE = new int[ METHOD_COUNT ];

	private static final int CHEF = 438;
	private static final int CLOCKWORK_CHEF = 1112;
	private static final int BARTENDER = 440;
	private static final int CLOCKWORK_BARTENDER = 1111;

	public static final AdventureResult CAR = new AdventureResult( 134, 1 );
	private static final AdventureResult OVEN = new AdventureResult( 157, 1 );
	private static final AdventureResult KIT = new AdventureResult( 236, 1 );
	private static final AdventureResult HAMMER = new AdventureResult( 338, 1 );
	private static final AdventureResult PLIERS = new AdventureResult( 709, 1 );

	private static final AdventureResult PASTE = new AdventureResult( ItemCreationRequest.MEAT_PASTE, 1 );
	private static final AdventureResult STACK = new AdventureResult( ItemCreationRequest.MEAT_STACK, 1 );
	private static final AdventureResult DENSE = new AdventureResult( ItemCreationRequest.DENSE_STACK, 1 );

	private static final AdventureResult ROLLING_PIN = new AdventureResult( 873, 1 );
	private static final AdventureResult UNROLLING_PIN = new AdventureResult( 873, 1 );

	private static final int DOUGH = 159;
	private static final int FLAT_DOUGH = 301;

	static
	{
		// This begins by opening up the data file and preparing
		// a buffered reader; once this is done, every line is
		// examined and double-referenced: once in the name-lookup,
		// and again in the ID lookup.

		BufferedReader reader = getReader( "concoctions.dat" );
		String [] data;

		while ( (data = readData( reader )) != null )
		{
			try
			{
				if ( data.length > 2 )
				{
					AdventureResult item = AdventureResult.parseResult( data[0] );
					int itemID = item.getItemID();

					if ( itemID != -1 )
					{
						int mixingMethod = Integer.parseInt( data[1] );
						Concoction concoction = new Concoction( item, mixingMethod );

						for ( int i = 2; i < data.length; ++i )
							concoction.addIngredient( parseIngredient( data[i] ) );

						if ( !concoction.isBadRecipe() )
						{
							concoctions.set( itemID, concoction );
							continue;
						}
					}

					// Bad item or bad ingredients
					System.out.println( "Bad recipe: " + data[0] );
				}
			}
			catch ( Exception e )
			{
				// If an exception is thrown, then something bad
				// happened, so do absolutely nothing.

				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}
		}

		try
		{
			reader.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace( KoLmafia.getLogStream() );
			e.printStackTrace();
		}
	}

	public static final boolean isKnownCombination( AdventureResult [] ingredients )
	{
		// Known combinations which could not be added because
		// there are limitations in the item manager.

		if ( ingredients.length == 2 )
		{
			// Handle meat stacks, which are created from fairy
			// gravy and meat from yesterday.

			if ( ingredients[0].getItemID() == 80 && ingredients[1].getItemID() == 87 )
				return true;
			if ( ingredients[1].getItemID() == 80 && ingredients[0].getItemID() == 87 )
				return true;

			// Handle plain pizza, which also allows flat dough
			// to be used instead of wads of dough.

			if ( ingredients[0].getItemID() == 246 && ingredients[1].getItemID() == 301 )
				return true;
			if ( ingredients[1].getItemID() == 246 && ingredients[0].getItemID() == 301 )
				return true;

			// Handle catsup recipes, which only exist in the
			// item table as ketchup recipes.

			if ( ingredients[0].getItemID() == 107 )
			{
				ingredients[0] = new AdventureResult( 106, 1 );
				return isKnownCombination( ingredients );
			}
			if ( ingredients[1].getItemID() == 107 )
			{
				ingredients[1] = new AdventureResult( 106, 1 );
				return isKnownCombination( ingredients );
			}

			// Handle ice-cold beer recipes, which only uses the
			// recipe for item #41 at this time.

			if ( ingredients[0].getItemID() == 81 )
			{
				ingredients[0] = new AdventureResult( 41, 1 );
				return isKnownCombination( ingredients );
			}
			if ( ingredients[1].getItemID() == 81 )
			{
				ingredients[1] = new AdventureResult( 41, 1 );
				return isKnownCombination( ingredients );
			}
		}

		int [] ingredientTestIDs;
		AdventureResult [] ingredientTest;

		for ( int i = 0; i < concoctions.size(); ++i )
		{
			ingredientTest = concoctions.get(i).getIngredients();
			if ( ingredientTest.length != ingredients.length )
				continue;

			ingredientTestIDs = new int[ ingredients.length ];
			for ( int j = 0; j < ingredientTestIDs.length; ++j )
				ingredientTestIDs[j] = ingredientTest[j].getItemID();

			boolean foundMatch = true;
			for ( int j = 0; j < ingredients.length && foundMatch; ++j )
			{
				foundMatch = false;
				for ( int k = 0; k < ingredientTestIDs.length && !foundMatch; ++k )
				{
					foundMatch |= ingredients[j].getItemID() == ingredientTestIDs[k];
					if ( foundMatch )  ingredientTestIDs[k] = -1;
				}
			}

			if ( foundMatch )
				return true;
		}

		return false;
	}

	public static final SortedListModel getKnownUses( AdventureResult item )
	{	return knownUses.get( item.getItemID() );
	}

	public static final boolean isPermittedMethod( int method )
	{	return PERMIT_METHOD[ method ];
	}

	private static AdventureResult parseIngredient( String data )
	{
		try
		{
			// If the ingredient is specified inside of brackets,
			// then a specific item ID is being designated.

			if ( data.startsWith( "[" ) )
			{
				int closeBracketIndex = data.indexOf( "]" );
				String itemIDString = data.substring( 0, closeBracketIndex ).replaceAll( "[\\[\\]]", "" ).trim();
				String quantityString = data.substring( closeBracketIndex + 1 ).trim();

				return new AdventureResult( df.parse( itemIDString ).intValue(), quantityString.length() == 0 ? 1 :
					df.parse( quantityString.replaceAll( "[\\(\\)]", "" ) ).intValue() );
			}

			// Otherwise, it's a standard ingredient - use
			// the standard adventure result parsing routine.

			return AdventureResult.parseResult( data );
		}
		catch ( Exception e )
		{
			e.printStackTrace( KoLmafia.getLogStream() );
			e.printStackTrace();

			return null;
		}
	}

	public static synchronized SortedListModel getConcoctions()
	{	return concoctionsList;
	}

	/**
	 * Returns the concoctions which are available given the list of
	 * ingredients.  The list returned contains formal requests for
	 * item creation.
	 */

	public static synchronized void refreshConcoctions()
	{
		List availableIngredients = new ArrayList();
		availableIngredients.addAll( KoLCharacter.getInventory() );

		boolean showClosetDrivenCreations = getProperty( "showClosetDrivenCreations" ).equals( "true" );

		if ( showClosetDrivenCreations )
		{
			List closetList = (List) KoLCharacter.getCloset();
			for ( int i = 0; i < closetList.size(); ++i )
				AdventureResult.addResultToList( availableIngredients, (AdventureResult) closetList.get(i) );
		}

		// First, zero out the quantities table.  Though this is not
		// actually necessary, it's a good safety and doesn't use up
		// that much CPU time.

		for ( int i = 1; i < concoctions.size(); ++i )
			concoctions.get(i).resetCalculations();

		// Make initial assessment of availability of mixing methods.
		// Do this here since some COMBINE recipes have ingredients
		// made using TINKER recipes.

		cachePermitted();

		// Next, do calculations on all mixing methods which cannot
		// be created.

		for ( int i = 1; i < concoctions.size(); ++i )
			if ( concoctions.get(i).getMixingMethod() == ItemCreationRequest.NOCREATE )
				concoctions.get(i).calculate( availableIngredients );

		// Adventures are considered Item #0 in the event that the
		// concoction will use ADVs.

		concoctions.get(0).total = KoLCharacter.getAdventuresLeft();

		// Next, meat paste and meat stacks can be created directly
		// and are dependent upon the amount of meat available.
		// This should also be calculated to allow for meat stack
		// recipes to be calculated.

		concoctions.get( ItemCreationRequest.MEAT_PASTE ).initial = PASTE.getCount( availableIngredients );
		concoctions.get( ItemCreationRequest.MEAT_PASTE ).creatable = KoLCharacter.getAvailableMeat() / 10;

		concoctions.get( ItemCreationRequest.MEAT_PASTE ).total =
			concoctions.get( ItemCreationRequest.MEAT_PASTE ).initial +
				concoctions.get( ItemCreationRequest.MEAT_PASTE ).creatable;

		concoctions.get( ItemCreationRequest.MEAT_STACK ).initial = STACK.getCount( availableIngredients );
		concoctions.get( ItemCreationRequest.MEAT_STACK ).creatable = KoLCharacter.getAvailableMeat() / 100;

		concoctions.get( ItemCreationRequest.MEAT_STACK ).total =
			concoctions.get( ItemCreationRequest.MEAT_STACK ).initial +
				concoctions.get( ItemCreationRequest.MEAT_STACK ).creatable;

		concoctions.get( ItemCreationRequest.DENSE_STACK ).initial = DENSE.getCount( availableIngredients );
		concoctions.get( ItemCreationRequest.DENSE_STACK ).creatable = KoLCharacter.getAvailableMeat() / 1000;

		concoctions.get( ItemCreationRequest.DENSE_STACK ).total =
			concoctions.get( ItemCreationRequest.DENSE_STACK ).initial +
				concoctions.get( ItemCreationRequest.DENSE_STACK ).creatable;

		// Ice-cold beer and ketchup are special instances -- for the
		// purposes of calculation, we assume that they will use the
		// ingredient which is present in the greatest quantity.

		int availableBeer = getBetterIngredient( SCHLITZ, WILLER, availableIngredients ).getCount( availableIngredients );

		concoctions.get( SCHLITZ.getItemID() ).initial = availableBeer;
		concoctions.get( SCHLITZ.getItemID() ).creatable = 0;
		concoctions.get( SCHLITZ.getItemID() ).total = availableBeer;

		concoctions.get( WILLER.getItemID() ).initial = availableBeer;
		concoctions.get( WILLER.getItemID() ).creatable = 0;
		concoctions.get( WILLER.getItemID() ).total = availableBeer;

		int availableKetchup = getBetterIngredient( KETCHUP, CATSUP, availableIngredients ).getCount( availableIngredients );

		concoctions.get( KETCHUP.getItemID() ).initial = availableKetchup;
		concoctions.get( KETCHUP.getItemID() ).creatable = 0;
		concoctions.get( KETCHUP.getItemID() ).total = availableKetchup;

		concoctions.get( CATSUP.getItemID() ).initial = availableKetchup;
		concoctions.get( CATSUP.getItemID() ).creatable = 0;
		concoctions.get( CATSUP.getItemID() ).total = availableKetchup;

		// Next, increment through all of the things which can be
		// created through the use of meat paste.  This allows for box
		// servant creation to be calculated in advance.

		for ( int i = 1; i < concoctions.size(); ++i )
			if ( concoctions.get(i).getMixingMethod() == ItemCreationRequest.COMBINE )
				concoctions.get(i).calculate( availableIngredients );

		// Now that we have calculated how many box servants are
		// available, cache permitted mixing methods.

		cachePermitted();

		// Finally, increment through all of the things which are
		// created any other way, making sure that it's a permitted
		// mixture before doing the calculation.

		for ( int i = 1; i < concoctions.size(); ++i )
		{
			if ( concoctions.get(i).concoction.getName() == null )
				continue;

			concoctions.get(i).calculate( availableIngredients );
		}

		// Now, to update the list of creatables without removing
		// all creatable items.  We do this by determining the
		// number of items inside of the old list.

		for ( int i = 1; i < concoctions.size(); ++i )
		{
			// We can't make this concoction now

			if ( concoctions.get(i).creatable <= 0 )
			{
				if ( concoctions.get(i).wasPossible() )
				{
					concoctionsList.remove( ItemCreationRequest.getInstance( client, i, 0, false ) );
					concoctions.get(i).setPossible( false );
				}

				continue;
			}

			// We can make the concoction now
			ItemCreationRequest currentCreation = ItemCreationRequest.getInstance( client, i, concoctions.get(i).creatable );

			if ( concoctions.get(i).wasPossible() )
			{
				if ( currentCreation.getCount( concoctionsList ) != concoctions.get(i).creatable )
				{
					concoctionsList.remove( currentCreation );
					concoctionsList.add( currentCreation );
				}
			}
			else
			{
				concoctionsList.add( currentCreation );
				concoctions.get(i).setPossible( true );
			}
		}
	}

	/**
	 * Utility method used to cache the current permissions on
	 * item creation, based on the given client.
	 */

	private static void cachePermitted()
	{
		boolean noServantNeeded = getProperty( "createWithoutBoxServants" ).equals( "true" );

		// It is never possible to create items which are flagged
		// NOCREATE, and it is always possible to create items
		// through meat paste combination.

		PERMIT_METHOD[ ItemCreationRequest.NOCREATE ] = false;
		ADVENTURE_USAGE[ ItemCreationRequest.NOCREATE ] = 0;

		PERMIT_METHOD[ ItemCreationRequest.COMBINE ] = true;
		ADVENTURE_USAGE[ ItemCreationRequest.COMBINE ] = 0;

		PERMIT_METHOD[ ItemCreationRequest.CLOVER ] = true;
		ADVENTURE_USAGE[ ItemCreationRequest.CLOVER ] = 0;

		// Cooking is permitted, so long as the person has a chef
		// or they don't need a box servant and have an oven.

		PERMIT_METHOD[ ItemCreationRequest.COOK ] = isAvailable( CHEF, CLOCKWORK_CHEF );

		if ( !PERMIT_METHOD[ ItemCreationRequest.COOK ] && noServantNeeded && KoLCharacter.getInventory().contains( OVEN ) )
		{
			PERMIT_METHOD[ ItemCreationRequest.COOK ] = true;
			ADVENTURE_USAGE[ ItemCreationRequest.COOK ] = 1;
		}
		else
			ADVENTURE_USAGE[ ItemCreationRequest.COOK ] = 0;

		// Cooking of reagents and noodles is possible whenever
		// the person can cook and has the appropriate skill.

		PERMIT_METHOD[ ItemCreationRequest.COOK_REAGENT ] = PERMIT_METHOD[ ItemCreationRequest.COOK ] && KoLCharacter.canSummonReagent();
		ADVENTURE_USAGE[ ItemCreationRequest.COOK_REAGENT ] = ADVENTURE_USAGE[ ItemCreationRequest.COOK ];

		PERMIT_METHOD[ ItemCreationRequest.COOK_PASTA ] = PERMIT_METHOD[ ItemCreationRequest.COOK ] && KoLCharacter.canSummonNoodles();
		ADVENTURE_USAGE[ ItemCreationRequest.COOK_PASTA ] = ADVENTURE_USAGE[ ItemCreationRequest.COOK ];

		// Mixing is possible whenever the person has a bartender
		// or they don't need a box servant and have a kit.

		PERMIT_METHOD[ ItemCreationRequest.MIX ] = isAvailable( BARTENDER, CLOCKWORK_BARTENDER );

		if ( !PERMIT_METHOD[ ItemCreationRequest.MIX ] && noServantNeeded && KoLCharacter.getInventory().contains( KIT ) )
		{
			PERMIT_METHOD[ ItemCreationRequest.MIX ] = true;
			ADVENTURE_USAGE[ ItemCreationRequest.MIX ] = 1;
		}
		else
			ADVENTURE_USAGE[ ItemCreationRequest.MIX ] = 0;

		// Mixing of advanced drinks is possible whenever the
		// person can mix drinks and has the appropriate skill.

		PERMIT_METHOD[ ItemCreationRequest.MIX_SPECIAL ] = PERMIT_METHOD[ ItemCreationRequest.MIX ] && KoLCharacter.canSummonShore();
		ADVENTURE_USAGE[ ItemCreationRequest.MIX_SPECIAL ] = ADVENTURE_USAGE[ ItemCreationRequest.MIX ];

		// Smithing of items is possible whenever the person
		// has a hammer.

		PERMIT_METHOD[ ItemCreationRequest.SMITH ] = KoLCharacter.getInventory().contains( HAMMER );

		// Advanced smithing is available whenever the person can
		// smith and has access to the appropriate skill.

		PERMIT_METHOD[ ItemCreationRequest.SMITH_WEAPON ] = PERMIT_METHOD[ ItemCreationRequest.SMITH ] && KoLCharacter.canSmithWeapons();
		ADVENTURE_USAGE[ ItemCreationRequest.SMITH_WEAPON ] = 1;

		PERMIT_METHOD[ ItemCreationRequest.SMITH_ARMOR ] = PERMIT_METHOD[ ItemCreationRequest.SMITH ] && KoLCharacter.canSmithArmor();
		ADVENTURE_USAGE[ ItemCreationRequest.SMITH_ARMOR ] = 1;

		// Standard smithing is also possible if the person is in
		// a muscle sign.

		if ( KoLCharacter.inMuscleSign() )
		{
			PERMIT_METHOD[ ItemCreationRequest.SMITH ] = true;
			ADVENTURE_USAGE[ ItemCreationRequest.SMITH ] = 0;
		}
		else
			ADVENTURE_USAGE[ ItemCreationRequest.SMITH ] = 1;

		// Jewelry making is possible as long as the person has the
		// appropriate pliers.

		PERMIT_METHOD[ ItemCreationRequest.JEWELRY ] = KoLCharacter.getInventory().contains( PLIERS );
		ADVENTURE_USAGE[ ItemCreationRequest.JEWELRY ] = 3;

		// Star charts and pixel chart recipes are available to all
		// players at all times.

		PERMIT_METHOD[ ItemCreationRequest.STARCHART ] = true;
		ADVENTURE_USAGE[ ItemCreationRequest.STARCHART ] = 0;

		PERMIT_METHOD[ ItemCreationRequest.PIXEL ] = true;
		ADVENTURE_USAGE[ ItemCreationRequest.PIXEL ] = 0;

		// A rolling pin or unrolling pin can be always used in item
		// creation because we can get the same effect even without the
		// tool.

		PERMIT_METHOD[ ItemCreationRequest.ROLLING_PIN ] = true;
		ADVENTURE_USAGE[ ItemCreationRequest.ROLLING_PIN ] = 0;

		// The gnomish tinkerer is available if the person is in a
		// moxie sign and they have a bitchin' meat car.

		PERMIT_METHOD[ ItemCreationRequest.TINKER ] = KoLCharacter.inMoxieSign() && KoLCharacter.getInventory().contains( CAR );
		ADVENTURE_USAGE[ ItemCreationRequest.TINKER ] = 0;

		// It's not possible to ask Uncle Crimbo to make toys

		PERMIT_METHOD[ ItemCreationRequest.TOY ] = false;
		ADVENTURE_USAGE[ ItemCreationRequest.TOY ] = 0;
	}

	private static boolean isAvailable( int servantID, int clockworkID )
	{
		// If it's a base case, return whether or not the
		// servant is already available at the camp.

		if ( servantID == CHEF && KoLCharacter.hasChef() )
			return true;
		if ( servantID == BARTENDER && KoLCharacter.hasBartender() )
			return true;

		// If the user did not wish to repair their boxes
		// on explosion, then the box servant is not available

		if ( getProperty( "autoRepairBoxes" ).equals( "false" ) )
			return false;

		// Otherwise, return whether or not the quantity possible for
		// the given box servants is non-zero.	This works because
		// cooking tests are made after item creation tests.

		return concoctions.get( servantID ).total > 0 || concoctions.get( clockworkID ).total > 0;
	}

	/**
	 * Returns the mixing method for the item with the given ID.
	 */

	public static int getMixingMethod( int itemID )
	{	return concoctions.get( itemID ).getMixingMethod();
	}

	private static final AdventureResult SCHLITZ = new AdventureResult( 41, 1 );
	private static final AdventureResult WILLER = new AdventureResult( 81, 1 );
	private static final AdventureResult KETCHUP = new AdventureResult( 106, 1 );
	private static final AdventureResult CATSUP = new AdventureResult( 107, 1 );

	/**
	 * Returns the item IDs of the ingredients for the given item.
	 * Note that if there are no ingredients, then <code>null</code>
	 * will be returned instead.
	 */

	public static AdventureResult [] getIngredients( int itemID )
	{
		List availableIngredients = new ArrayList();
		availableIngredients.addAll( KoLCharacter.getInventory() );

		boolean showClosetDrivenCreations = getProperty( "showClosetDrivenCreations" ).equals( "true" );

		if ( showClosetDrivenCreations )
		{
			List closetList = (List) KoLCharacter.getCloset();
			for ( int i = 0; i < closetList.size(); ++i )
				AdventureResult.addResultToList( availableIngredients, (AdventureResult) closetList.get(i) );
		}

		// Ensure that you're retrieving the same ingredients that
		// were used in the calculations.  Usually this is the case,
		// but ice-cold beer and ketchup are tricky cases.

		AdventureResult [] ingredients = concoctions.get( itemID ).getIngredients();

		for ( int i = 0; i < ingredients.length; ++i )
		{
			if ( ingredients[i].getItemID() == SCHLITZ.getItemID() || ingredients[i].getItemID() == WILLER.getItemID() )
				ingredients[i] = getBetterIngredient( SCHLITZ, WILLER, availableIngredients );
			else if ( ingredients[i].getItemID() == KETCHUP.getItemID() || ingredients[i].getItemID() == CATSUP.getItemID() )
				ingredients[i] = getBetterIngredient( KETCHUP, CATSUP, availableIngredients );
		}

		return ingredients;
	}

	private static AdventureResult getBetterIngredient( AdventureResult ingredient1, AdventureResult ingredient2, List availableIngredients )
	{	return ingredient1.getCount( availableIngredients ) > ingredient2.getCount( availableIngredients ) ? ingredient1 : ingredient2;
	}

	/**
	 * Internal class used to represent a single concoction.  It
	 * contains all the information needed to actually make the item.
	 */

	private static class Concoction
	{
		private AdventureResult concoction;
		private int mixingMethod;
		private boolean wasPossible;

		private List ingredients;
		private AdventureResult [] ingredientArray;

		private int multiplier;
		private int initial, creatable, total;

		public Concoction( AdventureResult concoction, int mixingMethod )
		{
			this.concoction = concoction;
			this.mixingMethod = mixingMethod;
			this.wasPossible = false;

			this.ingredients = new ArrayList();
			this.ingredientArray = new AdventureResult[0];
		}

		public void resetCalculations()
		{
			this.initial = -1;
			this.creatable = 0;
			this.total = 0;

			this.multiplier = 0;
		}

		public void setPossible( boolean wasPossible )
		{	this.wasPossible = wasPossible;
		}

		public boolean wasPossible()
		{	return wasPossible;
		}

		public void addIngredient( AdventureResult ingredient )
		{
			knownUses.get( ingredient.getItemID() ).add( concoction );

			ingredients.add( ingredient );
			ingredientArray = new AdventureResult[ ingredients.size() ];
			ingredients.toArray( ingredientArray );
		}

		public int getMixingMethod()
		{	return mixingMethod;
		}

		public boolean isBadRecipe()
		{
			for ( int i = 0; i < ingredientArray.length; ++i )
			{
				AdventureResult ingredient = ingredientArray[i];
				if ( ingredient == null || ingredient.getItemID() == -1 || ingredient.getName() == null )
					return true;
			}

			return false;
		}

		public AdventureResult [] getIngredients()
		{	return ingredientArray;
		}

		public void calculate( List availableIngredients )
		{
			// If a calculation has already been done for this
			// concoction, no need to calculate again.

			if ( this.initial != -1 )
				return;

			// Initialize creatable item count to 0.  This way,
			// you ensure that you're not always off by one.

			this.creatable = 0;

			// If the item doesn't exist in the item table,
			// then assume it can't be created.

			if ( concoction.getName() == null )
			{
				this.initial = 0;
				return;
			}

			// Determine how many were available initially in the
			// available ingredient list.

			this.initial = concoction.getCount( availableIngredients );
			this.total = initial;

			if ( this.mixingMethod == ItemCreationRequest.NOCREATE || !isPermittedMethod( mixingMethod ) )
				return;

			// First, preprocess the ingredients by calculating
			// how many of each ingredient is possible now.

			for ( int i = 0; i < ingredientArray.length; ++i )
				concoctions.get( ingredientArray[i].getItemID() ).calculate( availableIngredients );

			boolean inMuscleSign = KoLCharacter.inMuscleSign();
			this.mark( 1, inMuscleSign );

			// With all of the data preprocessed, calculate
			// the quantity creatable by solving the set of
			// linear inequalities.

			if ( mixingMethod == ItemCreationRequest.ROLLING_PIN || mixingMethod == ItemCreationRequest.CLOVER )
			{
				// If there's only one ingredient, then the
				// quantity depends entirely on it.

				this.creatable = concoctions.get( ingredientArray[0].getItemID() ).initial;
			}
			else
			{
				this.creatable = Integer.MAX_VALUE;
				for ( int i = 0; i < ingredientArray.length; ++i )
					this.creatable = Math.min( this.creatable, concoctions.get( ingredientArray[i].getItemID() ).quantity( inMuscleSign ) );

			}
			this.total = this.initial + this.creatable;

			// Now that all the calculations are complete, unmark
			// the ingredients so that later calculations can make
			// the correct calculations.

			this.unmark();
		}

		/**
		 * Utility method which calculates the available quantity of a
		 * recipe based on the multiplier of its ingredients
		 */

		private int quantity( boolean inMuscleSign )
		{
			// If there is no multiplier, assume that an infinite
			// number is available.

			if ( this.multiplier == 0 )
				return Integer.MAX_VALUE;

			// The maximum value is equivalent to the total divided
			// by the multiplier.

			int quantity = this.total / this.multiplier;

			// Avoid mutual recursion.

			if ( mixingMethod == ItemCreationRequest.ROLLING_PIN || mixingMethod == ItemCreationRequest.CLOVER )
				return quantity;

			// If not creatable, don't look at ingredients

			if ( this.mixingMethod == ItemCreationRequest.NOCREATE || !isPermittedMethod( mixingMethod ) )
				return quantity;

			// The true value is affected by the maximum value for
			// the ingredients.  Therefore, calculate the quantity
			// for all the ingredients to complete the solution
			// of the linear inequality.

			quantity = Integer.MAX_VALUE;
			for ( int i = 0; quantity > 0 && i < ingredientArray.length; ++i )
				quantity = Math.min( quantity, concoctions.get( ingredientArray[i].getItemID() ).quantity( inMuscleSign ) );

			// Adventures are also considered an ingredient; if
			// no adventures are necessary, the multiplier should
			// be zero and the infinite number available will have
			// no effect on the calculation.

			if ( quantity > 0 && this != concoctions.get(0) )
				quantity = Math.min( quantity, concoctions.get(0).quantity( inMuscleSign ) );

			// If this is item combination and the person is in a
			// non-muscle sign, item creation requires meat paste.

			if ( quantity > 0 && mixingMethod == ItemCreationRequest.COMBINE && !inMuscleSign )
				quantity = Math.min( quantity, concoctions.get( ItemCreationRequest.MEAT_PASTE ).quantity( inMuscleSign ) );

			// The true value is now calculated.  Return this
			// value to the requesting method.

			return (this.initial + quantity) / this.multiplier;
		}

		/**
		 * Utility method which marks the ingredient for usage with
		 * the given multiplier.
		 */

		private void mark( int multiplier, boolean inMuscleSign )
		{
			this.multiplier += multiplier;

			// Avoid mutual recursion

			if ( mixingMethod == ItemCreationRequest.ROLLING_PIN || mixingMethod == ItemCreationRequest.CLOVER )
				return;

			// If not creatable, don't look at ingredients

			if ( mixingMethod == ItemCreationRequest.NOCREATE || !isPermittedMethod( mixingMethod ) )
				return;

			// Mark all the ingredients, being sure to multiply
			// by the number of that ingredient needed in this
			// concoction.

			int instanceCount;

			for ( int i = 0; i < ingredientArray.length; ++i )
			{
				// In order to ensure that the multiplier
				// is added correctly, make sure you count
				// the ingredient as many times as it appears,
				// but only multi-count the ingredient once.

				instanceCount = ingredientArray[i].getCount();

				for ( int j = 0; j < i; ++j )
					if ( ingredientArray[i].getItemID() == ingredientArray[j].getItemID() )
						instanceCount += ingredientArray[j].getCount();

				// If the ingredient has already been counted
				// before, continue with the next ingredient.

				if ( instanceCount > ingredientArray[i].getCount() )
					continue;

				// Now that you know that this is the first
				// time the ingredient has been seen, proceed.

				instanceCount = ingredientArray[i].getCount();

				for ( int j = i + 1; j < ingredientArray.length; ++j )
					if ( ingredientArray[i].getItemID() == ingredientArray[j].getItemID() )
						instanceCount += ingredientArray[j].getCount();

				concoctions.get( ingredientArray[i].getItemID() ).mark(
					this.multiplier * instanceCount, inMuscleSign );
			}

			// Mark the implicit adventure ingredient, being
			// sure to multiply by the number of adventures
			// which are required for this mixture.

			if ( this != concoctions.get(0) && ADVENTURE_USAGE[ mixingMethod ] != 0 )
				concoctions.get(0).mark( this.multiplier * ADVENTURE_USAGE[ mixingMethod ], inMuscleSign );

			// In the event that this is a standard combine request,
			// and the person is not in a muscle sign, make sure that
			// meat paste is marked as a limiter also.

			if ( mixingMethod == ItemCreationRequest.COMBINE && !inMuscleSign )
				concoctions.get( ItemCreationRequest.MEAT_PASTE ).mark( this.multiplier, inMuscleSign );
		}

		/**
		 * Utility method which undoes the yielding process, resetting
		 * the ingredient and current total values to the given number.
		 */

		private void unmark()
		{
			if ( this.multiplier == 0 )
				return;

			this.multiplier = 0;

			for ( int i = 0; i < ingredientArray.length; ++i )
				concoctions.get( ingredientArray[i].getItemID() ).unmark();

			if ( this != concoctions.get(0) )
				concoctions.get(0).unmark();

			if ( mixingMethod == ItemCreationRequest.COMBINE )
				concoctions.get( ItemCreationRequest.MEAT_PASTE ).unmark();
		}

		/**
		 * Returns the string form of this concoction.  This is
		 * basically the display name for the item created.
		 */

		public String toString()
		{	return concoction.getName();
		}
	}



	/**
	 * Internal class which functions exactly an array of sorted lists,
	 * except it uses "sets" and "gets" like a list.  This could be
	 * done with generics (Java 1.5) but is done like this so that
	 * we get backwards compatibility.
	 */

	private static class SortedListModelArray
	{
		private ArrayList internalList = new ArrayList();

		public SortedListModel get( int index )
		{
			if ( index < 0 )
				return null;

			while ( index >= internalList.size() )
				internalList.add( new SortedListModel() );

			return (SortedListModel) internalList.get( index );
		}

		public void set( int index, SortedListModel value )
		{
			while ( index >= internalList.size() )
				internalList.add( new SortedListModel() );

			internalList.set( index, value );
		}
	}


	/**
	 * Internal class which functions exactly an array of concoctions,
	 * except it uses "sets" and "gets" like a list.  This could be
	 * done with generics (Java 1.5) but is done like this so that
	 * we get backwards compatibility.
	 */

	private static class ConcoctionArray
	{
		private ArrayList internalList = new ArrayList();

		public Concoction get( int index )
		{
			if ( index < 0 )
				return null;

			for ( int i = internalList.size(); i <= index; ++i )
				internalList.add( new Concoction( new AdventureResult( i, 0 ), ItemCreationRequest.NOCREATE ) );

			return (Concoction) internalList.get( index );
		}

		public void set( int index, Concoction value )
		{
			for ( int i = internalList.size(); i <= index; ++i )
				internalList.add( new Concoction( new AdventureResult( i, 0 ), ItemCreationRequest.NOCREATE ) );

			internalList.set( index, value );
		}

		public int size()
		{	return internalList.size();
		}
	}
}
