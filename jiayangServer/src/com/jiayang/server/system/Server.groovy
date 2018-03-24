package com.jiayang.server.system
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class Server {
	def static port=10006
	
def static startServer() {
	def ServerSocketChannel serverSocketChannel=ServerSocketChannel.open()
	serverSocketChannel.socket().setReuseAddress(true);
	serverSocketChannel.configureBlocking(false);
	serverSocketChannel.bind(new InetSocketAddress("localhost",port))
	def Selector selector=Selector.open()
	serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	while(selector.select()>0)
	{
		def Iterator<SelectionKey> it=selector.selectedKeys().iterator()
		while(it.hasNext())
		{
			SelectionKey key=it.next()
			if(key.isAcceptable())
			{
				def ServerSocketChannel ssc=(ServerSocketChannel)key.channel()
				def SocketChannel sc=ssc.accept()
				sc.configureBlocking(false)
				def ByteBuffer bb=ByteBuffer.allocate(1026)
				def Selector selectorCli=Selector.open()
				sc.register(selectorCli, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
				new CustomThread(new App(),selectorCli).start()
			}
			it.remove()
		}
	}
	}
}
