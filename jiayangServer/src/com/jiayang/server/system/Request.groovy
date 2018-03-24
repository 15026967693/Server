package com.jiayang.server.system

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class Request {
	def headers=[:]
	def originBody=""
	def requestParams=[:]
	def String method
	def String requestUrl
	def String httpVersion
	def static  ENCODING="UTF-8"
	def SocketChannel socketChannel
	def propertyMissing(String name) {
		return requestParams[name]
	}
	def Request(SelectionKey key) {
		this.socketChannel=key.channel()
		def byteBuffer=ByteBuffer.allocate(1026)
		def String requestData=""
		while(socketChannel.read(byteBuffer)>0) {
			byteBuffer.flip()
			requestData+=new String(byteBuffer.array(),this.ENCODING)
			byteBuffer.clear()
			if(requestData.endsWith("\r\n")&&!requestData.endsWith("\r\n\r\n"))
				continue
			def dataArray=requestData.split("\r\n\r\n")
			if(dataArray.length>1) {
				dataArray[1..-1].each{
				originBody+=it+"/r/n"
				}
				byteBuffer.clear()
				requestData=dataArray[0]
				while(socketChannel.read(byteBuffer)>0) {
					byteBuffer.flip()
					originBody+=new String(byteBuffer.array(),this.ENCODING)
					byteBuffer.clear()
				}
				dataArray=dataArray[0]
			}
			dataArray=requestData.split("\r\n")
			dataArray.each {
				if(it.indexOf(":")<0) {
					def Header=it.split("\\s+")
					this.method=Header[0].toUpperCase()
					this.requestUrl=Header[1]
					this.httpVersion=Header[2].toUpperCase()
				}
				else if(!(it.substring(0,it.indexOf(":")) in headers))
					this.headers[it.substring(0,it.indexOf(":"))]=it.substring(it.indexOf(":")+1)
				else
					this.headers[it.substring(0,it.indexOf(":"))]+=";"+it.substring(it.indexOf(":")+1)
					}	
		}
	}
}
