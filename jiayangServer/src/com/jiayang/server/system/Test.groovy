package com.jiayang.server.system

import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.text.SimpleDateFormat
import java.nio.ByteBuffer
import groovy.json.JsonOutput

class Test {
def static main(args)
{
	def app=new App()
	app.use(new StaticResolver())
	app.use("/",{req,res,next->
		res.text("hello")
		
	})
	app.use("/datarender",{req,res,next->
		res.app.data=["name":"hiii",hello:"hello222"]
		res.render("/main.txt")
		
	})
	app.use("/main",{req,res,next->
		res.render([hello:"hello this will change"],"/main.txt")
		
	})
	app.use("/json",{req,res,next->
		res.setCookie(name:"名字",value:"贾洋")
		println(req.cookies."名字")
		res.json([hello:"hello this will change",json:"i am json"])
	})
	app.use("/fileupload",{req,res,next->
		res.render(null,"/fileupload.html")
	})
	app.use("/upload",{req,res,next->
		res.text("提交成功")
		res.end()
	})
	Server.startServer()
	}
}
