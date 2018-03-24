package com.jiayang.server.system

class Router {
	def urlPatternFunc=[:]
	def basePath=""
	def Router(basePath) {
		this.basePath=basePath
	}
	def Router() {
	}
	def call(request,response,next) {
		urlPatternFunc.each { key,val->
			if(request.requestUrl ==~ key)
				val[request.method](request,response,next)
		}
	}
	def get(String pattern,func) {
		def patternFunc=[(basePath+pattern):["GET":func]]
		urlPatternFunc <<patternFunc
	}
	def post(String pattern,func) {
		def patternFunc=[(basePath+pattern):["POST":func]]
		urlPatternFunc <<patternFunc
	}
	def all(String pattern,func) {
		def patternFunc=[(basePath+pattern):["GET":func,"POST":func]]
		urlPatternFunc << patternFunc
	}
}
