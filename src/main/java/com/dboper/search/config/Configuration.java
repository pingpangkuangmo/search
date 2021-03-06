package com.dboper.search.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dboper.search.ProcessorHandler;
import com.dboper.search.format.value.ValueFormatter;
import com.dboper.search.observer.ObserverConfig;
import com.dboper.search.relation.TablesRelationService;
import com.dboper.search.sqlparams.parser.SqlParamsParser;

@Service
public class Configuration implements TableDBConfig,ObserverConfig,BaseTwoTablesRelationConfig,TableColumnsConfig{

	private String tablePrefix="";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private List<TablesRelationService> tablesRelationServices;
	
	private List<ValueFormatter> formatters;
	
	private List<SqlParamsParser> sqlParamsParsers;
	
	private Map<String,ProcessorHandler> processorHandlers=new HashMap<String,ProcessorHandler>();
	
	private String queryFileDirectory="query";
	
	private String complexQueryFileDirectory="complexQuery";
	
	private String relationDir="relation";
	
	private String baseRelationFilesDir="baseRelation";
	
	private String tableColumnsDir="tables";
	
	private String sonTables="sonTables";
	
	private String tablesPath="tablesPath";
	
	private String excelConfig="excelConfig";
	
	private boolean monitorQueryFile=false;
	
	private boolean monitorRelationFile=false;
	
	private boolean monitorBaseRelationFiles=false;
	
	public String getExcelConfig() {
		return excelConfig;
	}

	public void setExcelConfig(String excelConfig) {
		this.excelConfig = excelConfig;
	}

	public List<SqlParamsParser> getSqlParamsParsers() {
		return sqlParamsParsers;
	}

	public void setSqlParamsParsers(List<SqlParamsParser> sqlParamsParsers) {
		this.sqlParamsParsers = sqlParamsParsers;
	}

	public String getTablesPath() {
		return tablesPath;
	}

	public void setTablesPath(String tablesPath) {
		this.tablesPath = tablesPath;
	}

	public void setTableColumnsDir(String tableColumnsDir) {
		this.tableColumnsDir = tableColumnsDir;
	}

	public void addProcessorHandler(String name,ProcessorHandler processorHandler){
		processorHandlers.put(name, processorHandler);
	}
	
	public Map<String, ProcessorHandler> getProcessorHandlers() {
		return processorHandlers;
	}

	public void setProcessorHandlers(Map<String, ProcessorHandler> processorHandlers) {
		this.processorHandlers = processorHandlers;
	}

	public String getComplexQueryFileDirectory() {
		return complexQueryFileDirectory;
	}

	public void setComplexQueryFileDirectory(String complexQueryFileDirectory) {
		this.complexQueryFileDirectory = complexQueryFileDirectory;
	}

	public String getSonTables() {
		return sonTables;
	}

	public void setSonTables(String sonTables) {
		this.sonTables = sonTables;
	}

	public List<ValueFormatter> getFormatters() {
		return formatters;
	}

	public void setFormatters(List<ValueFormatter> formatters) {
		this.formatters = formatters;
	}

	public boolean isMonitorQueryFile() {
		return monitorQueryFile;
	}

	public void setMonitorQueryFile(boolean monitorQueryFile) {
		this.monitorQueryFile = monitorQueryFile;
	}

	public boolean isMonitorRelationFile() {
		return monitorRelationFile;
	}

	public void setMonitorRelationFile(boolean monitorRelationFile) {
		this.monitorRelationFile = monitorRelationFile;
	}

	public String getQueryFileDirectory() {
		return queryFileDirectory;
	}

	public void setQueryFileDirectory(String queryFileDirectory) {
		this.queryFileDirectory = queryFileDirectory;
	}

	public List<TablesRelationService> getTablesRelationServices() {
		return tablesRelationServices;
	}

	public void setTablesRelationServices(
			List<TablesRelationService> tablesRelationServices) {
		this.tablesRelationServices = tablesRelationServices;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getRelationDir() {
		return relationDir;
	}

	public void setRelationDir(String relationDir) {
		this.relationDir = relationDir;
	}

	public String getBaseRelationFilesDir() {
		return baseRelationFilesDir;
	}

	public void setBaseRelationFilesDir(String baseRelationFilesDir) {
		this.baseRelationFilesDir = baseRelationFilesDir;
	}

	public boolean isMonitorBaseRelationFiles() {
		return monitorBaseRelationFiles;
	}

	public void setMonitorBaseRelationFiles(boolean monitorBaseRelationFiles) {
		this.monitorBaseRelationFiles = monitorBaseRelationFiles;
	}

	@Override
	public String getBaseTwoTablesRelation() {
		return baseRelationFilesDir;
	}

	@Override
	public String getTableColumnsDir() {
		return tableColumnsDir;
	}

	@Override
	public String getTablesPathConfig() {
		return tablesPath;
	}
	
}
