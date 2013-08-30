package retrieve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PowerPointNarrationRetriever implements NarrationRetriever {
	static final int BUFFER = 2048;
	Object fileLock = new Object();
	
	File powerpointFile;
	
	public PowerPointNarrationRetriever(File powerpointFile){
		this.powerpointFile = powerpointFile;
	}
	
	private String getBaseFileName(){
		String fileName = powerpointFile.getAbsolutePath();
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
	
	private String getBaseTempFileName(){
		String fileName = powerpointFile.getAbsolutePath();
		return fileName.substring(0, fileName.lastIndexOf('\\')+1)+"."+fileName.substring(fileName.lastIndexOf('\\')+1, fileName.lastIndexOf('.'));
	}
	
	private synchronized void write(InputStream in, OutputStream out) throws IOException{

		int currentByte;
		byte data[] = new byte[BUFFER];
		while ((currentByte = in.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, currentByte);
		}

		in.close();
		out.close();
	}
	
	private synchronized void joinAudio(File joinedContent, File newContent, File tempOut) throws UnsupportedAudioFileException, IOException{

		AudioInputStream joinedStream = AudioSystem.getAudioInputStream(joinedContent);
		AudioInputStream newStream = AudioSystem.getAudioInputStream(newContent);
		AudioInputStream appendedFiles = 
				new AudioInputStream(
						new SequenceInputStream(joinedStream, newStream),     
						joinedStream.getFormat(), 
						joinedStream.getFrameLength() + newStream.getFrameLength());

		
		OutputStream tempOutStream = new FileOutputStream(tempOut);
		AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, tempOutStream);
		joinedStream.close();
		newStream.close();
		appendedFiles.close();
		tempOutStream.close();
		
		
		InputStream tempInStream = new FileInputStream(tempOut);
		OutputStream joinedOut = new FileOutputStream(joinedContent);
		write(tempInStream, joinedOut);
	}
	
	@Override
	public void writeFullNarration() {
		
		String baseFileName = getBaseFileName();
		String baseTempFileName = getBaseTempFileName();
		File finalNarrationFile = new File(baseFileName+".narration.wav");
		int narrationNum=1;
		while(finalNarrationFile.exists()){
			narrationNum++;
			finalNarrationFile = new File(baseFileName+".narration"+narrationNum+".wav");
		}

		File joinedNarrationFile = new File(baseTempFileName+".temp.joined.wav");
		File tempFile = new File(baseTempFileName+".temp.wav");
		tempFile.deleteOnExit();
		joinedNarrationFile.deleteOnExit();
		
		try {
			ZipFile zipIn = new ZipFile(powerpointFile);
			
			Map<Integer, ZipEntry> narrationEntries = new TreeMap<Integer, ZipEntry>();
			
			Enumeration zipEntries = zipIn.entries();
			while(zipEntries.hasMoreElements()){
				ZipEntry entry = (ZipEntry) zipEntries.nextElement();
				if(!entry.isDirectory() && entry.getName().endsWith(".wav")){
					String name = entry.getName();
					String num = name.substring(name.lastIndexOf("media")+5, name.lastIndexOf('.'));
					
					narrationEntries.put(Integer.parseInt(num), entry);
				}
			}
			
			for(ZipEntry entry: narrationEntries.values()){
				InputStream in = zipIn.getInputStream(entry);
				if(!finalNarrationFile.exists()){
					OutputStream joinedOut = new FileOutputStream(finalNarrationFile);
					write(in, joinedOut);
				}else{
					OutputStream tempOut = new FileOutputStream(tempFile);
					
					write(in, tempOut);
					joinAudio(finalNarrationFile, tempFile, joinedNarrationFile);
					
				}
			}
			
			System.out.print("Cleaning up temporary files...");
			System.out.println("done.");
			
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		PowerPointNarrationRetriever retriever = 
				new PowerPointNarrationRetriever(
						new File(
								"C:\\Users\\bartel\\Documents\\Papers\\My Papers\\Thesis Proposal\\Presentations\\Access Proposal 10.pptx"
							));
		
		retriever.writeFullNarration();
	}

}
