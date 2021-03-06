package com.jiayang.server.system

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class CustomThread extends Thread{
	def App app
	def Selector selector
	@Override
	def  void run() {
		try {
		while(this.selector.select()>0)
			{
				def Iterator<SelectionKey> it=this.selector.selectedKeys().iterator()
				while(it.hasNext())
				{
					def SelectionKey key=it.next()
					if(key.isReadable())
					{
						if(!app.request)
						app.request=new Request(key)
						app.request.app=app
					}
					if(key.isWritable())
					{
						if(!app.response)
						app.response=new Response(key)
						app.response.app=app
						if(app.request&&app.response)
						{	
							app.next()
							this.interrupted()
						}
					}
					it.remove()
				}
			}
			}
			catch(Exception e)
		    {
				e.printStackTrace()
				
				
				
			}
	}
def CustomThread(App app,Selector selector)
{
	this.app=app
	this.selector=selector

	
	}
}
