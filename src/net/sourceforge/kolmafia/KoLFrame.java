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

// containers
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;

// layout
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;

// event listeners
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

// other stuff
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import java.lang.reflect.Constructor;
import net.java.dev.spellcast.utilities.LicenseDisplay;
import net.java.dev.spellcast.utilities.ActionVerifyPanel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

/**
 * An extended <code>JFrame</code> which provides all the frames in
 * KoLmafia the ability to update their displays, given some integer
 * value and the message to use for updating.
 */

public abstract class KoLFrame extends javax.swing.JFrame implements KoLConstants
{
	protected boolean isEnabled;
	protected List existingFrames;
	protected KoLmafia client;
	protected KoLPanel contentPanel;
	private boolean isExecutingScript;

	protected CharsheetFrame statusPane;
	protected GearChangeFrame gearChanger;
	protected ItemManageFrame itemManager;
	protected MailboxFrame mailboxDisplay;
	protected KoLMessenger kolchat;

	protected JMenuItem statusMenuItem;
	protected JMenuItem mailMenuItem;

	/**
	 * Constructs a new <code>KoLFrame</code> with the given title,
	 * to be associated with the given client.
	 */

	protected KoLFrame( String title, KoLmafia client )
	{
		super( title );

		this.client = client;
		this.isEnabled = true;
		this.existingFrames = new ArrayList();
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
	}

	/**
	 * Updates the display to reflect the given display state and
	 * to contain the given message.  Note that if there is no
	 * content panel, this method does nothing.
	 */

	public void updateDisplay( int displayState, String message )
	{
		if ( contentPanel != null && client != null )
		{
			client.getLogStream().println( message );
			contentPanel.setStatusMessage( isExecutingScript && displayState != ERROR_STATE ? DISABLED_STATE : displayState, message );

			switch ( displayState )
			{
				case ERROR_STATE:
					setEnabled( true );
					break;

				case DISABLED_STATE:
					setEnabled( false );
					break;

				case NOCHANGE:
					break;

				default:
					if ( !isExecutingScript )
						setEnabled( true );
					break;

			}
		}
	}

	/**
	 * Utility method used to give the content panel for this
	 * <code>KoLFrame</code> focus.  Note that if the content
	 * panel is <code>null</code>< this method does nothing.
	 */

	public void requestFocus()
	{
		super.requestFocus();
		if ( contentPanel != null )
			contentPanel.requestFocus();
	}

