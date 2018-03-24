package com.jiayang.server.system

import groovy.json.JsonOutput
import groovy.text.StreamingTemplateEngine
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
class Response {
	def SocketChannel socketChannel
	def static final LINE_END="\r\n"
	def static final ALL_END="\r\n\r\n"
	def App app
	def headers=[:]
	def Response(SelectionKey key)
	{
		this.socketChannel=key.channel()
	}
	def text(String s)
	{
		def reschar="HTTP/1.1 200 ok ${LINE_END}"
		headers.each{
			key,val->
			reschar+="${key}:${val}${LINE_END}"
		}
		reschar+=s+ALL_END
		this.socketChannel.write(ByteBuffer.wrap(s.getBytes()))
		end()
	}
	def writeBytes(bytes)
	{
		this.socketChannel.write(ByteBuffer.wrap(bytes))
	}
	def end()
	{
		this.socketChannel.close()
	}
	def render(model,String path)
	{
		def String s=this.class.getResource(App.staticPath+path).text
		
		def engine = new StreamingTemplateEngine()
		def template = engine.createTemplate(s).make(model)
		
		def result
		result="HTTP/1.1 200 OK"+LINE_END
		headers["Content-Type"]="text/html;charset=UTF-8"
		headers.each{
			key,val->
			result+="${key}:${val}${LINE_END}"
		}
		result+=LINE_END
		result+=template
		writeBytes(result.getBytes())
		end()
	}
	def render(String path)
	{
		def String s=this.class.getResource(App.staticPath+path).text
		
		def engine = new StreamingTemplateEngine()
		def template = engine.createTemplate(s).make(this.app.data)
		this.app.data=[:]
		def result
		result="HTTP/1.1 200 OK"+LINE_END
		headers["Content-Type"]="text/html;charset=UTF-8"
		headers.each{
			key,val->
			result+="${key}:${val}${LINE_END}"
		}
		result+=LINE_END
		result+=template
		writeBytes(result.getBytes())
		end()
	}
	def json(obj)
	{
		headers["Content-Type"]="application/json;charset=UTF-8"
		def result="HTTP/1.1 200 OK"+LINE_END
		headers.each{
			key,val->
			result+="${key}:${val}${LINE_END}"
		}
		result+=LINE_END
		result+=JsonOutput.toJson(obj)+ALL_END
		writeBytes(result.getBytes())
		end()
	}
}
