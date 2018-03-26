package com.jiayang.server.system

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeUnit

class Request {
	def private key=null
	def private readLine=true
	def headers=[:]
	def originBody=[]
	def cookies=[:] 
	def requestParams=[:]
	def String method
	def String requestUrl
	def String httpVersion
	def static  ENCODING="UTF-8"
	def App app
	def SocketChannel socketChannel
	def propertyMissing(String name) {
		return requestParams[name]
	}
	def Request(SelectionKey key) {
		this.socketChannel=key.channel()
		def ByteBuffer byteBuffer=ByteBuffer.allocate(1026)
		def line=[]
		while(socketChannel.read(byteBuffer)>0) {
			byteBuffer.flip()
			for(int i=0;i<byteBuffer.limit();i++)
			{
				def temp=byteBuffer.get()
				    line+=temp
					if(line.size()>3&&line[-1]!='\r'&&line[-2]=='\n'&&line[-3]=='\r'&&line[-4]!='\n')
					{
						resolveHeaderLine(line[0..-4])
						line=[line[-1]]
					}
					if(line.size()>=4&&line[-1]=='\n'&&line[-2]=='\r'&&line[-3]=='\n'&&line[-4]=='\r')
					{
						resolveHeaderLine(line[0..-5])
						resolveBody(byteBuffer,socketChannel)
						break;
					}
					
			}
			byteBuffer.clear()
	}
	if(this.headers["Cookie"])
		this.headers["Cookie"].split(";").each {
		  def nameValuePair=it.split("=")
		  this.cookies[URLDecoder.decode(nameValuePair[0],ENCODING).trim()]=URLDecoder.decode(nameValuePair[1],ENCODING)
		  }
}
def resolveBody(ByteBuffer byteBuffer,SocketChannel socketChannel)
{
	if(this.method=="GET")
		return
	def matcher=this.headers["Content-Type"]=~"\\s*multipart/form-data;\\s*boundary=(.*)"
	if(matcher)
	{
		def boundary=matcher[0][1]
		def temp=[]
		for(int i=byteBuffer.position();i<byteBuffer.limit();i++)
		{
			temp+=byteBuffer.get()
			resolveByte(temp,boundary)
		}
		byteBuffer.clear()
		while(socketChannel.read(byteBuffer)>0)
			{
					   byteBuffer.flip()
					   for(int i=0;i<byteBuffer.limit();i++)
						   {
							   temp+=byteBuffer.get()
							   resolveByte(temp,boundary)
						   }
					   byteBuffer.clear()
			}
		
	}
	else
	{
		def temp=[]
		for(int i=byteBuffer.position();i<byteBuffer.limit();i++)
		{
			temp+=byteBuffer.get()
		}
		byteBuffer.clear()
		while(socketChannel.read(byteBuffer)>0)
		{
			byteBuffer.flip()
			temp+=byteBuffer.array()
		    byteBuffer.clear()
		}
		def paramString=new String(temp as byte[],"UTF-8").trim()
		paramString.split("&").each {
			if(it.split("=").length>1)
			this.requestParams[it.split("=")[0].trim()]=it.split("=")[1].trim()
		}
	}
		
}
	def resolveHeaderLine(headerLine)
	{
		def lineString=new String(headerLine as byte[],this.ENCODING)
		def matcher=lineString =~ "(.*?):(.*)"
		if(matcher)
			this.headers[matcher[0][1]]=matcher[0][2] 
		matcher=lineString =~"(GET|POST)\\s+(.*?)\\s+(.*?)"	
		if(matcher)
		{
			this.method=matcher[0][1]
	        this.requestUrl=matcher[0][2]
			if(this.requestUrl.indexOf('?')>0&&this.requestUrl.split("\\?").length>1)
			{
				this.requestUrl.split("\\?")[1].split("&").each{
					this.requestParams[it.split("=")[0].trim()]=URLDecoder.decode(it.split("=")[1].trim())
				}
				this.requestUrl=this.requestUrl.split("\\?")[0]
			}
	        this.httpVersion=matcher[0][3]
			println this.requestUrl
			println this.requestParams
		}
	}
	def  resolveByte(ArrayList temp,String boundary)
	{
		def matcher
		if(!this.key&&temp.size()==2&&temp[-1]=='\n'&&temp[-2]=='\r')
		{
			temp.clear()
			return
		}
		else if(this.key&&temp.size()==2&&temp[-1]=='\n'&&temp[-2]=='\r'&&this.readLine)
			{
				this.readLine=false
				temp.clear()
				return
			}
		else if(temp.size()>2&&temp[-1]=='\n'&&temp[-2]=='\r')
		{
			if(new String(temp[0..-3] as byte[],this.ENCODING)=="--"+boundary)
			{
				this.key=null
				this.readLine=true
				temp.clear()
				return
			}
			if(new String(temp[0..-3] as byte[],this.ENCODING)=="--"+boundary+"--")
				{
					this.key=null
					this.readLine=true
					temp.clear()
					return
				}
			matcher=new String(temp[0..-3] as byte[],this.ENCODING)=~"Content-Disposition:\\s*form-data;\\s*name=\"(.*?)\";\\s*filename=\"(.*?)\""
			if(matcher)
				{
					this.key=matcher[0][1]
					this.requestParams[this.key]=[:]
					this.requestParams[this.key]["filename"]=matcher[0][2]
					this.requestParams[this.key]["bytes"]=[]
					temp.clear()
					return
				}
			matcher=new String(temp[0..-3] as byte[],this.ENCODING)=~"Content-Disposition:\\s*form-data;\\s*name=\"(.*?)\""
		   if(matcher)
		   {
			   this.key=matcher[0][1]
			   this.requestParams[this.key]=""
			   temp.clear()
			   return
		   }
		   
			   matcher=new String(temp[0..-3] as byte[],this.ENCODING)=~"Content-Type:\\s*(.*)"
			   if(matcher)
				   {
					
					   this.requestParams[this.key]["Content-Type"]=matcher[0][1]
					   temp.clear()
					   return
				   }
		   if(this.requestParams[this.key]&&this.requestParams[this.key]["bytes"]!=null)
			   this.requestParams[this.key]["bytes"]+=temp[0..-3] as ArrayList
		  else if(this.requestParams[this.key]=="") 
			   this.requestParams[this.key]=this.requestParams[this.key]+new String(temp[0..-3] as byte[],this.ENCODING)
		   temp.clear()
		}
	}
	def getSession()
	{
		if(App.session[this.cookies["JIAYANGSESSIONID"]])
		{
			def session=App.session[this.cookies["JIAYANGSESSIONID"]]
			 session["invalidTime"]=new Date().getTime()*30*1000*60
		return session
			 }
			 else
			 {
				 def uid=UUID.randomUUID().toString()
				 this.app.response.setCookie("JIAYANGSESSIONID",uid)
				 def session=App.session[uid]
				session=[:]
				session["invalidTime"]=new Date().getTime()*30*1000*60
				 return session
			 }
	}
}