	protected final JMenu addStatusMenu( JMenuBar menuBar )
	{
		JMenu statusMenu = new JMenu( "My KoL" );
		statusMenu.setMnemonic( KeyEvent.VK_M );
		menuBar.add( statusMenu );

		this.statusMenuItem = new JMenuItem( "Status Pane", KeyEvent.VK_S );
		statusMenuItem.addActionListener( new ViewStatusPaneListener() );
		statusMenu.add( statusMenuItem );

		JMenuItem gearMenuItem = new JMenuItem( "Gear Changer", KeyEvent.VK_G );
		gearMenuItem.addActionListener( new ViewGearChangerListener() );

		statusMenu.add( gearMenuItem );

		JMenuItem itemMenuItem = new JMenuItem( "Item Manager", KeyEvent.VK_I );
		itemMenuItem.addActionListener( new ViewItemManagerListener() );

		statusMenu.add( itemMenuItem );
		return statusMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * people menu to the given menu bar.  The default menu contains
	 * the ability to open chat, compose green messages, and read
	 * current mail.
	 */

	protected final JMenu addPeopleMenu( JMenuBar menuBar )
	{
		JMenu peopleMenu = new JMenu( "People" );
		peopleMenu.setMnemonic( KeyEvent.VK_P );
		menuBar.add( peopleMenu );

		JMenuItem chatMenuItem = new JMenuItem( "Chat of Loathing", KeyEvent.VK_C );
		chatMenuItem.addActionListener( new ViewChatListener() );

		peopleMenu.add( chatMenuItem );

		JMenuItem composeMenuItem = new JMenuItem( "Green Composer", KeyEvent.VK_G );
		composeMenuItem.addActionListener( new DisplayFrameListener( GreenMessageFrame.class ) );

		peopleMenu.add( composeMenuItem );

		this.mailMenuItem = new JMenuItem( "IcePenguin Express", KeyEvent.VK_I );
		mailMenuItem.addActionListener( new DisplayFrameListener( MailboxFrame.class ) );

		peopleMenu.add( mailMenuItem );
		return peopleMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * scripting menu to the given menu bar.  The default menu contains
	 * the ability to load scripts.
	 */

	protected final JMenu addScriptMenu( JMenuBar menuBar )
	{
		JMenu scriptMenu = new JMenu("Scripts");
		scriptMenu.setMnemonic( KeyEvent.VK_S );
		menuBar.add( scriptMenu );

		JMenuItem loadScriptMenuItem = new JMenuItem( "Load Script...", KeyEvent.VK_L );
		loadScriptMenuItem.addActionListener( new LoadScriptListener() );

		scriptMenu.add( loadScriptMenuItem );

		JMenuItem loggerItem = new JMenuItem( "", KeyEvent.VK_R );
		loggerItem.addActionListener( new ToggleMacroListener( loggerItem ) );

		scriptMenu.add( loggerItem );
		return scriptMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code>
	 * configuration menu to the given menu bar.  The default menu
	 * contains the ability to customize preferences (global if it
	 * is invoked before login, character-specific if after) and
	 * initialize the debugger.
	 *
	 * @param	menuBar	The <code>JMenuBar</code> to which the configuration menu will be attached
	 */

	protected final JMenu addConfigureMenu( JMenuBar menuBar )
	{
		JMenu configureMenu = new JMenu("Configure");
		configureMenu.setMnemonic( KeyEvent.VK_C );
		menuBar.add( configureMenu );

		JMenuItem settingsItem = new JMenuItem( "Preferences", KeyEvent.VK_P );
		settingsItem.addActionListener( new DisplayFrameListener( OptionsFrame.class ) );

		configureMenu.add( settingsItem );

		JMenuItem loggerItem = new JMenuItem( "", KeyEvent.VK_S );
		loggerItem.addActionListener( new ToggleDebugListener( loggerItem ) );

		configureMenu.add( loggerItem );
		return configureMenu;
	}

	/**
	 * Utility method used to add the default <code>KoLmafia</code> Help
	 * menu to the given menu bar.  The default Help menu contains the
	 * copyright statement for <code>KoLmafia</code>.
	 *
	 * @param	menuBar	The <code>JMenuBar</code> to which the Help menu will be attached
	 */

	protected final JMenu addHelpMenu( JMenuBar menuBar )
	{
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic( KeyEvent.VK_H );
		menuBar.add( helpMenu );

		JMenuItem aboutItem = new JMenuItem( "About KoLmafia...", KeyEvent.VK_A );
		aboutItem.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{	(new LicenseDisplay( "KoLmafia: Copyright Notice" )).requestFocus();
			}
		});

		helpMenu.add( aboutItem );
		return helpMenu;
	}

	/**
	 * Auxilary method used to enable and disable a frame.  By default,
	 * this attempts to toggle the enable/disable status on the core
	 * content panel.  It is advised that descendants override this
	 * behavior whenever necessary.
	 *
	 * @param	isEnabled	<code>true</code> if the frame is to be re-enabled
	 */

	public void setEnabled( boolean isEnabled )
	{
		this.isEnabled = isEnabled;

		if ( contentPanel != null )
			contentPanel.setEnabled( isEnabled );

		Iterator framesIterator = existingFrames.iterator();
		KoLFrame currentFrame;

		while ( framesIterator.hasNext() )
		{
			currentFrame = (KoLFrame) framesIterator.next();
			if ( currentFrame.isShowing() )
				currentFrame.setEnabled( isEnabled );
		}
	}

	/**
	 * Overrides the default isEnabled() method, because the setEnabled()
	 * method does not call the superclass's version.
	 *
	 * @return	Whether or not this KoLFrame is enabled.
	 */

	public boolean isEnabled()
	{	return isEnabled;
	}

	/**
	 * An internal class which allows focus to be returned to the
	 * client's active frame when auxiliary windows are closed.
	 */

	protected class ReturnFocusAdapter extends WindowAdapter
	{
		public void windowClosing( WindowEvent e )
		{
			if ( client != null )
				client.requestFocus();
			else
				System.exit(0);
		}
	}

	private class ToggleMacroListener implements ActionListener
	{
		private JMenuItem loggerItem;

		public ToggleMacroListener( JMenuItem loggerItem )
		{
			this.loggerItem = loggerItem;
			loggerItem.setText( client == null || client.getMacroStream() instanceof NullStream ?
				"Record Script..." : "Stop Recording" );
		}

		public void actionPerformed(ActionEvent e)
		{	(new MacroRecordThread()).start();
		}

		private class MacroRecordThread extends Thread
		{
			public MacroRecordThread()
			{	setDaemon( true );
			}

			public void run()
			{
				if ( client != null && client.getMacroStream() instanceof NullStream )
				{
					JFileChooser chooser = new JFileChooser();
					chooser.setFileFilter( new TXTFileFilter() );
					int returnVal = chooser.showSaveDialog( KoLFrame.this );

					if ( chooser.getSelectedFile() == null )
						return;

					String filename = chooser.getSelectedFile().getAbsolutePath();
					if ( !filename.endsWith( ".txt" ) )
						filename += ".txt";

					if ( client != null && returnVal == JFileChooser.APPROVE_OPTION )
						client.initializeMacroStream( filename );

					loggerItem.setText( "Stop Recording" );
				}
				else if ( client != null )
				{
					client.deinitializeMacroStream();
					loggerItem.setText( "Record Script..." );
				}
			}
		}

		/**
		 * Internal file descriptor to make sure files are
		 * only saved to HTML format.
		 */

		private class TXTFileFilter extends javax.swing.filechooser.FileFilter
		{
			public boolean accept( java.io.File f )
			{	return f.getPath().endsWith( ".txt" );
			}

			public String getDescription()
			{	return "Text Documents";
			}
		}
	}

	private class ToggleDebugListener implements ActionListener
	{
		private JMenuItem loggerItem;

		public ToggleDebugListener( JMenuItem loggerItem )
		{
			this.loggerItem = loggerItem;
			loggerItem.setText( client == null || client.getLogStream() instanceof NullStream ?
				"Start Debug Logging" : "Stop Debug Logging" );
		}

		public void actionPerformed(ActionEvent e)
		{
			if ( client != null && client.getLogStream() instanceof NullStream )
			{
				client.initializeLogStream();
				loggerItem.setText( "Stop Debug Logging" );
			}
			else if ( client != null )
			{
				client.deinitializeLogStream();
				loggerItem.setText( "Start Debug Logging" );
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for loading a script.
	 */

	private class LoadScriptListener implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{	(new LoadScriptThread()).start();
		}

		private class LoadScriptThread extends Thread
		{
			public LoadScriptThread()
			{
				super( "Load-Script-Thread" );
				setDaemon( true );
			}

			public void run()
			{
				JFileChooser chooser = new JFileChooser( "." );
				int returnVal = chooser.showOpenDialog( KoLFrame.this );

				if ( chooser.getSelectedFile() == null )
					return;

				String filename = chooser.getSelectedFile().getAbsolutePath();

				try
				{
					if ( client != null && returnVal == JFileChooser.APPROVE_OPTION )
					{
						isExecutingScript = true;
						(new KoLmafiaCLI( client, filename )).listenForCommands();
					}

					isExecutingScript = false;
					if ( client.permitsContinue() )
						updateDisplay( ENABLED_STATE, "Script completed successfully." );
					else
						updateDisplay( ERROR_STATE, "Script <" + filename + "> encountered an error." );
				}
				catch ( Exception e )
				{
					// Here, notify the display that the script
					// file specified could not be loaded

					isExecutingScript = false;
					updateDisplay( ERROR_STATE, "Script file <" + filename + "> could not be found." );
					return;
				}
			}
		}
	}

	/**
	 * An internal class used as the basis for content panels.  This
	 * class builds upon the <code>ActionVerifyPanel</code> by adding
	 * a <code>setStatusMessage()</code>.
	 */

	protected abstract class KoLPanel extends ActionVerifyPanel
	{
		protected KoLPanel( String confirmedText, String cancelledText )
		{
			super( confirmedText, cancelledText );
		}

		protected KoLPanel( String confirmedText, String cancelledText, Dimension labelSize, Dimension fieldSize )
		{
			super( confirmedText, cancelledText, labelSize, fieldSize );
		}

		protected KoLPanel( String confirmedText, String cancelledText, Dimension labelSize, Dimension fieldSize, boolean isCenterPanel )
		{
			super( confirmedText, cancelledText, labelSize, fieldSize, isCenterPanel );
		}

		public abstract void setStatusMessage( int displayState, String s );
	}

	/**
	 * An internal class used as the basis for non-content panels. This
	 * class builds upon the <code>KoLPanel</code>, but specifically
	 * defines the abstract methods to not do anything.
	 */

	protected abstract class NonContentPanel extends KoLPanel
	{
		protected NonContentPanel( String confirmedText, String cancelledText )
		{
			super( confirmedText, cancelledText );
		}

		protected NonContentPanel( String confirmedText, String cancelledText, Dimension labelSize, Dimension fieldSize )
		{
			super( confirmedText, cancelledText, labelSize, fieldSize );
		}

		public void setStatusMessage( int displayState, String s )
		{
		}
	}

	/**
	 * A generic panel which adds a label to the bottom of the KoLPanel
	 * to update the panel's status.  It also provides a thread which is
	 * guaranteed to be a daemon thread for updating the frame which
	 * also retrieves a reference to the client's current settings.
	 */

	protected abstract class LabeledKoLPanel extends KoLPanel
	{
		private String panelTitle;
		private JPanel actionStatusPanel;
		private JLabel actionStatusLabel;

		public LabeledKoLPanel( String panelTitle, Dimension left, Dimension right )
		{	this( panelTitle, "apply", "defaults", left, right );
		}

		public LabeledKoLPanel( String panelTitle, String confirmButton, String cancelButton, Dimension left, Dimension right )
		{
			super( confirmButton, cancelButton, left, right, true );
			this.panelTitle = panelTitle;

			actionStatusPanel = new JPanel();
			actionStatusPanel.setLayout( new GridLayout( 2, 1 ) );

			actionStatusLabel = new JLabel( " ", JLabel.CENTER );
			actionStatusPanel.add( actionStatusLabel );
			actionStatusPanel.add( new JLabel( " ", JLabel.CENTER ) );
		}

		protected void setContent( VerifiableElement [] elements )
		{	setContent( elements, true );
		}

		protected void setContent( VerifiableElement [] elements, boolean isLabelPreceeding )
		{	setContent( elements, isLabelPreceeding, false );
		}

		protected void setContent( VerifiableElement [] elements, boolean isLabelPreceeding, boolean bothDisabledOnClick )
		{
			super.setContent( elements, null, null, null, isLabelPreceeding, bothDisabledOnClick );

			if ( panelTitle != null )
				add( JComponentUtilities.createLabel( panelTitle, JLabel.CENTER, Color.black, Color.white ), BorderLayout.NORTH );

			add( actionStatusPanel, BorderLayout.SOUTH );
		}

		public void setStatusMessage( int displayState, String s )
		{	actionStatusLabel.setText( s );
		}

		protected void actionCancelled()
		{
		}

		public void requestFocus()
		{
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing a character sheet.
	 */

	protected class DisplayFrameListener implements ActionListener
	{
		private Constructor creator;
		private KoLmafia [] parameters;
		protected KoLFrame lastCreatedFrame;

		public DisplayFrameListener( Class frameClass )
		{
			try
			{
				Class [] fields = new Class[1];
				fields[0] = KoLmafia.class;
				this.creator = frameClass.getConstructor( fields );
			}
			catch ( NoSuchMethodException e )
			{
				// If this happens, this is the programmer's
				// fault for not noticing that the frame was
				// more complex than is allowed by this
				// displayer.  The creator stays null, which
				// is harmless, so do nothing for now.
			}

			this.parameters = new KoLmafia[1];
			parameters[0] = KoLFrame.this.client;
		}

		public void actionPerformed( ActionEvent e )
		{	(new DisplayFrameThread()).start();
		}

		protected class DisplayFrameThread extends Thread
		{
			public DisplayFrameThread()
			{
				super( "DisplayFrame-Thread" );
				setDaemon( true );
			}

			public void run()
			{
				try
				{
					if ( creator != null )
					{
						boolean wasEnabled = isEnabled;
						KoLFrame.this.setEnabled( false );

						lastCreatedFrame = (KoLFrame) creator.newInstance( parameters );
						lastCreatedFrame.pack();
						lastCreatedFrame.setLocation( KoLFrame.this.getLocation() );
						lastCreatedFrame.setVisible( true );
						lastCreatedFrame.requestFocus();
						lastCreatedFrame.setEnabled( wasEnabled );
						existingFrames.add( lastCreatedFrame );

						updateDisplay( ENABLED_STATE, " " );
						KoLFrame.this.setEnabled( wasEnabled );
					}
					else
					{
						updateDisplay( ERROR_STATE, "Frame could not be loaded." );
						return;
					}
				}
				catch ( Exception e )
				{
					// If this happens, update the display to indicate
					// that it failed to happen (eventhough technically,
					// this should never have happened)

					updateDisplay( ERROR_STATE, "Frame could not be loaded." );
					e.printStackTrace( client.getLogStream() );
					return;
				}
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing the status pane.
	 */

	private class ViewStatusPaneListener extends DisplayFrameListener
	{
		public ViewStatusPaneListener()
		{	super( CharsheetFrame.class );
		}

		public void actionPerformed( ActionEvent e )
		{	(new ViewStatusPaneThread()).start();
		}

		private class ViewStatusPaneThread extends DisplayFrameThread
		{
			public void run()
			{
				if ( statusPane != null )
				{
					statusPane.setVisible( true );
					statusPane.requestFocus();
					statusPane.setEnabled( isEnabled );

					if ( isEnabled )
						statusPane.refreshStatus();
				}
				else
				{
					super.run();
					statusPane = (CharsheetFrame) lastCreatedFrame;
				}
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing the gear changer.
	 */

	private class ViewGearChangerListener extends DisplayFrameListener
	{
		public ViewGearChangerListener()
		{	super( GearChangeFrame.class );
		}

		public void actionPerformed( ActionEvent e )
		{	(new ViewGearChangerThread()).start();
		}

		private class ViewGearChangerThread extends DisplayFrameThread
		{
			public void run()
			{
				if ( gearChanger != null )
				{
					gearChanger.setVisible( true );
					gearChanger.requestFocus();
					gearChanger.setEnabled( isEnabled );
				}
				else
				{
					super.run();
					gearChanger = (GearChangeFrame) lastCreatedFrame;
				}
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing the item manager.
	 */

	private class ViewItemManagerListener extends DisplayFrameListener
	{
		public ViewItemManagerListener()
		{	super( ItemManageFrame.class );
		}

		public void actionPerformed( ActionEvent e )
		{	(new ViewItemManagerThread()).start();
		}

		private class ViewItemManagerThread extends DisplayFrameThread
		{
			public void run()
			{
				if ( itemManager != null )
				{
					itemManager.setVisible( true );
					itemManager.requestFocus();
					itemManager.setEnabled( isEnabled );
				}
				else
				{
					super.run();
					itemManager = (ItemManageFrame) lastCreatedFrame;
				}
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing the chat window.
	 */

	private class ViewChatListener implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{	(new ViewChatThread()).start();
		}

		private class ViewChatThread extends Thread
		{
			public ViewChatThread()
			{
				super( "Chat-Display-Thread" );
				setDaemon( true );
			}

			public void run()
			{
				if ( client.getMessenger() == null )
				{
					client.initializeChat();
					kolchat = client.getMessenger();
				}

				updateDisplay( ENABLED_STATE, " " );
			}
		}
	}

	/**
	 * In order to keep the user interface from freezing (or at least
	 * appearing to freeze), this internal class is used to process
	 * the request for viewing the item manager.
	 */

	private class DisplayMailListener extends DisplayFrameListener
	{
		public DisplayMailListener()
		{	super( MailboxFrame.class );
		}

		public void actionPerformed( ActionEvent e )
		{	(new DisplayMailThread()).start();
		}

		private class DisplayMailThread extends DisplayFrameThread
		{
			public void run()
			{
				if ( mailboxDisplay != null )
				{
					mailboxDisplay.setVisible( true );
					mailboxDisplay.requestFocus();
					mailboxDisplay.setEnabled( isEnabled );

					if ( isEnabled )
						mailboxDisplay.refreshMailbox();
				}
				else
				{
					(new MailboxRequest( client, "Inbox" )).run();
					super.run();
					mailboxDisplay = (MailboxFrame) lastCreatedFrame;
				}
			}
		}
	}
}