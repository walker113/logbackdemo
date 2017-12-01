package com.example.logbackdemo;

import org.litepal.crud.DataSupport;

/**
 *	日志表 
 *	@config_type	: 下发类型
 *	@result_code	: 处理结果
 *	@result_content	: 错误信息
 *	@run_time		: 时间
 */
public class Ad_Log extends DataSupport {
	
	private String config_type;
	private String result_code;
	private String result_content;
	private String run_time;
	
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getConfig_type() {
		return config_type;
	}
	public void setConfig_type(String config_type) {
		this.config_type = config_type;
	}
	
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	
	public String getResult_content() {
		return result_content;
	}
	public void setResult_content(String result_content) {
		this.result_content = result_content;
	}
	
	public String getRun_time() {
		return run_time;
	}
	public void setRun_time(String run_time) {
		this.run_time = run_time;
	}
	
	
	@Override
	public String toString() {
		return "{" + 
				"\"config_type\":\"" + config_type + "\"," + 
				"\"result_code\":\"" + result_code + "\"," +
			 	"\"result_content\":\"" + result_content + "\"," +  
			 	"\"run_time\":\"" + run_time + "\""
			 	 + "}";
	}
}
