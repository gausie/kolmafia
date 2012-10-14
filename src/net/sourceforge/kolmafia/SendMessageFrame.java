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
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;

// event listeners
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

// containers
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

// other imports
import java.util.List;
import java.util.ArrayList;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

public abstract class SendMessageFrame extends KoLFrame
{
	protected JPanel messagePanel;
	protected JTextField recipientEntry;
	protected JTextArea [] messageEntry;
	protected JButton sendMessageButton;

	protected ShowDescriptionList attachmentList;
	protected LockableListModel inventory;
	protected AttachmentFrame attachFrame;

	protected JTextField attachedMeat;
	protected LockableListModel attachments;

	protected SendMessageFrame( KoLmafia client, String title )
	{
		super( client, title );

		inventory = client == null ? new LockableListModel() : client.getInventory();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.Y_AXIS ) );

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout( new BorderLayout( 5, 5 ) );
		centerPanel.add( Box.createHorizontalStrut( 40 ), BorderLayout.CENTER );

		centerPanel.add( constructWestPanel(), BorderLayout.WEST );

		JPanel attachmentPanel = new JPanel();
		attachmentPanel.setLayout( new BorderLayout( 10, 10 ) );

		JPanel enclosePanel = new JPanel();
		enclosePanel.setLayout( new BorderLayout() );
		enclosePanel.add( new JLabel( "Enclose these items:    " ), BorderLayout.WEST );

		JButton attachButton = new JButton( JComponentUtilities.getSharedImage( "icon_plus.gif" ) );
		JComponentUtilities.setComponentSize( attachButton, 20, 20 );
		attachButton.addActionListener( new AttachItemsListener() );
		enclosePanel.add( attachButton, BorderLayout.EAST );

		attachmentPanel.add( enclosePanel, BorderLayout.NORTH );

		this.attachments = new LockableListModel();
		this.attachmentList = new ShowDescriptionList( attachments );

		JScrollPane attachmentArea = new JScrollPane( attachmentList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		attachmentPanel.add( attachmentArea, BorderLayout.CENTER );

		JPanel meatPanel = new JPanel();
		meatPanel.setLayout( new BoxLayout( meatPanel, BoxLayout.X_AXIS ) );

		attachedMeat = new JTextField( "0" );
		JComponentUtilities.setComponentSize( attachedMeat, 120, 24 );
		meatPanel.add( Box.createHorizontalStrut( 40 ) );
		meatPanel.add( new JLabel( "Enclose meat:    " ) );
		meatPanel.add( attachedMeat );
		meatPanel.add( Box.createHorizontalStrut( 40 ) );

		attachmentPanel.add( meatPanel, BorderLayout.SOUTH );

		centerPanel.add( attachmentPanel, BorderLayout.EAST );

		mainPanel.add( centerPanel );
		mainPanel.add( Box.createVerticalStrut( 18 ) );

		sendMessageButton = new JButton( "Send Message" );
		sendMessageButton.addActionListener( new SendMessageListener() );

		JPanel sendMessageButtonPanel = new JPanel();
		sendMessageButtonPanel.add( sendMessageButton, BorderLayout.CENTER );

		mainPanel.add( sendMessageButtonPanel );
		mainPanel.add( Box.createVerticalStrut( 4 ) );

		messagePanel = new JPanel();
		messagePanel.setLayout( new BorderLayout() );
		messagePanel.add( mainPanel, BorderLayout.CENTER );

		this.getContentPane().setLayout( new CardLayout( 20, 20 ) );
		this.getContentPane().add( messagePanel, "" );
		this.getRootPane().setDefaultButton( sendMessageButton );
	}

	protected JPanel constructWestPanel()
	{
		String [] entryHeaders = getEntryHeaders();

		recipientEntry = new JTextField();
		JComponentUtilities.setComponentSize( recipientEntry, 300, 20 );

		messageEntry = new JTextArea[ entryHeaders.length ];
		JScrollPane [] scrollArea = new JScrollPane[ entryHeaders.length ];

		for ( int i = 0; i < messageEntry.length; ++i )
		{
			messageEntry[i] = new JTextArea();
			messageEntry[i].setLineWrap( true );
			messageEntry[i].setWrapStyleWord( true );
			scrollArea[i] = new JScrollPane( messageEntry[i], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		}

		JPanel recipientPanel = new JPanel();
		recipientPanel.setLayout( new BoxLayout( recipientPanel, BoxLayout.Y_AXIS ) );
		recipientPanel.add( getLabelPanel( "Send to this person:" ) );
		recipientPanel.add( Box.createVerticalStrut( 4 ) );
		recipientPanel.add( recipientEntry );
		recipientPanel.add( Box.createVerticalStrut( 20 ) );

		String [] westHeaders = getWestHeaders();
		Component [] westComponents = getWestComponents();

		for ( int i = 0; i < westHeaders.length; ++i )
		{
			recipientPanel.add( getLabelPanel( westHeaders[i] ) );
			recipientPanel.add( Box.createVerticalStrut( 4 ) );
			recipientPanel.add( westComponents[i] );
			recipientPanel.add( Box.createVerticalStrut( 20 ) );
		}

		JPanel entryPanel = new JPanel();
		entryPanel.setLayout( new GridLayout( entryHeaders.length, 1 ) );

		JPanel holderPanel;
		for ( int i = 0; i < entryHeaders.length; ++i )
		{
			holderPanel = new JPanel();
			holderPanel.setLayout( new BorderLayout( 5, 5 ) );
			holderPanel.add( getLabelPanel( entryHeaders[i] ), BorderLayout.NORTH );
			holderPanel.add( scrollArea[i], BorderLayout.CENTER );

			entryPanel.add( holderPanel );
		}

		JPanel westPanel = new JPanel();
		westPanel.setLayout( new BorderLayout() );
		westPanel.add( recipientPanel, BorderLayout.NORTH );
		westPanel.add( entryPanel, BorderLayout.CENTER );

		return westPanel;
	}

	protected String [] getEntryHeaders()
	{	return new String[0];
	}

	protected String [] getWestHeaders()
	{	return new String[0];
	}

	protected Component [] getWestComponents()
	{	return new Component[0];
	}

	protected JPanel getLabelPanel( String text )
	{
		JPanel label = new JPanel();
		label.setLayout( new GridLayout( 1, 1 ) );
		label.add( new JLabel( text, JLabel.LEFT ) );

		return label;
	}

	protected abstract void sendMessage();

	private class SendMessageListener implements ActionListener, Runnable
	{
		public void actionPerformed( ActionEvent e )
		{	(new DaemonThread( this )).start();
		}

		public void run()
		{
			if ( client != null )
				sendMessage();
		}
	}

	private class AttachItemsListener implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{
			Object [] parameters = new Object[3];
			parameters[0] = null;
			parameters[1] = inventory;
			parameters[2] = attachments;

			(new CreateFrameRunnable( AttachmentFrame.class, parameters )).run();
		}
	}

	/**
	 * Sets all of the internal panels to a disabled or enabled state; this
	 * prevents the user from modifying the data as it's getting sent, leading
	 * to uncertainty and generally bad things.
	 */

	public void setEnabled( boolean isEnabled )
	{
		if ( messageEntry != null )
			for ( int i = 0; i < messageEntry.length; ++i )
				if ( messageEntry[i] != null )
					messageEntry[i].setEnabled( isEnabled );

		if ( sendMessageButton != null )
			sendMessageButton.setEnabled( isEnabled );

		if ( attachmentList != null )
			attachmentList.setEnabled( isEnabled );

		if ( attachedMeat != null )
			attachedMeat.setEnabled( isEnabled );
	}

	protected Object [] getAttachedItems()
	{	return attachments.toArray();
	}

	protected int getAttachedMeat()
	{	return getValue( attachedMeat );
	}

	public void dispose()
	{
		super.dispose();

		Object [] frames = existingFrames.toArray();

		for ( int i = frames.length - 1; i >= 0; --i )
			if ( frames[i] instanceof AttachmentFrame && ((AttachmentFrame)frames[i]).attachments == attachments )
				((AttachmentFrame)frames[i]).dispose();
	}

	/**
	 * Internal frame used to handle attachments.  This frame
	 * appears whenever the user wishes to add non-meat attachments
	 * to the message.
	 */

	public static class AttachmentFrame extends KoLFrame
	{
		private ShowDescriptionList newAttachments;
		private LockableListModel inventory, attachments;

		public AttachmentFrame( KoLmafia client, LockableListModel inventory, LockableListModel attachments )
		{
			super( client, "KoLmafia: Attachments" );

			this.inventory = (LockableListModel) inventory.clone();
			this.attachments = attachments;
			this.newAttachments = new ShowDescriptionList( this.attachments );
			this.newAttachments.setVisibleRowCount( 16 );

			JScrollPane attachmentArea = new JScrollPane( newAttachments, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

			JPanel labeledArea = new JPanel();
			labeledArea.setLayout( new BorderLayout() );
			labeledArea.add( JComponentUtilities.createLabel( "Currently Attached", JLabel.CENTER, Color.black, Color.white ), BorderLayout.NORTH );
			labeledArea.add( attachmentArea, BorderLayout.CENTER );

			JPanel eastPanel = new JPanel();
			eastPanel.setLayout( new CardLayout( 10, 10 ) );
			eastPanel.add( labeledArea, "" );

			getContentPane().setLayout( new BorderLayout() );
			getContentPane().add( new InventoryPanel(), BorderLayout.CENTER );
			getContentPane().add( eastPanel, BorderLayout.EAST );
		}

		private class InventoryPanel extends ItemManagePanel
		{
			private InventoryPanel()
			{	super( "Inside Inventory", " > > > ", " < < < ", inventory );
			}

			protected void actionConfirmed()
			{
				Object [] items = elementList.getSelectedValues();

				AdventureResult currentItem;
				int attachmentCount, maximumCount;

				for ( int i = 0; i < items.length; ++i )
				{
					try
					{
						currentItem = (AdventureResult) items[i];
						maximumCount = currentItem.getCount( inventory );

						attachmentCount = df.parse( JOptionPane.showInputDialog( "Attaching " + currentItem.getName() + "...",
							String.valueOf( maximumCount ) ) ).intValue();

						currentItem = currentItem.getInstance( Math.min( attachmentCount, maximumCount ) );
						AdventureResult.addResultToList( attachments, currentItem );
						AdventureResult.addResultToList( inventory, currentItem.getNegation() );

					}
					catch ( Exception e )
					{
						// If the number placed inside of the count list was not
						// an actual integer value, pretend nothing happened.
					}
				}
			}

			protected void actionCancelled()
			{
				Object [] items = newAttachments.getSelectedValues();

				for ( int i = 0; i < items.length; ++i )
				{
					AdventureResult.addResultToList( attachments, ((AdventureResult) items[i]).getNegation() );
					AdventureResult.addResultToList( inventory, (AdventureResult) items[i] );
				}
			}
		}
	}
}
