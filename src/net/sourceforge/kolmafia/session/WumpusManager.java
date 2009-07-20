/**
 * Copyright (c) 2005-2009, KoLmafia development team
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

package net.sourceforge.kolmafia.session;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.utilities.StringUtilities;

public abstract class WumpusManager
{
	public static HashMap warnings = new HashMap();

	// Links from current room
	public static String[] links = null;

	// Locations of hazards
	public static String bats1 = null;
	public static String bats2 = null;
	public static String pit1 = null;
	public static String pit2 = null;
	public static String wumpus = null;

	public static final int WARN_SAFE = 0;
	public static final int WARN_BATS = 1;
	public static final int WARN_PIT = 2;
	public static final int WARN_WUMPUS = 4;
	public static final int WARN_INDEFINITE = 8;
	public static final int WARN_ALL = 15;

	public static final String[] CHAMBERS =
	{
		"acrid",
		"breezy",
		"creepy",
		"dripping",
		"echoing",
		"fetid",
		"gloomy",
		"howling",
		"immense",
		null,	// j
		null,	// k
		"long",
		"moaning",
		"narrow",
		"ordinary",
		"pillared",
		"quiet",
		"round",
		"sparkling",
		null,	// t
		"underground",
		"vaulted",
		"windy",
		null,	// x
		null,	// y
		null,	// z
	};

	public static String[] WARN_STRINGS = new String[]
	{
		"safe",
		"definite bats",
		"definite pit",
		"ERROR: BATS AND PIT",
		"definite Wumpus",
		"ERROR: BATS AND WUMPUS",
		"ERROR: PIT AND WUMPUS",
		"ERROR: BATS, PIT, AND WUMPUS",

		"safe and unvisited",
		"possible bats",
		"possible pit",
		"possible bats or pit",
		"possible Wumpus",
		"possible bats or Wumpus",
		"possible pit or Wumpus",
		"possible bats, pit, or Wumpus",
	};

	private static final Pattern ROOM_PATTERN = Pattern.compile( ">The (\\w+) Chamber<" );
	private static final Pattern LINK_PATTERN = Pattern.compile( "Enter the (\\w+) chamber" );

	public static void reset()
	{
		// Clear out old room data
		WumpusManager.warnings.clear();

		// Initialize all rooms to unknown
		Integer all = new Integer( WARN_ALL );
		for ( int i = 0; i < CHAMBERS.length; ++i )
		{
			String room = CHAMBERS[ i ];
			if ( room != null )
			{
				WumpusManager.warnings.put( room, all );
			}
		}

		// We are not currently in a room
		WumpusManager.links = null;

		// We don't know where any of the hazards are
		WumpusManager.bats1 = null;
		WumpusManager.bats2 = null;
		WumpusManager.pit1 = null;
		WumpusManager.pit2 = null;
		WumpusManager.wumpus = null;
	}

	private static int get( String room )
	{
		Integer i = (Integer) WumpusManager.warnings.get( room );
		return i == null ? WARN_ALL : i.intValue();
	}
	
	private static int put( String room, int value )
	{
		int old = WumpusManager.get( room );
		if ( ( old & WARN_INDEFINITE ) != 0 )
		{
			WumpusManager.warnings.put( room, new Integer( old & value ) );
		}
		return old;
	}

	// Deductions we made from visiting this room
	public static final StringBuffer deductions = new StringBuffer();

	// Types of deductions
	public static final int NONE = 0;
	public static final int VISIT = 1;
	public static final int LISTEN = 2;
	public static final int ELIMINATION = 3;
	public static final int DEDUCTION = 4;

	public static String[] DEDUCTION_STRINGS = new String[]
	{
		"None",
		"Visit",
		"Listen",
		"Elimination",
		"Deduction"
	};

	public static void visitChoice( String text )
	{
		WumpusManager.links = null;
		WumpusManager.deductions.setLength( 0 );

		if ( text == null )
		{
			return;
		}

		Matcher m = WumpusManager.ROOM_PATTERN.matcher( text );
		if ( !m.find() )
		{
			// Shouldn't happen: if there is a choice option to
			// take, there must be a room name.
			return;
		}
		
		String current = m.group( 1 ).toLowerCase();

		if ( text.indexOf( "Wait for the bats to drop you" ) != -1 )
		{
			WumpusManager.knownBats( current, VISIT );
			return;
		}

		if ( text.indexOf( "Thump" ) != -1 )
		{
			WumpusManager.knownPit( current, VISIT );
			return;
		}

		// Initialize the array of rooms accessible from here

		WumpusManager.links = new String[ 3 ];
		m = WumpusManager.LINK_PATTERN.matcher( text );
		for ( int i = 0; i < 3; ++i )
		{
			if ( !m.find() )
			{
				// Should not happen; there are always three
				// exits from a room.
				links = null;
				return;
			}
			links[ i ] = m.group( 1 ).toLowerCase();
		}

		WumpusManager.printDeduction( "Exits: " + links[0] + ", " + links[1] + ", " + links[2] );

		WumpusManager.knownSafe( current, VISIT );

		// Basic logic: assume all rooms have all warnings initially.
		// Remove any warnings from linked rooms that aren't present
		// in the current room.

		int warn = WARN_INDEFINITE;
		if ( text.indexOf( "squeaking coming from somewhere nearby" ) != -1 )
		{
			warn |= WARN_BATS;
		}
		if ( text.indexOf( "hear a low roaring sound nearby" ) != -1 )
		{
			warn |= WARN_PIT;
		}
		if ( text.indexOf( "the Wumpus must be nearby" ) != -1 )
		{
			warn |= WARN_WUMPUS;
		}

		for ( int i = 0; i < 3; ++i )
		{
			WumpusManager.possibleHazard( links[ i ], warn, LISTEN );
		}
		
		// Advanced logic: if only one of the linked rooms has a given
		// warning, promote that to a definite danger, and remove any
		// other warnings from that room.

		WumpusManager.deduce();

		// Doing this may make further deductions possible.

		WumpusManager.deduce();

		// I'm not sure if a 3rd deduction is actually possible, but it
		// doesn't hurt to try.

		WumpusManager.deduce();
	}

	private static void knownSafe( final String room, final int type )
	{
		WumpusManager.knownHazard( room, WARN_SAFE, type );
	}

	private static void knownBats( final String room, final int type  )
	{
		WumpusManager.knownHazard( room, WARN_BATS, type );

		// There are exactly two bat rooms per cave
		if ( WumpusManager.bats1 == null )
		{
			// We've just identified the first bat room
			WumpusManager.bats1 = room;
			return;
		}

		if ( WumpusManager.bats1.equals( room ) || WumpusManager.bats2 != null )
		{
			return;
		}

		// We've just identified the second bat room
		WumpusManager.bats2 = room;

		// Eliminate bats from rooms that have only "possible" bats
		WumpusManager.eliminateHazard( WARN_BATS, WumpusManager.bats1, WumpusManager.bats2 );
	}

	private static void knownPit( final String room, final int type )
	{
		WumpusManager.knownHazard( room, WARN_PIT, type );

		// There are exactly two pit rooms per cave
		if ( WumpusManager.pit1 == null )
		{
			// We've just identified the first pit room
			WumpusManager.pit1 = room;
			return;
		}

		if ( WumpusManager.pit1.equals( room ) || WumpusManager.pit2 != null )
		{
			return;
		}

		// We've just identified the second pit room
		WumpusManager.pit2 = room;

		// Eliminate pits from rooms that have only "possible" pit
		WumpusManager.eliminateHazard( WARN_PIT, WumpusManager.pit1, WumpusManager.pit2 );
	}

	private static void knownWumpus( final String room, final int type )
	{
		WumpusManager.knownHazard( room, WARN_WUMPUS, type );

		// There is exactly one wumpus rooms per cave
		if ( WumpusManager.wumpus != null )
		{
			return;
		}

		// We've just identified the wumpus room
		WumpusManager.wumpus = room;

		// Eliminate wumpus from rooms that have only "possible" wumpus
		WumpusManager.eliminateHazard( WARN_WUMPUS, WumpusManager.wumpus, null );
	}
	
	private static void knownHazard( final String room, int warn, final int type )
	{
		int oldStatus = WumpusManager.put( room, warn );
		int newStatus = WumpusManager.get( room );
		if ( oldStatus == newStatus )
		{
			return;
		}

		// New deduction
		String idString = WumpusManager.DEDUCTION_STRINGS[ type ];
		String warnString = WumpusManager.WARN_STRINGS[ newStatus ];

		WumpusManager.addDeduction( idString + ": " + warnString + " in " + room + " chamber." );
	}
	
	private static void eliminateHazard( final int hazard, final String room1, final String room2 )
	{
		for ( int i = 0; i < CHAMBERS.length; ++i )
		{
			String chamber = CHAMBERS[ i ];
			if ( chamber == null || chamber.equals( room1 ) || chamber.equals( room2 ) )
			{
				continue;
			}

			Integer old = (Integer) WumpusManager.warnings.get( chamber );
			if ( old == null )
			{
				continue;
			}

			WumpusManager.possibleHazard( chamber, old.intValue() & ~hazard, DEDUCTION );
		}
	}
	
	private static void possibleHazard( final String room, int warn, final int type )
	{
		// If we have already positively identified this as a room with
		// a hazard, nothing more to do here.

		if ( room.equals( WumpusManager.bats1 ) ||
		     room.equals( WumpusManager.bats2 ) ||
		     room.equals( WumpusManager.pit1 ) ||
		     room.equals( WumpusManager.pit2 ) ||
		     room.equals( WumpusManager.wumpus ) )
		{
			return;
		}

		// If it's a definite hazard, pass it on through
		if ( ( warn & WARN_INDEFINITE ) == 0)
		{
			WumpusManager.knownHazard( room, warn, type );
			return;
		}

		// Otherwise, it's a possible warning.

		if ( ( warn & WARN_BATS ) != 0 &&
		     WumpusManager.bats1 != null &&
		     WumpusManager.bats2 != null )
		{
			warn &= ~WARN_BATS;
		}

		if ( ( warn & WARN_PIT ) != 0 &&
		     WumpusManager.pit1 != null &&
		     WumpusManager.pit2 != null )
		{
			warn &= ~WARN_PIT;
		}

		if ( ( warn & WARN_WUMPUS ) != 0 &&
		     WumpusManager.wumpus != null )
		{
			warn &= ~WARN_WUMPUS;
		}

		// Register possible hazard
		WumpusManager.knownHazard( room, warn, type );
	}

	private static void deduce()
	{
		WumpusManager.deduce( WARN_BATS );
		WumpusManager.deduce( WARN_PIT );
		WumpusManager.deduce( WARN_WUMPUS );
	}

	private static void deduce( int mask )
	{
		int which = -1;
		for ( int i = 0; i < 3; ++i )
		{
			if ( (WumpusManager.get( WumpusManager.links[ i ] ) & mask) != 0 )
			{
				if ( which != -1 )
				{
					return;	// warning not unique
				}
				which = i;
			}
		}

		if ( which == -1 )
		{
			return;
		}

		String room = WumpusManager.links[ which ];
		switch ( mask )
		{
		case WARN_BATS:
			WumpusManager.knownBats( room, ELIMINATION );
			break;
		case WARN_PIT:
			WumpusManager.knownPit( room, ELIMINATION );
			break;
		case WARN_WUMPUS:
			WumpusManager.knownWumpus( room, ELIMINATION );
			break;
		}
	}
	
	public static void takeChoice( int decision, String text )
	{
		if ( WumpusManager.links == null )
		{
			return;
		}

		// There can be 6 decisions - stroll into 3 rooms or charge
		// into 3 rooms.
		if ( decision > 3 )
		{
			decision -= 3;
		}

		String room = WumpusManager.links[ decision - 1 ];

		// Unfortunately, the wumpus was nowhere to be seen.
		if ( text.indexOf( "wumpus was nowhere to be seen" ) != -1  )
		{
			WumpusManager.knownSafe( room, VISIT );
			return;
		}

		// You stroll nonchalantly into the cavern chamber and find,
		// unexpectedly, a wumpus.
		//  or
		// Now that you have successfully snuck up and surprised the
		// wumpus, it doesn't seem to really know how to react.

		if ( text.indexOf( "unexpectedly, a wumpus" ) != -1 ||
		     text.indexOf( "surprised the wumpus" ) != -1 )
		{
			WumpusManager.knownWumpus( room, VISIT );
			return;
		}
	}

	public static String[] dynamicChoiceOptions( String text )
	{
		if ( links == null )
		{
			return new String[ 0 ];
		}

		String[] results = new String[ 6 ];
		for ( int i = 0; i < 3; ++i )
		{
			String warning = WumpusManager.WARN_STRINGS[ WumpusManager.get( links[i] ) ];
			results[ i ] = warning;
			results[ i + 3 ] = warning;
		}

		return results;
	}

	private static void printDeduction( final String text )
	{
		RequestLogger.printLine( text );
		RequestLogger.updateSessionLog( text );
	}

	private static void addDeduction( final String text )
	{
		// Print the string to the CLI and session log
		WumpusManager.printDeduction( text );

		if ( WumpusManager.deductions.length() == 0 )
		{
			WumpusManager.deductions.append( "<center>" );
		}
		else
		{
			WumpusManager.deductions.append( "<br>" );
		}

		WumpusManager.deductions.append( text ); 
	}

	public static final void decorate( final StringBuffer buffer )
	{
		if ( WumpusManager.deductions.length() == 0 )
		{
			return;
		}

		int index = buffer.indexOf( "<center><form name=choiceform1" );
		if ( index == -1 )
		{
			return;
		}

		WumpusManager.deductions.append( "</center><br>" );
		buffer.insert( index, WumpusManager.deductions.toString() );
		WumpusManager.deductions.setLength( 0 );
	}
}

