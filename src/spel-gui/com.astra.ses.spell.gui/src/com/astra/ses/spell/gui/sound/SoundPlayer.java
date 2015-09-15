///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.sound
// 
// FILE      : SoundPlayer.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2015 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Play a sound file.
 */
public class SoundPlayer
{
	/** Holds the external buffer size in bytes */
	private static final int	EXTERNAL_BUFFER_SIZE	= 128000;

	/**************************************************************************
	 * Play the given sound file at maximum volume.
	 *************************************************************************/
	public static void playFile(String soundFilename) throws FileNotFoundException
	{
		File audioFile = new File(soundFilename);
		SourceDataLine line = null;
		DataLine.Info info = null;
		try
		{
			AudioInputStream stream = AudioSystem
			        .getAudioInputStream(audioFile);
			/*
			 * From the AudioInputStream (the file) we fetch information about
			 * the format of the audio data. These information include the
			 * sampling frequency, the number of channels and the size of the
			 * samples. These information are needed to ask Java Sound for a
			 * suitable output line for this audio file.
			 */
			AudioFormat audioFormat = stream.getFormat();

			/*
			 * We have to construct an Info object that specifies the desired
			 * properties for the line. First, we have to say which kind of line
			 * we want. Then, we have to pass an AudioFormat object, so that the
			 * Line knows which format the data passed to it will have.
			 */
			info = new DataLine.Info(SourceDataLine.class, audioFormat);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);

			/* Set volume to maximum */
			FloatControl volume = (FloatControl) line
			        .getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue(volume.getMaximum());

			/*
			 * The line now can receive data, but will not pass them on to the
			 * audio output device (which means the sound card). This has to be
			 * activated.
			 */
			line.start();

			/*
			 * Now comes the real job: we have to write data to the line. We do
			 * this in a loop. First, we read data from the AudioInputStream to
			 * a buffer. Then, we write from this buffer to the Line. This is
			 * done until the end of the file is reached, which is detected by a
			 * return value of -1 from the read method of the AudioInputStream.
			 */
			int nBytesRead = 0;
			byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
			while (nBytesRead != -1)
			{
				nBytesRead = stream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
				{
					line.write(abData, 0, nBytesRead);
				}
			}
			/* Wait until all data are played. */
			line.drain();
			/* All data played. We can close the shop. */
			line.close();
		}
		catch (UnsupportedAudioFileException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
	}
}
