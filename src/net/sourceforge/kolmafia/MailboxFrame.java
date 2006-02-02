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

// layout and containers
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JEditorPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;

// event listeners
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// other imports
import java.util.StringTokenizer;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * An extension of <code>KoLFrame</code> used to display the current
 * mailbox contents.  This updates whenever the user wishes to retrieve
 * more mail from their mailbox but otherwise does nothing.
 */

public class MailboxFrame extends KoLFrame implements ChangeListener
{
	private KoLMailMessage displayed;
	private JEditorPane messageContent;
	private JTabbedPane tabbedListDisplay;
	private LimitedSizeChatBuffer mailBuffer;

	private MailSelectList messageListInbox;
	private MailSelectList messageListPvp;
	private MailSelectList messageListOutbox;
	private MailSelectList messageListSaved;

	public MailboxFrame( KoLmafia client )
	{
		super( client, "IcePenguin Express" );

		this.messageListInbox = new MailSelectList( "Inbox" );
		JScrollPane messageListInboxDisplay = new JScrollPane( messageListInbox,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		this.messageListPvp = new MailSelectList( "PvP" );
		JScrollPane messageListPvpDisplay = new JScrollPane( messageListPvp,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		this.messageListOutbox = new MailSelectList( "Outbox" );
		JScrollPane messageListOutboxDisplay = new JScrollPane( messageListOutbox,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		this.messageListSaved = new MailSelectList( "Saved" );
		JScrollPane messageListSavedDisplay = new JScrollPane( messageListSaved,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

		this.tabbedListDisplay = new JTabbedPane();
		tabbedListDisplay.addTab( "Inbox", messageListInboxDisplay );
		tabbedListDisplay.addTab( "PvP", messageListPvpDisplay );
		tabbedListDisplay.addTab( "Outbox", messageListOutboxDisplay );
		tabbedListDisplay.addTab( "Saved", messageListSavedDisplay );
		tabbedListDisplay.addChangeListener( this );

		tabbedListDisplay.setMinimumSize( new Dimension( 0, 150 ) );

		this.messageContent = new JEditorPane();
		messageContent.setEditable( false );
		messageContent.addHyperlinkListener( new MailLinkClickedListener() );

		this.mailBuffer = new LimitedSizeChatBuffer( "KoL Mail Message", false );
		JScrollPane messageContentDisplay = mailBuffer.setChatDisplay( messageContent );
		messageContentDisplay.setMinimumSize( new Dimension( 0, 150 ) );

		JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true,
			tabbedListDisplay, messageContentDisplay );

		splitPane.setOneTouchExpandable( true );
		JComponentUtilities.setComponentSize( splitPane, 500, 300 );
		getContentPane().add( splitPane );

		toolbarPanel.add( new SaveAllButton() );
		toolbarPanel.add( new DeleteButton() );
		toolbarPanel.add( new RefreshButton() );

		if ( client != null )
			(new RequestMailboxThread( "Inbox" )).start();
	}

	public void dispose()
	{
		displayed = null;
		messageContent = null;
		tabbedListDisplay = null;
		mailBuffer = null;

		messageListInbox = null;
		messageListPvp = null;
		messageListOutbox = null;
		messageListSaved = null;

		super.dispose();
	}

	public void setEnabled( boolean isEnabled )
	{
		refreshMailManager();

		if ( tabbedListDisplay != null )
			for ( int i = 0; i < tabbedListDisplay.getTabCount(); ++i )
				tabbedListDisplay.setEnabledAt( i, isEnabled );

		if ( messageListInbox != null )
			messageListInbox.setEnabled( isEnabled );

		if ( messageListOutbox != null )
			messageListOutbox.setEnabled( isEnabled );

		if ( messageListSaved != null )
			messageListSaved.setEnabled( isEnabled );
	}

	/**
	 * Whenever the tab changes, this method is used to retrieve
	 * the messages from the appropriate client, if the mailbox
	 * is currently empty.
	 */

	public void stateChanged( ChangeEvent e )
	{
		refreshMailManager();
		mailBuffer.clearBuffer();

		boolean requestMailbox;
		String currentTabName = tabbedListDisplay.getTitleAt( tabbedListDisplay.getSelectedIndex() );
		if ( currentTabName.equals( "PvP" ) )
			return;

		if ( currentTabName.equals( "Inbox" ) )
		{
			if ( messageListInbox.isInitialized() )
				messageListInbox.valueChanged( null );
			requestMailbox = !messageListInbox.isInitialized();
		}
		else if ( currentTabName.equals( "Outbox" ) )
		{
			if ( messageListOutbox.isInitialized() )
				messageListOutbox.valueChanged( null );
			requestMailbox = !messageListOutbox.isInitialized();
		}
		else
		{
			if ( messageListSaved.isInitialized() )
				messageListSaved.valueChanged( null );
			requestMailbox = !messageListSaved.isInitialized();
		}

		if ( requestMailbox )
			(new RequestMailboxThread( currentTabName )).start();
	}

	private void refreshMailManager()
	{
		messageListInbox.setModel( KoLMailManager.getMessages( "Inbox" ) );
		messageListPvp.setModel( KoLMailManager.getMessages( "PvP" ) );
		messageListOutbox.setModel( KoLMailManager.getMessages( "Outbox" ) );
		messageListSaved.setModel( KoLMailManager.getMessages( "Saved" ) );
	}

	private class RequestMailboxThread extends DaemonThread
	{
		private String mailboxName;

		public RequestMailboxThread( String mailboxName )
		{	this.mailboxName = mailboxName;
		}

		public void run()
		{
			refreshMailManager();
			mailBuffer.append( "Retrieving messages from server..." );

			if ( client != null )
				(new MailboxRequest( client, mailboxName )).run();

			mailBuffer.clearBuffer();

			if ( mailboxName.equals( "Inbox" ) )
				messageListInbox.setInitialized( true );
			else if ( mailboxName.equals( "Outbox" ) )
				messageListOutbox.setInitialized( true );
			else
				messageListSaved.setInitialized( true );

			if ( client != null )
				client.enableDisplay();
		}
	}

	/**
	 * An internal class used to handle selection of a specific
	 * message from the mailbox list.
	 */

	private class MailSelectList extends JList implements ListSelectionListener
	{
		private String mailboxName;
		private boolean initialized;

		public MailSelectList( String mailboxName )
		{
			super( KoLMailManager.getMessages( mailboxName ) );
			setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			this.mailboxName = mailboxName;
			addListSelectionListener( this );
			addKeyListener( new MailboxKeyListener() );
		}

		public void valueChanged( ListSelectionEvent e )
		{	(new UpdateDisplayThread()).start();
		}

		private boolean isInitialized()
		{	return initialized;
		}

		public void setInitialized( boolean initialized )
		{	this.initialized = initialized;
		}

		private class MailboxKeyListener extends KeyAdapter
		{
			public void keyPressed( KeyEvent e )
			{
				if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE )
				{
					if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( null,
						"Would you like to delete the selected messages?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) )
					{
						client.disableDisplay();
						KoLMailManager.deleteMessages( mailboxName, getSelectedValues() );
						client.enableDisplay();
					}
					return;
				}

				if ( e.getKeyCode() == KeyEvent.VK_S && mailboxName.equals( "Inbox" ) )
				{
					if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( null,
						"Would you like to save the selected messages?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) )
					{
						client.disableDisplay();
						KoLMailManager.saveMessages( getSelectedValues() );
						client.enableDisplay();
					}
					return;
				}
			}
		}

		private class UpdateDisplayThread extends DaemonThread
		{
			public void run()
			{
				mailBuffer.clearBuffer();
				int newIndex = getSelectedIndex();

				if ( newIndex >= 0 && getModel().getSize() > 0 )
				{
					displayed = ((KoLMailMessage)KoLMailManager.getMessages( mailboxName ).get( newIndex ));
					mailBuffer.append( displayed.getMessageHTML() );
					messageContent.setCaretPosition( 0 );
				}
			}
		}
	}

	private class SaveAllButton extends JButton implements ActionListener
	{
		public SaveAllButton()
		{
			super( JComponentUtilities.getSharedImage( "saveall.gif" ) );
			addActionListener( this );
			setToolTipText( "Save All" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String currentTabName = tabbedListDisplay.getTitleAt( tabbedListDisplay.getSelectedIndex() );
			if ( currentTabName.equals( "Inbox" ) || currentTabName.equals( "PvP" ) )
			{
				if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( null,
					"Would you like to save the selected messages?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) )
				{
					client.disableDisplay();
					KoLMailManager.saveMessages( messageListInbox.getSelectedValues() );
					client.enableDisplay();
				}
			}
			else
			{
				JOptionPane.showMessageDialog( null, "Messages in this mailbox cannot be saved." );
			}
		}
	}

	private class DeleteButton extends JButton implements ActionListener
	{
		public DeleteButton()
		{
			super( JComponentUtilities.getSharedImage( "delete.gif" ) );
			addActionListener( this );
			setToolTipText( "Delete" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if ( JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog( null,
				"Would you like to delete the selected messages?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) )
					return;

			client.disableDisplay();
			String currentTabName = tabbedListDisplay.getTitleAt( tabbedListDisplay.getSelectedIndex() );

			if ( currentTabName.equals( "Inbox" ) )
				KoLMailManager.deleteMessages( "Inbox", messageListInbox.getSelectedValues() );
			else if ( currentTabName.equals( "Outbox" ) )
				KoLMailManager.deleteMessages( "Outbox", messageListOutbox.getSelectedValues() );
			else
				KoLMailManager.deleteMessages( "Saved", messageListSaved.getSelectedValues() );

			client.enableDisplay();
		}
	}

	private class RefreshButton extends JButton implements ActionListener
	{
		public RefreshButton()
		{
			super( JComponentUtilities.getSharedImage( "refresh.gif" ) );
			addActionListener( this );
			setToolTipText( "Refresh" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String currentTabName = tabbedListDisplay.getTitleAt( tabbedListDisplay.getSelectedIndex() );
			(new RequestMailboxThread( currentTabName.equals( "PvP" ) ? "Inbox" : currentTabName )).start();
		}
	}

	/**
	 * Action listener responsible for displaying reply and quoted message
	 * windows when a username is clicked, or opening the page in
	 * a browser if you're clicking something other than the username.
	 */

	private class MailLinkClickedListener extends KoLHyperlinkAdapter
	{
		protected void handleInternalLink( String location )
		{
			StringTokenizer tokens = new StringTokenizer( location, "?=&" );
			tokens.nextToken();  tokens.nextToken();

			String recipient = tokens.nextToken();
			String quotedMessage = displayed.getMessageHTML().substring(
				displayed.getMessageHTML().indexOf( "<br><br>" ) + 8 ).replaceAll( "<b>", " " ).replaceAll(
					"><", "" ).replaceAll( "<.*?>", LINE_BREAK );

			Object [] parameters = new Object[ tokens.hasMoreTokens() ? 2 : 3 ];
			parameters[0] = client;
			parameters[1] = recipient;

			if ( parameters.length == 3 )
				parameters[2] = quotedMessage;

			(new CreateFrameRunnable( GreenMessageFrame.class, parameters )).run();
		}
	}

	public static void main( String [] args )
	{
		Object [] parameters = new Object[1];
		parameters[0] = null;

		(new CreateFrameRunnable( MailboxFrame.class, parameters )).run();
	}
}
