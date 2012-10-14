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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;

import net.java.dev.spellcast.utilities.LockableListModel;

/**
 * An extension of <code>KoLFrame</code> which handles all the clan
 * management functionality of Kingdom of Loathing.
 */

public class HagnkStorageFrame extends KoLFrame
{
	private JTabbedPane tabs;
	private HagnkStoragePanel all, consume, equip;
	private JCheckBoxMenuItem [] equipFilter;

	public HagnkStorageFrame( KoLmafia client )
	{
		super( client, "Ancestral Storage" );

		if ( client != null && client.getStorage().isEmpty() )
			(new RequestThread( new ItemStorageRequest( client ) )).start();

		tabs = new JTabbedPane();
		all = new HagnkStoragePanel();
		consume = new HagnkStoragePanel();
		equip = new HagnkStoragePanel();

		addTab( "All Items", all );
		addTab( "Consumables", consume );
		addTab( "Equipment", equip );

		getContentPane().setLayout( new CardLayout( 10, 10 ) );
		getContentPane().add( tabs, "" );
		addMenuBar();
		refreshFilters();
	}

	private void addTab( String name, HagnkStoragePanel panel )
	{
		JPanel wrapperPanel = new JPanel();
		wrapperPanel.setLayout( new CardLayout( 10, 10 ) );
		wrapperPanel.add( panel, "" );
		tabs.add( name, wrapperPanel );
	}

	private void addMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar( menuBar );

		addConsumeMenu( menuBar );

		JMenu equipMenu = new JMenu( "Equipment" );
		equipFilter = new JCheckBoxMenuItem[5];
		equipFilter[0] = new FilterMenuItem( "Show weapons" );
		equipFilter[1] = new FilterMenuItem( "Show hats" );
		equipFilter[2] = new FilterMenuItem( "Show shirts" );
		equipFilter[3] = new FilterMenuItem( "Show pants" );
		equipFilter[4] = new FilterMenuItem( "Show accessories" );

		for ( int i = 0; i < equipFilter.length; ++i )
			equipMenu.add( equipFilter[i] );
		menuBar.add( equipMenu );
	}

	public void refreshFilters()
	{
		consume.elementList.setCellRenderer( AdventureResult.getConsumableCellRenderer(
			consumeFilter[0].isSelected(), consumeFilter[1].isSelected(), consumeFilter[2].isSelected() ) );

		equip.elementList.setCellRenderer( AdventureResult.getEquipmentCellRenderer( equipFilter[0].isSelected(),
			equipFilter[1].isSelected(), equipFilter[2].isSelected(), equipFilter[3].isSelected(), equipFilter[4].isSelected() ) );
	}

	public void setEnabled( boolean isEnabled )
	{
		if ( all != null )
			all.setEnabled( isEnabled );
		if ( consume != null )
			consume.setEnabled( isEnabled );
		if ( equip != null )
			equip.setEnabled( isEnabled );
	}

	/**
	 * Internal class used to handle everything related to
	 * placing items into the stash.
	 */

	private class HagnkStoragePanel extends ItemManagePanel
	{
		public HagnkStoragePanel()
		{	super( "Inside Storage", "put in bag", "put in closet", client == null ? new LockableListModel() : client.getStorage() );
		}

		protected void actionConfirmed()
		{	withdraw( false );
		}

		protected void actionCancelled()
		{	withdraw( true );
		}

		private void withdraw( boolean isCloset )
		{
			Object [] items = elementList.getSelectedValues();
			AdventureResult selection;

			for ( int i = 0; i < items.length; ++i )
			{
				selection = (AdventureResult) items[i];
				items[i] = new AdventureResult( selection.getItemID(),
					getQuantity( "Retrieving " + selection.getName() + " from the storage...", selection.getCount() ) );
			}

			Runnable [] requests = isCloset ? new Runnable[2] : new Runnable[1];
			requests[0] = new ItemStorageRequest( client, ItemStorageRequest.STORAGE_TO_INVENTORY, items );

			if ( isCloset )
				requests[1] = new ItemStorageRequest( client, ItemStorageRequest.INVENTORY_TO_CLOSET, items );

			(new RequestThread( requests )).start();
		}
	}

	public static void main( String [] args )
	{
		Object [] parameters = new Object[1];
		parameters[0] = null;

		(new CreateFrameRunnable( HagnkStorageFrame.class, parameters )).run();
	}
}
