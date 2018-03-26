package com.jiayang.server.system

import groovy.json.JsonOutput
import groovy.text.StreamingTemplateEngine
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.text.SimpleDateFormat
import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
class Response {
	def SocketChannel socketChannel
	def static final LINE_END="\r\n"
	def static final ALL_END="\r\n\r\n"
	def App app
	def headers=[:] as IdentityHashMap
	def Response(SelectionKey key)
	{
		this.socketChannel=key.channel()
	}
	def text(String s)
	{
		def reschar="HTTP/1.1 200 ok ${LINE_END}"
		headers["Content-Type"]="text/plain;charset=UTF-8"
		headers.each{
			key,val->
			reschar+="${key}:${val}${LINE_END}"
		}
		reschar+=s+ALL_END
		reschar+=s
		this.socketChannel.write(ByteBuffer.wrap(reschar.getBytes()))
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
		
		def String s=this.class.getResource(path).text
		
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
	def setCookie(String name,String value) {
		name=URLEncoder.encode(name,"UTF-8")
		value=URLEncoder.encode(value,"UTF-8")
		this.headers[new String("Set-Cookie")]="${name}=${value};"	
	}
	def setCookie(Cookie cookie)
	{
		cookie.name=URLEncoder.encode(cookie.name,"UTF-8")
		cookie.value=URLEncoder.encode(cookie.value,"UTF-8")
		def sdf=new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
		def timestr=sdf.format(new Date(new Date().getTime()+cookie.expires))
		this.headers[new String("Set-Cookie")]="${cookie.name}=${cookie.value};path=${cookie.path};expires=${timestr};domain=${cookie.domain};"
	}
	def setCookie(args)
	{
		args.name=URLEncoder.encode(args.name,this.app.ENCODING)
		args.value=URLEncoder.encode(args.value,this.app.ENCODING)
		if(!args.name||!args.value)
			throw new RuntimeException("必须指定名字和值")
		def result="${args.name}=${args.value};"
		if(args.path)
			result+="path=${args.path};"
		if(args.expires)
		{
			def sdf=new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
			def timestr=sdf.format(new Date(new Date().getTime()+args.expires))
			result+="expires=${timestr};"
		}
		if(args.domain)
			result+="domain=${args.domain};"
		this.headers[new String("Set-Cookie")]=result
	}
}
