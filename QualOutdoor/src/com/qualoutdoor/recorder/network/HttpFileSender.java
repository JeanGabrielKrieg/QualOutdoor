package com.qualoutdoor.recorder.network;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

/**
 * Child of Sender Class, implementing file sending with HTTP protocols. This kind of connection doesn't depends on 
 * transfers that it set then different kind of sending are implemented. Attributes of this class are the specific parameters
 * of a HTTP connection
 * */

public class HttpFileSender implements Sender{
	
	/**
	 * name of the input of post form in which the file to upload will be associated with.
	 * 
	 *  this attributes makes HttpFileSender objects associated with a unique file to upload whereas
	 *  its methods can handle multiple file sending in a single connection. If this association became
	 *  problematic this attribute can be moved from here to UploadFile method */
	private String fileFieldName;
	
	/**HTTP connection in which form is uploaded*/
	private HttpURLConnection connection;
	/**String marking off form inputs*/
	private String delimiter;
	/**Output Stream enabling to write into HTTP request to send*/
	private OutputStream os;
	/**PrintWriter associated with os for writing characters into request*/
	private PrintWriter writer;

	/**
	 * Constructor
	 */
	public HttpFileSender(String fileFieldName) {
	    //input name associated to file to upload in post form is set
		this.fileFieldName = fileFieldName;
	}
	
	
	/**
	 * Implementing sendFile method : connection is opened, form is sent, connection is closed then server response
	 * is read : this method allows a single file sending in a request.
	 */
	@Override
	public boolean sendFile(String url,String fileName,InputStream content){
		try{
		    //connection is initialized from an URL
			this.initialize(url);
			//file is uploaded
			this.UploadFile(this.fileFieldName,fileName,content);
			//connection is closed
			this.endSending();
			//server response is read
			return this.readResponseStatus();
		}catch(HttpTransfertException e){
			return false;
		}catch(IOException e){
            return false;
        }
	}
	
	/**
	 * Method for initializing HTTP connection from an URL
	 */
	public void initialize(String url) throws HttpTransfertException{
		try{
			//URL is built from then given string
			URL targetAddress = new URL(url);
			//opening connection
			this.connection = (HttpURLConnection) targetAddress.openConnection();
			//setting request type
			this.connection.setRequestMethod("POST");
			//enabling input flows for writing information into connection
			this.connection.setDoInput(true);
			//enabling output flow for reading information from connection
			this.connection.setDoOutput(true);
			//setting TCP connection persistent
			this.connection.setRequestProperty("Connection", "Keep-Alive");
			//mark generation
			this.delimiter = "******"+Long.toString(System.currentTimeMillis())+"******";
			//indicating how data inside request are organized
			this.connection.setRequestProperty("Content-type", "multipart/form-data; boundary="+this.delimiter);
			//setting connection on
			this.connection.connect();
			//getting an output stream to write inside
			this.os = connection.getOutputStream();
			//building writer associated with the previous output stream.
		    this.writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
		    
		}catch(MalformedURLException e){
			Log.d("URL exception",e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("URL exception"+e.toString());
		}catch(IOException e){
			Log.d("initialize IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("initialize IO Exception :"+e.toString());
		}	
	}
	
	
	/**
	 * Method enabling to insert in a post form a simple input field that contains for example
	 * a name or a mail but not a file. Currently this method is not used. this method should be
	 * called on a initialized connection
	 * 
	 * fieldName is the input name
	 * val is the content of the input
	 */
	public void SendSimpleInput(String fieldName, String val){
			//indicating new input in form
			this.writer.append("--"+this.delimiter+"\r\n");
			//indicating that it is a simple input named fieldName
			this.writer.append("Content-Disposition: form-data; name=\""+fieldName+"\"\r\n");
			//indicating how input content is encoded
			this.writer.append("Content-Type: text/plain; charset=UTF-8\r\n");
			//writing field content
			this.writer.append("\r\n" + val + "\r\n");
			//flow is written into request
			this.writer.flush();
		
	}
	
	/**
	 * Method enabling to insert a File into post form. it will be associated with an input named fieldName.
	 * File is passed in tow parts : its name first : fileName then its content : content.
	 * This method sould be called on a initialized connection*/
	public void UploadFile(String fieldName, String fileName,InputStream content)throws HttpTransfertException{
		try{
			//indicating new form input
			this.writer.append("--"+this.delimiter+"\r\n");
			//indicating input's name
			this.writer.append("Content-Disposition: form-data; name=\"" + fieldName+ "\"; filename=\"" + fileName+ "\"\r\n");
			//indicating that input's content is text
			this.writer.append("Content-Type: text/plain\r\n");
			//indicating how input's contents is encoded
			this.writer.append("Content-Transfer-Encoding: binary\r\n");
			this.writer.append("\r\n");
			//flow is writen into request
			this.writer.flush();
		
			//reading/writing mecanisme to write file content into request
			byte[] temp = new byte[1024];
			int indic;
			while((indic = content.read(temp)) != -1){
				os.write(temp, 0, indic);
			}
			
			this.writer.append("\r\n");		
		}catch(IOException e){
			Log.d("Uploading text IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("Uploading text reading-writing mecanisme exception :"+e.toString());
		}	
		
	}
	
	
	/**
	 * Method for ending http sending
	 * */
	public void endSending()throws HttpTransfertException{
		try{
		    //writing last mark 
			this.writer.append("--"+this.delimiter+"--"+"\r\n");
			//flow is written in request
			this.writer.flush();	
			//closing writing tools
			this.writer.close();
			this.os.close();
		}catch(IOException e){
			Log.d("end sending IO Exception", e.toString());
			e.printStackTrace();
			throw new HttpTransfertException("end sending closing outputstream exception :"+e.toString());	
		}	
	}
	
	/**
	 * Method for reading server response to the sent request, 
	 * returns true if transfer has been correctly done
	 */
	public boolean readResponseStatus() throws IOException{
			int response = this.connection.getResponseCode();
			return (response==HttpURLConnection.HTTP_OK);
			
		}
}
