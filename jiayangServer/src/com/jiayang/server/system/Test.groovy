package com.jiayang.server.system

import groovy.json.JsonOutput

class Test {
def static main(args)
{
	def app=new App()
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
		res.json([hello:"hello this will change",json:"i am json"])
		
	})
	Server.startServer()
	}
}
