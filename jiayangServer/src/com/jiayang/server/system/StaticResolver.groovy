package com.jiayang.server.system

class StaticResolver {
def call(req,res,next)
{
	if(req.requestUrl.startsWith(App.staticPath))
		{
			res.writeBytes(this.getClass().getResourceAsStream(req.requestUrl).bytes)
			res.end()
		}
		next()
	}
}
