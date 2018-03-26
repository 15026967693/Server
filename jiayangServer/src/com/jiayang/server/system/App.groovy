package com.jiayang.server.system
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import org.codehaus.groovy.runtime.GStringImpl
import java.nio.channels.Selector
class App {
def static String staticPath="/public"
def static chain=[]
def static WeakHashMap session=[:] 
def index=0
def static global=[:]
def static ENCODING="UTF-8"
def data=[:]
def Request request
def Response response
def use(func) 
{
	chain << func
}
def use(url,func)
{
	def router=new Router(url)
	router.all("",func)
	chain << router
}
def  next=
{->
	if(this.index>=chain.size())
	{
			this.index=0
			return
    }
	def pluginfunc=chain[index++]
	pluginfunc(this.request,this.response,this.next)
}
def propertyMissing(String name)
{
	return this.data[name]
}
/*def static main(args) {
	def app=new App()
	app.use({
		Request request,Response response,Closure next->
		println "a"
		next()
		println "b"
		})
	app.next()
	ScriptEngine engine=new ScriptEngineManager().getEngineByName("groovy");
	def binding=engine.createBindings()
	println(Selector.open().is(Selector.open()))
	}*/
}
