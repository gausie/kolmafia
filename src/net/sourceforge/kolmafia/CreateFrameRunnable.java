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

import java.awt.Dimension;
import javax.swing.SwingUtilities;
import java.lang.reflect.Constructor;

/**
 * A special class which ensures that frames can be created inside
 * of the Swing thread.  This avoids deadlock problems that often
 * cause KoLmafia not to load properly.
 */

public class CreateFrameRunnable implements Runnable, KoLConstants
{
	private static final Class [] SINGLE_INSTANCE =
	{
		AdventureFrame.class,
		BuffBotFrame.class,
		BuffRequestFrame.class,
		CalendarFrame.class,
		CakeArenaFrame.class,
		CharsheetFrame.class,
		ClanManageFrame.class,
		CommandDisplayFrame.class,
		ContactListFrame.class,
		CouncilFrame.class,
		ExamineItemsFrame.class,
		FamiliarTrainingFrame.class,
		FightFrame.class,
		GearChangeFrame.class,
		HagnkStorageFrame.class,
		ItemManageFrame.class,
		LoginFrame.class,
		MailboxFrame.class,
		MuseumFrame.class,
		MushroomFrame.class,
		OptionsFrame.class,
		PendingTradesFrame.class,
		SendMessageFrame.class,
		StoreManageFrame.class,
		TabbedChatFrame.class
	};

	private Class creationType;
	private KoLFrame creation;

	private Constructor creator;
	private Object [] parameters;

	public CreateFrameRunnable( Class creationType )
	{	this( creationType, new Object[0] );
	}

	public CreateFrameRunnable( Class creationType, Object [] parameters )
	{
		this.creationType = creationType;
		this.parameters = parameters;

		Class [] parameterTypes= new Class[ parameters.length ];
		for ( int i = 0; i < parameters.length; ++i )
			parameterTypes[i] = parameters[i] == null ? null : parameters[i].getClass();

		this.creator = null;
		boolean isValidConstructor = true;
		Class [] constructorParameterTypes;
		Constructor [] constructors = creationType.getConstructors();

		for ( int i = 0; i < constructors.length; ++i )
		{
			constructorParameterTypes = constructors[i].getParameterTypes();
			if ( constructorParameterTypes.length != parameters.length )
				continue;

			isValidConstructor = true;
			for ( int j = 0; j < constructorParameterTypes.length && isValidConstructor; ++j )
				if ( parameterTypes[j] != null && !constructorParameterTypes[j].isAssignableFrom( parameterTypes[j] ) )
					isValidConstructor = false;

			if ( isValidConstructor )
				this.creator = constructors[i];
		}
	}

	public KoLFrame getCreation()
	{	return creation;
	}

	public void run()
	{
		// If there is no creation creation, then return
		// from the method because there's nothing to do.

		if ( this.creator == null )
		{
			DEFAULT_SHELL.updateDisplay( ERROR_STATE, "Frame could not be loaded." );
			return;
		}

		// If you are not in the Swing thread, then wait
		// until you are in the Swing thread before making
		// the object to avoid deadlocks.

		if ( !SwingUtilities.isEventDispatchThread() )
		{
			try
			{
				SwingUtilities.invokeAndWait( this );
				return;
			}
			catch ( Exception e )
			{
				e.printStackTrace( KoLmafia.getLogStream() );
				e.printStackTrace();
			}
		}

		// Check to see if this is a frame that should
		// only be loaded once, based on the static list.

		this.creation = null;

		KoLFrame currentFrame;
		Class currentType;

		for ( int i = 0; i < existingFrames.size(); ++i )
		{
			currentFrame = (KoLFrame) existingFrames.get(i);
			currentType = currentFrame.getClass();

			if ( currentType == creationType )
				for ( int j = 0; j < SINGLE_INSTANCE.length; ++j )
					if ( currentType == SINGLE_INSTANCE[j] )
						this.creation = currentFrame;
		}

		// Now, if you aren't supposed to create a new instance,
		// do not do so -- however, if it's okay to do so, then
		// go ahead and create it.

		try
		{
			if ( this.creation == null )
			{
				this.creation = (KoLFrame) creator.newInstance( parameters );
				this.creation.pack();
			}

			// Load the KoL frame to the appropriate location
			// on the screen now that the frame has been packed
			// to the appropriate size.

			KoLFrame frame = (KoLFrame) this.creation;
			String frameName = frame.getFrameName();

			if ( StaticEntity.getSettings().containsKey( frameName ) )
			{
				String [] location = StaticEntity.getSettings().getProperty( frameName ).split( "," );
				int xLocation = Integer.parseInt( location[0] );
				int yLocation = Integer.parseInt( location[1] );

				Dimension screenSize = TOOLKIT.getScreenSize();
				if ( xLocation > 0 && yLocation > 0 && xLocation < screenSize.getWidth() && yLocation < screenSize.getHeight() )
					frame.setLocation( xLocation, yLocation );
			}

			// With the location set set on screen, make sure
			// to disable it (if necessary), ensure the frame's
			// visibility on screen and request focus.

			this.creation.setEnabled( true );
			this.creation.setVisible( true );
			this.creation.requestFocus();
		}
		catch ( Exception e )
		{
			// If this happens, update the display to indicate
			// that it failed to happen (eventhough technically,
			// this should never have happened)

			DEFAULT_SHELL.updateDisplay( ERROR_STATE, "Frame could not be loaded." );
			e.printStackTrace( KoLmafia.getLogStream() );
			e.printStackTrace();

			return;
		}
	}
}
