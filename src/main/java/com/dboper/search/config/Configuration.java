package com.dboper.search.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.dboper.search.format.value.ValueFormatter;
import com.dboper.search.observer.ObserverConfig;
import com.dboper.search.relation.TablesRelationService;
import com.dboper.search.sqlparams.SqlParamsHandler;

@Service
public class Configuration implements TableDBConfig,ObserverConfig,BaseTwoTablesRelationConfig{

	private String tablePrefix="";
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private List<TablesRelationService> tablesRelationServices;
	
	private List<SqlParamsHandler> sqlParamsHandlers;
	
	private List<ValueFormatter> formatters;
	
	private String queryFileDirectory="query";
	
	private String relationDir="relation";
	
	private boolean monitorQueryFile=false;
	
	private boolean monitorRelationFile=false;
	
	private boolean monitorModule=false;
	
	private String baseTwoTablesRelation="baseRelation";
	

	public List<ValueFormatter> getFormatters() {
		return formatters;
	}

	public void setFormatters(List<ValueFormatter> formatters) {
		this.formatters = formatters;
	}

	public boolean isMonitorModule() {
		return monitorModule;
	}

	public void setMonitorModule(boolean monitorModule) {
		this.monitorModule = monitorModule;
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

	public List<SqlParamsHandler> getSqlParamsHandlers() {
		return sqlParamsHandlers;
	}

	public void setSqlParamsHandlers(List<SqlParamsHandler> sqlParamsHandlers) {
		this.sqlParamsHandlers = sqlParamsHandlers;
	}

	@Override
	public String getBaseTwoTablesRelation() {
		return baseTwoTablesRelation;
	}

	public void setBaseTwoTablesRelation(String baseTwoTablesRelation) {
		this.baseTwoTablesRelation = baseTwoTablesRelation;
	}
	
}
