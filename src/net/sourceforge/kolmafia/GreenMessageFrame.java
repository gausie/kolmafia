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

// other imports
import java.util.List;
import java.util.ArrayList;
import net.java.dev.spellcast.utilities.LockableListModel;
import net.java.dev.spellcast.utilities.JComponentUtilities;

public class GreenMessageFrame extends SendMessageFrame
{
	private static final String [] HEADERS = { "Send this message:" };

	public GreenMessageFrame( KoLmafia client )
	{	this( client, "" );
	}

	public GreenMessageFrame( KoLmafia client, String recipient )
	{	this( client, recipient, "" );
	}

	public GreenMessageFrame( KoLmafia client, String recipient, String quotedMessage )
	{
		super( client, "KoLmafia: Send a Green Message", HEADERS );
		recipientEntry.setText( recipient );
		messageEntry[0].setText( quotedMessage );
		sendMessageButton.addActionListener( new SendGreenMessageListener() );
	}

	/**
	 * Internal class used to handle sending a green message to the server.
	 */

	private class SendGreenMessageListener implements ActionListener
	{
		public void actionPerformed( ActionEvent e )
		{	(new SendGreenMessageThread()).start();
		}

		private class SendGreenMessageThread extends RequestThread
		{
			public void run()
			{
				GreenMessageFrame.this.setEnabled( false );
				(new GreenMessageRequest( client, recipientEntry.getText(), messageEntry[0].getText(), getAttachedItems(), getValue( quantities[11] ) )).run();
				GreenMessageFrame.this.setEnabled( true );

				if ( client.permitsContinue() )
				{
					client.updateDisplay( ENABLED_STATE, "Message sent to " + recipientEntry.getText() );
					sendMessageStatus.setText( "Message sent to " + recipientEntry.getText() );

					String closeWindowSetting = client.getSettings().getProperty( "closeSending" );
					if ( closeWindowSetting != null && closeWindowSetting.equals( "true" ) )
						GreenMessageFrame.this.dispose();
				}
				else
				{
					client.updateDisplay( ERROR_STATE, "Failed to send message to " + recipientEntry.getText() );
					sendMessageStatus.setText( "Failed to send message to " + recipientEntry.getText() );
				}
			}
		}
	}

	/**
	 * Main class used to view the user interface without having to actually
	 * start the program.  Used primarily for user interface testing.
	 */

	public static void main( String [] args )
	{
		JFrame test = new GreenMessageFrame( null );
		test.pack();  test.setVisible( true );  test.requestFocus();
	}
}