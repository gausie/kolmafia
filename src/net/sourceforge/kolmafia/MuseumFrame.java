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

// layout
import java.awt.Color;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;

// event listeners
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

// containers
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

// other imports
import java.util.List;
import java.text.ParseException;
import net.java.dev.spellcast.utilities.SortedListModel;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * An extension of <code>KoLFrame</code> which handles all the item
 * management functionality of Kingdom of Loathing.  This ranges from
 * basic transfer to and from the Display to item creation, cooking,
 * item use, and equipment.
 */

public class MuseumFrame extends KoLFrame
{
	private JPanel storing;

	/**
	 * Constructs a new <code>MuseumFrame</code> and inserts all
	 * of the necessary panels into a tabular layout for accessibility.
	 *
	 * @param	client	The client to be notified in the event of error.
	 */

	public MuseumFrame( KoLmafia client )
	{
		super( "KoLmafia: Display Case", client );

		if ( client != null && client.getCollection().isEmpty() )
			(new MuseumRequest( client )).run();

		storing = new StoragePanel();

		getContentPane().setLayout( new CardLayout( 10, 10 ) );
		getContentPane().add( storing, "" );
		addWindowListener( new ReturnFocusAdapter() );
		setDefaultCloseOperation( HIDE_ON_CLOSE );
	}

	public void setEnabled( boolean isEnabled )
	{
		super.setEnabled( isEnabled );
		if ( storing != null )
			storing.setEnabled( isEnabled );
	}

	/**
	 * Internal class used to handle everything related to
	 * placing items into the Display and taking items from
	 * the Display.
	 */

	private class StoragePanel extends JPanel
	{
		private JList availableList, displayList;
		private ItemManagePanel inventoryPanel, displayPanel;

		public StoragePanel()
		{
			setLayout( new GridLayout( 2, 1, 10, 10 ) );

			inventoryPanel = new OutsideDisplayPanel();
			displayPanel = new InsideDisplayPanel();

			add( inventoryPanel );
			add( displayPanel );
		}

		public void setEnabled( boolean isEnabled )
		{
			super.setEnabled( isEnabled );
			inventoryPanel.setEnabled( isEnabled );
			displayPanel.setEnabled( isEnabled );
		}

		private class OutsideDisplayPanel extends ItemManagePanel
		{
			public OutsideDisplayPanel()
			{
				super( "Inventory", "add to display", "put in closet", client == null ? new LockableListModel() : client.getInventory() );
				availableList = elementList;
			}

			protected void actionConfirmed()
			{	(new InventoryStorageThread( false )).start();
			}

			protected void actionCancelled()
			{	(new InventoryStorageThread( true )).start();
			}

			public void setEnabled( boolean isEnabled )
			{
				super.setEnabled( isEnabled );
				availableList.setEnabled( isEnabled );
			}
		}

		private class InsideDisplayPanel extends ItemManagePanel
		{
			public InsideDisplayPanel()
			{
				super( "Display Case", "put in bag", "put in closet", client == null ? new LockableListModel() : client.getCollection() );
				displayList = elementList;
			}

			protected void actionConfirmed()
			{	(new DisplayStorageThread( false )).start();
			}

			protected void actionCancelled()
			{	(new DisplayStorageThread( true )).start();
			}

			public void setEnabled( boolean isEnabled )
			{
				super.setEnabled( isEnabled );
				displayList.setEnabled( isEnabled );
			}
		}

		/**
		 * In order to keep the user interface from freezing (or at
		 * least appearing to freeze), this internal class is used
		 * to actually move items around in the inventory.
		 */

		private class InventoryStorageThread extends RequestThread
		{
			private boolean isCloset;

			public InventoryStorageThread( boolean isCloset )
			{	this.isCloset = isCloset;
			}

			public void run()
			{
				Object [] items = availableList.getSelectedValues();

				Runnable request = isCloset ? (Runnable) new ItemStorageRequest( client, ItemStorageRequest.INVENTORY_TO_CLOSET, items ) :
					(Runnable) new MuseumRequest( client, true, items );

				request.run();
				client.updateDisplay( ENABLED_STATE, "Items moved." );
			}
		}

		/**
		 * In order to keep the user interface from freezing (or at
		 * least appearing to freeze), this internal class is used
		 * to actually move items around in the inventory.
		 */

		private class DisplayStorageThread extends RequestThread
		{
			private boolean isCloset;

			public DisplayStorageThread( boolean isCloset )
			{	this.isCloset = isCloset;
			}

			public void run()
			{
				Object [] items = displayList.getSelectedValues();
				(new MuseumRequest( client, false, items )).run();

				if ( isCloset )
					(new ItemStorageRequest( client, ItemStorageRequest.INVENTORY_TO_CLOSET, items )).run();

				client.updateDisplay( ENABLED_STATE, "Items moved." );
			}
		}
	}

	/**
	 * The main method used in the event of testing the way the
	 * user interface looks.  This allows the UI to be tested
	 * without having to constantly log in and out of KoL.
	 */

	public static void main( String [] args )
	{
		KoLFrame uitest = new MuseumFrame( null );
		uitest.pack();  uitest.setVisible( true );  uitest.requestFocus();
	}
}
