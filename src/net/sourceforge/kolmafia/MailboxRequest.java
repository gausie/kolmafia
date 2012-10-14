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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MailboxRequest extends KoLRequest
{
	private static boolean isRequesting = false;
	private static long lastRequest = System.currentTimeMillis();

	private String boxname;
	private int beginIndex;
	private String action;

	public static boolean isRequesting()
	{	return isRequesting;
	}

	public static long getLastRequest()
	{	return lastRequest;
	}

	public MailboxRequest( KoLmafia client, String boxname )
	{	this( client, boxname, 1 );
	}

	public MailboxRequest( KoLmafia client, String boxname, KoLMailMessage message, String action )
	{
		super( client, "messages.php" );
		addFormField( "box", boxname );
		addFormField( "pwd", client.getPasswordHash() );
		addFormField( "the_action", action );

		this.action = action;
		this.boxname = boxname;
		addFormField( message.getMessageID(), "on" );
	}

	public MailboxRequest( KoLmafia client, String boxname, Object [] messages, String action )
	{
		super( client, "messages.php" );
		addFormField( "box", boxname );
		addFormField( "pwd", client.getPasswordHash() );
		addFormField( "the_action", action );

		this.action = action;
		this.boxname = boxname;
		for ( int i = 0; i < messages.length; ++i )
			addFormField( ((KoLMailMessage) messages[i]).getMessageID(), "on" );
	}

	private MailboxRequest( KoLmafia client, String boxname, int beginIndex )
	{
		super( client, "messages.php" );
		addFormField( "box", boxname );
		addFormField( "begin", String.valueOf( beginIndex ) );

		this.action = null;
		this.boxname = boxname;
		this.beginIndex = beginIndex;
	}

	public void run()
	{
		// In order to prevent multiple mailbox requests from running,
		// a test is made on a static variable to halt concurrent requests.

		if ( isRequesting )
			return;

		// Now you know that there is a request in progress, so you
		// reset the variable (to avoid concurrent requests).

		if ( action == null )
			updateDisplay( DISABLE_STATE, "Retrieving mail from " + boxname + "..." );
		else
			updateDisplay( DISABLE_STATE, "Executing " + action + " request for " + boxname + "..." );

		lastRequest = System.currentTimeMillis();
		isRequesting = true;

		super.run();

		if ( action != null )
		{
			updateDisplay( NORMAL_STATE, "Selected mail successfully " + action + "d" );
			isRequesting = false;
			return;
		}

		// Determine how many messages there are, and how many there
		// are left to go.  This will cause a lot of server load for
		// those with lots of messages.	 But!  This can be fixed by
		// testing the mail manager to see if it thinks all the new
		// messages have been retrieved.

		if ( responseText.indexOf( "There are no messages in this mailbox." ) != -1 )
		{
			updateDisplay( NORMAL_STATE, "Your mailbox is empty." );
			isRequesting = false;
			return;
		}

		int lastMessageID = 0;
		int totalMessages = Integer.MAX_VALUE;

		try
		{
			Matcher matcher = Pattern.compile( "Messages: \\w*?, page \\d* \\(\\d* - (\\d*) of (\\d*)\\)</b>" ).matcher( responseText );

			if ( matcher.find() )
			{
				lastMessageID = Integer.parseInt( matcher.group(1) );
				totalMessages = Integer.parseInt( matcher.group(2) );
			}
			else
			{
				matcher = Pattern.compile( "Messages: \\w*?, page 1 \\((\\d*) messages\\)</b>" ).matcher( responseText );
				if ( matcher.find() )
				{
					lastMessageID = Integer.parseInt( matcher.group(1) );
					totalMessages = lastMessageID;
				}
			}
		}
		catch ( Exception e )
		{
			// If an exception is caught, then something bad
			// probably happened.  Return. :D

			updateDisplay( ERROR_STATE, "Error occurred in mail retrieval." );
			client.cancelRequest();
			isRequesting = false;
			return;
		}

		int nextMessageIndex = responseText.indexOf( "<td valign=top>" );

		if ( nextMessageIndex == -1 )
		{
			updateDisplay( NORMAL_STATE, "Your mailbox is empty." );
			isRequesting = false;
			return;
		}

		nextMessageIndex = processMessages( nextMessageIndex );
		isRequesting = false;

		if ( nextMessageIndex != -1 && lastMessageID != totalMessages )
			(new MailboxRequest( client, boxname, beginIndex + 1 )).run();
		else
			updateDisplay( NORMAL_STATE, "Mail retrieved from " + boxname );
	}

	private int processMessages( int startIndex )
	{
		boolean shouldContinueParsing = true;
		int lastMessageIndex = startIndex;
		int nextMessageIndex = lastMessageIndex;

		String currentMessage;
		while ( nextMessageIndex != -1 && shouldContinueParsing )
		{
			lastMessageIndex = nextMessageIndex;
			nextMessageIndex = responseText.indexOf( "<td valign=top>", lastMessageIndex + 15 );

			// The last message in the inbox has no "next message index".
			// In this case, locate the last index of the link tag and
			// use that as the last message index.

			if ( nextMessageIndex == -1 )
			{
				nextMessageIndex = responseText.lastIndexOf( "<a" );
				shouldContinueParsing = false;
			}

			// If the next message index is still non-positive, that
			// means there aren't any messages left to parse.

			if ( nextMessageIndex != -1 )
			{
				currentMessage = responseText.substring( lastMessageIndex, nextMessageIndex );

				// This replaces all of the HTML contained within the message to something
				// that can be rendered with the default JEditorPane, and also be subject
				// to the custom font sizes provided by LimitedSizeChatBuffer.

				currentMessage = currentMessage.replaceAll( "<br />" , "<br>" ).replaceAll( "</?t.*?>" , "\n" ).replaceAll(
					"<blockquote>", "<br>" ).replaceAll( "</blockquote>", "" ).replaceAll( "\n", "" ).replaceAll( "<center>", "<br><center>" );

				// At this point, the message is registered with the mail manager, which
				// records the message and updates whether or not you should continue.

				shouldContinueParsing &= BuffBotHome.isBuffBotActive() ? BuffBotManager.addMessage( boxname, currentMessage ) :
					KoLMailManager.addMessage( boxname, currentMessage );
			}
		}

		return nextMessageIndex;
	}
}
